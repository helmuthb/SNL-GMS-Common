import { PubSub } from 'graphql-subscriptions';
import * as config from 'config';
import * as model from './model';
import * as fkDataClient from './fk-client';
import { delayExecution } from '../util/delay-execution';
import { stationProcessor } from '../station/station-processor';

/**
 * Resolvers for the signal detection API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
// export const pubsub = new PubSub();

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// Load configuration settings
const settings = config.get('fk');
const simulatedDelay = settings.resolvers.simulatedDelayMillis;

// GraphQL Resolvers
export const resolvers = {

    // Mutation resolvers
     Mutation: {
        // Create a new signal detection
        createFk: async (_, { input, signalDetectionHypothesisId }) => delayExecution(
            () => {
                // Create a new Fk based on the input
                const fkData: model.FkData =
                    fkDataClient.createFk(input, signalDetectionHypothesisId);
                // Publish the newly-created FK to the subscription channel
                pubsub.publish(settings.subscriptions.channels.fkCreated, { fkCreated: fkData });
                return fkData;
            },
            simulatedDelay
        )
    },
    // Subscription Resolvers
    Subscription: {
        // Subscription for newly-created FKs
        fkCreated: {
            // Set up the subscription for newly-create FKs
            subscribe: () => pubsub.asyncIterator(settings.subscriptions.channels.fkCreated)
        }
    },

    FkData: {
        contribChannels: async (fkData: model.FkData) => delayExecution(
            () => stationProcessor.getChannelsBySiteAndChannelNameString(fkData.contribChannelIds), simulatedDelay)
    }
};
