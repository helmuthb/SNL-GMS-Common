/**
 * Resolvers for the Workflow User Interface API
 */
import { PubSub } from 'graphql-subscriptions';
import * as config from 'config';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { delayExecution } from '../util/delay-execution';
import * as model from './model';
import * as workflowClient from './workflow-client';

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// Load subscription configuration settings
const subConfig = config.get('workflow.subscriptions');
const simulatedDelay = config.get('workflow.resolvers.simulatedDelayMillis');

/**
 * Publish newly-created ProcessingStageIntervals to the GraphQL subscription channel
 * and store them in the canned data list.
 * @param stageInterval 
 */
export function stageIntervalCreated(stageInterval: model.ProcessingStageInterval) {

    logger.info(`Publishing newly-created ProcessingStageInterval with ID: ${stageInterval.id} to GraphQL subscribers`);

    // Publish the new stage interval to the subscription channel
    pubsub.publish(subConfig.channels.stageIntervalCreated, { stageIntervalCreated: stageInterval });
}

/**
 * Publish newly-created ProcessingIntervals to the GraphQL subscription channel
 * and store them in the canned data list.
 * @param interval 
 */
export function intervalCreated(interval: model.ProcessingInterval) {

    logger.info(`Publishing newly-created ProcessingInterval with ID: ${interval.id} to GraphQL subscribers`);

    // Publish the new interval to the subscription channel
    pubsub.publish(subConfig.channels.intervalCreated, { intervalCreated: interval });
}

// GraphQL Resolvers
logger.info('Creating GraphQL resolvers for the workflow API...');
export const resolvers = {

    // Query resolvers
    Query: {
        analysts: async () => delayExecution(() => workflowClient.getAllAnalysts(), simulatedDelay),
        stages: async () => delayExecution(() => workflowClient.getStages(), simulatedDelay),
        stage: async (_, { id }) => delayExecution(() => workflowClient.getStage(id), simulatedDelay),
        intervalsInRange: async (_, { timeRange }) => delayExecution(
            () => workflowClient.getIntervalsInRange(timeRange.startTime, timeRange.endTime),
            simulatedDelay),
        interval: async (_, { id }) => delayExecution(
            () => workflowClient.getInterval(id),
            simulatedDelay),
        stageIntervals: async () => delayExecution(() => workflowClient.getStageIntervals(), simulatedDelay),
        stageInterval: async (_, { id }) => delayExecution(
            () => workflowClient.getStageInterval(id),
            simulatedDelay),
        activities: async () => delayExecution(() => workflowClient.getActivities(), simulatedDelay),
        activity: async (_, { id }) => delayExecution(
            () => workflowClient.getActivity(id),
            simulatedDelay),
        activityIntervals: async () => delayExecution(
            () => workflowClient.getActivityIntervals(),
            simulatedDelay),
        activityInterval: async (_, { id }) => delayExecution(
            () => workflowClient.getActivityInterval(id),
            simulatedDelay),
        stageIntervalsInRange: async (_, { timeRange }) => delayExecution(
            () => workflowClient.getStageIntervalsInRange(timeRange.startTime, timeRange.endTime),
            simulatedDelay)
    },

    // Mutation resolvers
    Mutation: {
        // Mark the processing stage interval, updating the status
        markStageInterval: async (_, { stageIntervalId, input }) => delayExecution(
            () =>  {
                // Apply the marking input to the processing stage with the provided ID
                const stageInterval = workflowClient.markStageInterval(stageIntervalId, input);
                // Publish the updated stage interval to the subscription channel
                pubsub.publish(subConfig.channels.stageIntervalMarked, { stageIntervalMarked: stageInterval });
                return stageInterval;
            },
            simulatedDelay),

        // Mark the processing activity interval, updating the status
        markActivityInterval: async (_, { activityIntervalId, input }) => delayExecution(
            () => {
                // Apply the marking input to the processing activity with the provided ID
                const activityInterval = workflowClient.markActivityInterval(activityIntervalId, input);
                // Publish the updated stage interval to the subscription channel
                pubsub.publish(subConfig.channels.activityIntervalMarked, { activityIntervalMarked: activityInterval });
                return activityInterval;
            },
            simulatedDelay),
    },

    // Subscription Resolvers
    Subscription: {
        stageIntervalMarked: {
            subscribe: () => pubsub.asyncIterator(subConfig.channels.stageIntervalMarked),
        },
        activityIntervalMarked: {
            subscribe: () => pubsub.asyncIterator(subConfig.channels.activityIntervalMarked),
        },
        stageIntervalCreated: {
            subscribe: () => pubsub.asyncIterator(subConfig.channels.stageIntervalCreated),
        },
        intervalCreated: {
            subscribe: () => pubsub.asyncIterator(subConfig.channels.intervalCreated),
        },
    },

    // Field resolvers for ProcessingStage
    ProcessingStage: {
        activities: async (stage: model.ProcessingStage) => delayExecution(
            () => workflowClient.getActivitiesByStage(stage.id),
            simulatedDelay),
        // Field intervals accepts an optional TimeRange parameter with startTime and endTime
        // date objects. If the parameter is provided, filter the stage intervals based on the
        // TimeRange bounds.
        intervals: async (stage: model.ProcessingStage, { timeRange }) => delayExecution(
            () => workflowClient.getIntervalsByStage(stage.id, timeRange),
            simulatedDelay)
    },

    // Field resolvers for ProcessingActivity
    ProcessingActivity: {
        stage: async (activity: model.ProcessingActivity) =>
            delayExecution(() => workflowClient.getStage(activity.stageId), simulatedDelay),
        intervals: async (activity: model.ProcessingActivity) =>
            delayExecution(() => workflowClient.getIntervalsByActivity(activity.id), simulatedDelay)
    },

    // Field resolvers for ProcessingInterval
    ProcessingInterval: {
        stageIntervals: async (interval: model.ProcessingInterval) =>
            delayExecution(() => workflowClient.getStageIntervalsByInterval(interval.id), simulatedDelay)
    },

    // Field resolvers for ProcessingStageInterval
    ProcessingStageInterval: {
        activityIntervals: async (stageInterval: model.ProcessingStageInterval) =>
            delayExecution(
                () => workflowClient.getActivityIntervalsByStageInterval(stageInterval.id),
                simulatedDelay),
        stage: async (stageInterval: model.ProcessingStageInterval) =>
            delayExecution(() => workflowClient.getStage(stageInterval.stageId), simulatedDelay),
        interval: async (stageInterval: model.ProcessingStageInterval) =>
            delayExecution(() => workflowClient.getInterval(stageInterval.intervalId), simulatedDelay),
        completedBy: async (stageInterval: model.ProcessingStageInterval) =>
            delayExecution(
                () => workflowClient.getAnalyst(stageInterval.completedByUserName),
                simulatedDelay)
    },

    // Field resolvers for ProcessingActivityInterval
    ProcessingActivityInterval: {
        stageInterval: async (activityInterval: model.ProcessingActivityInterval) =>
            delayExecution(
                () => workflowClient.getStageInterval(activityInterval.stageIntervalId),
                simulatedDelay),
        activity: async (activityInterval: model.ProcessingActivityInterval) =>
            delayExecution(() => workflowClient.getActivity(activityInterval.activityId), simulatedDelay),
        activeAnalysts: async (activityInterval: model.ProcessingActivityInterval) =>
            delayExecution(
                () => workflowClient.getAnalysts(activityInterval.activeAnalystUserNames),
                simulatedDelay),
        completedBy: async (activityInterval: model.ProcessingActivityInterval) =>
            delayExecution(
                () => workflowClient.getAnalyst(activityInterval.completedByUserName),
                simulatedDelay)
    },
};
