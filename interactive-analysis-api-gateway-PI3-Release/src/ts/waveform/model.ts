import { TimeSeries, CreationInfo } from '../common/model';

/**
 * Type enumeration for channel segments
 */
export enum ChannelSegmentType {
    Acquired = 'Acquired',
    Raw = 'Raw',
    DetectionBeam = 'DetectionBeam',
    FkBean = 'FkBean',
    Filter = 'Filter'
}

/**
 * Represents a channel time segment comprised of one or more waveforms for a (derived or raw).
 */
export interface ChannelSegment {
    id: string;
    segmentType: ChannelSegmentType;
    startTime: number;
    endTime: number;
    channelId: string;
    timeseriesList: TimeSeries[];
    featureMeasurementIds: string[];
    creationInfo: CreationInfo;
}
