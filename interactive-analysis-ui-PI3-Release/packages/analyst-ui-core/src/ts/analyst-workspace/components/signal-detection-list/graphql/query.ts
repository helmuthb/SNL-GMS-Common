import gql from 'graphql-tag';

/**
 * Data to populate the Signal Detection List display
 */
export interface SignalDetectionListData {
    defaultStations: {
        id: string;
        signalDetections: {
            id: string;
            currentHypothesis: {
                id: string;
                phase: string;
                isRejected: boolean;
                arrivalTimeMeasurement: {
                    id: string;
                    featureType: string;
                    timeSec: number;
                    uncertaintySec: number;
                };
                signalDetectionAssociations: {
                    id: string;
                    isRejected: boolean;
                    eventHypothesis: {
                        id: string;
                        event: {
                            id: string;
                            status: string;
                        };
                    };
                }[];
            };
        }[];
    }[];
}

/**
 * Input variables for the Signal Detection list display query
 */
export interface SignalDetectionListQueryInput {
    timeRange: {
        startTime: number;
        endTime: number;
    };
}

/**
 * GraphQL query for data needed to populate the signal detection list display
 */
export const signalDetectionListQuery = gql`
query defaultStations($timeRange: TimeRange!) {
    defaultStations {
        id
        signalDetections(timeRange: $timeRange) {
            id
            currentHypothesis {
                id
                phase
                isRejected
                arrivalTimeMeasurement {
                    id
                    featureType
                    timeSec
                    uncertaintySec
                }
                signalDetectionAssociations {
                    id
                    isRejected
                    eventHypothesis {
                        id
                        event {
                            id
                            status
                        }
                    }
                }
            }
        }
    }
}
`;
