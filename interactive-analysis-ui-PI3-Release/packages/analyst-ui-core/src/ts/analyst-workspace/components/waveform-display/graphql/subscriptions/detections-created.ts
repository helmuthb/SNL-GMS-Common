import gql from 'graphql-tag';
import { HypothesisData } from '../queries/station-detections';

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
        currentHypothesis: HypothesisData;
        hypotheses: HypothesisData[];
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
                    }
                }
            }
            creationInfo {
                id
                creationTime
                creatorId
                creatorType
            }
        }
        hypotheses {
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
                    }
                }
            }
            creationInfo {
                id
                creationTime
                creatorId
                creatorType
            }
        }
    }
}
`;
