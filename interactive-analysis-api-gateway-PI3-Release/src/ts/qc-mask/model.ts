import { TimeRange } from '../common/model';

/**
 * qc-mask categories 
 */
export enum QcMaskCategory {
    AnalystDefined = 'ANALYST_DEFINED',
    ChannelProcessing = 'CHANNEL_PROCESSING',
    DataAuthentication = 'DATA_AUTHENTICATION',
    Rejected = 'REJECTED',
    StationSOH = 'STATION_SOH',
    WaveformQuality = 'WAVEFORM_QUALITY'
}

/**
 * Qc-Mask types
 */
export enum QcMaskType {
    SensorProblem = 'SENSOR_PROBLEM',
    StationProblem = 'STATION_PROBLEM',
    Calibration = 'CALIBRATION',
    StationSecurity = 'STATION_SECURITY',
    Timing = 'TIMING',
    RepairableGap = 'REPAIRABLE_GAP',
    RepeatableAdjacentAmplitudeValue = 'REPEATED_ADJACENT_AMPLITUDE_VALUE',
    LongGap = 'LONG_GAP',
    Spike = 'SPIKE'
}

/**
 * Represents a QC Mask channel time segment
 */
export interface QcMask {
    id: string;
    channelId: string;
    qcMaskVersions: QcMaskVersion[];
    currentVersion: QcMaskVersion;
}

/**
 * Represents a QC Mask entry
 */
export interface QcMaskVersion {
    category: QcMaskCategory;
    channelSegmentIds: string[];
    creationInfoId: string;
    endTime: number; // seconds?
    parentQcMasks: QcMask[];
    rationale: string;
    startTime: number;
    type: QcMaskType;
    version: string;
}

export interface NewQcMaskInput {
    channelId: string;
    timeRange: TimeRange;
    category: QcMaskCategory;
    type: QcMaskType;
}

/**
 * Update Input
 */
export interface UpdateQcMaskInput {
    timeRange: TimeRange;
    category: QcMaskCategory;
    type: QcMaskType;
}
