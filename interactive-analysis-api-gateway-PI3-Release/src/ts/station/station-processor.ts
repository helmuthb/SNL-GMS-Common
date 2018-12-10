import { find, filter, merge } from 'lodash';
import * as geolib from 'geolib';
import * as model from './model';
import * as config from 'config';

import { configProcessor } from '../config/config-processor';
import * as stationMockBackend from './station-mock-backend';
import { Location, StationType, TimeRange } from '../common/model';
import { HttpClientWrapper } from '../util/http-wrapper';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { qcMaskProcessor } from '../qc-mask/qc-mask-processor';

// TODO replace with a more robust caching solution
/**
 * Encapsulates station-related data cached in memory
 */
interface StationDataCache {
    networks: model.ProcessingNetwork[];
    stations: model.ProcessingStation[];
    defaultStationInfo: model.DefaultStationInfo[];
    sites: model.ProcessingSite[];
    channels: model.ProcessingChannel[];
}

/**
 * API gateway processor for station-related data APIs. This class supports:
 * - data fetching & caching from the backend service interfaces
 * - mocking of backend service interfaces based on test configuration
 * - session management
 * - GraphQL query resolution from the user interface client
 */
class StationProcessor {

    // Local configuration settings
    private settings: any;

    // HTTP client wrapper for communicationg with backend services
    private httpWrapper: HttpClientWrapper;

    // Local cache of data fetched from the backend
    private stationDataCache: StationDataCache;
    /**
     * Constructor - initialize the processor, loading settings and initializing the HTTP client wrapper.
     */
    public constructor() {

        // Load configuration settings
        this.settings = config.get('station');

        // Initialize an http client
        this.httpWrapper = new HttpClientWrapper();
    }

    /**
     * Initialize the station processor, fetching station data from the backend.
     * This function sets up a mock backend if configured to do so.
     */
    public initialize(): void {

        logger.info('Initializing the station processor');

        // If service mocking is enabled, initialize the mock backend
        if (this.settings.backend.mock.enable) {
            stationMockBackend.initialize(this.httpWrapper.createHttpMockWrapper(
                { delayResponse: this.settings.backend.mock.serviceDelayMs }));
        }

        // Cache station-related data needed to support the interactive analysis UI
        this.getDefaultStations();
    }

    /**
     * Retrieve a collection of processing channels for the provided processing
     * site ID. If the provided site ID is undefined or does not match any
     * processing channel entries, this function returns undefined.
     * @param siteId the ID of the processing name to retrieve processing channels for
     */
    public getChannelsBySite(siteId: string): model.ProcessingChannel[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return filter(this.stationDataCache.channels, { siteId });
    }

    /**
     * Retrieve a processing channel= for the provided site and channel name.
     * If the provided site and channel names do not match any processing
     * channel entries, this function returns undefined.
     * @param siteName the name of the site associated with the processing channel to retrieve
     * @param channelName the name of the processing channel to retrieve
     */
    public getChannelBySiteAndChannelName(siteName: string, channelName: string): model.ProcessingChannel {
        // Throw and error if uninitialized
        this.handleUninitializedCache();

        return find(this.stationDataCache.channels,
                    channel => channel.siteName.toLowerCase() === siteName.toLowerCase()
                        && channel.name.toLowerCase() === channelName.toLowerCase());
    }

    public getChannelsBySiteAndChannelNameString(siteChannelNameStrings: string[]): model.ProcessingChannel[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();

        if (!siteChannelNameStrings || siteChannelNameStrings.length === 0) {
            return undefined;
        }

        return siteChannelNameStrings.map(nameString => {
            const names = nameString.split('/', 2);
            return this.getChannelBySiteAndChannelName(names[0], names[1]);
        });
    }

    /**
     * Retrieve the processing channel with the provided ID.
     * If the provided ID is undefined or does not match any processing
     * channel entries, the function returns undefined.
     * @param id The ID of the processing channel to retrieve
     */
    public getChannelById(id: string): model.ProcessingChannel {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return find(this.stationDataCache.channels, { id });
    }

    /**
     * Retrieve the processing channels matching the provided list of IDs.
     * If the provided list of IDs is undefined or does not match any processing
     * channel entries, the function returns undefined.
     * @param ids The list of IDs to retrieve processing channels for
     */
    public getChannelsById(ids: string[]): model.ProcessingChannel[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return ids.map(id => find(this.stationDataCache.channels, { id }));
    }

    /**
     * Retrieve the configured default list of processing stations to display
     * on the interactive analysis displays. If the default station configuration
     * is uninitialized, this function returns undefined.
     */
    public async getDefaultStations(): Promise<model.ProcessingStation[]> {
        if (!this.stationDataCache) {
            await this.fetchStationData(this.settings.defaultNetwork);
        }

        // Throw and error if uninitialized
        this.handleUninitializedCache();

        // Filter the cached station data based on the default station ID list
        return filter(this.stationDataCache.stations, station => this.stationDataCache.defaultStationInfo.map(
            defaultStation => defaultStation.stationId).indexOf(station.id) > -1);
    }

    /**
     * Retrieve the processing station with the provided ID.
     * If the provided ID is undefined or does not match any processing
     * station entries, the function returns undefined.
     * @param id The ID of the processing station to retrieve
     */
    public getStationById(id: string): model.ProcessingStation {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return find(this.stationDataCache.stations, { id });
    }

    /**
     * Retrieve the processing station with the provided name.
     * If the provided name is undefined or does not match any processing
     * station entries, the function returns undefined.
     * @param name The name of the processing station to retrieve
     */
    public getStationByName(name: string): model.ProcessingStation {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return find(this.stationDataCache.stations, { name });
    }

    /**
     * Retrieve the processing site for the provided ID.
     * If the provided ID is undefined or does not match any processing
     * site entries, the function returns undefined.
     * @param Id The id of the processing site to retrieve
     */
    public getSiteById(id: string): model.ProcessingSite {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return find(this.stationDataCache.sites, { id });
    }

    /**
     * Retrieve the processing sites for the provided processing station ID.
     * If the provided ID is undefined or does not match any processing
     * site entries, the function returns undefined.
     * @param stationId The processing station ID to retrieve processing sites for
     */
    public getSitesByStation(stationId: string): model.ProcessingSite[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return filter(this.stationDataCache.sites, { stationId });
    }

    /**
     * Retrieve the default processing channel for the processing station with the
     * provided ID. If the provided processing station ID is undefined or does not
     * match any default channel entries, the function returns undefined.
     * @param stationId The ID of the processing station to retrieve the default
     * processing channel for
     */
    public getDefaultChannelForStation(stationId: string): model.ProcessingChannel {
        // Throw and error if uninitialized
        this.handleUninitializedCache();

        const defaultInfo = find(this.stationDataCache.defaultStationInfo, { stationId });

        if (!defaultInfo) {
            throw new Error(`No default station info found for station with ID: ${stationId}`);
        }

        return find(this.stationDataCache.channels, { id : defaultInfo.channelId });

/*         return find(this.stationDataCache.channels, channel =>
            this.stationDataCache.defaultStationInfo.map(defaultStation =>
                defaultStation.channelId).indexOf(channel.id) > -1
        ); */
    }

    /**
     * Retrieve the processing networks for the provided list of IDs.
     * If the provided list of IDs is undefined or does not match any processing
     * network entries, the function returns undefined.
     * @param ids The list of IDs to retrieve processing networks for
     */
    public getNetworksByIdList(ids: string[]): model.ProcessingNetwork[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return filter(this.stationDataCache.networks, network => ids.indexOf(network.id) > -1);
    }

    /**
     * Retrieve the processing stations for the provided processing nework ID.
     * If the provided network ID is undefined or does not match any processing
     * station entries, the function returns undefined.
     * @param networkId The ID of the processing network to retrieve processing
     * stations for
     */
    public getStationsByNetworkId(networkId: string): model.ProcessingStation[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return filter(this.stationDataCache.stations, station => station.networkIds.indexOf(networkId) > -1);
    }

    /**
     * Retrieve the processing stations for the provided processing nework name.
     * If the provided network name is undefined or does not match any processing
     * station entries, the function returns undefined.
     * @param networkName The name of the processing network to retrieve processing
     * stations for
     */
    public getStationsByNetworkName(networkName: string): model.ProcessingStation[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();

        const network: model.ProcessingNetwork = find(this.stationDataCache.networks, { name: networkName});

        if (!network) {
            return undefined;
        }

        return filter(this.stationDataCache.stations, station => network.stationIds.indexOf(station.id) > -1);
    }

    /**
     * Calculate the distance in kilometers between a provided source location and processing station.
     * @param sourceLoc The source location for which to calculate distance to the provided station
     * @param station The station for which to calculate distance to the provided source location
     */
    public getDistanceToSource(sourceLoc: Location, station: model.ProcessingStation): number {
        const accuracy = 1000;
        const precision = 0;
        const KM = 1000;
        const dist: number = geolib.getDistance(
            { latitude: station.location.latDegrees, longitude: station.location.lonDegrees },
            { latitude: sourceLoc.latDegrees, longitude: sourceLoc.lonDegrees }, accuracy, precision
        );
        return dist / KM;
    }

    /**
     * Fetch station-related data from backend services for the provided network name.
     * This is an asynchronous function.
     * This function propagates errors from the underlying HTTP call.
     * Fetched data include processing networks, stations, sites, and channels.
     * @param networkName The name of the network to retrieve station-related data for 
     */
    private async fetchStationData(networkName: string): Promise<StationDataCache> {

        // TODO replace this with a call to fetch stations by a list of IDs
        logger.info(`Fetching processing station data for network with name: ${networkName}`);

        // Handle invalid input
        if (!networkName) {
            throw new Error('Cannot fetch processing station data for an undefined network name');
        }

        // Build the query to be encoded as query string parameters
        const query = {
            name: networkName,
            // TODO should effective time be passed in as a parameter?
            time: new Date().toISOString()
        };

        // Retrieve the request configuration for the service call
        const requestConfig = this.settings.backend.services.networkByName.requestConfig;

        // Call the service and process the response data
        const promise = this.httpWrapper.request(requestConfig, query)
            .then(responseData => this.processStationData(responseData))
            .then(() => this.retrieveQcMasks())
            .then(() => {
                logger.info(`Station data ${this.settings.backend.mock.enable ? 'mock' : 'OSD'} ` +
                                    `fetch complete`);
            })
            .catch(error => logger.error(error));
        await promise;
        return this.stationDataCache;
    }

    private retrieveQcMasks = (): Promise<any> => {
        // FIXME hack for now to load 24 hours of mask will fix in PI 4
        const timeRange: TimeRange = {
            startTime: 1274313600,
            endTime: 1274400000
        };
        const channelIds = this.stationDataCache.channels.map(chan => chan.id);
        return qcMaskProcessor.getQcMasks(timeRange, channelIds);
    }
    /**
     * Process station-related data response from the backend service call. Specifically,
     * parse the response JSON into model entities (processing networks, stations, sites & channels),
     * and store the parsed data in the cache
     * @param stationResponseData The JSON station data response received from a backend service to be processed
     */
    private processStationData(stationResponseData: any): void {
        // If the response data is valid, parse it into model entities and merge into the member cache
        if (stationResponseData) {
            // Initialize a new data cache for the parsed response data
            const stationDataCache: StationDataCache = {
                networks: [],
                stations: [],
                defaultStationInfo: [],
                sites: [],
                channels: []
            };

            // Parse the network from the response
            const network: model.ProcessingNetwork = {
                id: stationResponseData.id,
                name: stationResponseData.name,
                monitoringOrganization: stationResponseData.organization,
                stationIds: []
            };

            // Parse the list of stations from the response
            stationResponseData.stations.forEach(stationResponse => {
                const stationId: string = stationResponse.id;
                network.stationIds.push(stationId);
                const station: model.ProcessingStation = {
                    id: stationId,
                    name: stationResponse.name,
                    stationType: StationType.Invalid,
                    location: {
                        latDegrees: stationResponse.latitude,
                        lonDegrees: stationResponse.longitude,
                        elevationKm: stationResponse.elevation
                    },
                    siteIds: [],
                    networkIds: [network.id]
                };

                // For each station, parse the list of sites from the response
                stationResponse.sites.forEach(siteResponse => {
                    const siteId: string = siteResponse.id;
                    station.siteIds.push(siteId);
                    const site: model.ProcessingSite = {
                        id: siteId,
                        name: siteResponse.name,
                        location: {
                            latDegrees: siteResponse.latitude,
                            lonDegrees: siteResponse.longitude,
                            elevationKm: siteResponse.elevation
                        },
                        stationId,
                        channelIds: []
                    };
                    // For each site, parse the list of channels from the response
                    siteResponse.channels.forEach(channelResponse => {
                        const channelId: string = channelResponse.id;
                        site.channelIds.push(channelId);
                        const channel: model.ProcessingChannel = {
                            id: channelId,
                            name: channelResponse.name,
                            channelType: channelResponse.channelType,
                            // TODO backend channel has a location, rather than a location code;
                            // update the model and populate
                            locationCode: 'Invalid',
                            siteId,
                            siteName: site.name,
                            verticalAngleDegrees: channelResponse.verticalAngle,
                            horizontalAngleDegrees: channelResponse.horizontalAngle,
                            nominalSampleRateHz: channelResponse.sampleRate
                        };
                        stationDataCache.channels.push(channel);
                    });
                    stationDataCache.sites.push(site);
                });
                stationDataCache.stations.push(station);
            });
            stationDataCache.networks.push(network);

            // If the member station data cache is uninitialized, set it to the parsed response data;
            // otherwise merge the parsed response data into the existing member cache instance
            this.stationDataCache = this.stationDataCache
                ? merge(this.stationDataCache, stationDataCache)
                : stationDataCache;

            // Retrieve default station configuration from the config processor
            const defaultStationConfig = configProcessor.getConfigByKey('defaultStations');
            // TODO, remove site/channel name mapping logic to channel ID once channel IDs
            // are stable in the OSD.
            // For now, the configuration includes a default station ID, default site name and
            // default channel name for each entry, map this into a default station ID and channel ID
            // to store in the cache. These are needed to support GraphQL resolvers for default stations
            // and default channels for each station.
            defaultStationConfig.forEach(defaultConfig => {

                // Find the cached station in in the default config
                const defaultStation = find(stationDataCache.stations, { name : defaultConfig.stationName });

                // If the station is found, find the default channel for that station
                // (by matching site and channel name)
                if (defaultStation) {
                    const defaultChannel = find(stationDataCache.channels, cachedChannel =>
                        cachedChannel.siteName === defaultConfig.defaultChannel.siteName
                        && cachedChannel.name === defaultConfig.defaultChannel.channelName);

                        // If the channel is found, store an entry in the cache with the default
                        // station ID and associated default channel ID
                    if (defaultChannel) {
                        stationDataCache.defaultStationInfo.push({
                            stationId: defaultStation.id,
                            channelId: defaultChannel.id
                        });
                    }
                }
            });

            logger.debug('Updated station data cache following fetch from the backend:',
                         `\n${JSON.stringify(this.stationDataCache, undefined, 2)}`);
        }
    }

    /**
     * Handle cases where the data cache has not yet been initialized.
     */
    private handleUninitializedCache(): void {
        if (!this.stationDataCache) {
            logger.error('Attempt to access uninitialized station cache.');
            throw new Error('The station processor has not been initialized');
        }
    }
}

// Export an initialized instance of the processor
export const stationProcessor: StationProcessor = new StationProcessor();
stationProcessor.initialize();
