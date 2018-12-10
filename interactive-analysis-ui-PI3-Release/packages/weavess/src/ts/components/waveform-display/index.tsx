import * as d3 from 'd3';
import * as elementResizeEvent from 'element-resize-event';
import * as lodash from 'lodash';
import * as moment from 'moment';
import 'moment-precise-range-plugin';
import * as React from 'react';
import * as THREE from 'three';
import { RpcProvider } from 'worker-rpc';

import * as Entities from '../../entities';
import { isHotKeyCommandSatisfied } from '../../util/HotKeyUtils';
import { TimeAxis } from './axes';
import { MarkerObjects, MoveableMarker, SelectionWindow, VerticalMarker } from './marker';
import { MeasureWindow } from './measure-window';
import { Station } from './station';
import { StationLabel } from './station/label';
declare var require;
const WeavessWorker = require('worker-loader?inline&fallback=false!../../workers'); // tslint:disable-line

/**
 * Props for the Waveform Display component
 */
export interface WaveformDisplayProps {
    startTimeSecs: number;
    endTimeSecs: number;
    stations: Entities.StationConfig[];

    events?: Entities.WeavessEvents;
    selections?: Entities.WeavessSelections;
    hotKeyOverrides?: Entities.HotKeyOverridesConfig;
    defaultZoomWindow?: {
        startTimeSecs: number;
        endTimeSecs: number;
    };
    markers?: MarkerObjects;
    flex?: boolean;
}

/**
 * State for the Waveform Display component
 */
export interface WaveformDisplayState {
    expansionStates: {
        stationId: string;
        expanded: boolean;
    }[];
    yAxisBounds: {
        channelId: string;
        bounds: [number, number];
    }[];
    showMeasureWindow: boolean;
}

// create web workers responsible for creating line geometries
const defaultNumWorkers = 4;
const workerRpcs = lodash.range(window.navigator.hardwareConcurrency || defaultNumWorkers)
    .map(_ => {
        const worker = new WeavessWorker();
        const workerRpc = new RpcProvider(
            (message, transfer) => worker.postMessage(message, transfer),
        );
        worker.onmessage = e => workerRpc.dispatch(e.data);
        return workerRpc;
    });

/**
 * Primary Waveform Display component
 */
export class WaveformDisplay extends React.Component<WaveformDisplayProps, WaveformDisplayState> {

    /**
     * The default hotkey overrides.
     */
    public static defaultHotKeyOverridesConfig: Entities.HotKeyOverridesDefaultConfig = {
        amplitudeScale: 'a',
        amplitudeScaleSingleReset: 'Control+a',
        amplitudeScaleReset: 'Control+Shift+A',
    };

    /**
     * Refs to each station label component.
     */
    private stationLabelComponentRefs: StationLabel[] | null;

    /**
     * Refs to each station component
     */
    private stationComponentRefs: Station[] | null;

    /**
     * Ref to the measure window
     */
    private measureWindowRef: MeasureWindow | null;

    /**
     * Ref to the root element of weavess
     */
    private weavessRootRef: HTMLDivElement | null;

    /**
     * Ref to the station labels column, so scroll can be kept in sync with the waveforms viewport
     */
    private stationLabelsColumnRef: HTMLDivElement | null;

    /**
     * Ref to the viewport where waveforms are rendered
     */
    private waveformsViewportRef: HTMLDivElement | null;

    /**
     * Ref to the container where waveforms are held, directly within the viewport
     */
    private waveformsContainerRef: HTMLDivElement | null;

    /**
     * Ref to the element where we display the current time range. Updated manually for performance reasons
     */
    private timeRangeRef: HTMLSpanElement | null;

    /**
     * Ref to the translucent selection brush-effect region, which is updated manually for performance reasons 
     */
    private selectionAreaRef: HTMLDivElement | null;

    /**
     * Ref to the TimeAxis component, which is updated manually for performance reasons
     */
    private timeAxisRef: TimeAxis | null;

    /**
     * Ref to the vertical crosshair indicator element
     */
    private crosshairRef: HTMLDivElement | null;

    /**
     * Ref to the primary canvas element where the waveforms are drawn
     */
    private canvasRef: HTMLCanvasElement | null;

    /**
     * THREE.js WebGLRenderer used to draw waveforms
     */
    private renderer: THREE.WebGLRenderer;

    /**
     * a list of active web workers
     */
    private readonly workerRpcs: any[];

    /**
     * if the user is currently zooming using the brush effect
     */
    private isZooming: boolean = false;

    /**
     * the start of the zoom using brush effect in [0,1]
     * where 0 = this.viewRange.left and 1 = this.viewRange.right
     */
    private zoomStart: number | undefined;

    /**
     * a tuple with each element in [0,1] of form [start, end]
     * 0 = this.props.startTimeSecs
     * 1 = this.props.endTimeSecs
     */
    private viewRange: [number, number] = [0, 1];

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
            expansionStates: [],
            yAxisBounds: [],
            showMeasureWindow: false
        };
        this.workerRpcs = workerRpcs;
    }

    /**
     * React component lifecycle
     */
    public componentDidMount() {
        if (!this.canvasRef) {
            console.error('weavess error - canvas not present at mount time'); // tslint:disable-line
            return;
        }

        this.renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true, canvas: this.canvasRef });
        elementResizeEvent(this.waveformsViewportRef, () => {
            this.renderWaveforms();
            if (this.timeAxisRef) this.timeAxisRef.update();
        });
        this.renderWaveforms();
        this.displayCurrentTimeRange();
    }

    /**
     * React component lifecycle
     */
    public componentDidUpdate(prevProps: WaveformDisplayProps) {
        if (!lodash.isEqual(this.props.stations, prevProps.stations)) {
            this.clearWaveforms();
        }

        this.renderWaveforms();
        this.displayCurrentTimeRange();
    }

    /**
     * React component lifecycle
     */
    public render() {
        this.stationLabelComponentRefs = [];
        this.stationComponentRefs = [];
        const waveformComponents = this.createStationsJsx();
        const weavessRootStyle = this.createRootStyle();

        const defaultZoomWindow = this.props.defaultZoomWindow ?
            this.props.defaultZoomWindow
            : {
                startTimeSecs: this.props.startTimeSecs,
                endTimeSecs: this.props.endTimeSecs
            };

        return (
            <div
                className="weavess-root"
                ref={ref => this.weavessRootRef = ref}
                style={weavessRootStyle}
                onDoubleClick={e => {
                    this.zoomToTimeWindow(defaultZoomWindow.startTimeSecs, defaultZoomWindow.endTimeSecs);
                }}
            >
                <canvas
                    ref={canvas => { this.canvasRef = canvas; }}
                    style={{
                        height: '100%',
                        position: 'absolute',
                        width: '100%',
                        zIndex: 0
                    }}
                />
                <div
                    style={{
                        MozUserSelect: 'none',
                        WebkitUserSelect: 'none',
                        bottom: '0px',
                        display: 'flex',
                        flexDirection: 'column',
                        left: '0px',
                        outline: '1px solid',
                        position: 'absolute',
                        right: '0px',
                        top: '0px',
                        zIndex: 1
                    }}
                >
                    {
                        this.state.showMeasureWindow ?
                            (
                                <MeasureWindow
                                    ref={ref => this.measureWindowRef = ref}
                                    // tslint:disable-next-line:no-magic-numbers
                                    heightPx={200}
                                    workerRpcs={this.workerRpcs}
                                    selectedSignalDetections={
                                        this.props.selections ? this.props.selections.signalDetections : undefined
                                    }
                                    waveformDisplayRef={this.waveformsContainerRef}
                                    // hotkeys
                                    amplitudeScaleHotKey={this.amplitudeScaleHotKey()}
                                    amplitudeScaleSingleResetHotKey={this.amplitudeScaleSingleResetHotKey()}
                                    // callback  props
                                    toggleExpansion={this.toggleExpansion}
                                    onLoad={this.renderWaveforms}
                                    // tslint:disable:no-unbound-method
                                    // these methods shouldn't be bound as they are events passed in by a 3rd party user
                                    // and have already been bound to the appropriate context.
                                    onMaskClick={this.props.events ? this.props.events.onMaskClick : undefined}
                                    onContextMenu={this.props.events ? this.props.events.onContextMenu : undefined}
                                    onChannelLabelClick={this.props.events ?
                                        this.props.events.onChannelLabelClick : undefined}
                                    onSignalDetectionClick={this.props.events ?
                                        this.props.events.onSignalDetectionClick : undefined}
                                    onSignalDetectionDragEnd={
                                        this.props.events ? this.props.events.onSignalDetectionDragEnd : undefined
                                    }
                                    onKeyPress={this.props.events ? this.props.events.onKeyPress : undefined}
                                    // tslint:enable:no-unbound-method
                                    onMouseMove={this.onMouseMove}
                                    onMouseDown={this.onMouseDown}
                                    onMouseUp={this.onMouseUp}
                                />
                            )
                            :
                            null
                    }
                    <div
                        style={{ flex: '1 1 auto', position: 'relative' }}
                    >
                        <div
                            style={{
                                position: 'absolute',
                                top: '0px',
                                right: '0px',
                                bottom: '0px',
                                left: '0px',
                                display: 'flex'
                            }}
                        >
                            <div
                                style={{
                                    display: 'flex',
                                    flexDirection: 'column',
                                    flex: '0 0 auto',
                                    height: '100%',
                                    overflow: 'hidden'
                                }}
                                ref={ref => this.stationLabelsColumnRef = ref}
                            >
                                {this.createStationLabelsJsx()}
                                <div
                                    style={{ height: '10px', width: '100%', flex: '0 0 auto' }}
                                />
                            </div>
                            <div
                                style={{
                                    flex: '1 0 auto',
                                    position: 'relative',
                                }}
                            >
                                <div
                                    style={{
                                        position: 'absolute',
                                        top: '0px',
                                        right: '0px',
                                        bottom: '0px',
                                        left: '0px',
                                        overflow: 'scroll',
                                    }}
                                    ref={ref => { this.waveformsViewportRef = ref; }}
                                    onWheel={this.onMouseWheel}
                                    onScroll={e => {
                                        if (!this.waveformsContainerRef
                                            || !this.waveformsViewportRef
                                            || !this.timeAxisRef
                                            || !this.stationLabelsColumnRef) return;

                                        const viewport = this.waveformsViewportRef;
                                        const waveformBounds = this.waveformsContainerRef.getBoundingClientRect();
                                        const left = this.waveformsViewportRef.scrollLeft / waveformBounds.width;
                                        const right = (this.waveformsViewportRef.scrollLeft + viewport.clientWidth)
                                            / waveformBounds.width;
                                        this.viewRange = [left, right];

                                        this.timeAxisRef.update();
                                        this.displayCurrentTimeRange();
                                        this.renderWaveforms();
                                        this.stationLabelsColumnRef.scrollTop = this.waveformsViewportRef.scrollTop;
                                    }}
                                    onKeyDown={this.onKeyDown}
                                >
                                    <div
                                        ref={waveformsContainer => this.waveformsContainerRef = waveformsContainer}
                                        style={{
                                            display: 'flex',
                                            flexDirection: 'column',
                                            position: 'relative'
                                        }}
                                    >
                                        {waveformComponents}
                                        {this.createAllMarkers()}
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div
                            style={{
                                position: 'absolute',
                                top: '0px',
                                left: '184px',
                                right: '10px',
                                bottom: '0px',
                                pointerEvents: 'none'
                            }}
                        >
                            <div
                                ref={ref => { this.crosshairRef = ref; }}
                                style={{
                                    position: 'absolute', width: '0px', borderLeft: '1px solid rgba(255,255,255,0.4)',
                                    left: '0%', top: '0px', bottom: '0px'
                                }}
                            />
                            <div
                                ref={ref => { this.selectionAreaRef = ref; }}
                                style={{
                                    position: 'absolute', top: '0px', bottom: '0px', display: 'none',
                                    backgroundColor: 'rgba(150,150,150,0.3)', left: '0px', right: '0px'
                                }}
                            />
                        </div>
                    </div>
                    {
                        this.props.stations.length > 0 ?
                            <TimeAxis
                                ref={ref => this.timeAxisRef = ref}
                                startTimeSecs={this.props.startTimeSecs}
                                endTimeSecs={this.props.endTimeSecs}
                                borderTop={true}
                                getViewRange={this.getViewRange}
                            />
                            : []
                    }
                </div>
                <div
                    style={{
                        position: 'absolute',
                        bottom: '0px',
                        left: '184px',
                        pointerEvents: 'none',
                        fontSize: '0.8rem',
                        zIndex: 1
                    }}
                >
                    <span
                        ref={ref => this.timeRangeRef = ref}
                    />
                </div>
            </div>
        );
    }

    // ************************************
    // END REACT COMPONENT LIFECYCLE METHODS
    // ************************************

    /**
     * Exposed primarily for non-react users.
     */
    public refresh = (): void => {
        this.forceUpdate();
    }

    /**
     * force a redraw of waveforms & time axis
     */
    public refreshWaveforms = (): void => {
        this.renderWaveforms();
        if (this.timeAxisRef) this.timeAxisRef.update();
    }

    /**
     * zoom to [startTimeSecs, endTimeSecs]
     */
    public zoomToTimeWindow = (startTimeSecs: number, endTimeSecs: number) => {
        const scale = d3.scaleLinear()
            .domain([this.props.startTimeSecs, this.props.endTimeSecs])
            .range([0, 1])
            .clamp(true);
        this.zoom(scale(startTimeSecs), scale(endTimeSecs));
    }

    /**
     * Toggle waveform state variable
     */
    public toggleMeasureWindowVisability = () => {
        if (this.measureWindowRef &&
            this.measureWindowRef.state.measureWindowSelection &&
            this.measureWindowRef.state.measureWindowSelection.channelRef) {
            this.measureWindowRef.state.measureWindowSelection.channelRef.removeMeasureWindowSelection();
        }
        this.setState({
            showMeasureWindow: !this.state.showMeasureWindow
        });
    }

    /**
     * return the current view range in [0,1]
     * where 0 = this.props.startTimeSecs
     * and 1 = this.props.endTimeSecs
     */
    private readonly getViewRange = () => this.viewRange;

    /**
     * Zoom in on mouse wheel
     */
    private readonly onMouseWheel = (e: React.WheelEvent<HTMLDivElement>) => {

        if (!this.waveformsViewportRef) return;

        const modPercent = 0.4;

        if (e.ctrlKey && !e.shiftKey) {
            e.preventDefault();
            // compute current x position in [0,1] and zoom to that point
            const xFrac = (e.clientX - this.waveformsViewportRef.getBoundingClientRect().left)
                / (this.waveformsViewportRef.getBoundingClientRect().width);

            // zoom out
            if (e.deltaY > 0) {
                this.zoomByPercentageToPoint(modPercent, xFrac);
            } else { // zoom in
                this.zoomByPercentageToPoint(-modPercent, xFrac);
            }
            this.renderWaveforms();
        } else if (e.ctrlKey && e.shiftKey) {
            e.preventDefault();
            if (e.deltaY > 0) {
                // pan left
                this.panByPercentage(-modPercent);
            } else {
                // pan right
                this.panByPercentage(modPercent);
            }
            this.renderWaveforms();
        }
    }

    /**
     * onKeyDown event handler
     */
    private readonly onKeyDown = (e: React.KeyboardEvent<HTMLDivElement>) => {
        const amplitudeScaleResetHotKey = (this.props.hotKeyOverrides &&
            this.props.hotKeyOverrides.amplitudeScaleReset) ?
            this.props.hotKeyOverrides.amplitudeScaleReset :
            WaveformDisplay.defaultHotKeyOverridesConfig.amplitudeScaleReset;

        if (isHotKeyCommandSatisfied(e.nativeEvent, amplitudeScaleResetHotKey)) {
            if (this.stationComponentRefs) {
                this.stationComponentRefs.forEach(station => station.resetAmplitude());
            }
        }
    }

    /**
     * Create all markers
     */
    private readonly createAllMarkers = (): JSX.Element[] =>
        [...this.createVerticalMarkers(),
        ...this.createMoveableMarkers(),
        ...this.createSelectionWindowMarkers()]

    /**
     * Create vertical marker elements
     */
    private readonly createVerticalMarkers = (): JSX.Element[] => {
        if (!this.props.markers || !this.props.markers.verticalMarkers) return [];
        let curKey = 0;

        return this.props.markers.verticalMarkers.map(verticalMarker => {
            const leftPct = this.calcLeftPercent(verticalMarker.timeSecs);
            return (
                <VerticalMarker
                    key={curKey++}
                    color={verticalMarker.color}
                    lineStyle={verticalMarker.lineStyle}
                    percentageLocation={leftPct}
                />
            );
        });
    }

    /**
     * Create vertical marker elements
     */
    private readonly createMoveableMarkers = (): JSX.Element[] => {
        if (!this.props.markers || !this.props.markers.moveableMarkers) return [];
        let curKey = 0;

        return this.props.markers.moveableMarkers.map(moveableMarker => {
            const leftPct = this.calcLeftPercent(moveableMarker.timeSecs);
            return (
                <MoveableMarker
                    key={curKey++}
                    color={moveableMarker.color}
                    lineStyle={moveableMarker.lineStyle}
                    percentageLocation={leftPct}
                    moveableMarkers={[moveableMarker]}
                    waveformsContainerRef={this.waveformsContainerRef}
                    waveformsViewportRef={this.waveformsViewportRef}
                    waveformDisplayStartTimeSecs={this.props.startTimeSecs}
                    waveformDisplayEndTimeSecs={this.props.endTimeSecs}
                />
            );
        });
    }

    /**
     * Create selection window elements
     */
    private readonly createSelectionWindowMarkers = (): JSX.Element[] => {
        if (!this.props.markers || !this.props.markers.selectionWindows) return [];
        let curKey = 0;
        return this.props.markers.selectionWindows.map(selectionWindow =>
            (
                <SelectionWindow
                    key={curKey++}
                    waveformDisplayStartTimeSecs={this.props.startTimeSecs}
                    waveformDisplayEndTimeSecs={this.props.endTimeSecs}
                    selectionWindowProps={selectionWindow}
                    waveformsContainerRef={this.waveformsContainerRef}
                    waveformsViewportRef={this.waveformsViewportRef}
                    // tslint:disable:no-unbound-method
                    // these methods shouldn't be bound as they are events passed in by a 3rd party user
                    // and have already been bound to the appropriate context.
                    updateMoveablelMarkersValue={this.props.events ?
                        this.props.events.updateMoveableMarkersValue : undefined}
                />
            ));
    }

    /**
     * Create hot key strings for amplitude
     */
    private readonly amplitudeScaleHotKey = (): string =>
        (this.props.hotKeyOverrides && this.props.hotKeyOverrides.amplitudeScale) ?
            this.props.hotKeyOverrides.amplitudeScale : WaveformDisplay.defaultHotKeyOverridesConfig.amplitudeScale

    private readonly amplitudeScaleSingleResetHotKey = (): string => (this.props.hotKeyOverrides &&
        this.props.hotKeyOverrides.amplitudeScaleSingleReset) ?
        this.props.hotKeyOverrides.amplitudeScaleSingleReset :
        WaveformDisplay.defaultHotKeyOverridesConfig.amplitudeScaleSingleReset

    /**
     * Create station elements
     */
    private createStationsJsx(): JSX.Element[] {
        if (!this.stationComponentRefs) return [];

        const stationElements: JSX.Element[] = [];
        for (const stationConfig of this.props.stations) {
            const expansionState = this.state.expansionStates.find(state => state.stationId === stationConfig.id);
            const expanded = expansionState ? expansionState.expanded : false;

            stationElements.push(
                <Station
                    // data props
                    key={stationConfig.id}
                    ref={stationRef => {
                        if (!this.stationComponentRefs) return;
                        if (stationRef) { this.stationComponentRefs.push(stationRef); }
                    }}
                    displayStartTimeSecs={this.props.startTimeSecs}
                    displayEndTimeSecs={this.props.endTimeSecs}
                    workerRpcs={this.workerRpcs}
                    selectedSignalDetections={
                        this.props.selections ? this.props.selections.signalDetections : undefined
                    }
                    selectedChannels={this.props.selections ? this.props.selections.channels : undefined}
                    expanded={expanded}

                    stationConfig={{ ...stationConfig }}
                    isMeasureWindow={false}

                    // hotkeys
                    amplitudeScaleHotKey={this.amplitudeScaleHotKey()}
                    amplitudeScaleSingleResetHotKey={this.amplitudeScaleSingleResetHotKey()}

                    // callback  props
                    toggleExpansion={this.toggleExpansion}
                    setYAxisBounds={this.setYAxisBounds}
                    getViewRange={this.getViewRange}
                    onLoad={this.renderWaveforms}
                    // tslint:disable:no-unbound-method
                    // these methods shouldn't be bound as they are events passed in by a 3rd party user
                    // and have already been bound to the appropriate context.
                    onContextMenu={this.props.events ? this.props.events.onContextMenu : undefined}
                    onChannelLabelClick={this.props.events ? this.props.events.onChannelLabelClick : undefined}
                    onSignalDetectionClick={this.props.events ? this.props.events.onSignalDetectionClick : undefined}
                    onSignalDetectionDragEnd={
                        this.props.events ? this.props.events.onSignalDetectionDragEnd : undefined
                    }
                    onKeyPress={this.props.events ? this.props.events.onKeyPress : undefined}
                    // tslint:enable:no-unbound-method
                    onMouseMove={this.onMouseMove}
                    onMouseDown={this.onMouseDown}
                    onMouseUp={this.onMouseUp}
                    updateMeasureWindow={this.updateMeasureWindow}
                    // tslint:disable:no-unbound-method
                    // these methods shouldn't be bound as they are events passed in by a 3rd party user
                    // and have already been bound to the appropriate context.
                    onMaskClick={this.props.events ? this.props.events.onMaskClick : undefined}
                />,
            );
        }
        return stationElements;
    }

    /**
     * Create station labels
     */
    private createStationLabelsJsx(): JSX.Element[] {
        if (!this.stationLabelComponentRefs) return [];

        const stationLabels: JSX.Element[] = [];
        for (const stationConfig of this.props.stations) {
            const expansionState = this.state.expansionStates.find(state => state.stationId === stationConfig.id);
            const expanded = expansionState ?
                expansionState.expanded : false;

            stationLabels.push(
                <StationLabel
                    ref={stationLabelRef => {
                        if (!this.stationLabelComponentRefs) return;
                        if (stationLabelRef) { this.stationLabelComponentRefs.push(stationLabelRef); }
                    }}

                    // data props
                    key={stationConfig.id}
                    stationConfig={{ ...stationConfig }}

                    selectedChannels={this.props.selections ? this.props.selections.channels : undefined}
                    toggleExpansion={this.toggleExpansion}
                    expanded={expanded}
                    yAxisBounds={this.state.yAxisBounds}

                    // tslint:disable-next-line:no-unbound-method
                    onChannelLabelClick={this.props.events ? this.props.events.onChannelLabelClick : undefined}
                />,
            );
        }
        return stationLabels;
    }

    /**
     * return time range of current view as human-readable string
     */
    private readonly displayCurrentTimeRange = () => {
        if (!this.timeRangeRef) return;

        const scale = d3.scaleLinear()
            .domain([0, 1])
            .range([this.props.startTimeSecs, this.props.endTimeSecs]);
        const left = scale(this.viewRange[0]);
        const right = scale(this.viewRange[1]);
        this.timeRangeRef.innerHTML = `${moment.unix(left)
            .utc()
            .format('DD MMM YYYY HH:mm:ss')}
            + ${(moment as any).preciseDiff(moment.unix(right), moment.unix(left))}`;
    }

    /**
     * If WEAVESS is contained inside of a div with flex layout, sizing it with height=100% doesn't work.
     */
    private createRootStyle(): React.CSSProperties {
        if (this.props.flex) {
            return {
                flex: '1 1 0',
                position: 'relative',
            };
        } else {
            return {
                height: '100%',
                position: 'relative',
                width: '100%',
                boxSizing: 'content-box'
            };
        }
    }

    /**
     * Clear any waveforms currently rendered on the screen
     */
    private clearWaveforms(): void {
        this.renderer.clear();
    }

    /**
     * toggle the expansion state for a given stationId
     */
    private readonly toggleExpansion = (stationId: string) => {

        let expanded: boolean;

        this.setState(
            prevState => {
                const newExpansions = [...prevState.expansionStates];
                const expansionsEntry = prevState.expansionStates.find(state => state.stationId === stationId);
                if (expansionsEntry) {
                    expansionsEntry.expanded = !expansionsEntry.expanded;
                    expanded = expansionsEntry.expanded;
                } else {
                    newExpansions.push({
                        stationId,
                        expanded: true
                    });
                    expanded = true;
                }
                return {
                    expansionStates: newExpansions
                };
            },
            () => {
                // after setting state, call event callbacks if passed in
                if (!this.props.events) return;

                if (expanded) {
                    if (this.props.events.onChannelExpanded) this.props.events.onChannelExpanded(stationId);
                    if (this.props.events.onChannelCollapsed) this.props.events.onChannelCollapsed(stationId);
                }
            });
    }

    /**
     * set the y-axis bounds for a particular channel
     */
    private readonly setYAxisBounds = (channelId: string, min: number, max: number) => {
        this.setState(prevState => {
            const newYAxisBounds = [...prevState.yAxisBounds];
            const boundsEntry = prevState.yAxisBounds.find(state => state.channelId === channelId);
            if (boundsEntry) {
                boundsEntry.bounds = [min, max];
            } else {
                newYAxisBounds.push({
                    channelId,
                    bounds: [min, max]
                });
            }

            return {
                yAxisBounds: newYAxisBounds
            };
        });
    }

    /**
     * Render currently visible waveforms to the canvas.
     */
    private readonly renderWaveforms = (): void => {
        window.requestAnimationFrame(() => {
            // if we don't have a set size to display, abort
            if (!this.weavessRootRef ||
                !this.stationComponentRefs ||
                !this.waveformsViewportRef ||
                this.waveformsViewportRef.clientHeight === 0 ||
                this.waveformsViewportRef.clientWidth === 0) { return; }

            this.renderer.setScissorTest(true);
            this.updateSize();
            const boundsRect = this.weavessRootRef.getBoundingClientRect();

            this.updateMaskLabels();

            for (const waveform of this.stationComponentRefs) {
                const channels = [waveform.defaultChannelRef];
                for (const channelId in waveform.additionalChannelRefs) {
                    if (waveform.additionalChannelRefs.hasOwnProperty(channelId)) {
                        const channel = waveform.additionalChannelRefs[channelId];
                        if (channel) {
                            channels.push(channel);
                        }
                    }
                }

                channels.forEach(channel => {
                    if (!channel || !channel.containerRef) return;
                    // get its position relative to the page's viewport
                    const rect = channel.containerRef.getBoundingClientRect();

                    // check if it's out of bounds. If so skip it
                    if (rect.bottom < boundsRect.top || rect.top > boundsRect.bottom) {
                        return;  // it's out of bounds
                    }

                    // set the viewport
                    const width = rect.width;
                    const height = rect.height;
                    const x = rect.left - boundsRect.left;
                    const y = rect.top - boundsRect.top;

                    this.renderer.setViewport(x, y, width, height);
                    this.renderer.setScissor(x, y, width, height);

                    this.renderer.render(channel.scene, channel.camera);
                });
            }

            // draw the measure window as a special case
            if (this.measureWindowRef && this.measureWindowRef.stationRef) {
                const bounds = this.weavessRootRef.getBoundingClientRect();
                const rect = this.measureWindowRef.getChannelClientBounds();
                const channel = this.measureWindowRef.stationRef.defaultChannelRef;
                if (!rect || !channel) return;
                const width = rect.width;
                const height = rect.height;
                const x = rect.left - bounds.left;
                const y = rect.top - bounds.top;

                this.renderer.setViewport(x, y, width, height);
                this.renderer.setScissor(x, y, width, height);

                this.renderer.render(channel.scene, channel.camera);
            }

            this.renderer.setScissorTest(false);
        });
    }

    /**
     * Update the mask labels based on the viewing area.
     */
    private readonly updateMaskLabels = () => {
        // update the mask labels to be display only if the mask is within the viewing area
        if (this.stationLabelComponentRefs) {
            const durationSecs = this.props.endTimeSecs - this.props.startTimeSecs;
            const axisStart = this.props.startTimeSecs + (durationSecs * this.getViewRange()[0]);
            const axisEnd = this.props.startTimeSecs + (durationSecs * this.getViewRange()[1]);

            this.stationLabelComponentRefs.forEach(stationLabel => {
                // check to see if there are any masks on the default
                // channel or any of its non-default channels
                const showMaskIndicator =
                    Boolean((stationLabel.props.stationConfig.nonDefaultChannels &&
                        stationLabel.props.stationConfig.nonDefaultChannels
                            .map(channel => ((channel.masks !== undefined) && (channel.masks.length > 0)) &&
                                // check to see if any of the masks are in the viewing area
                                (channel.masks.some(mask =>
                                    (mask.startTimeSecs <= axisEnd &&
                                        mask.endTimeSecs >= axisStart))))
                            .reduce((c1, c2) => c1 || c2, false)));

                if (showMaskIndicator !== stationLabel.state.showMaskIndicator) {
                    stationLabel.setState({
                        showMaskIndicator
                    });
                }
            });
        }
    }

    /**
     * resize the renderer to fit the new canvas size
     */
    private updateSize() {
        if (!this.canvasRef) return;

        const width = this.canvasRef.offsetWidth;
        const height = this.canvasRef.offsetHeight;

        if (this.canvasRef.width !== width || this.canvasRef.height !== height) {
            this.renderer.setSize(width, height, false);
        }
    }

    /**
     * mouse down event handler
     */
    private readonly onMouseDown = (e: React.MouseEvent<HTMLDivElement>, xPct: number) => {
        // set the zoom start point
        if (e.ctrlKey || e.metaKey) {
            this.zoomStart = xPct;
        }
    }

    /**
     * mouse move event handler
     */
    private readonly onMouseMove = (e: React.MouseEvent<HTMLDivElement>, xPct: number) => {
        if (!this.selectionAreaRef) return;

        const fracToPct = 100;
        // minimum amount the mouse must move until it begins a zoom brush effect
        // 0.01 = 1% of the current view range
        const minMovementDeltaFrac = 0.01;

        // move the crosshair to the current pointer location
        if (this.crosshairRef) {
            this.crosshairRef.style.left = `${xPct * fracToPct}%`;
        }
        // if the user has moved more than 1% of the viewport, consider it a zoom operation
        if (this.zoomStart &&
            Math.abs(this.zoomStart - xPct) > minMovementDeltaFrac ||
            Math.abs(xPct - (this.zoomStart as number)) > minMovementDeltaFrac) {
            if (!this.isZooming) {
                this.selectionAreaRef.style.display = 'initial';
                this.isZooming = true;
            }
            const start = Math.min((this.zoomStart as number), xPct);
            const end = Math.max((this.zoomStart as number), xPct);
            this.selectionAreaRef.style.left = `${start * fracToPct}%`;
            this.selectionAreaRef.style.right = `${(1 - end) * fracToPct}%`;
        }
    }

    /**
     * mouse up event handler
     */
    private readonly onMouseUp = (e: React.MouseEvent<HTMLDivElement>,
        xPct: number, channelId: string, timeSecs: number) => {

        if (!this.selectionAreaRef) return;

        // if the user is zooming, perform the zoom
        if (this.isZooming) {
            const scale = d3.scaleLinear()
                .domain([0, 1])
                .range([this.viewRange[0], this.viewRange[1]]);
            const start = Math.min((this.zoomStart as number), xPct);
            const end = Math.max((this.zoomStart as number), xPct);
            this.zoom(scale(start), scale(end));
        } else if (!this.isZooming && this.zoomStart) {
            // else, register as a click operation
            if (this.props.events && this.props.events.onChannelClick) {
                this.props.events.onChannelClick(e, channelId, timeSecs);
            }
        }
        this.selectionAreaRef.style.display = 'none';
        this.zoomStart = undefined;
        this.isZooming = false;
    }

    /**
     * zoomPct in [0,1], x in [0,1]
     */
    private readonly zoomByPercentageToPoint = (zoomPct: number, x: number) => {
        const range = this.viewRange[1] - this.viewRange[0];
        const zoom = (range * zoomPct) / 2.0; // tslint:disable-line
        const left = this.viewRange[0] - (zoom * x);
        const right = this.viewRange[1] + (zoom * (1 - x));
        this.zoom(left, right);
    }

    /**
     * pct in [0,1]
     */
    private readonly panByPercentage = (pct: number) => {
        const range = this.viewRange[1] - this.viewRange[0];
        const delta = range * pct;
        const left = this.viewRange[0] + delta;
        const right = this.viewRange[1] + delta;
        this.zoom(left, right);
    }

    /**
     * left/right are numbers between [0,1] denoting the left/right percentages of [start,end]
     */
    private readonly zoom = (start: number, end: number) => {
        if (!this.waveformsContainerRef
            || !this.waveformsViewportRef
            || !this.timeAxisRef) return;

        if (start < 0) {
            // tslint:disable-next-line:no-parameter-reassignment
            start = 0;
        }
        if (end > 1) {
            // tslint:disable-next-line:no-parameter-reassignment
            end = 1;
        }
        if (end < start) {
            const minDelta = 0.001;
            // tslint:disable-next-line:no-parameter-reassignment
            end = start + minDelta;
        }

        if (start === 0 && end === 1) {
            this.waveformsContainerRef.style.width = 'initial';
        } else {
            const range = end - start;
            const pixels = this.waveformsViewportRef.clientWidth / range;

            this.waveformsContainerRef.style.width = `${pixels}px`;
            this.waveformsViewportRef.scrollLeft = start * pixels;
        }

        this.viewRange = [start, end];
        this.timeAxisRef.update();
        this.displayCurrentTimeRange();
        this.renderWaveforms();
    }

    /**
     * Update measure window
     */
    private readonly updateMeasureWindow = (measureWindowSelection: Entities.MeasureWindowSelection) => {
        this.setState({
            showMeasureWindow: true
        });

        if (!this.measureWindowRef ||
            !this.stationComponentRefs) {
            return;
        }

        // clear out any existing measure windows selections if referenecing a different channel
        if (this.measureWindowRef.state.measureWindowSelection &&
            this.measureWindowRef.state.measureWindowSelection.channelRef &&
            measureWindowSelection.channelRef !== this.measureWindowRef.state.measureWindowSelection.channelRef) {
            this.measureWindowRef.state.measureWindowSelection.channelRef.removeMeasureWindowSelection();
        } else {
            // special case: referencing the same channel, to
            // force a complete update set the measure window selection
            // area to 'undefined' temporarily
            this.measureWindowRef.setState({
                measureWindowSelection: undefined
            });
        }

        const yBoundEntry = this.state.yAxisBounds.find(state => state.channelId ===
            measureWindowSelection.channelId);
        // update the measure window state with the new selection
        this.measureWindowRef.setState(
            {
                measureWindowSelection,
                yAxisBounds: yBoundEntry ? yBoundEntry.bounds : [0, 1]
            }
        );
    }

    /**
     * Create vertical marker elements
     */
    private readonly calcLeftPercent = (timeSecs: number): number => {
        const scale = d3.scaleLinear()
            .domain([this.props.startTimeSecs, this.props.endTimeSecs])
            .range([0, 1]);
        const fracToPct = 100;
        return scale(timeSecs) * fracToPct;
    }
}
// tslint:disable-next-line:max-file-line-count
