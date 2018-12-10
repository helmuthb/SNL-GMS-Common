import gql from 'graphql-tag';

/**
 * Data structure for stageIntervalMarked subscription callback
 */
export interface StageIntervalMarkedSubscription {
    stageIntervalMarked: {
        id: string;
        startTime: number;
        endTime: number;
        eventCount: number;
        status: string;
        completedBy: {
            userName: string;
        };
        stage: {
            id: string;
        };
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
    };
}

export const stageIntervalMarkedSubscription = gql`
subscription {
    stageIntervalMarked {
        id
        startTime
        endTime
        eventCount
        status
        completedBy {
            userName
        }
        stage {
            id
        }
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
`;
