import { NonIdealState } from '@blueprintjs/core';
import * as React from 'react';
import { MeasureWindowSelection, StationConfig } from '../../../entities';
import { TimeAxis } from '../axes';
import { Station } from '../station';
import { StationLabel } from '../station/label';

/**
 * MeasureWindow Props
 */
export interface MeasureWindowProps {
    heightPx: number;
    workerRpcs: any[]; // webWorkers used to load waveforms into graphics space
    selectedSignalDetections: string[] | undefined;
    waveformDisplayRef: HTMLDivElement | null;

    // hotkeys
    amplitudeScaleHotKey: string;
    amplitudeScaleSingleResetHotKey: string;

    // callbacks
    toggleExpansion(stationId: string);
    onLoad(): void;
    onMouseMove(e: React.MouseEvent<HTMLDivElement>, xPct: number): void;
    onMouseDown(e: React.MouseEvent<HTMLDivElement>, xPct: number): void;
    onMouseUp(e: React.MouseEvent<HTMLDivElement>, xPct: number, channelId: string, timeSecs: number): void;
    onContextMenu?(e: React.MouseEvent<HTMLDivElement>, channelId: string, sdId?: string): void;
    onChannelLabelClick?(e: React.MouseEvent<HTMLDivElement>, channelName: string): void;
    onSignalDetectionClick?(e: React.MouseEvent<HTMLDivElement>, sdId: string): void;
    onSignalDetectionDragEnd?(sdId: string, timeSecs: number): void;
    onKeyPress?(e: React.KeyboardEvent<HTMLDivElement>, clientX: number,
        clientY: number, channelId: string, timeSecs: number): void;
    onMaskClick?(event: React.MouseEvent<HTMLDivElement>, channelId: string, maskId: string[]): void;

}

/**
 * MeasureWindow State
 */
export interface MeasureWindowState {
    measureWindowSelection?: MeasureWindowSelection;
    yAxisBounds?: [number, number];
    xAxisBounds?: [number, number];
    measureWindowHeightPx: number;
}

/**
 * MeasureWindow Component
 */
export class MeasureWindow extends React.Component<MeasureWindowProps, MeasureWindowState> {

    /**
     * Reference to the containing div.
     */
    public containerRef: HTMLDivElement | null;

    /**
     * Reference to the station.
     */
    public stationRef: Station | null;

    /**
     * Reference to the station label.
     */
    public stationLabelRef: StationLabel | null;

    /**
     * Reference to the measureWindowDiv.
     */
    public measureWindowRef: HTMLDivElement | null;

    /**
     * Constructor
     */
    public constructor(props: MeasureWindowProps) {
        super(props);
        this.state = {
            measureWindowSelection: undefined,
            yAxisBounds: undefined,
            xAxisBounds: undefined,
            measureWindowHeightPx: this.props.heightPx
        };
    }

    /**
     * React component lifecycle
     */
    public render() {
        const TIME_AXIS_HEIGHT = 35;

        if (!this.state.measureWindowSelection ||
            !this.state.measureWindowSelection.channelRef) {
            return (
                <div
                    ref={ref => this.containerRef = ref}
                    style={{
                        flex: '0 0 auto',
                        borderBottom: '0.25rem solid',
                        height: `${this.state.measureWindowHeightPx}px`,
                        width: '100%',
                        backgroundColor: '#202B33'
                    }}
                >
                    <NonIdealState
                        visual="timeline-line-chart"
                        title="No Measure Window Data Selected"
                    />
                </div>
            );
        }

        const stationConfig: StationConfig = {
            id: 'measure',
            name: 'measure',
            height: this.state.measureWindowHeightPx - TIME_AXIS_HEIGHT,
            defaultChannel: this.state.measureWindowSelection.channelRef.props.channelConfig,
            nonDefaultChannels: []
        };
        return (
            <div
                className="weavess-measure-window"
                ref={ref => this.containerRef = ref}
                style={{
                    flex: '0 0 auto',
                    borderBottom: '0.25rem solid',
                    height: `${this.state.measureWindowHeightPx}px`,
                    width: '100%',
                }}
            >
                <div
                    ref={ref => this.measureWindowRef = ref}
                    style={{
                        height: `${this.state.measureWindowHeightPx - TIME_AXIS_HEIGHT}px`,
                        width: '100%',
                        display: 'flex',
                    }}
                >
                    <StationLabel
                        ref={ref => this.stationLabelRef = ref}
                        selectedChannels={[]}
                        expanded={false}
                        stationConfig={stationConfig}
                        yAxisBounds={[{
                            channelId: this.state.measureWindowSelection.channelId ?
                                this.state.measureWindowSelection.channelId : '1',
                            bounds: this.state.yAxisBounds ?
                                    [this.state.yAxisBounds[0], this.state.yAxisBounds[1]] : [0, 1]
                        }]}
                        toggleExpansion={() => { /* no-op */ }}
                    />
                    <div
                        style={{
                            flex: '1 0 auto',
                            position: 'relative'
                        }}
                    >
                        <div
                            style={{
                                position: 'absolute',
                                top: '0px',
                                bottom: '0px',
                                right: '0px',
                                left: '0px',
                                overflowY: 'scroll'
                            }}
                        >
                            <Station
                                ref={ref => this.stationRef = ref}
                                stationConfig={stationConfig}
                                displayStartTimeSecs={this.state.measureWindowSelection.startTimeSecs}
                                displayEndTimeSecs={this.state.measureWindowSelection.endTimeSecs}
                                workerRpcs={this.props.workerRpcs}
                                selectedSignalDetections={this.props.selectedSignalDetections}
                                selectedChannels={[]}
                                expanded={false}
                                isMeasureWindow={true}
                                amplitudeScaleHotKey={this.props.amplitudeScaleHotKey}
                                amplitudeScaleSingleResetHotKey={this.props.amplitudeScaleSingleResetHotKey}
                                onSignalDetectionClick={this.props.onSignalDetectionClick}
                                onSignalDetectionDragEnd={this.props.onSignalDetectionDragEnd}
                                toggleExpansion={() => { /* no-op */ }}
                                setYAxisBounds={this.setYAxisBounds}
                                getViewRange={() => [0, 1]}
                                onContextMenu={this.props.onContextMenu}
                                onLoad={this.props.onLoad}
                                onKeyPress={this.props.onKeyPress}
                                onMouseMove={() => { /* no-op */ }}
                                onMouseDown={this.props.onMouseDown}
                                onMouseUp={this.props.onMouseUp}
                                updateMeasureWindow={() => { /* no-op */ }}
                                onMaskClick={this.props.onMaskClick}
                            />
                        </div>
                    </div>
                </div>
                <TimeAxis
                    startTimeSecs={this.state.measureWindowSelection.startTimeSecs}
                    endTimeSecs={this.state.measureWindowSelection.endTimeSecs}
                    getViewRange={() => [0, 1]}
                    borderTop={false}
                />
                <div
                    className="measure-window-divider"
                    onMouseDown={this.onMeasureWindowDividerDrag}
                    style={{
                        position: 'relative',
                        height: '0.5rem',
                        width: '100%',
                        cursor: 'row-resize',
                        flex: '0 0 auto'
                    }}
                >
                    <div
                        style={{
                            position: 'absolute',
                            top: '0.2rem',
                            bottom: '0.2rem',
                            left: '0px',
                            right: '0px',
                        }}
                    />
                </div>
            </div>
        );
    }

    /**
     * Get Channel Client bounds
     */
    public getChannelClientBounds = () => {
        if (!this.stationRef ||
            !this.stationRef.defaultChannelRef ||
            !this.stationRef.defaultChannelRef.containerRef) return;
        return this.stationRef.defaultChannelRef.containerRef.getBoundingClientRect();
    }

    /**
     * set the y-axis bounds for a particular channel
     */
    private readonly setYAxisBounds = (channelId: string, min: number, max: number) => {
        this.setState({
            yAxisBounds: [min, max]
        });
        this.props.onLoad();
    }

    /**
     * Update size of the measure window
     */
    private readonly onMeasureWindowDividerDrag = (e: React.MouseEvent<HTMLDivElement>) => {
        let prevPosition = e.clientY;
        let currentPos = e.clientY;
        let diff = 0;
        const minHeightPx = StationLabel.defaultProps.height;
        const maxHeightPx = 500;
        const onMouseMove = (e2: MouseEvent) => {
            if (this.containerRef && this.stationLabelRef) {
                currentPos = e2.clientY;
                diff = currentPos - prevPosition;
                prevPosition = currentPos;
                const heightPx = this.containerRef.clientHeight + diff;
                if (heightPx > minHeightPx && heightPx < maxHeightPx) {
                    this.setState({
                        measureWindowHeightPx: heightPx
                    });
                    this.props.onLoad();
                }
            }
        };

        const onMouseUp = (e2: MouseEvent) => {
            this.props.onLoad();
            document.body.removeEventListener('mousemove', onMouseMove);
            document.body.removeEventListener('mouseup', onMouseUp);
        };

        document.body.addEventListener('mousemove', onMouseMove);
        document.body.addEventListener('mouseup', onMouseUp);
    }
}
