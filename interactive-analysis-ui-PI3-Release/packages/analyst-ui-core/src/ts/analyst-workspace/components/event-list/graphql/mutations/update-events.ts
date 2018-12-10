import gql from 'graphql-tag';

/**
 * Input variables to update an event
 */
export interface UpdateEventsVariables {
    eventIds: string[];
    input: {
        creatorId: string;
        processingStageId: string;
        status?: 'ReadyForRefinement' | 'OpenForRefinement' | 'AwaitingReview' | 'Complete';
        preferredHypothesisId?: string;
        activeAnalystUserNames?: string[];
    };
}

export const updateEventsMutation = gql`
mutation updateEvents($eventIds: [String]!, $input: UpdateEventInput!) {
    updateEvents(eventIds: $eventIds, input: $input) {
        id
    }
}
`;
