import gql from 'graphql-tag';

/**
 * Input to the create detection mutation
 */
export interface CreateDetectionInput {
    stationId: string;
    phase: string;
    time: number; // epoch time seconds
    timeUncertaintySec: number;
}

export const createDetectionMutation = gql`
mutation createDetection($input: NewDetectionInput!) {
    createDetection(input: $input) {
        id
    }
}
`;
