import { PubSub } from 'graphql-subscriptions';
import * as config from 'config';

import * as model from './model';
import * as eventClient from './event-client';
import * as signalDetectionClient from '../signal-detection/signal-detection-client';
import { SignalDetectionEventAssociation } from '../signal-detection/model';
import * as workflowClient from '../workflow/workflow-client';
import { delayExecution } from '../util/delay-execution';

/**
 * Resolvers for the event API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// Load configuration settings
const settings = config.get('event');

const simulatedDelay = settings.resolvers.simulatedDelayMillis;

// GraphQL Resolvers
export const resolvers = {

    // Query resolvers
    Query: {
        eventHypothesesInTimeRange: async (_, { timeRange }) => delayExecution(
            () => eventClient.getEventHypothesesInTimeRange(timeRange),
            simulatedDelay),

        eventHypothesisById: async (_, { hypothesisId }) => delayExecution(
                () => eventClient.getEventHypothesisById(hypothesisId),
                simulatedDelay),

        eventHypothesesById: async (_, { hypothesisIds }) => delayExecution(
            () => eventClient.getEventHypothesesById(hypothesisIds),
            simulatedDelay),
    },

    // Mutation resolvers
    Mutation: {
        // Update an existing event (without creating a new event hypothesis)
        updateEvents: async (_, { eventIds, input }) => delayExecution(
            () => {
                // Update the event directly
                const events: model.Event[] =
                    eventClient.updateEvents(eventIds, input);
                // Publish the updated event hypothesis to the subscription channel
                pubsub.publish(settings.subscriptions.channels.eventsUpdated,
                               { eventsUpdated: events });
                return events;
            },
            simulatedDelay
        ),
        // Create a new event hypothesis
        createEventHypotheses: async (_, { eventIds, input }) => delayExecution(
            () => {
                // Update the event, creating  new hypothesis
                const hypotheses: model.EventHypothesis[] =
                    eventClient.createEventHypotheses(eventIds, input);
                // Publish the new hypothesis to the subscription channel
                pubsub.publish(settings.subscriptions.channels.eventHypothesesCreated,
                               { eventHypothesesCreated: hypotheses });
                return hypotheses;
            },
            simulatedDelay
        ),
        // Update an existing event hypothesis
        updateEventHypotheses: async (_, {hypothesisIds,  input }) => delayExecution(
            () => {
                // Update the event hypothesis directly
                const hypotheses: model.EventHypothesis[] =
                    eventClient.updateEventHypotheses(hypothesisIds, input);
                // Publish the updated event hypothesis to the subscription channel
                pubsub.publish(settings.subscriptions.channels.eventHypothesesUpdated,
                               { eventHypothesesUpdated: hypotheses });
                return hypotheses;
            },
            simulatedDelay
        )
    },

    // Subscription Resolvers
    Subscription: {
            // Subscription for events updated directly (without new event hypotheses)
            eventsUpdated: {
                subscribe: () => pubsub.asyncIterator(settings.subscriptions.channels.eventsUpdated)
            },
            // Subscription for newly-created event hypotheses resulting from event updates
            eventHypothesesCreated: {
                subscribe: () => pubsub.asyncIterator(settings.subscriptions.channels.eventHypothesesCreated)
            },
            // Subscription for an updated event hypothesis
            eventHypothesesUpdated: {
                subscribe: () => pubsub.asyncIterator(settings.subscriptions.channels.eventHypothesesUpdated)
            }
    },

    // Field resolvers for Event
    Event: {
        hypotheses: async (event: model.Event) => delayExecution(
            () => eventClient.getHypothesesForEvent(event.id), simulatedDelay),
        preferredHypothesisForStage: async (event: model.Event, { stageId }) => delayExecution(
            () => eventClient.getPreferredHypothesisForStage(event.id, stageId), simulatedDelay),
        preferredHypothesis: async (event: model.Event, { stageId }) => delayExecution(
            () => eventClient.getPreferredHypothesis(event.id), simulatedDelay),
        activeAnalysts: async (event: model.Event, { stageId }) => delayExecution(
            () => workflowClient.getAnalysts(event.activeAnalystUserNames), simulatedDelay)
    },

    // Field resolvers for PreferredEventHypothesis
    PreferredEventHypothesis: {
        processingStage: async (preferredHypothesis: model.PreferredEventHypothesis) => delayExecution(
            () => workflowClient.getStage(preferredHypothesis.processingStageId), simulatedDelay),
        hypothesis: async (preferredHypothesis: model.PreferredEventHypothesis) => delayExecution(
            () => eventClient.getEventHypothesisById(preferredHypothesis.hypothesisId), simulatedDelay)
    },

    // Field resolvers for EventHypothesis
    EventHypothesis: {
        event: async (hypothesis: model.EventHypothesis) => delayExecution(
            () => eventClient.getEventById(hypothesis.eventId), simulatedDelay),
        signalDetectionAssociations: async (hypothesis: model.EventHypothesis) => delayExecution(
            () => signalDetectionClient.getAssociationsByEventHypothesis(hypothesis.id), simulatedDelay),
    },

    // Field resolvers for SignalDetectionEventAssociation
    SignalDetectionEventAssociation: {
        signalDetectionHypothesis: async (association: SignalDetectionEventAssociation) => delayExecution(
            () => signalDetectionClient.getSignalDetectionHypothesisById(
                association.signalDetectionHypothesisId),
            simulatedDelay),
            eventHypothesis: async (association: SignalDetectionEventAssociation) => delayExecution(
            () => eventClient.getEventHypothesisById(association.eventHypothesisId), simulatedDelay)
    }
};
