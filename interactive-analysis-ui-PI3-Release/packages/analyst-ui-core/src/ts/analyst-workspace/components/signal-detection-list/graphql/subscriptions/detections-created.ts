import gql from 'graphql-tag';

/**
 * Data structure for detectionCreated subscription callback
 */
export interface DetectionsCreatedSubscription {
    detectionsCreated: {
        id: string;
        station: {
            id: string;
            name: string;
        };
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
}

export const detectionsCreatedSubscription = gql`
subscription detectionsCreated {
    detectionsCreated {
        id
        station {
            id
            name
        }
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
`;
