import gql from 'graphql-tag';
import { SignalDetection } from '../queries/station-detections';

/**
 * Data type for detectionHypothesesRejected subscription callback
 */
export interface DetectionHypothesesRejectedSubscription {
    detectionHypothesesRejected: SignalDetection[];
}

export const detectionHypothesesRejectedSubscription = gql`
subscription detectionsRejected {
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
