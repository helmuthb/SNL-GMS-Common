import gql from 'graphql-tag';

/**
 * Data to populate the event list display
 */
export interface EventListData {
    eventHypothesesInTimeRange: {
        id: string;
        isRejected: boolean;
        event: {
            id: string;
            status: string;
            preferredHypothesis: {
                processingStage: {
                    id: string;
                };
            };
            activeAnalysts: {
                userName: string;
            }[];
        };
        preferredLocationSolution: {
            locationSolution: {
                latDegrees: number;
                lonDegrees: number;
                depthKm: number;
                timeSec: number;
                networkMagnitudeSolutions: {
                    magnitudeType: 'mb' | 'mbMLE' | 'mbrel' | 'ms' | 'msMLE' | 'msVMAX' | 'ml';
                    magnitude: number;
                }[];
            };
        };
        signalDetectionAssociations: {
            signalDetectionHypothesis: {
                id: string;
            };
            isRejected: boolean;
        }[];
    }[];
}

/**
 * Input variables for the event list query
 */
export interface EventListQueryInput {
    timeRange: {
        startTime: number;
        endTime: number;
    };
}

export const eventListQuery = gql`
query eventHypothesesInTimeRange($timeRange: TimeRange!) {
    eventHypothesesInTimeRange(timeRange: $timeRange) {
        id
        isRejected
        event {
            id
            status
            preferredHypothesis {
                processingStage {
                    id
                }
            }
            activeAnalysts {
                userName
            }
        }
        preferredLocationSolution {
            locationSolution {
                latDegrees
                lonDegrees
                depthKm
                timeSec
                networkMagnitudeSolutions {
                    magnitudeType
                    magnitude
                }
            }
        }
        signalDetectionAssociations {
            signalDetectionHypothesis {
                id
            }
            isRejected
        }
    }
}
`;
