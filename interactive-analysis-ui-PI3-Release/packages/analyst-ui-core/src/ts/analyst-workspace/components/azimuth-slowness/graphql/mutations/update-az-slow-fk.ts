import gql from 'graphql-tag';
import { FkDataInput } from '../query';

/**
 * Input to the update detection mutation
 */
export interface UpdateAzSlowFkInput {
    sdHypothesisId: string;
    fkDataInput: FkDataInput;
}

export const updateAzSlowFromFkMutation = gql`
mutation updateAzSlowFromFk($sdHypothesisId: String!, $fkDataInput: FkDataInput!) {
    updateAzSlowFromFk(sdHypothesisId: $sdHypothesisId, fkDataInput: $fkDataInput) {
        id
    }
  }
`;
