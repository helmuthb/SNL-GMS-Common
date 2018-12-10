import gql from 'graphql-tag';

/**
 * Input to the reject detection hypotheses mutation
 */
export interface RejectDetectionsInput {
    detectionIds: string[];
}

export const rejectDetections = gql`
mutation rejectDetections($detectionIds: [String]!) {
    rejectDetections(detectionIds: $detectionIds) {
        id
    }
}
`;
