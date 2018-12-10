import gql from 'graphql-tag';

/**
 * Data structure for detectionUpdated subscription callback
 */
export interface DetectionsUpdatedSubscription {
    detectionsUpdated: {
        id: string;
        currentHypothesis: {
            id: string;
            phase: string;
            arrivalTimeMeasurement: {
                id: string;
                featureType: string;
                timeSec: number;
            };
        };
    }[];
}

export const detectionsUpdatedSubscription = gql`
subscription detectionsUpdated {
    detectionsUpdated {
        id
        currentHypothesis {
            id
            phase
            arrivalTimeMeasurement {
                id
                featureType
                timeSec
            }
        }
    }
}
`;
