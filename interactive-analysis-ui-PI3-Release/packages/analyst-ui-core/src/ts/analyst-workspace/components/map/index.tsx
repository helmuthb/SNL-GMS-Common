import { Checkbox, Colors } from '@blueprintjs/core';
import { DateInput, TimePickerPrecision } from '@blueprintjs/datetime';
import * as Gl from '@gms/golden-layout';
import * as React from 'react';
import { ChildProps, compose, graphql, QueryProps } from 'react-apollo';
import * as ReactRedux from 'react-redux';

import { analystUiConfig } from '../../config';
import { AnalystWorkspaceState, TimeInterval } from '../../state';
import * as Actions from '../../state/actions';
import {
    MapData, MapEventData,
    mapEventsQuery, MapEventsQueryInput, mapQuery,
    MapSignalDetectionData, mapSignalDetectionsQuery, MapSignalDetectionsQueryInput
} from './graphql/query';
import { detectionsUpdatedSubscription, eventUpdatedSubscription } from './graphql/subscriptions';

import { TimeUtil } from '@gms/ui-core-components';
import * as lodash from 'lodash';
import { addGlForceUpdateOnResize, addGlForceUpdateOnShow } from '../../util/gl-util';
import { CesiumMap } from './cesium-map';
import { MapAPI, MapState } from './map-api';

/**
 * Mutations used in the map
 */
// tslint:disable-next-line:no-empty-interface
export interface MapMutations {
}

/**
 * Props mapped in from Redux state
 */
export interface MapReduxProps {
    // passed in from golden-layout
    glContainer?: Gl.Container;
    currentTimeInterval: TimeInterval;
    selectedEventHypIds: string[];
    openEventHypId: string;
    selectedSdIds: string[];

    // callbacks
    setSelectedEventHypIds(eventHypIds: string[]): void;
    setSelectedSdIds(SdIds: string[]): void;
    setOpenEventHypId(openEventHypId: string): void;
}

/**
 * Consolidated props type for map
 */
export type MapProps = MapReduxProps
    & ChildProps<MapMutations, MapData>
    & { eventData: QueryProps & MapEventData }
    & { signalDetectionData: QueryProps & MapSignalDetectionData };

/**
 * Primary map display
 */
export class Map extends React.Component<MapProps, MapState> {

    /**
     * handle to the dom element we want to render Map inside of.
     */
    private containerDomElement: HTMLDivElement;

    /**
     * Handlers to unsubscribe from apollo subscriptions
     */
    private readonly unsubscribeHandlers: { (): void }[] = [];

    private readonly map: MapAPI;

    // ***************************************
    // BEGIN REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    /**
     * Constructor.
     * 
     * @param props The initial props
     */
    public constructor(props) {
        super(props);
        this.map = new CesiumMap({
            events: {
                onMapClick: this.onMapClick,
                onMapShiftClick: this.onMapShiftClick,
                onMapDoubleClick: this.onMapDoubleClick
            },
            analystUiConfig
        });
    }

    /**
     * Invoked when the componented mounted.
     */
    public componentDidMount() {
        addGlForceUpdateOnShow(this.props.glContainer, this);
        addGlForceUpdateOnResize(this.props.glContainer, this);
        this.map.initialize(this.containerDomElement);
    }

    /**
     * Invoked when the componented mounted.
     * 
     * @param prevProps The previous props
     * @param prevState The previous state
     */
    public componentDidUpdate(prevProps: MapProps) {
        if (this.props.currentTimeInterval &&
            !lodash.isEqual(this.props.currentTimeInterval, prevProps.currentTimeInterval)) {
            this.setupSubscriptions(this.props);
            this.resetMapForIntervalChange();
        }

        if (!lodash.isEqual(this.props.data.defaultStations, prevProps.data.defaultStations)) {
            this.map.drawDefaultStations(prevProps.data.defaultStations, this.props.data.defaultStations);
        }

        if (!lodash.isEqual(this.props.eventData.eventHypothesesInTimeRange,
                            prevProps.eventData.eventHypothesesInTimeRange)
            || this.props.selectedEventHypIds !== prevProps.selectedEventHypIds) {
            this.map.drawEvents(prevProps, this.props);
        }

        if (this.props.openEventHypId !== prevProps.openEventHypId) {
            const currentOpenEvent = prevProps.openEventHypId ?
                this.props.eventData.eventHypothesesInTimeRange.find(eh => eh.id === prevProps.openEventHypId)
                : undefined;

            const nextOpenEvent = this.props.openEventHypId ?
                this.props.eventData.eventHypothesesInTimeRange.find(eh => eh.id === this.props.openEventHypId)
                : undefined;

            this.map.highlightOpenEvent(
                this.props.currentTimeInterval,
                currentOpenEvent,
                nextOpenEvent,
                this.props.selectedEventHypIds);

            // show signal detections for the selected event
            if (this.props.signalDetectionData) {
                this.map.drawSignalDetections(this.props.signalDetectionData.signalDetectionHypothesesByStation,
                                              this.props.openEventHypId);
                const selectedSDIds = this.props.selectedSdIds;
                const localSelectedSDIds = this.props.selectedSdIds;
                if (selectedSDIds.length) {
                    this.map.highlightSelectedSignalDetections(selectedSDIds);
                } else if (localSelectedSDIds.length) {
                    this.map.highlightSelectedSignalDetections(localSelectedSDIds);
                }
                this.map.updateStations(
                    prevProps.signalDetectionData.signalDetectionHypothesesByStation, prevProps.openEventHypId,
                    this.props.signalDetectionData.signalDetectionHypothesesByStation, this.props.openEventHypId);
            }
        }

        if (this.props.selectedSdIds !== prevProps.selectedSdIds) {
            this.selectSignalDetectionsFromProps(this.props);
        }
    }

    /**
     * Invoked when the componented will unmount.
     */
    public componentWillUnmount() {
        // unsubscribe from all current subscriptions
        this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
        this.unsubscribeHandlers.length = 0;
    }

    /**
     * Renders the component.
     */
    public render() {
        return (
            <div style={{ width: '100%', height: '100%', padding: '0.5rem' }}>
                <div style={{ display: 'flex', height: '100%', flexDirection: 'column' }}>
                    {this.mapTopOptions()}
                    <div style={{ flex: '1 1 auto', display: 'flex' }}>
                        <div
                            style={{
                                flex: '0 0 auto', width: '100px', display: 'flex', flexDirection: 'column',
                                backgroundColor: Colors.DARK_GRAY1, border: `1px solid ${Colors.GRAY1}`
                            }}
                        >
                            {this.generateLayerToggles()}
                        </div>
                        <div
                            style={{
                                flex: '1 1 auto', marginLeft: '0.5rem',
                                display: 'flex', flexDirection: 'column'
                            }}
                        >
                            <div style={{ flex: '1 1 auto', position: 'relative', }}>
                                <div
                                    style={{ border: `1px solid ${Colors.GRAY1}`, }}
                                    ref={ref => { if (ref) { this.containerDomElement = ref; } }}
                                    className="max"
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    // ***************************************
    // END REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    /**
     * Initialize graphql subscriptions on the apollo client
     */
    private readonly setupSubscriptions = (props: MapProps): void => {
        if (!props.data) return;

        // first, unsubscribe from all current subscriptions
        this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
        this.unsubscribeHandlers.length = 0;

        // don't register subscriptions if the current time interval is undefined/null
        if (!props.currentTimeInterval) return;

        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                document: eventUpdatedSubscription
            })
        );
        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                document: detectionsUpdatedSubscription
            })
        );
    }

    /**
     * Generate the sidebar with layer toggles
     */
    private readonly generateLayerToggles = (): JSX.Element[] =>
        Object.keys(this.map.getDataLayers())
        .map(id => (
            <Checkbox
                key={id}
                label={id}
                style={{
                    margin: '0.5rem'
                }}
                checked={this.map.getDataLayers()[id].show}
                onChange={e => this.toggleDataLayerVisibility(id)}
            />
        ))

    private readonly mapTopOptions = (): JSX.Element => (
        <div style={{ flex: '0 0 auto', display: 'flex', alignItems: 'center' }}>
                        <Checkbox
                            label="Sync with user actions"
                            style={{ marginBottom: '0px' }}
                        />
                        <label className="pt-label pt-inline" style={{ marginBottom: '0.25rem', marginLeft: '1rem' }}>
                            Start time:
                            <DateInput
                                defaultValue={new Date()}
                                timePrecision={TimePickerPrecision.SECOND}
                                parseDate={str => new Date(str)}
                                formatDate={date => TimeUtil.dateToString(date)}
                            />
                        </label>
                        <label className="pt-label pt-inline" style={{ marginBottom: '0.25rem', marginLeft: '1rem' }}>
                            End time:
                            <DateInput
                                defaultValue={new Date()}
                                timePrecision={TimePickerPrecision.SECOND}
                                parseDate={str => new Date(str)}
                                formatDate={date => TimeUtil.dateToString(date)}
                            />
                        </label>
        </div>
    )

    /**
     * Toggle the visibility of a data source
     */
    private readonly toggleDataLayerVisibility = (id: string, show?: boolean) => {
        const layer = this.map.getDataLayers()[id];
        if (!layer) return;
        show == undefined ?
            layer.show = !layer.show
            : layer.show = show;
        this.setState(prevState => ({
            ...this.map.state
        }));
    }

    /**
     * Handler for map click event
     */
    private readonly onMapClick = (clickEvent: any, entity?: any) => {
        if (entity && entity.entityType === 'event') {
                this.props.setSelectedEventHypIds([entity.id]);
            } else if (entity && entity.entityType === 'sd') {
                this.props.setSelectedSdIds([entity.id]);
            } else {
                this.props.setSelectedSdIds([]);
                this.props.setSelectedEventHypIds([]);
            }

    }

    /**
     * Handler for map interval change
     */
    private readonly resetMapForIntervalChange = () => {
        this.props.setOpenEventHypId(undefined);
        this.props.setSelectedEventHypIds([]);
        this.map.resetView(this.props);
    }

    /**
     * Handler for map ctrl+click
     */
    private readonly onMapShiftClick = (clickEvent: any, entity?: any) => {
        if (entity && entity.entityType === 'sd') {
            this.props.setSelectedSdIds([...this.props.selectedSdIds, entity.id]);
        }
        if (entity && entity.entityType === 'event') {
            this.props.setSelectedEventHypIds([...this.props.selectedEventHypIds, entity.id]);
        }
    }

    /**
     * Handler for map double click
     */
    private readonly onMapDoubleClick = (clickEvent: any, entity?: any) => {
        if (entity && entity.entityType === 'event') {
            // TODO: fix this so completed events can't be opened again
            const filteredList = this.props.eventData.eventHypothesesInTimeRange.filter(hypothesis =>
                                                                                        hypothesis.id === entity.id);
            filteredList.forEach(hyp => {
                if (hyp.event.status !== 'Complete') {
                    this.props.setOpenEventHypId(entity.id);
                }
            });
        }
    }

    /**
     * Selects clicked signal detections
     */
    private readonly selectSignalDetectionsFromProps = (props: MapProps) => {
            this.map.highlightSelectedSignalDetections(props.selectedSdIds);
        }
    }

// map parts of redux state into this component as props
const mapStateToProps = (state: AnalystWorkspaceState): Partial<MapReduxProps> => ({
    currentTimeInterval: state.app.currentTimeInterval,
    selectedEventHypIds: state.app.selectedEventHypIds,
    openEventHypId: state.app.openEventHypId,
    selectedSdIds: state.app.selectedSdIds
});

// map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<MapReduxProps> => ({
    setSelectedEventHypIds: (ids: string[]) => {
        dispatch(Actions.setSelectedEventHypIds(ids));
    },
    setSelectedSdIds: (ids: string[]) => {
        dispatch(Actions.setSelectedSdIds(ids));
    },
    setOpenEventHypId: (id: string) => {
        dispatch(Actions.setOpenEventHypId(id));
    }
});

/**
 * higher-order component react-redux(react-apollo(Map))
 */
export const ReduxApolloMap: React.ComponentClass<Pick<{}, never>> = compose(
    ReactRedux.connect(mapStateToProps, mapDispatchToProps),
    graphql(mapQuery),
    graphql(mapEventsQuery, {
        options: (props: MapReduxProps) => {
            const skip = props.currentTimeInterval === undefined;
            const variables: MapEventsQueryInput | {} = skip ? {}
                : {
                    timeRange: {
                        startTime: String(props.currentTimeInterval.startTimeSecs -
                                    analystUiConfig.environment.additionalTimeToLoad),
                        endTime: String(props.currentTimeInterval.endTimeSecs +
                                    analystUiConfig.environment.additionalTimeToLoad),
                    }
                };
            // work-around to only fetch based on props, if the current time interval is undefined
            return {
                variables,
                fetchPolicy: skip ? 'cache-only' : undefined
            };
        },
        name: 'eventData'
    }),
    graphql(mapSignalDetectionsQuery, {
        options: (props: MapReduxProps & ChildProps<MapMutations, MapData>) => {
            const skip = props.currentTimeInterval === undefined || props.data.defaultStations === undefined;
            const variables: MapSignalDetectionsQueryInput | {} = skip ? {}
                : {
                    stationIds: props.data.defaultStations.map(station => station.id),
                    timeRange: {
                        startTime: String(props.currentTimeInterval.startTimeSecs -
                                        analystUiConfig.environment.additionalTimeToLoad),
                        endTime: String(props.currentTimeInterval.endTimeSecs +
                                        analystUiConfig.environment.additionalTimeToLoad),
                    }
                };
            // work-around to only fetch based on props, if the current time interval is undefined
            return {
                variables,
                fetchPolicy: skip ? 'cache-only' : undefined
            };
        },
        name: 'signalDetectionData'
    }),
)(Map);
