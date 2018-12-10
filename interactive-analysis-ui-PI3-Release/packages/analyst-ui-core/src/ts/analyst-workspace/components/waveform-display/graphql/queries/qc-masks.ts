import gql from 'graphql-tag';

export interface MaskData {
    version: string;
    creationInfo: {
        id: string;
        creationTime: number;
        creatorId: string;
        creatorType: string;
    };
    channelSegmentIds: string[];
    category: string;
    type: string;
    startTime: number;
    endTime: number;
    rationale: string;
}

export interface QcMasksQuery {
    qcMasksByChannelId: {
        id: string;
        channelId: string;
        currentVersion: MaskData;
        qcMaskVersions: MaskData[];
    };
}

export interface QcMasksQueryInput {
    timeRangeWithChannelIds: {
        timeRange: {
            startTime: number;
            endTime: number;
        };
        channelIds: string[];
    };
}

export const qcMasksQuery = gql`
query waveformQcMasks($timeRangeWithChannelIds: TimeRangeWithChannelIds) {
    qcMasksByChannelId(timeRangeWithChannelIds: $timeRangeWithChannelIds) {
        id
        channelId
        currentVersion {
            version
            creationInfo {
                id
                creationTime
                creatorId
                creatorType
            }
            channelSegmentIds
            category
            type
            startTime
            endTime
            rationale
        }
        qcMaskVersions {
            version
            creationInfo {
                id
                creationTime
                creatorId
                creatorType
            }
            channelSegmentIds
            category
            type
            startTime
            endTime
            rationale
        }
    }
}
`;
