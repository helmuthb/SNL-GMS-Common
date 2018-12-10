import * as React from 'react';
import { VerticalMarkerObject } from '../components/waveform-display/marker/vertical-marker';
import { Channel } from '../components/waveform-display/station/channel';

export interface StationConfig {
    id: string;
    name: string;
    defaultChannel: ChannelConfig;
    nonDefaultChannels?: ChannelConfig[];
    height?: number;
    distanceKm?: number;
}

export interface StationDefaultConfig {
    height: number;
}

export interface ChannelConfig {
    id: string;
    name: string;
    sampleRate: number;
    dataSegments: ChannelSegment[];
    color?: string;

    // only exist on the default channel, in practice
    signalDetections?: SignalDetectionConfig[];
    theoreticalPhaseWindows?: TheoreticalPhaseWindow[];
    masks?: Mask[];
    description?: string;
    displayType?: DisplayType[];
    pointSize?: number;
}

export interface ChannelDefaultConfig {
    displayType: DisplayType[];
    pointSize: number;
    color: string;
}

export enum DisplayType {
    LINE = 'LINE',
    SCATTER = 'SCATTER'
}

export enum LineStyle {
    SOLID = 'solid',
    DASHED = 'dashed',
}

export interface ChannelSegment {
    startTimeSecs: number;
    data: Float32Array | number[];
}

export interface SignalDetectionConfig {
    id: string;
    timeSecs: number;
    label: string;
    color: string;
}

export interface TheoreticalPhaseWindow {
    id: string;
    startTimeSecs: number;
    endTimeSecs: number;
    label: string;
    color: string;
}

export interface Mask {
    id: string;
    startTimeSecs: number;
    endTimeSecs: number;
    color: string;
}

export interface WeavessEvents {
    onChannelExpanded? (channelId: string): void;
    onChannelCollapsed? (channelId: string): void;
    onContextMenu? (e: React.MouseEvent<HTMLDivElement>, channelId: string, sdId?: string): void;
    onChannelLabelClick? (e: React.MouseEvent<HTMLDivElement>, channelId: string): void;
    onChannelClick? (e: React.MouseEvent<HTMLDivElement>, channelId: string, timeSecs: number): void;
    onSignalDetectionClick? (e: React.MouseEvent<HTMLDivElement>, sdId: string): void;
    onSignalDetectionDragEnd? (sdId: string, timeSecs: number): void;
    onKeyPress? (e: React.KeyboardEvent<HTMLDivElement>, clientX: number,
                 clientY: number, channelId: string, timeSecs: number): void;
    onMaskClick?(event: React.MouseEvent<HTMLDivElement>, channelId: string, maskId: string[]): void;
    updateMoveableMarkersValue?(moveablelMarkers: VerticalMarkerObject[]): void;
}

export interface WeavessSelections {
    channels?: string[];
    signalDetections?: string[];
}

export interface HotKeyOverridesConfig {
    amplitudeScale?: string;
    amplitudeScaleSingleReset?: string;
    amplitudeScaleReset?: string;
}

export interface HotKeyOverridesDefaultConfig {
    amplitudeScale: string;
    amplitudeScaleSingleReset: string;
    amplitudeScaleReset: string;
}

export interface MeasureWindowSelection {
    stationId?: string;
    channelId?: string;
    channelRef?: Channel;
    startTimeSecs: number;
    endTimeSecs: number;
}
