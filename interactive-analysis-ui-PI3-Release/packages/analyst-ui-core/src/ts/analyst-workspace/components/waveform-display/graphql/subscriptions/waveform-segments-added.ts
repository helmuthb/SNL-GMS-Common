import gql from 'graphql-tag';

/**
 * Data structure for waveform ChannelSegment subscription callback
 */
export interface WaveformSegmentsAddedSubscription {
    waveformChannelSegmentsAdded: {
        channel: {
            id: string;
        };
        startTime: number;
        endTime: number;
    }[];
}

export const waveformSegmentsAddedSubscription = gql`
subscription waveformChannelSegmentsAdded {
    waveformChannelSegmentsAdded {
        channel {
            id
        }
        startTime
        endTime
    }
  }
`;
