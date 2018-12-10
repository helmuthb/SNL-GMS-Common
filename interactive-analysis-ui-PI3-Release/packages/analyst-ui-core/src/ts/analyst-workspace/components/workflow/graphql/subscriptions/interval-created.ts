import gql from 'graphql-tag';

/**
 * Data structure of stage interval created subscription callback
 */
export interface IntervalCreatedSubscription {
    intervalCreated: {
        id: string;
        startTime: number;
        endTime: number;
        stageIntervals: {
            id: string;
            stage: {
                id: string;
            };
            completedBy: {
                userName: string;
            };
            startTime: number;
            endTime: number;
            status: string;
            eventCount: number;
            activityIntervals: {
                id: string;
                activeAnalysts: {
                    userName: string;
                }[];
                activity: {
                    name: string;
                };
                completedBy: {
                    userName: string;
                };
                status: string;
                eventCount: number;
                timeStarted: number;
            }[];
        }[];
    };
}

export const intervalCreatedSubscription = gql`
subscription intervalCreated{
    intervalCreated {
        id
        startTime
        endTime
        stageIntervals {
            id
            stage {
                id
            }
            completedBy {
                userName
            }
            startTime
            endTime
            status
            eventCount
            activityIntervals {
                id
                activeAnalysts {
                    userName
                }
                activity {
                    name
                }
                completedBy {
                    userName
                }
                status
                eventCount
                timeStarted
            }
        }
    }
}
`;
