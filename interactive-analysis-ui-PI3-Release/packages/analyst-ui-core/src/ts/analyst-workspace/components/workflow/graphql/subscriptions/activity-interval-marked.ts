import gql from 'graphql-tag';

/**
 * Data structure for activityIntervalMarked subscription callback
 */
export interface ActivityIntervalMarkedSubscription {
    activityIntervalMarked: {
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
        stageInterval: {
            id: string;
            startTime: number;
            endTime: number;
            status: string;
            eventCount: number;
            stage: {
                id: string;
            };
        };
    };
}

export const activityIntervalMarkedSubscription = gql`
subscription activityIntervalMarked{
    activityIntervalMarked {
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
        stageInterval {
            id
          	startTime
          	endTime
          	status
          	eventCount
            stage {
                id
            }
        }
    }
}
`;
