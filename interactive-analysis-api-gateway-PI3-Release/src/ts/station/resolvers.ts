import * as model from './model';
import { stationProcessor } from './station-processor';
import * as eventClient from '../event/event-client';
import { EventHypothesis } from '../event/model';
import * as signalDetectionClient from '../signal-detection/signal-detection-client';
import { qcMaskProcessor } from '../qc-mask/qc-mask-processor';
import { PubSub } from 'graphql-subscriptions';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { DistanceToSource, DistanceSourceType } from '../common/model';

/**
 * Resolvers for the waveform API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// GraphQL Resolvers
logger.info('Creating GraphQL resolvers for the processing channel API...');
export const resolvers = {

    // Query resolvers
    Query: {
        // Retrieve the collection of processing stations that are part of the provided network
        stationsByNetwork: async (_, { networkName }) => stationProcessor.getStationsByNetworkName(networkName),

        // Retrieve the default set of stations configured to be included in the waveform dispay
        defaultStations: async () => stationProcessor.getDefaultStations(),

        // Retrieve a collection of processing channels corresponding to the provided list of IDs
        channelsById: async (_, { ids }) => stationProcessor.getChannelsById(ids)
    },

    // Field resolvers for Channel
    ProcessingChannel: {
        site: async (channel: model.ProcessingChannel) => stationProcessor.getSiteById(channel.siteId),

        qcMasks: async (channel: model.ProcessingChannel, { timeRange }) =>
            qcMaskProcessor.getQcMasks(timeRange, [channel.id])
    },

    // Field resolvers for Site
    ProcessingSite: {
        station: async (site: model.ProcessingSite) => stationProcessor.getStationById(site.stationId),
        channels: async (site: model.ProcessingSite) => stationProcessor.getChannelsBySite(site.id),
        // TODO Replace with processor call
        defaultChannel: async (site: model.ProcessingSite) =>
            stationProcessor.getDefaultChannelForStation(site.stationId)
    },

    // Field resolvers for Station
    ProcessingStation: {
        sites: async (station: model.ProcessingStation) => stationProcessor.getSitesByStation(station.id),
        // TODO Replace with processor call
        defaultChannel: async (station: model.ProcessingStation) =>
            stationProcessor.getDefaultChannelForStation(station.id),
        networks: async (station: model.ProcessingStation) => stationProcessor.getNetworksByIdList(station.networkIds),
        signalDetections: async (station: model.ProcessingStation, { timeRange }) =>
            signalDetectionClient.getSignalDetectionsByStation([station.id], timeRange),
        distanceToSource: async (station: model.ProcessingStation, { distanceToSourceInput }) => {
                const dTSInput: DistanceToSource = distanceToSourceInput;
                const dTSReturn: DistanceToSource = {
                    sourceId: dTSInput.sourceId,
                    sourceLocation: dTSInput.sourceLocation,
                    sourceType: dTSInput.sourceType,
                    distanceKm: undefined
                };

                // If no source location set and this is an source type of event
                // with the sourceId (eventHypId) set then look up the source loc
                if (!dTSReturn.sourceLocation && dTSReturn.sourceType === DistanceSourceType.Event &&
                    dTSReturn.sourceId) {
                    // Find the event
                    const event: EventHypothesis = eventClient.getEventHypothesisById(dTSInput.sourceId);
                    if (event) {
                        dTSReturn.sourceLocation = {
                            latDegrees: event.preferredLocationSolution.locationSolution.latDegrees,
                            lonDegrees: event.preferredLocationSolution.locationSolution.lonDegrees,
                            elevationKm: event.preferredLocationSolution.locationSolution.depthKm
                        };
                    }
                }
                if (!dTSReturn.sourceLocation) {
                    if (dTSReturn.sourceId) {
                        logger.info(`For source id ${dTSReturn.sourceId}` +
                            `source location is not set cannot compute distance for station ${station.id}.`);
                    }
                    return undefined;
                }
                dTSReturn.distanceKm = stationProcessor.getDistanceToSource(dTSReturn.sourceLocation, station);
                if (!dTSReturn.distanceKm) {
                    logger.info(`For source id ${dTSReturn.sourceId} Distance is undefined for station ${station.id}.`);
                }
                return dTSReturn;
            },
    },

    // Field resolvers for Network
    ProcessingNetwork: {
        stations: async (network: model.ProcessingNetwork) => stationProcessor.getStationsByNetworkId(network.id)
    }
};
