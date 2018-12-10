import { PubSub, withFilter } from 'graphql-subscriptions';
import * as config from 'config';
import { filter } from 'lodash';

import * as model from './model';
import { TimeRange } from '../common/model';
import { qcMaskProcessor } from './qc-mask-processor';

/**
 * Resolvers for the signal detection API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// Load configuration settings
const settings = config.get('qcMask');

// GraphQL Resolvers
export const resolvers = {

    // Query resolvers
    Query: {
        qcMasksByChannelId: async (_, { timeRange, channelIds }) => qcMaskProcessor.getQcMasks(timeRange, channelIds)
    },

    // Mutation: {
    //     updateQcMask: async (_, { qcMaskId, input }) => {
    //             const qcMask: Promise<model.QcMask> = qcMaskProcessor.updateQcMask(qcMaskId, input);
    //             // Publish the updated signal detection to the subscription channel
    //             pubsub.publish(settings.subscriptions.channels.qcMasksUpdated,
    //                            { qcMasksUpdated: qcMask });
    //             return qcMask;
    //     }
    // },

    // Field Resolvers
    QcMask: {
        currentVersion: async (qcMask: model.QcMask) => qcMask.qcMaskVersions[qcMask.qcMaskVersions.length - 1]

    },
    QcMaskVersion: {
        creationInfo: async (qcMaskVersion: model.QcMaskVersion) =>
                      qcMaskProcessor.getCreationInfo(qcMaskVersion.creationInfoId)
    },

    // Subscription resolvers
    Subscription: {
        // Subscription for an updated QcMasks
        qcMasksUpdated: {
            subscribe: withFilter(
                () => pubsub.asyncIterator(settings.subscriptions.channels.qcMasksUpdated),
                (payload, variables) => {
                    const timeRange: TimeRange = variables.timeRange;
                    const channelIds: string[] = variables.channelIds;

                    // Filter the updated masks down to those in the input time range
                    // if a time range is provided
                    const qcMasks: model.QcMask[] = timeRange
                        ? filter(payload.qcMasksUpdated, mask => {
                            const maskVersion = mask.qcMaskVersions[mask.qcMaskVersions.length - 1];
                            return maskVersion.startTime < timeRange.endTime
                                && maskVersion.endTime > timeRange.startTime;
                        })
                        : payload.qcMasksUpdated;

                    // Further filter the masks down to those matching an entry in the input channel ID list
                    // if a channel ID list is provided
                    payload.qcMasksUpdated = channelIds && channelIds.length > 0
                        ? filter(qcMasks, mask => channelIds.indexOf(mask.channelId) > -1)
                        : qcMasks;

                    // Only send the subscription callback if the filtered list of updated masks
                    // is not empty
                    return payload.qcMasksUpdated.length > 0;
                })
        }
    }

};
