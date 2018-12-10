import gql from 'graphql-tag';
import { Stage } from './types';

/**
 * Data structure for the workflow display
 */
export interface WorkflowData {
    stages?: Stage[];
}

export const workflowQuery = gql`
query workflowData{
    stages {
        id
        name
        activities {
            name
        }
        intervals {
            id
            startTime
            endTime
            status
            eventCount
            completedBy {
                userName
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
}
`;
