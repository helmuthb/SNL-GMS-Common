import gql from 'graphql-tag';
import { SignalDetection } from '../queries/station-detections';

/**
 * Data structure for detectionUpdated subscription callback
 */
export interface DetectionsUpdatedSubscription {
    detectionsUpdated: SignalDetection[];
}

export const detectionsUpdatedSubscription = gql`
subscription detectionsUpdated {
    detectionsUpdated {
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
