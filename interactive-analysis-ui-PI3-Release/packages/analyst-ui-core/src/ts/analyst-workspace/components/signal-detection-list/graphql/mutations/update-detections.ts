import gql from 'graphql-tag';

/**
 * Input to the update detection mutation
 */
export interface UpdateDetectionsInput {
    detectionIds: string[];
    input: {
        time?: number;
        timeUncertaintySec?: number;
        phase?: string;
    };
}

export const updateDetectionsMutation = gql`
mutation updateDetections($detectionIds: [String]!, $input: UpdateDetectionInput!) {
    updateDetections(detectionIds: $detectionIds, input: $input) {
        id
    }
  }
`;
