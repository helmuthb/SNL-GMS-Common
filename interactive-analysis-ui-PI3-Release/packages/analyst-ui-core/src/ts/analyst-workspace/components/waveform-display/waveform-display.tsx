import { Button, Colors, ContextMenu, Intent, NonIdealState, NumericInput, Position, Spinner } from '@blueprintjs/core';
import * as Gl from '@gms/golden-layout';
import { LineStyle, StationConfig, WaveformDisplay as Weavess } from '@gms/weavess';
import * as lodash from 'lodash';
import * as React from 'react';
import { ChildProps, MutationFunc, QueryProps } from 'react-apollo';
import { QcMaskDisplayFilters, systemConfig, userPreferences } from '../../config/';
import { WaveformDisplayControls, WaveformSortType } from './waveform-display-controls';

declare var require;
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const electron = require('electron');

import { TimeInterval } from '../../state';
import { eventUpdatedSubscription } from '../event-list/graphql/subscriptions';
import {
    SetPhaseBlueprintContextMenu,
    SetPhaseElectronContextMenu,
    WfDisplayBlueprintContextMenu,
    WfDisplayElectronContextMenu
} from './context-menus';
import { CreateDetectionInput, RejectDetectionsInput, UpdateDetectionsInput } from './graphql/mutations';
import { QcMasksQuery, WaveformDisplayData } from './graphql/queries';
import {
    waveformDisplayQuery,
    WaveformDisplayQueryInput
} from './graphql/queries/station-detections';
import {
    detectionHypothesesRejectedSubscription,
    detectionsCreatedSubscription,
    DetectionsCreatedSubscription,
    detectionsUpdatedSubscription,
    waveformSegmentsAddedSubscription,
    WaveformSegmentsAddedSubscription
} from './graphql/subscriptions';
import { ChannelSegment, fetchWaveforms } from './waveform-client';
import { WfDataCache } from './wf-data-cache';

import { MaskDisplayFilter } from '../../config/user-preferences';
import { addGlForceUpdateOnResize, addGlForceUpdateOnShow } from '../../util/gl-util';
import { QcMaskDetails } from './qc-mask-details';
import { QcMaskLegend } from './qc-mask-legend';
import { SignalDetectionDetails } from './signal-detection-details';
/**
 * wf display state.
 * keep track of selected channels & signal detections
 */
export interface WaveformDisplayState {
    stations: StationConfig[];
    selectedChannels: string[];
    defaultSdPhase: string;
    currentTimeInterval: {
        startTimeSecs: number;
        endTimeSecs: number;
    };
    // because the user may load more waveform
    // data than the currently opened time interval
    viewTimeInterval: {
        startTimeSecs: number;
        endTimeSecs: number;
    };
    loading: boolean;
    maskDisplayFilters: QcMaskDisplayFilters;
    analystNumberOfWaveforms: number;
}

/**
 * Mutations used by the workflow display
 */
export interface WaveformDisplayMutations {
    // {} because we don't care about mutation results for now, handling that through subscriptions
    createDetection: MutationFunc<{}>;
    updateDetections: MutationFunc<{}>;
    rejectDetectionHypotheses: MutationFunc<{}>;
}

/**
 * Props mapped in from Redux state
 */
export interface WaveformDisplayReduxProps {
    // passed in from golden-layout
    glContainer?: Gl.Container;

    currentTimeInterval: TimeInterval;
    currentOpenEventHypId: string;
    selectedSdIds: string[];
    waveformSortType: WaveformSortType;

    // callbacks
    setSelectedSdIds(idx: string[]): void;
    setSelectedSortType(selectedSortType: WaveformSortType): void;
}

/**
 * Consolidated props type for waveform display.
 */
export type WaveformDisplayProps = WaveformDisplayReduxProps
    & ChildProps<WaveformDisplayMutations, WaveformDisplayData>
    & { qcMasks: QueryProps & QcMasksQuery };

/**
 * Primary waveform display component.
 */
export class WaveformDisplay extends React.Component<WaveformDisplayProps, WaveformDisplayState> {

    // how much additional waveform data to load
    private readonly additionalWaveformAmountSecs: number = 900; // 15 minutes

    /**
     * A ref handle to the weavess component
     */
    private weavess: Weavess;

    /**
     * A flag indicating if waveforms are being fetched or not.
     */
    private isFetching: boolean;

    /**
     * A flag to indicate when to zoom weaves window
     */
    private needsToZoom: boolean = false;

    /**
     * handlers to unsubscribe from apollo subscriptions
     */
    private readonly unsubscribeHandlers: { (): void }[] = [];

    /**
     * A cache storing which channels we've already loaded data for.
     */
    private readonly wfDataCache: WfDataCache = new WfDataCache();

    /**
     * A Ref to the waveform display div
     */
    private waveformDisplayRef: HTMLDivElement | undefined;

    // ***************************************
    // BEGIN REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    /**
     * Constructor.
     * 
     * @param props The initial props
     */
    public constructor(props: WaveformDisplayProps) {
        super(props);
        this.state = {
            stations: [],
            selectedChannels: [],
            defaultSdPhase: userPreferences.defaultSignalDetectionPhase,
            loading: false,
            maskDisplayFilters: userPreferences.colors.waveforms.maskDisplayFilters,
            analystNumberOfWaveforms: 12,
            currentTimeInterval: props.currentTimeInterval ?
                {
                    startTimeSecs: props.currentTimeInterval.startTimeSecs,
                    endTimeSecs: props.currentTimeInterval.endTimeSecs
                }
                : undefined,
            viewTimeInterval: props.currentTimeInterval ?
                {
                    startTimeSecs: props.currentTimeInterval.startTimeSecs,
                    endTimeSecs: props.currentTimeInterval.endTimeSecs
                }
                : undefined
        };
    }

    /**
     * Updates the derived state from the next props.
     * 
     * @param nextProps The next (new) props
     * @param prevState The previous state
     */
    public static getDerivedStateFromProps(nextProps: WaveformDisplayProps, prevState: WaveformDisplayState) {
        // if next props is not populated properly skip
        if (!nextProps || !nextProps.data || !nextProps.currentTimeInterval ||
            !nextProps.data.eventHypothesesInTimeRange) {
            // return null to indicate no change to state.
            return null;
        }

        // time interval changed = clear cache
        if (!lodash.isEqual(nextProps.currentTimeInterval, prevState.currentTimeInterval)) {
            // update current interval to the selected open interval time
            // reset the interval to the new one, overriding any extra data the user has loaded.
            return {
                currentTimeInterval: {
                    startTimeSecs: nextProps.currentTimeInterval.startTimeSecs,
                    endTimeSecs: nextProps.currentTimeInterval.endTimeSecs
                },
                viewTimeInterval: {
                    startTimeSecs: nextProps.currentTimeInterval.startTimeSecs,
                    endTimeSecs: nextProps.currentTimeInterval.endTimeSecs
                }
            };
        }

        // return null to indicate no change to state.
        return null;
    }

    /**
     * Invoked when the componented mounted.
     */
    public componentDidMount() {
        addGlForceUpdateOnShow(this.props.glContainer, this);
        addGlForceUpdateOnResize(this.props.glContainer, this);
    }

    /**
     * Invoked when the componented mounted.
     * 
     * @param prevProps The previous props
     * @param prevState The previous state
     */
    public componentDidUpdate(prevProps: WaveformDisplayProps, prevState: WaveformDisplayState) {
        if (this.props.currentTimeInterval &&
            !lodash.isEqual(this.props.currentTimeInterval, prevProps.currentTimeInterval)) {
            this.setupSubscriptions(this.props);
        }

        this.updateWaveformData(prevProps);
        this.updateWaveformSortType(prevProps);

        // if selected a new event, zoom to time window around that event
        this.needsToZoom = this.needsToZoom ||
            (this.props.currentOpenEventHypId && this.props.currentOpenEventHypId !== prevProps.currentOpenEventHypId);

        // If need to zoom due to a changed event hyp id and done loading
        if (this.needsToZoom && !this.props.data.loading) {
            const eventHyp = this.props.data.eventHypothesesInTimeRange
                .find(eh => eh.id === this.props.currentOpenEventHypId);
            const eventTime = eventHyp.preferredLocationSolution.locationSolution.timeSec;
            const detectionTimes = eventHyp.signalDetectionAssociations
                .filter(association => !association.isRejected)
                .map(association => association.signalDetectionHypothesis.arrivalTimeMeasurement.timeSec);
            const maxTime = lodash.max(detectionTimes);
            const paddingSecs = 60;
            if (this.weavess) {
                this.weavess.zoomToTimeWindow(eventTime, maxTime + paddingSecs);
                this.needsToZoom = false;
            }
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
        const sortedStations: StationConfig[] = this.displayNumberOfWaveforms(this.sortWaveformList());
        return (
            <div
                ref={ref => this.waveformDisplayRef = ref}
                style={{
                    height: '100%',
                    width: '100%',
                    padding: '0.5rem',
                    color: Colors.GRAY4,
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center'
                }}
            >
                {
                    // if the golden-layout container is not visible, do not attempt to render
                    // the compoent, this is to prevent JS errors that may occur when trying to
                    // render the component while the golden-layout container is hidden
                    (this.props.glContainer && this.props.glContainer.isHidden) ?
                        <NonIdealState /> :

                        !this.props.currentTimeInterval ?
                            (
                                <NonIdealState
                                    visual="timeline-line-chart"
                                    title="No waveform data currently loaded"
                                />
                            )
                            : this.props.data && (this.props.data.loading || this.state.loading) ?
                                (
                                    <NonIdealState
                                        action={<Spinner intent={Intent.PRIMARY} />}
                                        title="Loading:"
                                        description={
                                            this.props.data.loading ?
                                                'Default station set...'
                                                : `Data for current interval across
                                               ${this.props.data.defaultStations.length} channels...`
                                        }
                                    />
                                )
                                : this.props.data.error ?
                                    (
                                        <NonIdealState
                                            visual="error"
                                            action={<Spinner intent={Intent.DANGER} />}
                                            title="Something went wrong!"
                                            description={this.props.data.error.message}
                                        />
                                    )
                                    :
                                    (
                                        <div
                                            style={{
                                                height: '100%',
                                                width: '100%',
                                                display: 'flex',
                                                flexDirection: 'column'
                                            }}
                                        >
                                            <div
                                                style={{
                                                    flex: '0 0 auto',
                                                    display: 'flex',
                                                    justifyContent: 'flex-end',
                                                    marginBottom: '0.5rem'
                                                }}
                                            >
                                                {
                                                    /*
                                                        this.state.loadingAdditionalChannels ?
                                                            <Spinner
                                                                className="pt-small"
                                                            />
                                                            : undefined
                                                        <Button
                                                        className="pt-small"
                                                        text="load all channels"
                                                        disabled={this.state.loadingAdditionalChannels}
                                                        onClick={e => this.fetchNonDefaultChannelWaveforms(this.props)}
                                                        intent={Intent.NONE}
                                                        iconName="horizontal-bar-chart"
                                                        style={{ marginRight: '0.5rem' }}
                                                        />
                                                    */
                                                }
                                                <div className="pt-select pt-small">
                                                    <select
                                                        className="pt-select pt-small"
                                                        value={this.state.defaultSdPhase}
                                                        onChange={e => {
                                                            this.setState({
                                                                defaultSdPhase: (e.target as HTMLSelectElement).value
                                                            });
                                                        }}
                                                        style={{ marginRight: '0.5rem' }}
                                                    >
                                                        {systemConfig.defaultSdPhases.map(phase =>
                                                            <option key={phase} value={phase}>
                                                                {phase}
                                                            </option>
                                                        )}
                                                    </select>
                                                </div>
                                                <NumericInput
                                                    className="pt-small"
                                                    allowNumericCharactersOnly={true}
                                                    buttonPosition={Position.LEFT}
                                                    onValueChange={this.setAnalystNumberOfWaveforms}
                                                    min={1}
                                                    value={this.state.analystNumberOfWaveforms}
                                                    clampValueOnBlur={true}
                                                    style={{ marginRight: '0.5rem', width: '35px' }}
                                                />
                                                <WaveformDisplayControls
                                                    currentSortType={this.props.waveformSortType}
                                                    setSelectedSortType={this.props.setSelectedSortType}
                                                    currentOpenEventHypId={this.props.currentOpenEventHypId}
                                                />
                                                <QcMaskLegend
                                                    maskDisplayFilters={this.state.maskDisplayFilters}
                                                    setMaskDisplayFilters={this.setMaskDisplayFilters}
                                                />
                                                <Button
                                                    className="pt-small"
                                                    text="Measure Window"
                                                    intent={Intent.NONE}
                                                    onClick={e =>
                                                        this.toggleMeasureWindow()
                                                    }
                                                    style={{ marginRight: '0.5rem' }}
                                                />
                                                <Button
                                                    className="pt-small"
                                                    text="load more waveforms"
                                                    intent={Intent.NONE}
                                                    onClick={async e =>
                                                        this.fetchDataOutsideInterval(
                                                            this.state.viewTimeInterval.startTimeSecs
                                                            - this.additionalWaveformAmountSecs,
                                                            this.state.viewTimeInterval.startTimeSecs)
                                                    }
                                                    icon="arrow-left"
                                                    style={{ marginRight: '0.5rem' }}
                                                />
                                                <Button
                                                    className="pt-small"
                                                    text="load more waveforms"
                                                    onClick={async e =>
                                                        this.fetchDataOutsideInterval(
                                                            this.state.viewTimeInterval.endTimeSecs,
                                                            this.state.viewTimeInterval.endTimeSecs
                                                            + this.additionalWaveformAmountSecs)
                                                    }
                                                    intent={Intent.NONE}
                                                    icon="arrow-right"
                                                />
                                            </div>
                                            <div
                                                style={{
                                                    flex: '1 1 auto',
                                                    position: 'relative'
                                                }}
                                            >
                                                <div
                                                    style={{
                                                        position: 'absolute',
                                                        top: '0px', bottom: '0px', left: '0px', right: '0px',
                                                    }}
                                                >
                                                    <Weavess
                                                        ref={ref => this.weavess = ref}
                                                        startTimeSecs={this.state.viewTimeInterval.startTimeSecs}
                                                        endTimeSecs={this.state.viewTimeInterval.endTimeSecs}
                                                        defaultZoomWindow={{
                                                            startTimeSecs: this.props.currentTimeInterval.startTimeSecs,
                                                            endTimeSecs: this.props.currentTimeInterval.endTimeSecs
                                                        }}
                                                        stations={sortedStations}
                                                        selections={{
                                                            signalDetections: this.props.selectedSdIds,
                                                            channels: this.state.selectedChannels
                                                        }}
                                                        hotKeyOverrides={{
                                                            amplitudeScale:
                                                                systemConfig.
                                                                    defaultWeavessHotKeyOverrides.amplitudeScale,
                                                            amplitudeScaleSingleReset:
                                                                systemConfig.defaultWeavessHotKeyOverrides.
                                                                    amplitudeScaleSingleReset,
                                                            amplitudeScaleReset:
                                                                systemConfig.defaultWeavessHotKeyOverrides.
                                                                    amplitudeScaleReset,
                                                        }}
                                                        events={{
                                                            onChannelExpanded: this.onChannelExpanded,
                                                            onContextMenu: this.onContextMenu,
                                                            onChannelLabelClick: this.onChannelLabelClick,
                                                            onChannelClick: this.onChannelClick,
                                                            onSignalDetectionClick: this.onSignalDetectionClick,
                                                            onKeyPress: this.onKeyPress,
                                                            onSignalDetectionDragEnd: this.onSignalDetectionDragEnd,
                                                            onMaskClick: this.onMaskClick
                                                        }}
                                                        markers={{
                                                            verticalMarkers:
                                                                [
                                                                    {
                                                                        color: '#44EE55',
                                                                        lineStyle: LineStyle.SOLID,
                                                                        timeSecs:
                                                                            this.props.currentTimeInterval.startTimeSecs
                                                                    },
                                                                    {
                                                                        color: '#44EE55',
                                                                        lineStyle: LineStyle.SOLID,
                                                                        timeSecs:
                                                                            this.props.currentTimeInterval.endTimeSecs
                                                                    }
                                                                ]
                                                        }}
                                                        flex={false}
                                                    />
                                                </div>
                                            </div>
                                        </div>
                                    )
                }
            </div>
        );
    }

    // ***************************************
    // END REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    /**
     * Updates the waveform data based on the WaveformDisplayProps
     * 
     * @param prevProps the previous props
     */
    // tslint:disable-next-line:cyclomatic-complexity
    private readonly updateWaveformData = (prevProps: WaveformDisplayProps): void => {
        if (!this.props.data ||
            !this.props.data.defaultStations ||
            !this.props.currentTimeInterval ||
            !this.props.data.defaultStations[0].signalDetections) {
            return;
        }

        // clear the cache if the current time interval has changed
        if (!lodash.isEqual(this.props.currentTimeInterval, prevProps.currentTimeInterval)) {
            this.wfDataCache.clear();
        }

        // If something is changed on the stations, but ignore changes when the event id
        // was set from defined to undefined
        // FIXME in PI4 this is very brittle. It depends on the order of props changing so we
        // don't render on things like distance being nulled out before the event status
        // goes to complete (causes the SD to change from yellow->red->green)
        if (!lodash.isEqual(this.props.data.defaultStations, prevProps.data.defaultStations) &&
            this.props.currentOpenEventHypId || !prevProps.currentOpenEventHypId) {
            this.fetchDefaultWaveforms(this.props)
                .catch();
            return;
        }
    }

    /**
     * Update the waveform sort type.
     * 
     * @param prevProps the previous props
     */
    private readonly updateWaveformSortType = (prevProps: WaveformDisplayProps) => {
        // if the event hyp event id changed set the sort type to distance
        let sortType: WaveformSortType = this.props.waveformSortType;
        if (!this.props.currentOpenEventHypId) {
            sortType = WaveformSortType.stationName;
        } else if (!prevProps.currentOpenEventHypId ||
            prevProps.currentOpenEventHypId !== this.props.currentOpenEventHypId) {
            sortType = WaveformSortType.distance;
        }

        if (this.props.waveformSortType !== sortType) {
            this.props.setSelectedSortType(sortType);
        }
    }

    /**
     * Updates the selectedSdIds.
     * 
     * @param selectedSdIds the sd ids to mark as selected
     */
    private readonly updateSelectedSdIds = (selectedSdIds: string[]) => {
        if (!lodash.isEqual(this.props.selectedSdIds, selectedSdIds)) {
            this.props.setSelectedSdIds(selectedSdIds);
        }
    }

    /**
     * Initialize graphql subscriptions on the apollo client
     */
    private readonly setupSubscriptions = (props: WaveformDisplayProps): void => {
        if (!props.data) return;

        // unsubscribe from all current subscriptions
        this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
        this.unsubscribeHandlers.length = 0;

        // don't register subscriptions if the current time interval is undefined/null
        if (!props.currentTimeInterval) return;

        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                document: waveformSegmentsAddedSubscription,
                updateQuery: (prev: WaveformDisplayData, cur) => {
                    const currentInterval = this.state.viewTimeInterval;
                    const data = cur.subscriptionData.data as WaveformSegmentsAddedSubscription;

                    // For each newly-added waveform channel segment received via subscription...
                    data.waveformChannelSegmentsAdded.forEach(segmentAdded => {
                        // If the new segment overlaps the current interval,
                        // Retrieve the waveform samples for the segment
                        if (segmentAdded.startTime < currentInterval.endTimeSecs
                            && segmentAdded.endTime > currentInterval.startTimeSecs) {
                            this.fetchAndCacheWaveforms(
                                Math.max(segmentAdded.startTime, currentInterval.startTimeSecs),
                                Math.min(segmentAdded.endTime, currentInterval.endTimeSecs),
                                [segmentAdded.channel.id])
                                .then(() => {
                                    this.convertWaveforms(this.props);
                                })
                                .catch(e => window.alert(e));
                        }
                    });

                    return prev;
                }
            })
        );
        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                document: detectionsCreatedSubscription,
                updateQuery: (prev: WaveformDisplayData, cur) => {
                    const data = cur.subscriptionData.data as DetectionsCreatedSubscription;

                    if (data) {
                        // merge the new signal detection into the appropriate place in the current data.
                        // most of this work is done to avoid mutating any data
                        const prevStations = prev.defaultStations;
                        const newStations = (prevStations) ? [...prevStations] : [];

                        data.detectionsCreated.forEach(detectionCreated => {

                            // if the newly created detection is outside the current interval, don't add it
                            const arrivalTimeSecs = detectionCreated.currentHypothesis.arrivalTimeMeasurement.timeSec;
                            if (arrivalTimeSecs < this.state.viewTimeInterval.startTimeSecs
                                || arrivalTimeSecs > this.state.viewTimeInterval.endTimeSecs) {
                                return;
                            }

                            const newDetection = {
                                ...detectionCreated,
                                station: undefined
                            };

                            const prevStation = newStations
                                .find(station => station.id === detectionCreated.station.id);
                            const prevDetections = prevStation.signalDetections;
                            const newDetections = [...prevDetections, newDetection];

                            const newStation = {
                                ...prevStation,
                                signalDetections: newDetections
                            };

                            newStations[newStations.findIndex(station => station.id === newStation.id)] = newStation;
                        });
                        return {
                            defaultStations: newStations
                        };
                    }
                }
            })
        );
        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                document: detectionsUpdatedSubscription
            })
        );
        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                document: detectionHypothesesRejectedSubscription
            })
        );
        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                document: eventUpdatedSubscription
            })
        );
    }

    /**
     * If necessary, fetch waveforms from the api-gateway, then convert them to the weavess representation for display
     */
    private readonly fetchDefaultWaveforms = async (props: WaveformDisplayProps) => {
        if (!props.data.defaultStations || this.isFetching) {
            return;
        }

        this.isFetching = true;
        const startTime = props.currentTimeInterval.startTimeSecs;
        const endTime = props.currentTimeInterval.endTimeSecs;

        const defaultChannelIds = props.data.defaultStations.map(station => station.defaultChannel.id);

        // check if there are any new channel IDs whose waveform data we haven't already cached.
        const newChannelIds = lodash.difference(defaultChannelIds, this.wfDataCache.getChannelIds());
        // Retrieve waveform sample data for the new channel IDs and input time range, adding
        // the waveforms to the cache
        if (newChannelIds.length > 0) {
            newChannelIds.forEach(channelId => this.wfDataCache.clear(channelId));
            this.setState({
                loading: true
            });
        }
        await this.fetchAndCacheWaveforms(startTime, endTime, newChannelIds, true);
        this.convertWaveforms(this.props);
        this.isFetching = false;
    }

    /**
     * Load data outside the current interval.
     * assumes data has already been loaded, and the waveform cache has entries
     */
    private readonly fetchDataOutsideInterval = async (startTimeSecs: number, endTimeSecs: number) => {
        const channelIds = this.wfDataCache.getChannelIds();
        // Retrieve waveform sample data for the channel IDs and input time range, adding
        // the waveforms to the cache
        this.fetchSignalDetectionsOutsideInterval(startTimeSecs, endTimeSecs, this.props.currentOpenEventHypId);
        await this.fetchAndCacheWaveforms(startTimeSecs, endTimeSecs, channelIds);

        this.setState((prevState: WaveformDisplayState) => ({
            viewTimeInterval: {
                startTimeSecs: Math.min(prevState.viewTimeInterval.startTimeSecs, startTimeSecs),
                endTimeSecs: Math.max(prevState.viewTimeInterval.endTimeSecs, endTimeSecs)
            }
        }));

        this.convertWaveforms(this.props);
    }

    private readonly fetchSignalDetectionsOutsideInterval = (startTimeSecs: number,
        endTimeSecs: number,
        currentOpenEventHypId: string) => {
        const variables: WaveformDisplayQueryInput = {
            timeRange: {
                startTime: startTimeSecs,
                endTime: endTimeSecs
            },
            distanceToSourceInput: {
                sourceId: currentOpenEventHypId,
                sourceType: 'Event'
            }
        };
        this.props.data.fetchMore({
            query: waveformDisplayQuery,
            variables,
            updateQuery: (prev: WaveformDisplayData, cur) => {
                const data = cur.fetchMoreResult as WaveformDisplayData;

                // merge the new detections into the current data, respecting the immutable state
                const prevStations = prev.defaultStations;
                const newStations = [...prevStations];

                data.defaultStations.forEach(station => {
                    const prevStation = prevStations.find(prevSta => prevSta.id === station.id);
                    const oldDetections = prevStation.signalDetections;
                    const newDetections = [...oldDetections, ...station.signalDetections];

                    const newStation = {
                        ...prevStation,
                        signalDetections: newDetections
                    };
                    newStations[newStations.findIndex(sta => sta.id === prevStation.id)] = newStation;
                });

                return {
                    defaultStations: newStations
                };
            }
        })
            .catch();
    }

    /**
     * Retrieves ChannelSegments from the API gateway for the provided time range and channel IDs, and
     * updates the waveform data cache based on the results. If the overwrite parameter is provided and
     * set to true, cache entries will be overwritten with the retrieved ChannelSegments; otherwise
     * timeseries data from the retrieved ChannelSegments will be added to the existing cache entries
     * where they exist.
     */
    private readonly fetchAndCacheWaveforms = async (startTimeSecs: number,
        endTimeSecs: number,
        channelIds: string[],
        overwrite: boolean = false) => {

        // Retrieve ChannelSegments from the API gateway for the provided time range and channle IDs
        let channelSegments: ChannelSegment[] = [];
        if (channelIds.length > 0) {
            channelSegments = await fetchWaveforms(startTimeSecs, endTimeSecs, channelIds);
        }

        // Update the waveform data cache from the retrieved ChannelSegments
        this.wfDataCache.updateFromChannelSegments(channelSegments, overwrite);
    }

    /**
     * Convert the current props to weavess waveforms and display,
     * assuming sample data has already been fetched & is in the cache
     */
    private readonly convertWaveforms = (props: WaveformDisplayProps) => {
        if (!props.data.defaultStations) return;
        const weavessStations: StationConfig[] = props.data.defaultStations.map(station => {
            // if we didn't get any waveform data for this station, set it to dummy data in the cache
            const cachedData = this.wfDataCache.get(station.defaultChannel.id);
            const sampleRate = cachedData && cachedData.sampleRate ? cachedData.sampleRate : 1;
            const dataSegments = cachedData &&
                cachedData.dataSegments ? cachedData.dataSegments
                : [{ startTimeSecs: props.currentTimeInterval.startTimeSecs, data: [] }];

            let distanceToEvent: number;
            if (station.distanceToSource) distanceToEvent = station.distanceToSource.distanceKm;

            const weavessStation: StationConfig = {
                id: station.id,
                name: station.name,
                height: this.calculateStationHeight(),
                distanceKm: distanceToEvent,
                defaultChannel: {
                    id: station.id,
                    color: userPreferences.colors.waveforms.raw,
                    name: `${station.name}/${station.defaultChannel.name}`,
                    sampleRate,
                    dataSegments,
                    description: station.defaultChannel.channelType,
                    signalDetections: station.signalDetections
                        .filter(detection => detection && !detection.currentHypothesis.isRejected)
                        .map(detection => {
                            const associatedEvent = props.data.eventHypothesesInTimeRange
                                .find(eh => detection.currentHypothesis.signalDetectionAssociations
                                    .find(assoc => assoc.eventHypothesis.id === eh.id) !== undefined);

                            const isComplete = associatedEvent && associatedEvent.event.status === 'Complete';
                            const isSelectedEvent =
                                associatedEvent && associatedEvent.id === props.currentOpenEventHypId;

                            // If a detection is in the current interval and it was created after the
                            // current interval was opened and the detection was created by System
                            // (as opposed to an analyst)
                            /*const detectionTime = detection.currentHypothesis.arrivalTimeMeasurement.timeSec;
                            const isNewDetection = detectionTime > this.props.currentTimeInterval.startTimeSecs &&
                                                detectionTime < this.props.currentTimeInterval.endTimeSecs &&
                                                detection.currentHypothesis.creationInfo.creatorType == "System" &&
                                                detection.currentHypothesis.creationInfo.creationTime >
                                                    this.currentIntervalOpenTime;
                            const color: string =
                                associatedEvent ?
                                    isSelectedEvent ?
                                        analystUiConfig.colors.events.inProgress
                                        : isComplete ?
                                            analystUiConfig.colors.events.complete
                                            : isNewDetection ? analystUiConfig.colors.signalDetections.newDetection
                                    : analystUiConfig.colors.events.toWork
                                        : analystUiConfig.colors.signalDetections.unassociated;*/

                            const color: string =
                                associatedEvent ?
                                    isSelectedEvent ?
                                        userPreferences.colors.events.inProgress
                                        : isComplete ?
                                            userPreferences.colors.events.complete
                                            : userPreferences.colors.events.toWork
                                    : userPreferences.colors.signalDetections.unassociated;

                            return {
                                timeSecs: detection.currentHypothesis.arrivalTimeMeasurement.timeSec,
                                label: detection.currentHypothesis.phase,
                                id: detection.id,
                                color
                            };
                        }),
                    masks: undefined
                },
                nonDefaultChannels: lodash.flatMap(station.sites, site => site.channels.map(channel => {
                    if (!this.wfDataCache.get(channel.id)) {
                        return {
                            id: channel.id,
                            color: userPreferences.colors.waveforms.raw,
                            name: `${site.name}/${channel.name}`,
                            sampleRate: 1,
                            dataSegments: [{
                                data: [],
                                startTimeSecs: props.currentTimeInterval.startTimeSecs
                            }],

                            // if the mask category matches the enabled masks then return the mask else skip it
                            masks: channel.qcMasks.filter(qcMask => Object.keys(this.state.maskDisplayFilters)
                                .find(key => qcMask.currentVersion.category === key &&
                                    this.state.maskDisplayFilters[key].visible))
                                .map(qcMask => ({
                                    id: qcMask.id,
                                    startTimeSecs: qcMask.currentVersion.startTime,
                                    endTimeSecs: qcMask.currentVersion.endTime,
                                    color: userPreferences.colors.waveforms.
                                        maskDisplayFilters[qcMask.currentVersion.category].color,
                                }))
                        };
                    }

                    const cachedSubChannelData = this.wfDataCache.get(channel.id);
                    const subChannelSampleRate = cachedSubChannelData.sampleRate;
                    const subChannelDataSegments = cachedSubChannelData.dataSegments;
                    return {
                        id: channel.id,
                        name: `${site.name}/${channel.name}`,
                        sampleRate: subChannelSampleRate,
                        dataSegments: subChannelDataSegments,
                        // if the mask category matches the enabled masks then return the mask else skip it
                        masks: channel.qcMasks.filter(qcMask => Object.keys(this.state.maskDisplayFilters)
                            .find(key => qcMask.currentVersion.category === key &&
                                this.state.maskDisplayFilters[key].visible))
                            .map(qcMask => ({
                                id: qcMask.id,
                                startTimeSecs: qcMask.currentVersion.startTime,
                                endTimeSecs: qcMask.currentVersion.endTime,
                                color: userPreferences.colors.waveforms.
                                    maskDisplayFilters[qcMask.currentVersion.category].color,
                            }))
                    };
                })
                ),
            };
            return weavessStation;
        })
            .filter(weavessStation => weavessStation !== undefined);

        if (this.state.loading || !lodash.isEqual(this.state.stations, weavessStations)) {
            this.setState({
                stations: weavessStations,
                loading: false
            });
        }
    }

    /**
     * Toggle measure window visability
     */
    private readonly toggleMeasureWindow = () => {
        if (this.weavess) this.weavess.toggleMeasureWindowVisability();
    }

    /**
     * Display QC Mask information.
     */
    private readonly onMaskClick = (event: React.MouseEvent<HTMLDivElement>, channelId: string, masks: string[]) => {
        if (masks && masks.length > 0) {
            const qcMasks = lodash.flatMap(this.props.data.defaultStations, station =>
                lodash.flatMap(station.sites, site =>
                    lodash.flatMap(site.channels, c => c.qcMasks)))
                .filter(m => lodash.includes(masks, m.id));

            ContextMenu.show(
                <QcMaskDetails masks={qcMasks} />,
                { left: event.clientX, top: event.clientY }, () => {
                    // menu was closed; callback optional
                });
        }
    }

    private readonly onChannelExpanded = (channelId: string) => {
        // get the ids of all sub-channels
        // TODO trim this list down based on what's already in the cache
        const subChannelIds: string[] = lodash.flattenDeep<string>(
            this.props.data.defaultStations.find(station => station.id === channelId)
                .sites
                .map(site => site.channels.map(channel => channel.id)));

        this.fetchAndCacheWaveforms(this.state.viewTimeInterval.startTimeSecs,
                                    this.state.viewTimeInterval.endTimeSecs, subChannelIds)
            .then(() => {
                this.convertWaveforms(this.props);
            })
            .catch(e => window.alert(e));
    }

    /**
     * Handle clicks on a channel
     */
    private readonly onChannelLabelClick = (e: React.MouseEvent<HTMLDivElement>, channelId: string) => {
        e.preventDefault();

        const alreadySelected = this.state.selectedChannels.indexOf(channelId) > -1;

        // if ctrl|meta is pressed, append to current list, otherwise new singleton list
        let selectedChannels: string[] =
            e.metaKey || e.ctrlKey ?
                alreadySelected ?
                    // ctrl|meta + already selected = remove the element
                    this.state.selectedChannels.filter(id => id !== channelId)
                    // ctrl|meta + not selected = add to selection list
                    : [...this.state.selectedChannels, channelId]
                : alreadySelected ?
                    // already selected = unselect
                    []
                    // not selected = select
                    : [channelId];

        const clickedDefaultChannel = this.props.data.defaultStations.find(station => station.id === channelId);
        if (e.shiftKey && clickedDefaultChannel) {
            // The click occured on a default channel while the shift key was held down.
            // Look up all of the subchannels that fall under the selected default channel.
            const subChannelIds: string[] =
                lodash.flattenDeep<string>(clickedDefaultChannel.sites
                    .map(site => site.channels
                        .map(channel => channel.id)));

            // If the default channel was previously selected, unselect the default channel
            // and all of its subchannels.  Otherwise, select all of the channels.
            // Toggle the state of selected default channel and its subchannels.
            selectedChannels =
                alreadySelected ?
                    // shift + default channel is already selected = unselect the default
                    // channel and all of its subchannels.
                    selectedChannels.filter(id => subChannelIds.indexOf(id) < 0)
                    // shift + default channel is not selected = select the default
                    // channel and all of its subchannels.
                    : lodash.uniq([...selectedChannels, ...subChannelIds]);
        }

        this.setState({
            selectedChannels
        });
        // clear signal detection selections
        this.updateSelectedSdIds([]);
    }

    /**
     * Register channel click events
     */
    private readonly onChannelClick = (e: React.MouseEvent<HTMLDivElement>, stationId: string, timeSecs: number) => {
        // ctrl or meta click = create a signal detection
        if (e.ctrlKey || e.metaKey) {
            const clickedDefaultChannel = this.props.data.defaultStations.find(station => station.id === stationId);
            if (clickedDefaultChannel) {
                const input: CreateDetectionInput = {
                    stationId,
                    phase: this.state.defaultSdPhase,
                    time: timeSecs,
                    timeUncertaintySec: 0.5,
                };
                this.props.createDetection({
                    variables: {
                        input
                    }
                })
                    .catch(err => window.alert(err));
            }
        }
    }

    /**
     * Handle clicks on a signal detection
     */
    private readonly onSignalDetectionClick = (e: React.MouseEvent<HTMLDivElement>, sdId: string) => {
        e.preventDefault();

        if (e.shiftKey) {
            // display information of the signal detection
            const detection = lodash.flatMap(this.props.data.defaultStations, station =>
                station.signalDetections.filter(d => d.id === sdId));

            ContextMenu.show(
                <SignalDetectionDetails detection={detection[0]} />,
                { left: e.clientX, top: e.clientY }, () => {
                    // menu was closed; callback optional
                });

        } else {
            const alreadySelected = this.props.selectedSdIds.indexOf(sdId) > -1;

            // if ctrl is pressed, append to current list, otherwise new singleton list
            const selectedSdIds: string[] =
                e.metaKey || e.ctrlKey ?
                    alreadySelected ?
                        // meta + already selected = remove the element
                        this.props.selectedSdIds.filter(id => id !== sdId)
                        // meta + not selected = add to selection list
                        : [...this.props.selectedSdIds, sdId]
                    : alreadySelected ?
                        // already selected = unselect
                        []
                        // not selected = select
                        : [sdId];

            this.setState({
                // clear channel selections
                selectedChannels: []
            });
            this.updateSelectedSdIds(selectedSdIds);
        }
    }

    /**
     * On drag-end of a signal detection, update it with a new time.
     */
    private readonly onSignalDetectionDragEnd = (sdId: string, timeSecs: number): void => {
        const input: UpdateDetectionsInput = {
            detectionIds: [sdId],
            input: {
                time: timeSecs,
                timeUncertaintySec: 0.5
            }
        };
        this.props.updateDetections({
            variables: input
        })
            .catch(err => window.alert(err));

        this.setState({
            // clear channel selections and select current detection
            selectedChannels: [sdId]
        });
        this.updateSelectedSdIds([sdId]);
    }

    /**
     * Handle key press on waveform display
     */
    private readonly onKeyPress = (e: React.KeyboardEvent<HTMLDivElement>,
        clientX: number,
        clientY: number,
        channelId: string,
        timeSecs: number) => {
        e.preventDefault();
        if (e.key === 'Escape') {
            this.setState({
                selectedChannels: []
            });
        } else if (e.ctrlKey || e.metaKey) {
            switch (e.key) {
                case 's':
                    this.showRephaseMenu(clientX, clientY);
                    return;
                case '-':
                    this.setAnalystNumberOfWaveforms(this.state.analystNumberOfWaveforms + 1);
                    return;
                case '=':
                    this.setAnalystNumberOfWaveforms(this.state.analystNumberOfWaveforms - 1);
                    return;
                default:
                    return;
            }
        } else {
            switch (e.key) {
                case 'Delete':
                    this.rejectDetections(this.props.selectedSdIds);
                    return;
                case 'Backspace':
                    this.rejectDetections(this.props.selectedSdIds);
                    return;
                default:
                    return;
            }
        }

    }

    /**
     * Show an appropriate context menu based on what's currently selected, etc.
     */
    private readonly onContextMenu = (e: React.MouseEvent<HTMLDivElement>, channelId: string, sdId?: string) => {
        e.preventDefault();

        // if provided && not already selected, set the current selection to just the context-menu'd detection
        const detections = sdId && this.props.selectedSdIds.indexOf(sdId) === -1 ?
            [sdId]
            : this.props.selectedSdIds;
        this.updateSelectedSdIds(detections);

        // if running in electron, use an electron native context menu
        // TODO address/workaround this bug with the electron context menu which causes a click on the underlying
        // dom element when dismissing the context menu by clicking elsewhere
        // https://github.com/electron/electron/issues/6770
        if (electron) {
            const menu = WfDisplayElectronContextMenu(detections, this.rePhaseDetections, this.rejectDetections);
            menu.popup(electron.remote.getCurrentWindow(), {
                async: true
            });
        } else {
            // otherwise, use a blueprint one.

            // TODO follow up on this issue
            // https://github.com/palantir/blueprint/issues/1640
            const stageIntervalContextMenu =
                WfDisplayBlueprintContextMenu(detections, this.rePhaseDetections, this.rejectDetections);
            ContextMenu.show(
                stageIntervalContextMenu, {
                    left: e.clientX,
                    top: e.clientY
                });
        }
    }

    /**
     * Show a context menu with re-phase options.
     */
    private readonly showRephaseMenu = (clientX: number, clientY: number) => {
        if (this.props.selectedSdIds.length === 0) return;

        if (electron) {
            const menu = SetPhaseElectronContextMenu(this.props.selectedSdIds, this.rePhaseDetections);
            menu.popup(electron.remote.getCurrentWindow());
        } else {
            const stageIntervalContextMenu =
                SetPhaseBlueprintContextMenu(this.props.selectedSdIds, this.rePhaseDetections);
            ContextMenu.show(
                stageIntervalContextMenu, {
                    left: clientX,
                    top: clientY
                });
        }
    }

    /**
     * modify phase for detections
     */
    private readonly rePhaseDetections = (sdIds: string[], phase: string) => {
        const input: UpdateDetectionsInput = {
            detectionIds: sdIds,
            input: {
                phase
            }
        };
        this.props.updateDetections({
            variables: input
        })
            .catch(err => window.alert(err));
    }

    /**
     * reject multiple detections
     */
    private readonly rejectDetections = (sdIds: string[]) => {
        const input: RejectDetectionsInput = {
            detectionIds: sdIds
        };
        this.props.rejectDetectionHypotheses({
            variables: input
        })
            .catch(err => window.alert(err));
    }

    /**
     * sort waveform list based on sort type
     */
    private readonly sortWaveformList = (): StationConfig[] => {
        if (!this.props.data.defaultStations) return [];
        // apply sort based on sort type
        let newStations = [];
        // Sort by distance
        if (this.props.waveformSortType === WaveformSortType.distance) {
            newStations = lodash.sortBy(this.state.stations, [station => station.distanceKm]);
        } else {
            // For station name sort, order a-z by station config name
            if (this.props.waveformSortType === WaveformSortType.stationName) {
                newStations = lodash.orderBy(this.state.stations, [station => station.name], ['asc']);
            }
        }
        return newStations;
    }

    /**
     * Set the mask filters selected in the qc mask legend
     */
    private readonly setMaskDisplayFilters = (key: string, maskDisplayFilter: MaskDisplayFilter) => {
        this.setState(
            {
                maskDisplayFilters: {
                    ...this.state.maskDisplayFilters,
                    [key]: maskDisplayFilter
                }
            },
            () => {
                this.convertWaveforms(this.props);
            }
        );
    }

    /**
     * Display the number of waveforms choosen by the analyst
     * Also updates the state variable holding the selection
     */
    private readonly displayNumberOfWaveforms = (stations: StationConfig[]): StationConfig[] => {
        stations.forEach(station =>
            station.height = this.calculateStationHeight());
        return stations;
    }

    /**
     * Calculate height for the station based of number of display
     */
    private readonly calculateStationHeight = (): number => {
        const waveformDisplayButtonsAndAxisHeightPx = 100;
        return (this.waveformDisplayRef.clientHeight - waveformDisplayButtonsAndAxisHeightPx)
            / this.state.analystNumberOfWaveforms;
    }

    /**
     * Update the state of the Analyst number of Waveforms
     */
    private readonly setAnalystNumberOfWaveforms = (value: number, valueAsString?: string) => {
        const base = 10;
        let analystNumberOfWaveforms = value;

        if (valueAsString) {
            // tslint:disable-next-line:no-parameter-reassignment
            valueAsString = valueAsString.replace(/e|\+|-/, '');
            analystNumberOfWaveforms = isNaN(parseInt(valueAsString, base)) ?
                this.state.analystNumberOfWaveforms : parseInt(valueAsString, base);
        }

        // minimum number of waveforms must be 1
        if (analystNumberOfWaveforms < 1) {
            analystNumberOfWaveforms = 1;
        }

        if (this.state.analystNumberOfWaveforms !== analystNumberOfWaveforms) {
            this.setState({
                analystNumberOfWaveforms
            });
        }
    }
}
// tslint:disable-next-line:max-file-line-count
