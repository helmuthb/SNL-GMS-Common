import gql from 'graphql-tag';

/**
 * Input to the update detection mutation
 */
export interface CreateFkInput {
    input: {
        stationId: string;
        frequencyBand: {
            minFrequencyHz: number;
            maxFrequencyHz: number;
        };
        windowParams: {
            windowType: string;
            leadSeconds: number;
            lengthSeconds: number;
        };
        contribChannelIds: string[];
    };
    signalDetectionHypothesisId: string;
}

export const createFkMutation = gql`
mutation createFk($input: NewFkInput!, $signalDetectionHypothesisId: String!) {
    createFk (input: $input, signalDetectionHypothesisId: $signalDetectionHypothesisId) {
      id
    }
  }
`;
