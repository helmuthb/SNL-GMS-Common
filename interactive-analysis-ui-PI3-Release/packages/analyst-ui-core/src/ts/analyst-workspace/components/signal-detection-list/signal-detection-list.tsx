import * as Gl from '@gms/golden-layout';
import { Column, Row, Table, TableApi } from '@gms/ui-core-components';
import { flatten, isEqual } from 'lodash';
import * as React from 'react';
import { ChildProps, MutationFunc } from 'react-apollo';
import { userPreferences } from '../../config';
import { TimeInterval } from '../../state';
import { addGlForceUpdateOnResize, addGlForceUpdateOnShow } from '../../util/gl-util';
import { UpdateDetectionsInput } from './graphql/mutations';
import { SignalDetectionListData } from './graphql/query';
import {
    detectionsCreatedSubscription, DetectionsCreatedSubscription,
    detectionsRejectedSubscription,
    detectionsUpdatedSubscription,
    eventUpdatedSubscription
} from './graphql/subscriptions';
import * as Render from './render-functions';

/**
 * Handle for user preference filtering functionality
 */
const autoFilter = userPreferences.signalDetectionList.autoFilter;

/**
 * Different filters that are available
 */
export enum FilterType {
    allRows = 'All Detections',
    openEvent = 'Open Event',
    complete = 'Completed',
}

export interface SignalDetectionListRow extends Row {
    id: string;
    hypothesisId: string;
    station: string;
    phase: string;
    time: number;
    timeUnc: number;
    assocEventId: string;
    color: string;
}

/**
 * signal detection list local state
 */
export interface SignalDetectionListState {
    selectedFilter: FilterType;
}

/**
 * Mutations used by the workflow display
 */
export interface SignalDetectionListMutations {
    // {} because we don't care about mutation results for now, handling that through subscriptions
    updateDetections: MutationFunc<{}>;
}

/**
 * Props mapped in from Redux state
 */
export interface SignalDetectionListReduxProps {
    // Passed in from golden-layout
    glContainer?: Gl.Container;
    // The currently-open processing interval time range
    currentTimeInterval: TimeInterval;
    // The currently-open event hypothesis IDs
    openEventHypId: string;
    // The currently-selected signal detection IDs
    selectedSdIds: string[];

    // callbacks
    setSelectedSdIds(ids: string[]): void;
}

/**
 * Consolidated props type for signal detection list
 */
export type SignalDetectionListProps = SignalDetectionListReduxProps
    & ChildProps<SignalDetectionListMutations, SignalDetectionListData>;

/**
 * Displays signal detection information in tabular form
 */
export class SignalDetectionList extends React.Component<SignalDetectionListProps, SignalDetectionListState> {

    /**
     * To interact directly with the table
     */
    private mainTable: TableApi;

    /**
     * Handlers to unsubscribe from apollo subscriptions
     */
    private readonly unsubscribeHandlers: { (): void }[] = [];

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
        this.state = {
            selectedFilter: FilterType.allRows
        };
    }

    /**
     * Updates the derived state from the next props.
     * 
     * @param nextProps The next (new) props
     * @param prevState The previous state
     */
    public static getDerivedStateFromProps(nextProps: SignalDetectionListProps, prevState: SignalDetectionListState) {
        // we need to set new filters based on incoming props
        if (autoFilter) {
            if (nextProps.openEventHypId) {
                return {
                    selectedFilter: FilterType.openEvent
                };
            }

            return {
                selectedFilter: FilterType.allRows
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
     * React component lifecycle
     * @param prevProps The previous properties available to this react component
     */
    public componentDidUpdate(prevProps: SignalDetectionListProps) {
        if (this.props.currentTimeInterval &&
            !isEqual(this.props.currentTimeInterval, prevProps.currentTimeInterval)) {
            this.setupSubscriptions(this.props);
        }

        if (this.mainTable && this.mainTable.getSortModel().length === 0) {
            this.mainTable.setSortModel([{ colId: 'time', sort: 'asc' }]);
        }

        // if the selected event has changed, select it in the table
        if (prevProps.selectedSdIds !== this.props.selectedSdIds) {
            this.selectRowsFromProps(this.props);
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
        // Creates the table column definitions
        const mainTableColumnDefs: Column[] = Render.getColumnDefs();

        const mainTableRowData: SignalDetectionListRow[] = this.props.data && this.props.data.defaultStations ?
            this.generateTableRows(this.props)
            : [];

        return (
            <div
                style={{ height: '100%', width: '100%', padding: '0.5rem', overflow: 'auto' }}
                className="ag-dark"
            >
                <div
                    style={{
                        height: '100%', display: 'flex', flexDirection: 'column',
                    }}
                >
                    <div className="pt-select pt-fill" style={{ marginBottom: '0.5rem' }}>
                        <select value={this.state.selectedFilter} onChange={this.handleFilterChange}>
                            {Render.createDropdownItems()}
                        </select>
                    </div>
                    <div style={{ flex: '1 1 auto', position: 'relative', minHeight: '100px' }}>
                        <div className="max">
                            <Table
                                context={{
                                }}
                                onGridReady={this.onMainTableReady}
                                columnDefs={mainTableColumnDefs}
                                rowData={mainTableRowData}
                                onCellValueChanged={this.onCellValueChanged}
                                getRowNodeId={node => node.id}
                                deltaRowDataMode={true}
                                getRowClass={this.computeRowClass}
                                onRowClicked={this.onRowClicked}
                                rowSelection="multiple"
                                rowDeselection={true}
                                overlayNoRowsTemplate="No SDs Loaded"
                                enableSorting={true}
                                enableFilter={true}
                                enableColResize={true}
                            />
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
     * Select rows in the table based on the selected SD IDs in the properties.
     */
    private readonly selectRowsFromProps = (props: SignalDetectionListProps) => {
        if (this.mainTable) {
            this.mainTable.deselectAll();
            this.mainTable.forEachNode(node => {
                props.selectedSdIds.forEach(sdId => {
                    if (node.data.id === sdId) {
                        node.setSelected(true);
                        // must pass in null here as ag-grid expects it
                        this.mainTable.ensureNodeVisible(node, null);
                    }
                });
            });
        }
    }

    /**
     * Initialize graphql subscriptions on the apollo client
     */
    private readonly setupSubscriptions = (props: SignalDetectionListProps): void => {
        if (!props.data) return;

        // first, unsubscribe from all current subscriptions
        this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
        this.unsubscribeHandlers.length = 0;

        // don't register subscriptions if the current time interval is undefined/null
        if (!props.currentTimeInterval) return;

        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                document: detectionsCreatedSubscription,
                updateQuery: (prev: SignalDetectionListData, cur) => {
                    const data = cur.subscriptionData.data as DetectionsCreatedSubscription;

                    // merge the new signal detection into the appropriate place in the current data.
                    // most of this work is done to avoid mutating any data
                    const prevStations = prev.defaultStations;
                    const newStations = [...prevStations];

                    if (data) {
                        data.detectionsCreated.forEach(detectionCreated => {

                            // if the newly created detection is outside the current interval, don't add it
                            const arrivalTimeSecs = detectionCreated.currentHypothesis.arrivalTimeMeasurement.timeSec;
                            if (arrivalTimeSecs < this.props.currentTimeInterval.startTimeSecs
                                || arrivalTimeSecs > this.props.currentTimeInterval.endTimeSecs) {
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
                    }

                    return {
                        defaultStations: newStations
                    };
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
                document: detectionsRejectedSubscription
            })
        );
        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                document: eventUpdatedSubscription
            })
        );
    }

    /**
     * Set class members when main table is ready
     */
    private readonly onMainTableReady = (e: any) => {
        this.mainTable = e.api;
    }

    /**
     * style rows
     */
    private readonly computeRowClass = (params: any) => {
        // if the signal detection is outside the current interval, show it in a disabled state
        if (params.data.disabled) {
            return 'signal-detection-list-disabled-row';
        }
    }

    /**
     * Convert the signal detection data into table rows
     */
    private readonly generateTableRows = (props: SignalDetectionListProps): SignalDetectionListRow[] =>
        // For each station, filter down to the detections that are not rejected (we don't show rejected detections).
        // For each non-rejected detection, extract the table row content into a collection.
        // Finally, flatten the collection of row collections into a single row collection
        flatten(props.data.defaultStations.map(station =>
            station.signalDetections.filter(detection => this.filterDetection(detection))
                .map(detection =>
                    ({
                        id: detection.id,
                        hypothesisId: detection.currentHypothesis.id,
                        station: station.id,
                        phase: detection.currentHypothesis.phase,
                        time: detection.currentHypothesis.arrivalTimeMeasurement.timeSec,
                        timeUnc: detection.currentHypothesis.arrivalTimeMeasurement.uncertaintySec,
                        assocEventId: detection.currentHypothesis.signalDetectionAssociations.length > 0
                            ? detection.currentHypothesis.signalDetectionAssociations[0].eventHypothesis.id
                            : undefined,
                        color: this.determineDetectionColor(detection),
                    })
                )
        )
        )

    /**
     * Determine the color for the detection list marker based on the state of the detection.
     */
    private readonly determineDetectionColor = (detection: any): string => {
        const associatedEvent = detection.currentHypothesis.signalDetectionAssociations.length > 0
            ? detection.currentHypothesis.signalDetectionAssociations[0].eventHypothesis
            : undefined;
        const isComplete = associatedEvent && associatedEvent.event.status === 'Complete';
        const isSelectedEvent = associatedEvent && associatedEvent.id === this.props.openEventHypId;
        return associatedEvent ?
            isSelectedEvent ?
                userPreferences.colors.events.inProgress
                : isComplete ?
                    userPreferences.colors.events.complete
                    : userPreferences.colors.events.toWork
            : userPreferences.colors.signalDetections.unassociated;
    }

    /**
     * Determines if the detection should be included based on detection status and the current filter
     * @param detection detection to filter
     */
    private readonly filterDetection = (detection: any) => {
        const associatedEvent = detection.currentHypothesis.signalDetectionAssociations.length > 0
            ? detection.currentHypothesis.signalDetectionAssociations[0].eventHypothesis
            : undefined;
        const isSelectedEvent = associatedEvent && associatedEvent.id === this.props.openEventHypId;
        const isComplete = associatedEvent && associatedEvent.event.status === 'Complete';

        const filter = this.state.selectedFilter === FilterType.openEvent ? isSelectedEvent
            : this.state.selectedFilter === FilterType.complete ? isComplete
                : true /*All Rows*/;

        return !detection.currentHypothesis.isRejected && filter;
    }

    /**
     * Handles the filter dropdown change event
     * @param event react form event
     */
    private readonly handleFilterChange = (event: React.FormEvent<HTMLSelectElement>) => {
        // update the filter
        const newFilter = event.currentTarget.value as FilterType;
        this.setState((prevState: SignalDetectionListState) => ({
            selectedFilter: newFilter
        }));
    }

    /**
     * Update the selected SD IDs when the user clicks on a row in the table.
     */
    private readonly onRowClicked = (params: any) => {
        if (this.mainTable) {
            const selectedSdIds = this.mainTable.getSelectedNodes()
                .map(node => node.data.id);
            this.props.setSelectedSdIds(selectedSdIds);
        }
    }

    private readonly onCellValueChanged = (params: any) => {
        // exit if the value didn't actually change, at least according to ===
        if (params.oldValue === params.newValue) return;

        const tableRow: SignalDetectionListRow = params.data;
        const variables: UpdateDetectionsInput = {
            detectionIds: [tableRow.id],
            input: {
                phase: tableRow.phase,
                time: tableRow.time,
                timeUncertaintySec: tableRow.timeUnc
            }
        };
        this.props.updateDetections({
            variables
        })
            .catch(err => window.alert(err));
    }
}
