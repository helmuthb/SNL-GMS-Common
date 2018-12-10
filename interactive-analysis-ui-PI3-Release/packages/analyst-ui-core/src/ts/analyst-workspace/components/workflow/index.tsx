import {
    Button, Colors, ContextMenu, Intent,
    NonIdealState, Spinner
} from '@blueprintjs/core';
import * as d3 from 'd3';
import * as moment from 'moment';
import * as React from 'react';
import { ChildProps, compose, graphql, MutationFunc } from 'react-apollo';
import * as ReactRedux from 'react-redux';

import * as Gl from '@gms/golden-layout';
import { AnalystWorkspaceState } from '../../state';
import * as Actions from '../../state/actions';
import { addGlForceUpdateOnResize, addGlForceUpdateOnShow } from '../../util/gl-util';
import { AnalystLogger } from '../../util/log/analyst-logger';
import {
    ActivityIntervalBlueprintContextMenu,
    ActivityIntervalElectronContextMenu,
    StageIntervalBlueprintContextMenu,
    StageIntervalElectronContextMenu
} from './context-menus';
import { IntervalStatusInput, markActivityIntervalMutation, markStageIntervalMutation } from './graphql/mutations';
import { WorkflowData, workflowQuery } from './graphql/query';
import {
    activityIntervalMarkedSubscription,
    intervalCreatedSubscription,
    IntervalCreatedSubscription,
    stageIntervalMarkedSubscription
} from './graphql/subscriptions';
import { ActivityInterval, Interval } from './graphql/types';
import { WorkflowTimeAxis } from './time-axis';

declare var require;
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const electron = require('electron');

/**
 * Status values for a stage time interval
 */
export enum IntervalStatus {
    NotStarted = 'NotStarted',
    InProgress = 'InProgress',
    Complete = 'Complete',
    NotComplete = 'NotComplete'
}

/**
 * Mapping from IntervalStatus to e.g. a color
 */
interface IntervalStatusMapping {
    NotStarted: string;
    InProgress: string;
    Complete: string;
    NotComplete: string;
}

const IntervalStatusColors: IntervalStatusMapping = {
    NotStarted: Colors.DARK_GRAY3,
    InProgress: Colors.ORANGE1,
    Complete: Colors.GREEN2,
    NotComplete: Colors.DARK_GRAY1
};

const IntervalStatusBorderColors: IntervalStatusMapping = {
    NotStarted: Colors.DARK_GRAY4,
    InProgress: Colors.ORANGE2,
    Complete: Colors.GREEN3,
    NotComplete: Colors.DARK_GRAY2
};

/**
 * Mutations used by the workflow display
 */
export interface WorkflowMutations {
    // {} because we don't care about mutation results for now, handling that through subscriptions
    markActivityInterval: MutationFunc<{}>;
    markStageInterval: MutationFunc<{}>;
}

/**
 * Props mapped in from Redux
 */
export interface WorkflowReduxProps {
    // passed in from golden-layout
    glContainer?: Gl.Container;
    // redux state mapped to props
    currentProcessingStageIntervalId: string;

    // redux callbacks
    setCurrentTimeInterval(startTimeSecs: number, endTimeSecs: number): void;
    setCurrentProcessingStageIntervalId(id: string);
}

/**
 * State for the workflow display
 */
export interface WorkflowState {
    // in seconds
    startTimeSecs: number;
    // in seconds
    endTimeSecs: number;
    // in seconds
    intervalDurationSecs: number;
    expansionStates: {
        stageName: string;
        expanded: boolean;
    }[];
}

export type WorkflowProps = WorkflowReduxProps & ChildProps<WorkflowMutations, WorkflowData>;

const SECONDS_PER_HOUR = 3600;
const FRACTION_TO_PCT = 100;

/**
 * Note that the main section of this display is currently comprised of
 * 3 vertical divs, and the scroll position of each div is kept in sync via onScroll and onWheel events.
 * This is in order to have the horizontal scrollbar live under all 3 of the divs. TODO If we can take more liberty
 * with the display, then we can get rid of most of the nasty manual scroll-synchronization.
 */
export class Workflow extends React.Component<WorkflowProps, WorkflowState> {

    /**
     * The height of a workflow block in pixels
     */
    public static BLOCK_HEIGHT_PX: number = 24;

    /**
     * Number of pixels horizontally per hour
     */
    public static PIXELS_PER_HOUR: number = 50;

    /**
     * the element containing expansion buttons. Necessary due to scroll synchronization
     */
    private expansionButtonContainer: HTMLDivElement;
    /**
     * The element containing the main workflow time-blocks. Necessary due to scroll synchronization
     */
    private timeblockContainer: HTMLDivElement;
    /**
     * The element containing the processing stage & activity names. Necessary due to scroll synchronization
     */
    private processingStageNameContainer: HTMLDivElement;
    /**
     * A reference to the WorkflowTimeAxis. Necessary due to scroll synchronization
     */
    private timeAxis: WorkflowTimeAxis;

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
    public constructor(props: WorkflowProps) {
        super(props);
        this.state = {
            expansionStates: [],
            startTimeSecs: 0,
            endTimeSecs: 1,
            intervalDurationSecs: 7200,
        };
    }

    /**
     * Updates the derived state from the next props.
     * 
     * @param nextProps The next (new) props
     * @param prevState The previous state
     */
    public static getDerivedStateFromProps(nextProps: WorkflowProps, prevState: WorkflowState) {
        if (nextProps && nextProps.data && nextProps.data.stages) {
            let startTimeSecs = Infinity;
            let endTimeSecs = -Infinity;
            nextProps.data.stages.forEach(stage => {
                stage.intervals.forEach(interval => {
                    if (interval.startTime < startTimeSecs) startTimeSecs = interval.startTime;
                    if (interval.endTime > endTimeSecs) endTimeSecs = interval.endTime;
                });
            });
            const expansionStates = nextProps.data.stages.map(stage => ({
                stageName: stage.name,
                expanded: true,
            }));
            return {
                expansionStates,
                startTimeSecs,
                endTimeSecs
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

        this.setupSubscriptions(this.props);
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
     * React component lifecycle
     * On mount, scroll all the way to the right
     */
    public componentDidUpdate(prevProps: ChildProps<{}, WorkflowData>) {

        AnalystLogger.info('workflow componentDidUpdate');

        // the first time we get data, set the scroll position to the right
        if (!prevProps.data.stages && this.props.data.stages && this.timeblockContainer) {
            this.timeblockContainer.scrollLeft = this.timeblockContainer.scrollWidth
                - this.timeblockContainer.clientWidth;
        } else {
            // otherwise, only scroll to the right if the user is already scrolled all the way right.
            if (this.timeblockContainer && this.timeblockContainer.scrollLeft === this.timeblockContainer.scrollWidth
                - this.timeblockContainer.clientWidth - Workflow.PIXELS_PER_HOUR * 2) {
                this.timeblockContainer.scrollLeft = this.timeblockContainer.scrollWidth
                    - this.timeblockContainer.clientWidth;
            }
        }
    }

    /**
     * Renders the component.
     */
    public render() {
        // if the golden-layout container is not visible, do not attempt to render
        // the compoent, this is to prevent JS errors that may occur when trying to
        // render the component while the golden-layout container is hidden
        if (this.props.glContainer) {
            if (this.props.glContainer.isHidden) {
                return (<NonIdealState />);
            }
        }

        if (this.props.data.loading) {
            return (
                <div
                    style={{
                        height: '100%', width: '100%', display: 'flex',
                        justifyContent: 'center', alignItems: 'center'
                    }}
                >
                    <Spinner intent={Intent.PRIMARY} />
                </div>
            );
        } else if (this.props.data.error) {
            return (
                <div
                    style={{
                        height: '100%', width: '100%', display: 'flex',
                        justifyContent: 'center', alignItems: 'center'
                    }}
                    className="pt-intent-danger"
                >
                    <NonIdealState
                        visual="error"
                        action={<Spinner intent={Intent.DANGER} />}
                        className="pt-intent-danger"
                        title="Something went wrong!"
                        description={this.props.data.error.message}
                    />
                </div>
            );
        } else {
            return (
                <div
                    style={{ padding: '0.25rem', height: '100%', width: '100%', userSelect: 'none' }}
                >
                    <div
                        className="gms-workflow-wrapper"
                        style={{
                            maxHeight: '100%', display: 'flex',
                            flexDirection: 'column'
                        }}
                    >
                        {this.generateTopButtonBar()}
                        <div
                            style={{ flex: '1 1 auto', display: 'flex', paddingLeft: '10px' }}
                        >
                            {this.generateExpansionButtonColumn()}
                            {this.generateWorkflowContent()}
                            {this.generateStageNameColumn()}
                        </div>
                        <WorkflowTimeAxis
                            ref={ref => { this.timeAxis = ref; }}
                            pixelsPerHour={Workflow.PIXELS_PER_HOUR}
                            startTimeSecs={this.state.startTimeSecs}
                            endTimeSecs={this.state.endTimeSecs}
                            intervalDurationSecs={this.state.intervalDurationSecs}
                        />
                    </div>
                </div>
            );
        }
    }

    // ***************************************
    // END REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    /**
     * Initialize graphql subscriptions on the apollo client
     */
    private readonly setupSubscriptions = (props: WorkflowProps): void => {
        if (!props.data) return;

        // first, unsubscribe from all current subscriptions
        this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
        this.unsubscribeHandlers.length = 0;

        // subscribe to interval-created topic
        this.unsubscribeHandlers.push(
            this.props.data.subscribeToMore({
                document: intervalCreatedSubscription,
                updateQuery: (prev: WorkflowData, cur) => {
                    const data = cur.subscriptionData.data as IntervalCreatedSubscription;
                    this.setState({
                        endTimeSecs: data.intervalCreated.endTime
                    });

                    const stages = prev.stages.map(stage => {
                        const newInterval = data.intervalCreated.stageIntervals
                            .filter(interval => interval.stage.id === stage.id)[0];
                        return {
                            ...stage,
                            intervals: [...stage.intervals, newInterval]
                        };
                    });

                    return {
                        ...prev,
                        stages: [...stages]
                    };
                }
            })
        );
        // subscribe to activity-interval-marked topic
        this.unsubscribeHandlers.push(
            this.props.data.subscribeToMore({
                document: activityIntervalMarkedSubscription,
                // update query may not be necessary here? apollo seems to be updating the cache automatically based on
                // the results.
                // updateQuery: (prev: WorkflowData, cur) => {
                //     const data = cur.subscriptionData.data as ActivityIntervalMarkedSubscription;
                //     return prev;
                // }
            })
        );
        this.unsubscribeHandlers.push(
            this.props.data.subscribeToMore({
                document: stageIntervalMarkedSubscription,
            })
        );
    }

    /**
     * Create the button bar at the top of the workflow display
     */
    private readonly generateTopButtonBar = (): JSX.Element => {
        const isAnyExpanded = this.state.expansionStates.reduce(
            (prev, cur) => cur.expanded ? cur.expanded : prev, false);
        return (
            <div
                style={{
                    flex: '0 0 auto', display: 'flex', justifyContent: 'flex-start', marginBottom: '0.5rem'
                }}
            >
                <Button
                    text={isAnyExpanded ? 'Collapse all' : 'Expand all'}
                    rightIcon={isAnyExpanded ? 'collapse-all' : 'expand-all'}
                    onClick={() => {
                        this.setState({
                            expansionStates: this.state.expansionStates.map(activity => ({
                                stageName: activity.stageName,
                                expanded: !isAnyExpanded
                            }))
                        });
                    }}
                />
            </div>
        );
    }

    /**
     * Create the column with the buttons to expans processing stage rows
     */
    private readonly generateExpansionButtonColumn = (): JSX.Element => (
        <div
            onWheel={e => {
                this.processingStageNameContainer.scrollTop += e.deltaY;
            }}
            ref={ref => { this.expansionButtonContainer = ref; }}
            style={{
                width: '25px', flex: '0 0 auto', overflow: 'hidden',
                marginBottom: '10px',
            }}
        >
            {
                /* map processing stages into buttons to expand that stage*/
                this.props.data.stages.map(stage => {
                    const isExpanded = this.state.expansionStates
                        .filter(expansion => expansion.stageName === stage.name)[0].expanded;
                    const marginBottom = isExpanded ?
                        Workflow.BLOCK_HEIGHT_PX * stage.activities.length
                        : 0;
                    const iconName = isExpanded ? 'small-minus' : 'small-plus';
                    return (
                        <Button
                            key={stage.name}
                            style={{
                                height: `${Workflow.BLOCK_HEIGHT_PX}px`,
                                border: '1px solid black', width: `${Workflow.BLOCK_HEIGHT_PX}px`,
                                marginBottom, transition: 'margin-bottom 0.15s',
                            }}
                            icon={iconName}
                            onClick={() => {
                                this.setState({
                                    expansionStates: this.state.expansionStates.map(expansion => ({
                                        stageName: expansion.stageName,
                                        expanded: expansion.stageName === stage.name ?
                                            !expansion.expanded : expansion.expanded
                                    }))
                                });
                            }}
                            disabled={stage.activities.length === 0}
                        />
                    );
                })
            }
        </div>
    )

    /**
     * Generate the main content of the workflow display
     */
    private readonly generateWorkflowContent = (): JSX.Element => {
        const totalDurationSecs = moment.unix(this.state.endTimeSecs)
            .diff(moment.unix(this.state.startTimeSecs), 's');
        const rowWidthPx = Math.ceil(totalDurationSecs / SECONDS_PER_HOUR * Workflow.PIXELS_PER_HOUR);
        return (
            <div
                ref={ref => { this.timeblockContainer = ref; }}
                onWheel={e => {
                    this.processingStageNameContainer.scrollTop += e.deltaY;
                }}
                onScroll={e => { this.timeAxis.setScrollLeft(e.currentTarget.scrollLeft); }}
                style={{
                    flex: '0 1 auto',
                    overflowX: 'scroll', overflowY: 'hidden',
                    position: 'relative', width: `${rowWidthPx}px`
                }}
            >
                {this.generateDayBoundaryIndicators(rowWidthPx)}
                {this.generateProcessingStageRows(totalDurationSecs, rowWidthPx)}
            </div>
        );
    }

    /**
     * Generate the day boundary indicator lines
     */
    private readonly generateDayBoundaryIndicators = (rowWidthPx: number): JSX.Element[] => (
        d3.utcDay.every(1)
            .range(
                moment.unix(this.state.startTimeSecs)
                    // tslint:disable-next-line:no-magic-numbers
                    .subtract(10, 's')
                    .toDate(),
                moment.unix(this.state.endTimeSecs)
                    .toDate())
            .map(date => {
                const widthPx = 4;
                const left = `calc(${(date.valueOf() / 1000 - this.state.startTimeSecs)
                    / (this.state.endTimeSecs - this.state.startTimeSecs) * rowWidthPx}px - ${widthPx / 2}px)`;
                return (
                    <div
                        key={date.valueOf()}
                        style={{
                            position: 'absolute',
                            top: '0px', bottom: '0px',
                            left, width: `${widthPx}px`,
                            backgroundColor: Colors.GOLD4,
                            zIndex: 1
                        }}
                    />
                );
            })
    )

    /**
     * Generate rows for each processing stage
     */
    private readonly generateProcessingStageRows = (totalDuration: number, rowWidthPx: number): JSX.Element[] => (
        /* Map processing stages into a row for that stage*/
        this.props.data.stages.map(stage => {
            const expanded = this.state.expansionStates
                .filter(expansion => expansion.stageName === stage.name)[0]
                .expanded;
            const height = expanded ?
                `${stage.activities.length * Workflow.BLOCK_HEIGHT_PX + Workflow.BLOCK_HEIGHT_PX}px`
                : `${Workflow.BLOCK_HEIGHT_PX}px`;
            return (
                <div
                    key={stage.name}
                    style={{
                        width: `${rowWidthPx}px`, position: 'relative', height, transition: 'height 0.15s'
                    }}
                >
                    {
                        /* Then for each time block, create an element for the status */
                        stage.intervals.map(interval => {
                            const selected = this.props.currentProcessingStageIntervalId === interval.id;
                            return (
                                <div
                                    key={interval.startTime}
                                    style={{
                                        display: 'flex', flexDirection: 'column',
                                        height: '100%', overflow: 'hidden', position: 'absolute',
                                        top: '0px',
                                        width: `${Math.ceil((interval.endTime - interval.startTime)
                                            / SECONDS_PER_HOUR * Workflow.PIXELS_PER_HOUR)}px`,
                                        left: `${moment.unix(interval.startTime)
                                            .diff(moment.unix(this.state.startTimeSecs), 's') /
                                            totalDuration * FRACTION_TO_PCT}%`,
                                    }}
                                >
                                    <div
                                        onContextMenu={e => this.showStageIntervalContextMenu(e, interval)}
                                        style={{
                                            width: '100%',
                                            height: `${Workflow.BLOCK_HEIGHT_PX}px`,
                                            border: `1px solid ${Colors.DARK_GRAY1}`,
                                        }}
                                    >
                                        <div
                                            className="gms-workflow-cell"
                                            style={{
                                                height: '100%',
                                                width: '100%',
                                                backgroundColor: IntervalStatusColors[interval.status],
                                                border: selected ?
                                                    `2px solid #44EE55` :
                                                    `1px solid ${IntervalStatusBorderColors[interval.status]}`
                                            }}
                                        >
                                            {
                                                interval.status !== IntervalStatus.NotStarted ?
                                                    interval.eventCount
                                                    : undefined
                                            }
                                        </div>
                                    </div>
                                    {
                                        /* & activities, if this stage is expanded */
                                        expanded ?
                                            interval.activityIntervals.map(activityInterval =>
                                                <div
                                                    key={`${interval.startTime}
                                                                          -${activityInterval.activity.name}`}
                                                    onContextMenu={e =>
                                                        this.showActivityIntervalContextMenu(
                                                            e, interval, activityInterval)}
                                                    onDoubleClick={e =>
                                                        this.showActivityIntervalContextMenu(
                                                            e, interval, activityInterval)}
                                                    style={{
                                                        width: '100%',
                                                        height: `${Workflow.BLOCK_HEIGHT_PX}px`,
                                                        border: `1px solid ${Colors.DARK_GRAY1}`
                                                    }}
                                                >
                                                    <div
                                                        className="gms-workflow-cell sub"
                                                        title={`${activityInterval.status === IntervalStatus.Complete ?
                                                            activityInterval.completedBy.userName
                                                            : activityInterval
                                                                .activeAnalysts
                                                                .map(a => a.userName)
                                                                .join(', ')}`}
                                                        style={{
                                                            backgroundColor:
                                                                IntervalStatusColors[activityInterval.status],
                                                            // tslint:disable-next-line:max-line-length
                                                            border: `1px solid ${IntervalStatusBorderColors[activityInterval.status]}`,
                                                        }}
                                                    >
                                                        {
                                                            activityInterval.status === IntervalStatus.Complete ?
                                                                activityInterval.completedBy.userName
                                                                : activityInterval
                                                                    .activeAnalysts
                                                                    .map(a => a.userName)
                                                                    .join(', ')
                                                        }
                                                    </div>
                                                </div>
                                            )
                                            : undefined
                                    }
                                </div>
                            );
                        }
                        )
                    }
                </div>
            );
        })
    )

    /**
     * Generate the stage names column for the workflow display
     */
    private readonly generateStageNameColumn = (): JSX.Element => (
        <div
            ref={ref => { this.processingStageNameContainer = ref; }}
            style={{
                flex: '0 0 auto', width: '125px', overflowY: 'auto',
                overflowX: 'hidden',
                marginBottom: '10px'
            }}
            onScroll={e => {
                this.timeblockContainer.scrollTop = e.currentTarget.scrollTop;
                this.expansionButtonContainer.scrollTop = e.currentTarget.scrollTop;
            }}
        >
            {
                /* Map processing stages into a block displaying the stage name */
                this.props.data.stages.map(stage => {
                    const isExpanded = this.state.expansionStates
                        .filter(expansion => expansion.stageName === stage.name)[0].expanded;
                    const height = isExpanded ?
                        `${stage.activities.length * Workflow.BLOCK_HEIGHT_PX + Workflow.BLOCK_HEIGHT_PX}px`
                        : `${Workflow.BLOCK_HEIGHT_PX}px`;
                    return (
                        <div
                            key={stage.name}
                            style={{
                                display: 'flex', flexDirection: 'column',
                                height, overflowY: 'hidden', transition: 'height 0.15s'
                            }}
                        >
                            <div
                                style={{
                                    height: `${Workflow.BLOCK_HEIGHT_PX}px`, width: '100%',
                                    border: '1px solid', display: 'flex',
                                    borderColor: Colors.DARK_GRAY1,
                                    justifyContent: 'center',
                                    alignItems: 'center',
                                    background: Colors.GRAY1,
                                    flex: '0 0 auto'
                                }}
                            >
                                {stage.name}
                            </div>
                            {
                                /* & activities, if this stage is expanded */
                                isExpanded ?
                                    stage.activities.map(activityName => (
                                        <div
                                            key={activityName.name}
                                            style={{
                                                flex: '0 0 auto',
                                                height: `${Workflow.BLOCK_HEIGHT_PX}px`, width: '100%',
                                                border: '1px solid', display: 'flex',
                                                borderColor: Colors.DARK_GRAY1,
                                                justifyContent: 'center', alignItems: 'center',
                                                background: Colors.GRAY1,
                                                filter: 'brightness(0.8)'
                                            }}
                                        >
                                            {activityName.name}
                                        </div>
                                    ))
                                    : undefined
                            }
                        </div>
                    );
                })
            }
        </div>
    )

    /**
     * Show a context menu for a stage interval block
     */
    private readonly showStageIntervalContextMenu = (e: React.MouseEvent<HTMLDivElement>, interval: Interval) => {
        e.preventDefault();
        const markInterval = (status: IntervalStatus) => {
            const stageIntervalId = interval.id;
            const analystUserName = 'Mark';
            this.markStageInterval(
                stageIntervalId,
                analystUserName,
                status,
                interval.startTime,
                interval.endTime
            )
                .catch(err => window.alert(err));
        };
        // if running in electron, show an electron native context menu
        if (electron) {
            const menu = StageIntervalElectronContextMenu(markInterval);
            menu.popup(electron.remote.getCurrentWindow(), {
                async: true
            });
        } else {
            // otherwise, use a blueprint one.
            const stageIntervalContextMenu = StageIntervalBlueprintContextMenu(markInterval);
            ContextMenu.show(
                stageIntervalContextMenu, {
                    left: e.clientX,
                    top: e.clientY
                });
        }

    }

    /**
     * Show a context menu for an activity interval block.
     */
    private readonly showActivityIntervalContextMenu = (e: React.MouseEvent<HTMLDivElement>,
        interval: Interval, activityInterval: ActivityInterval) => {
        e.preventDefault();
        const markInterval = (status: IntervalStatus) => {
            const analystUserName = 'Mark';
            this.markActivityInterval(
                interval.id,
                activityInterval.id,
                analystUserName,
                status,
                interval.startTime,
                interval.endTime
            )
                .catch(err => window.alert(err));
        };

        // if running in electron, show an electron native context menu
        if (electron) {
            const menu = ActivityIntervalElectronContextMenu(markInterval);
            menu.popup(electron.remote.getCurrentWindow(), {
                async: true
            });
        } else {
            // otherwise, use a blueprint one.
            const stageIntervalContextMenu = ActivityIntervalBlueprintContextMenu(markInterval);
            ContextMenu.show(
                stageIntervalContextMenu, {
                    left: e.clientX,
                    top: e.clientY
                });
        }
    }

    /**
     * Triggers a mutation to mark an activity interval status
     */
    private async markActivityInterval(stageIntervalId: string, activityIntervalId: string,
        analystUserName: string, status: IntervalStatus,
        startTimeSecs: number, endTimeSecs: number) {
        const input: IntervalStatusInput = {
            analystUserName,
            status
        };

        await this.props.markActivityInterval({
            variables: {
                activityIntervalId,
                input
            }
        })
            .catch(e => {
                alert(e.message);
            });

        // if marking in progress, set this as the current operational interval
        if (status === IntervalStatus.InProgress) {
            this.openActivityInterval(stageIntervalId, startTimeSecs, endTimeSecs);
        }
    }

    /**
     * Triggers a mutation to mark a stage interval status
     */
    private async markStageInterval(stageIntervalId: string, analystUserName: string, status: IntervalStatus,
        startTimeSecs: number, endTimeSecs: number) {
        const input: IntervalStatusInput = {
            analystUserName,
            status
        };
        await this.props.markStageInterval({
            variables: {
                stageIntervalId,
                input
            }
        })
            .catch(e => {
                alert(e.message);
            });

        // if marking in progress, set this as the current operational interval
        if (status === IntervalStatus.InProgress) {
            this.openActivityInterval(stageIntervalId, startTimeSecs, endTimeSecs);
        }
    }

    /**
     * Open an activity Interval, setting the start/end time and other info in redux state.
     * @param processingStageIntervalId // Processing Stage Interval ID
     * @param startTimeSecs // Start Time Seconds
     * @param endTimeSecs // End Time Seconds
     */
    private openActivityInterval(processingStageIntervalId: string, startTimeSecs: number, endTimeSecs: number) {
        this.props.setCurrentTimeInterval(startTimeSecs, endTimeSecs);
        this.props.setCurrentProcessingStageIntervalId(processingStageIntervalId);
    }
}

const mapStateToProps = (state: AnalystWorkspaceState): Partial<WorkflowReduxProps> => ({
    currentProcessingStageIntervalId: state.app.currentProcessingStageIntervalId
});

const mapDispatchToProps = (dispatch): Partial<WorkflowReduxProps> => ({
    setCurrentTimeInterval: (startTimeSecs: number, endTimeSecs: number) => {
        dispatch(Actions.setCurrentTimeInterval(startTimeSecs, endTimeSecs));
    },
    setCurrentProcessingStageIntervalId: (id: string) => {
        dispatch(Actions.setCurrentProcessingStageIntervalId(id));
    }
});

/**
 * s 
 */
export const ReduxApolloWorkflow = compose(
    ReactRedux.connect(mapStateToProps, mapDispatchToProps),
    graphql(workflowQuery),
    graphql(markActivityIntervalMutation, { name: 'markActivityInterval' }),
    graphql(markStageIntervalMutation, { name: 'markStageInterval' })
)(Workflow);
