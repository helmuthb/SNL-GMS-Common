import gql from 'graphql-tag';

/**
 * Event data necessary for the waveform display to know about
 */
export interface WaveformDisplayEventData {
    eventHypothesesInTimeRange: {
        id: string;
        isRejected: boolean;
        event: {
            id: string;
            status: string;
        };
        preferredLocationSolution: {
            locationSolution: {
                latDegrees: number;
                lonDegrees: number;
                depthKm: number;
                timeSec: number;
            };
        };
        signalDetectionAssociations: {
            isRejected: boolean;
            signalDetectionHypothesis: {
                id: string;
                phase: string;
                isRejected: boolean;
                arrivalTimeMeasurement: {
                    id: string;
                    featureType: string;
                    timeSec: number;
                    uncertaintySec: number;
                };
            };
        }[];
    }[];
}

/**
 * Input variables for the Waveform display events query
 */
export interface WaveformDisplayEventsQueryInput {
    timeRange: {
        startTime: number;
        endTime: number;
    };
}

export const waveformDisplayEventsQuery = gql`
query eventHypothesesInTimeRange($timeRange: TimeRange!) {
    eventHypothesesInTimeRange(timeRange: $timeRange) {
        id
        isRejected
        event {
            id
            status
        }
        preferredLocationSolution {
            locationSolution {
                latDegrees
                lonDegrees
                depthKm
                timeSec
            }
        }
        signalDetectionAssociations {
            isRejected
            signalDetectionHypothesis {
                id
                phase
                isRejected
                arrivalTimeMeasurement {
                    id
                    featureType
                    timeSec
                    uncertaintySec
                }
            }
        }
    }
}
`;
