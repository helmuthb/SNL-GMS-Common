import { PubSub, withFilter } from 'graphql-subscriptions';
import * as config from 'config';
import { filter } from 'lodash';

import * as model from './model';
import * as signalDetectionClient from './signal-detection-client';
import * as fkClient from '../fk/fk-client';
import { stationProcessor } from '../station/station-processor';
import { delayExecution } from '../util/delay-execution';

/**
 * Resolvers for the signal detection API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// Load configuration settings
const settings = config.get('signalDetection');

const simulatedDelay = settings.resolvers.simulatedDelayMillis;

// GraphQL Resolvers
export const resolvers = {

    // Query resolvers
    Query: {
        signalDetectionHypothesesByStation:  async (_, { stationIds, timeRange }) => delayExecution(
            () => signalDetectionClient.getSignalDetectionHypothesesByStation(stationIds, timeRange),
            simulatedDelay),
        signalDetectionHypothesesById: async (_, { hypothesisIds }) => delayExecution(
            () => signalDetectionClient.getSignalDetectionHypothesesById(hypothesisIds),
            simulatedDelay),
        signalDetectionsByStation:  async (_, { stationIds, timeRange }) => delayExecution(
            () => signalDetectionClient.getSignalDetectionsByStation(stationIds, timeRange),
            simulatedDelay),
        signalDetectionsById: async (_, { detectionIds }) => delayExecution(
            () => signalDetectionClient.getSignalDetectionsById(detectionIds),
            simulatedDelay),
    },

    // Mutation resolvers
    Mutation: {
        // Create a new signal detection
        createDetection: async (_, { input }) => delayExecution(
            () => {
                // Create & store the hypothesis, detection & feature measurement
                const detection: model.SignalDetection =
                    signalDetectionClient.createDetection(input);
                // Publish the newly-created signal detection to the subscription channel
                pubsub.publish(settings.subscriptions.channels.detectionsCreated,
                               { detectionsCreated: [ detection ] });
                return detection;
            },
            simulatedDelay
        ),

        // Update an existing signal detection
        updateDetection: async (_, { detectionId, input }) => delayExecution(
            () => {
                // Update the hypothesis
                const detection: model.SignalDetection =
                    signalDetectionClient.updateDetection(detectionId, input);
                // Publish the updated signal detection to the subscription channel
                pubsub.publish(settings.subscriptions.channels.detectionUpdated,
                               { detectionUpdated: detection });
                return detection;
            },
            simulatedDelay
        ),
        // Update a collection of existing signal detections
        updateDetections: async (_, { detectionIds, input }) => delayExecution(
            () => {
                // Update the hypothesis
                const detections: model.SignalDetection[] =
                    signalDetectionClient.updateDetections(detectionIds, input);
                // Publish the updated signal detection to the subscription channel
                pubsub.publish(settings.subscriptions.channels.detectionsUpdated,
                               { detectionsUpdated: detections });
                return detections;
            },
            simulatedDelay
        ),
        // Reject a collection of existing signal detections
        rejectDetections: async (_, { detectionIds }) => delayExecution(
            () => {
                // Update the detections
                const detections: model.SignalDetection[] =
                    signalDetectionClient.rejectDetections(detectionIds);
                // Publish the rejected signal detections to the subscription channel
                pubsub.publish(settings.subscriptions.channels.detectionsRejected,
                               { detectionsRejected: detections });
                return detections;
            },
            simulatedDelay
        ),
        updateAzSlowFromFk: async (_, { sdHypothesisId, fkDataInput }) => delayExecution(
            () => {
                // Handle undefined FK input
                if (!fkDataInput) {
                    throw new Error('Unable to update azimuth/slowness measurement from undefined FK data input');
                }

                // Update the detection hypothesis' azimuth/slowness feature measurement
                // and update the FK collection
                const sdHypothesis: model.SignalDetectionHypothesis =
                    signalDetectionClient.updateAzSlowFromFk(sdHypothesisId, fkDataInput);

                // Publish the updated signal detection to the subscription channel
                pubsub.publish(settings.subscriptions.channels.sdHypothesisUpdated,
                               { sdHypothesisUpdated: sdHypothesis });
                return sdHypothesis;
            },
            simulatedDelay
        ),
    },

    // Subscription Resolvers
    Subscription: {
            // Subscription for newly-created signal detection hypotheses
            detectionsCreated: {
                // Set up the subscription to filter results down to those detections that overlap
                // a time range provided by the subscriber upon creating the subscription
                subscribe: withFilter(
                    () => pubsub.asyncIterator(settings.subscriptions.channels.detectionsCreated),
                    (payload, variables) => {
                        // If the subscriber has provided a time range (start and end times), then
                        // filter the array of available channel segment notifications
                        // down to those that overlap with the subscriber-provided time range
                        if (variables.startTime && variables.endTime) {
                            const detections: model.SignalDetection[] = payload.detectionsCreated;
                            payload.detectionsCreated =
                                filter(detections, detection => {
                                    const detectionTime =
                                        detection.currentHypothesis.arrivalTimeMeasurement.timeSec;
                                    return (detectionTime < variables.endTime &&
                                    detectionTime > variables.startTime);
                                });
                        }

                        // Only send the subscription callback if one or more of the available
                        // channel segment notifications matched the subscribed-for time range
                        return payload.detectionsCreated.length > 0;
                    })
            },
            // Subscription for an updated signal detection hypothesis
            detectionUpdated: {
                subscribe: () => pubsub.asyncIterator(settings.subscriptions.channels.detectionUpdated)
            },
            // Subscription for an updated collection of signal detection hypotheses
            detectionsUpdated: {
                subscribe: () => pubsub.asyncIterator(settings.subscriptions.channels.detectionsUpdated)
            },
            // Subscription for rejected collections of signal detection hypotheses
            detectionsRejected: {
                subscribe: () => pubsub.asyncIterator(settings.subscriptions.channels.detectionsRejected)
            },
            // Subscription for when hypothesis fk data changes
            sdHypothesisUpdated: {
                subscribe: () => pubsub.asyncIterator(settings.subscriptions.channels.sdHypothesisUpdated)
            }
    },

    // Field resolvers for SignalDetection
    SignalDetection: {
        station: async (signalDetection: model.SignalDetection) => delayExecution(
            () => stationProcessor.getStationById(signalDetection.stationId), simulatedDelay)
    },

    // Field resolvers for SignalDetectionHypothesis
    SignalDetectionHypothesis: {
        signalDetection: async (hypothesis: model.SignalDetectionHypothesis) => delayExecution(
            () => signalDetectionClient.getSignalDetectionById(hypothesis.signalDetectionId), simulatedDelay),
        signalDetectionAssociations: async (hypothesis: model.SignalDetectionHypothesis) => delayExecution(
            () => signalDetectionClient.getAssociationsByDetectionHypothesis(hypothesis.id), simulatedDelay),
    },

    // Field resolvers for FeatureMeasurement
/*     FeatureMeasurement: {
         // Special interface resolver to determine the implementing type based on field content
        __resolveType(obj, context, info) {
            switch (obj.featureType) {
                case model.FeatureType.ArrivalTime:
                    return 'TimeFeatureMeasurement';
                case model.FeatureType.AzimuthSlowness:
                    return 'AzSlownessFeatureMeasurement';
                default:
                    return undefined;
            }
        }
    }, */
    // Field resolvers for AzSlownessFeatureMeasurement
    AzSlownessFeatureMeasurement: {
        fkData: async (azSlowFm: model.AzSlownessFeatureMeasurement) => delayExecution(
            () => fkClient.getFkDataById(azSlowFm.fkDataId), simulatedDelay)
    }
};
