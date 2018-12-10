import { IntervalStatus } from '../../';

/**
 * input data to mark activity interval complete
 */
export interface IntervalStatusInput {
    status: IntervalStatus;
    analystUserName: string;
}

export { markActivityIntervalMutation } from './mark-activity-interval';
export { markStageIntervalMutation } from './mark-stage-interval';
