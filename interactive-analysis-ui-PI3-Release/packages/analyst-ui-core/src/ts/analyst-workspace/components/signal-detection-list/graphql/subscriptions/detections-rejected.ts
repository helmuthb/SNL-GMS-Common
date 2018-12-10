import gql from 'graphql-tag';

/**
 * Data type for detectionsRejected subscription callback
 */
export interface DetectionsRejectedSubscription {
    detectionsRejected: {
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
}

export const detectionsRejectedSubscription = gql`
subscription detectionsRejected{
    detectionsRejected {
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
`;
