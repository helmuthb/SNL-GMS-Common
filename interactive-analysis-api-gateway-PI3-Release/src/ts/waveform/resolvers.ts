import { PubSub, withFilter } from 'graphql-subscriptions';
import * as config from 'config';
import { filter } from 'lodash';

import * as model from './model';
import { TimeRange } from '../common/model';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { delayExecution } from '../util/delay-execution';
import * as waveformClient from './waveform-client';
import { stationProcessor } from '../station/station-processor';

/**
 * Resolvers for the waveform API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// Load subscription configuration settings
const subConfig = config.get('waveform.subscriptions');

// Load delay simulation from config
const simulatedDelay = config.get('waveform.resolvers.simulatedDelayMillis');

// GraphQL Resolvers
logger.info('Creating GraphQL resolvers for the waveform API...');
export const resolvers = {

    // Query resolvers
    Query: {
        waveformChannelSegments: async (_, { timeRange, channelIds }) => delayExecution(
            () => waveformClient.getWaveformSegmentsByChannel(timeRange.startTime, timeRange.endTime, channelIds),
            simulatedDelay),
    },
    // Subscription Resolvers
    Subscription: {
        waveformChannelSegmentsAdded: {
            // Set up the subscription to filter results down to those channel segments that overlap
            // a time range provided by the subscriber upon creating the subscription
            subscribe: withFilter(
                () => pubsub.asyncIterator(subConfig.channels.waveformChannelSegmentsAdded),
                (payload, variables) => {

                    // If the subscriber has provided subscription input parameters
                    // (e.g. time range, channel IDs) filter the array of channel
                    // segments down to those that in the time range & matching an entry
                    // in the channel IDs
                    const timeRange: TimeRange = variables.timeRange;
                    const channelIds: string[] = variables.channelIds;

                    if (timeRange && channelIds) {
                        const segmentsAdded: model.ChannelSegment[] =
                            payload.waveformChannelSegmentsAdded;
                        payload.waveformChannelSegmentsAdded =
                            filter(segmentsAdded, segment =>
                                    segment.startTime < timeRange.endTime &&
                                    segment.endTime > timeRange.startTime &&
                                    channelIds.indexOf(segment.channelId) > -1);
                    }

                    // Only send the subscription callback if one or more of the available
                    // channel segment notifications matched the subscribed-for time range
                    return payload.waveformChannelSegmentsAdded.length > 0;
                }
            )
        }
    },
    // Field resolvers for Waveform Channel Segment
    ChannelSegment: {
        channel: async (channelSegment: model.ChannelSegment) => delayExecution(
            () => stationProcessor.getChannelById(channelSegment.channelId), simulatedDelay)
    }
};
