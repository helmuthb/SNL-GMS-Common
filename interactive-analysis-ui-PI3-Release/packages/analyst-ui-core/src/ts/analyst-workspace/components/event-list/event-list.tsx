import { Button } from '@blueprintjs/core';
import * as Gl from '@gms/golden-layout';
import { Column, Row, Table, TableApi } from '@gms/ui-core-components';
import * as lodash from 'lodash';
import * as React from 'react';
import { ChildProps, MutationFunc } from 'react-apollo';
import { analystUiConfig } from '../../config';
import { TimeInterval } from '../../state';
import { addGlForceUpdateOnResize, addGlForceUpdateOnShow } from '../../util/gl-util';
import * as Columns from './column-defs';
import { UpdateEventsVariables } from './graphql/mutations';
import { EventListData } from './graphql/query';
import { eventUpdatedSubscription } from './graphql/subscriptions';

/**
 * event list local state
 */
// tslint:disable-next-line:no-empty-interface
export interface EventListState {

}

/**
 * Mutations used in the event list
 */
export interface EventListMutations {
    updateEvents: MutationFunc<{}>;
}

/**
 * Props mapped in from Redux state
 */
export interface EventListReduxProps {
    // passed in from golden-layout
    glContainer?: Gl.Container;
    currentTimeInterval: TimeInterval;

    openEventHypId: string;
    selectedEventHypIds: string[];

    // callbacks
    setOpenEventHypId(id: string): void;
    setSelectedEventHypIds(ids: string[]): void;
}

/**
 * Consolidated props type for event list
 */
export type EventListProps = EventListReduxProps & ChildProps<EventListMutations, EventListData>;

/**
 * Displays event information in tabular form
 */
export class EventList extends React.Component<EventListProps, EventListState> {

    /**
     * To interact directly with the table
     */
    private mainTable: TableApi;

    /**
     * To interact directly with the completed table
     */
    private completedTable: TableApi;

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
    public constructor(props: EventListProps) {
        super(props);
        this.state = {};
    }

    /*
     * Invoked when the componented mounted.
     */
    public componentDidMount() {
       addGlForceUpdateOnShow(this.props.glContainer, this);
       addGlForceUpdateOnResize(this.props.glContainer, this);
    }

    /**
     * React component lifecycle
     * 
     */
    public componentDidUpdate(prevProps: EventListProps) {
        if (this.props.currentTimeInterval &&
            !lodash.isEqual(this.props.currentTimeInterval, prevProps.currentTimeInterval)) {
            this.setupSubscriptions(this.props);
        }

        if (this.mainTable && this.mainTable.getSortModel().length === 0) {
            // console.dir(this.mainTable.getSortModel());
            this.mainTable.setSortModel([{ colId: 'time', sort: 'asc' }]);
        }
        if (this.completedTable && this.completedTable.getSortModel().length === 0) {
            this.completedTable.setSortModel([{ colId: 'time', sort: 'asc' }]);
        }

        if (this.props.openEventHypId && prevProps.openEventHypId !== this.props.openEventHypId) {
            let openNode;
            this.mainTable.forEachNode(node => {
                if (node.data.id === this.props.openEventHypId) {
                    openNode = node;
                }
            });
            this.handleOpenEvent(openNode, true);
        }
        // if the selected event has changed, select it in the table
        if (prevProps.selectedEventHypIds !== this.props.selectedEventHypIds) {
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
        const columnDefs: Column[] = Columns.getColumnDefs();

        // add the 'mark complete' column for the main table
        const mainTableColumnDefs: Column[] = Columns.getAdditionalColumnDefs();

        const mainTableRowData = this.props.data && this.props.data.eventHypothesesInTimeRange ?
            this.generateTableRows(this.props, false)
            : [];

        const completedRowData = this.props.data && this.props.data.eventHypothesesInTimeRange ?
            this.generateTableRows(this.props, true)
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
                    <div
                        style={{
                            flex: '0 0 auto', display: 'flex',
                            justifyContent: 'flex-end',
                            alignItems: 'center',
                        }}
                    >
                        <Button
                            text="Mark selected complete"
                            style={{ marginLeft: '1rem', marginBottom: '0px' }}
                            onClick={() => {
                                const selectedNodes = this.mainTable.getSelectedNodes();
                                if (selectedNodes.length === 0) return;
                                const eventHypIds = selectedNodes.map(node => node.data.eventId);
                                const stageId = selectedNodes[0].data.stageId;
                                this.markEventsComplete(eventHypIds, stageId);
                            }}
                        />
                    </div>
                    <div style={{ flex: '0 0 auto' }}>
                        <h6
                            style={{
                                color: analystUiConfig.userPreferences.colors.events.toWork,
                                margin: '0px 0px 0.25rem 0px'
                            }}
                        >
                            Events to work
                        </h6>
                    </div>
                    <div style={{ flex: '1 1 auto', position: 'relative', minHeight: '100px' }}>
                        <div className="max">
                            <Table
                                context={{
                                    markEventComplete: this.markEventsComplete
                                }}
                                onGridReady={this.onMainTableReady}
                                columnDefs={mainTableColumnDefs}
                                rowData={mainTableRowData}
                                onRowDoubleClicked={params => this.handleOpenEvent(params, true)}
                                onRowClicked={this.onRowClicked}
                                getRowNodeId={node => node.id}
                                deltaRowDataMode={true}
                                rowClassRules={{
                                    'event-list-disabled-row': params => params.data.disabled,
                                    'event-list-open-event': params => params.data.id === this.props.openEventHypId
                                }}
                                rowSelection="multiple"
                                overlayNoRowsTemplate="No Events Loaded"
                                rowDeselection={true}
                                enableSorting={true}
                                enableFilter={true}
                                enableColResize={true}
                            />
                        </div>
                    </div>
                    <div style={{ flex: '0 0 auto' }}>
                        <h6
                            style={{
                                color: analystUiConfig.userPreferences.colors.events.complete,
                                margin: '0.5rem 0px 0.25rem 0px'
                            }}
                        >
                            Complete Events
                        </h6>
                    </div>
                    <div style={{ flex: '1 1 auto', position: 'relative', minHeight: '50px' }}>
                        <div className="max">
                            <Table
                                onGridReady={this.onCompletedTableReady}
                                columnDefs={columnDefs}
                                rowData={completedRowData}
                                onRowDoubleClicked={params => this.handleOpenEvent(params, false)}
                                getRowNodeId={node => node.id}
                                deltaRowDataMode={true}
                                rowClassRules={{
                                    'event-list-disabled-row': params => params.data.disabled,
                                    'event-list-open-event': params => params.data.id === this.props.openEventHypId
                                }}
                                rowSelection="single"
                                overlayNoRowsTemplate="No Events Loaded"
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

    // *************************************
    // END REACT COMPONENT LIFECYCLE METHODS
    // *************************************

    /**
     * Initialize graphql subscriptions on the apollo client
     */
    private readonly setupSubscriptions = (props: EventListProps): void => {
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
    }

    /**
     * Set class members when main table is ready
     */
    private readonly onMainTableReady = (e: any) => {
        this.mainTable = e.api;
        // this.mainTableColumns = e.columnApi;
    }

    /**
     * Set class members when completed table is ready
     */
    private readonly onCompletedTableReady = (e: any) => {
        this.completedTable = e.api;
        // this.completedTableColumns = e.columnApi;
    }

    private readonly selectRowsFromProps = (props: EventListProps) => {
        if (this.mainTable) {
            this.mainTable.deselectAll();
            this.mainTable.forEachNode(node => {
                props.selectedEventHypIds.forEach(eid => {
                    if (node.data.id === eid) {
                        node.setSelected(true);
                        // must pass in null here as ag-grid expects it
                        this.mainTable.ensureNodeVisible(node, null);
                    }
                });
            });
        }
    }

    /**
     * Convert the event data into table rows
     */
    private readonly generateTableRows = (props: EventListProps, completed: boolean): Row[] => {
        // filter to only completed or not completed events
        const eventHyps = completed ?
            props.data.eventHypothesesInTimeRange.filter(eventHyp => eventHyp.event.status === 'Complete')
            : props.data.eventHypothesesInTimeRange.filter(eventHyp => eventHyp.event.status !== 'Complete');

        return eventHyps.map(eventHyp => ({
            id: eventHyp.id,
            eventId: eventHyp.event.id,
            isOpen: eventHyp.id === this.props.openEventHypId,
            stageId: eventHyp.event.preferredHypothesis.processingStage.id,
            lat: eventHyp.preferredLocationSolution.locationSolution.latDegrees,
            lon: eventHyp.preferredLocationSolution.locationSolution.lonDegrees,
            depth: eventHyp.preferredLocationSolution.locationSolution.depthKm,
            time: eventHyp.preferredLocationSolution.locationSolution.timeSec,
            activeAnalysts: eventHyp.event.activeAnalysts.map(analyst => analyst.userName),
            numDetections: eventHyp.signalDetectionAssociations.length,
            magMb: eventHyp.preferredLocationSolution.locationSolution.networkMagnitudeSolutions
                .find(magSol => magSol.magnitudeType === 'mb') ?
                eventHyp.preferredLocationSolution.locationSolution.networkMagnitudeSolutions
                    .find(magSol => magSol.magnitudeType === 'mb').magnitude
                : undefined,
            disabled: eventHyp.preferredLocationSolution.locationSolution.timeSec <
                this.props.currentTimeInterval.startTimeSecs
                || eventHyp.preferredLocationSolution.locationSolution.timeSec >
                this.props.currentTimeInterval.endTimeSecs
        }));
    }

    /**
     * Handle table row click
     */
    private readonly onRowClicked = (event: any) => {
        if (this.mainTable) {
            const selectedEventHypIds = this.mainTable.getSelectedNodes()
                .map(node => node.data.id);
            this.props.setSelectedEventHypIds(selectedEventHypIds);
        }
    }

    /**
     * handle double-click on a row
     */
    private readonly handleOpenEvent = (params: any, openForRefinement: boolean) => {
        // mark the row open for refinement, and select it in the UI
        if (openForRefinement) {
            const variables: UpdateEventsVariables = {
                eventIds: [params.data.eventId],
                input: {
                    creatorId: 'Mark',
                    processingStageId: params.data.stageId,
                    status: 'OpenForRefinement',
                    activeAnalystUserNames: [...params.data.activeAnalysts, 'Mark']
                }
            };
            this.props.updateEvents({
                variables
            })
            .then(() => {
                this.props.setOpenEventHypId(params.data.id);
            })
            .catch(e => window.alert(e));
            // otherwise simply select it in the UI
        } else {
            this.props.setOpenEventHypId(params.data.id);
        }
    }

    /**
     * execute mutation - mark event complete
     */
    private readonly markEventsComplete = (eventIds: string[], processingStageId: string) => {
        const variables: UpdateEventsVariables = {
            eventIds,
            input: {
                creatorId: 'Mark',
                processingStageId,
                status: 'Complete',
                activeAnalystUserNames: []
            }
        };
        this.props.updateEvents({
            variables
        })
        .then(() => {
            if (eventIds.find(eh => eh === this.props.openEventHypId)) this.props.setOpenEventHypId(undefined);
        })
        .catch(e => window.alert(e));
    }
}
