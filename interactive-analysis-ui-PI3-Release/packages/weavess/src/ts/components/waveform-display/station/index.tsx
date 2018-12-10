import * as React from 'react';

import { forEach } from 'lodash';
import * as Entities from '../../../entities';
import { Channel } from './channel';

/**
 * Station Props
 */
export interface StationProps {

    stationConfig: Entities.StationConfig;

    displayStartTimeSecs: number;
    displayEndTimeSecs: number;
    workerRpcs: any[];
    selectedSignalDetections: string[] | undefined;
    selectedChannels: string[] | undefined;
    expanded: boolean;
    isMeasureWindow: boolean;

    // hotkeys
    amplitudeScaleHotKey: string;
    amplitudeScaleSingleResetHotKey: string;

    // callbacks
    toggleExpansion(stationId: string);
    setYAxisBounds(channelId: string, min: number, max: number);
    getViewRange(): [number, number];
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
    updateMeasureWindow(measureWindowSelection: Entities.MeasureWindowSelection): void;
    onMaskClick?(event: React.MouseEvent<HTMLDivElement>, channelId: string, maskId: string[]): void;
}

/**
 * Station State
 */
// tslint:disable-next-line:no-empty-interface
export interface StationState {
}

/**
 * A station consisting of a default channel and 0 or more non-default channels with data
 */
export class Station extends React.Component<StationProps, StationState> {

    /**
     * the default props for all stations, if not passed in
     */
    public static defaultProps: Entities.StationDefaultConfig = {
        height: 75
    };

    /**
     * A ref to the default channel
     */
    public defaultChannelRef: Channel | null;

    /**
     * An object containing refs to all additional channels
     */
    public additionalChannelRefs: { [id: string]: Channel | null } = {};

    public constructor(props: StationProps) {
        super(props);
    }

    // ************************************
    // BEGIN REACT COMPONENT LIFECYCLE METHODS
    // ************************************

    /**
     * React component lifecycle
     */
    public render() {

        const singleHeight = this.props.stationConfig.height || Station.defaultProps.height;
        const totalHeight = this.props.expanded && this.props.stationConfig.nonDefaultChannels ?
            singleHeight * (this.props.stationConfig.nonDefaultChannels.length + 1)
            : singleHeight;
        // console.log("state height: " + this.state.height);
        return (
            <div
                style={{
                    height: totalHeight,
                    display: 'flex',
                    flexDirection: 'column',
                    flex: '1 0 auto'
                }}
            >
                <Channel
                    ref={ref => this.defaultChannelRef = ref}
                    key={this.props.stationConfig.defaultChannel.id}

                    stationId={this.props.stationConfig.id}
                    subChannel={false}
                    height={this.props.stationConfig.height || Station.defaultProps.height}
                    channelConfig={this.props.stationConfig.defaultChannel}

                    displayStartTimeSecs={this.props.displayStartTimeSecs}
                    displayEndTimeSecs={this.props.displayEndTimeSecs}
                    workerRpcs={this.props.workerRpcs}
                    selectedSignalDetections={this.props.selectedSignalDetections}
                    selectedChannels={this.props.selectedChannels}
                    isMeasureWindow={this.props.isMeasureWindow}

                    // hotkeys
                    amplitudeScaleHotKey={this.props.amplitudeScaleHotKey}
                    amplitudeScaleSingleResetHotKey={this.props.amplitudeScaleSingleResetHotKey}

                    // callbacks
                    setYAxisBounds={this.props.setYAxisBounds}
                    getViewRange={this.props.getViewRange}
                    onLoad={this.props.onLoad}
                    onContextMenu={this.props.onContextMenu}
                    onChannelLabelClick={this.props.onChannelLabelClick}
                    onSignalDetectionClick={this.props.onSignalDetectionClick}
                    onSignalDetectionDragEnd={this.props.onSignalDetectionDragEnd}
                    onKeyPress={this.props.onKeyPress}
                    onMouseMove={this.props.onMouseMove}
                    onMouseDown={this.props.onMouseDown}
                    onMouseUp={this.props.onMouseUp}
                    updateMeasureWindow={this.props.updateMeasureWindow}
                    onMaskClick={this.props.onMaskClick}
                />
                {
                    this.props.expanded && this.props.stationConfig.nonDefaultChannels ?
                        (
                            this.props.stationConfig.nonDefaultChannels.map(channelConfig => (
                                <Channel
                                    key={channelConfig.id}
                                    ref={ref => this.additionalChannelRefs[channelConfig.id] = ref}

                                    stationId={this.props.stationConfig.id}
                                    subChannel={true}
                                    height={this.props.stationConfig.height || Station.defaultProps.height}
                                    channelConfig={{ ...channelConfig }}

                                    displayStartTimeSecs={this.props.displayStartTimeSecs}
                                    displayEndTimeSecs={this.props.displayEndTimeSecs}
                                    workerRpcs={this.props.workerRpcs}
                                    selectedSignalDetections={this.props.selectedSignalDetections}
                                    selectedChannels={this.props.selectedChannels}
                                    isMeasureWindow={false}

                                    // hotkeys
                                    amplitudeScaleHotKey={this.props.amplitudeScaleHotKey}
                                    amplitudeScaleSingleResetHotKey={this.props.amplitudeScaleSingleResetHotKey}

                                    // callbacks
                                    setYAxisBounds={this.props.setYAxisBounds}
                                    getViewRange={this.props.getViewRange}
                                    onLoad={this.props.onLoad}
                                    onContextMenu={this.props.onContextMenu}
                                    onChannelLabelClick={this.props.onChannelLabelClick}
                                    onSignalDetectionClick={this.props.onSignalDetectionClick}
                                    onSignalDetectionDragEnd={this.props.onSignalDetectionDragEnd}
                                    onKeyPress={this.props.onKeyPress}
                                    onMouseMove={this.props.onMouseMove}
                                    onMouseDown={this.props.onMouseDown}
                                    onMouseUp={this.props.onMouseUp}
                                    updateMeasureWindow={this.props.updateMeasureWindow}
                                    onMaskClick={this.props.onMaskClick}
                                />
                            ))
                        )
                        : []
                }
            </div>
        );
    }

    /**
     * React component lifecycle
     */
    public componentDidUpdate(prevProps: StationProps) {
        const delay = 100;
        if (prevProps.expanded !== this.props.expanded) {
            window.setTimeout(this.props.onLoad, delay);
        }
    }

    // ************************************
    // END REACT COMPONENT LIFECYCLE METHODS
    // ************************************

    /**
     * Reset the amplitude to the default.
     */
    public resetAmplitude = () => {
        if (this.defaultChannelRef) {
            this.defaultChannelRef.resetAmplitude();
        }
        if (this.additionalChannelRefs) {
            forEach(
                this.additionalChannelRefs,
                // tslint:disable-next-line
                channel => (channel) ? channel.resetAmplitude() : null);
        }
    }
}
