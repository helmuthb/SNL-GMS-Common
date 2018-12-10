import gql from 'graphql-tag';

/**
 * Data structure for eventUpdated subscription callback
 */
export interface EventUpdatedSubscription {
    eventsUpdated: {
        id: string;
        status: string;
        activeAnalysts: {
            userName: string;
        }[];
    }[];
}

export const eventUpdatedSubscription = gql`
subscription {
    eventsUpdated {
        id
        status
        activeAnalysts {
            userName
        }
    }
}
`;
