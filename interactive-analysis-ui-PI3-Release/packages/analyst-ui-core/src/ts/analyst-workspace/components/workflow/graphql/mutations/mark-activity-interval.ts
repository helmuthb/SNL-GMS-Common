import gql from 'graphql-tag';

export const markActivityIntervalMutation = gql`
mutation markActivityInterval($activityIntervalId: String!, $input: IntervalStatusInput!) {
    markActivityInterval(activityIntervalId: $activityIntervalId, input: $input) {
        id
    }
}
`;
