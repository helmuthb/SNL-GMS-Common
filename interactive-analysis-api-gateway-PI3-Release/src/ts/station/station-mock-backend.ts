/**
 * Mock backend HTTP services providing access to processing station data. If mock services are enabled in the
 * configuration file, this module loads a test data set specified in the configuration file and configures
 * mock HTTP interfaces for the API gateway backend service calls.
 */
import { filter, find } from 'lodash';
import * as path from 'path';
import * as config from 'config';
import { readJsonData } from '../util/file-parse';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { HttpMockWrapper } from '../util/http-wrapper';

/**
 * Encapsulates the query parameters used to retrieve a collection of data by processing station ID list.
 */
interface StationIdListInput {
    stationIds: string[];
}

/**
 * Encapsulates the query parameters used to retrieve a collection of data by processing site ID list.
 */
interface SiteIdListInput {
    siteIds: string[];
}

/**
 * Encapsulates the query parameters used to retrieve a network by name.
 */
interface NetworkByNameQuery {
    name: string;
    time: string;
}

/**
 * Encapsulates backend data supporting retrieval by the API gateway.
 */
interface StationDataStore {
    networkData: any[];
    stationData: any[];
    siteData: any[];
    channelData: any[];
}

// Declare a backend data store for the mock station backend
let dataStore: StationDataStore;

/**
 * Retrieve a collection of processing station data valid at the provided effective time,
 * for the provided perocessing network name.
 * @param input The query parameters used to retrieve processing stations by processing
 * network name and effective time.
 */
export function getSwolNetworkByName(input: NetworkByNameQuery): any {
    // Handle undefined input
    if (!input || !input.name) {
        throw new Error('Unable to retrieve stations for undefined network name');
    }

    // Handle uninitialized data store
    handleUnitializedDataStore();

    // Ignore the effective time parameter in the mock backend. All data are assumed to be
    // valid for the current time

    // Find the network matching the provided name
    const network: any = find(dataStore.networkData, { name: input.name });

    // If no network match is found, throw an error
    if (!network) {
        throw new Error(`No processing network found matching name: ${input.name}`);
    }

    return {
        id: network.id,
        name: network.name,
        organization: network.organization,
        region: network.region,
        stations: filter(dataStore.stationData, station => network.stationIds.indexOf(station.id) > -1)
            .map(station => ({
                id: station.id,
                name: station.name,
                latitude: station.latitude,
                longitude: station.longitude,
                elevation: station.elevation,
                sites: filter(dataStore.siteData, site => station.siteIds.indexOf(site.id) > -1)
                    .map(site => ({
                        id: site.id,
                        name: site.name,
                        latitude: site.latitude,
                        longitude: site.longitude,
                        elevation: site.elevation,
                        channels: filter(dataStore.channelData, channel => site.channelIds.indexOf(channel.id) > -1)
                            .map(channel => ({
                                id: channel.id,
                                name: channel.name,
                                channelType: channel.channelType,
                                dataType: channel.dataType,
                                latitude: channel.latitude,
                                longitude: channel.longitude,
                                elevation: channel.elevation,
                                depth: channel.depth,
                                verticalAngle: channel.verticalAngle,
                                horizontalAngle: channel.horizontalAngle,
                                sampleRate: channel.sampleRate,
                                response: channel.response
                            })
                        )
                    })
                )
            })
        )
    };
}

/**
 * Retrieve a collection of pocessing sites matching the input list of processing station IDs.
 * @param input: The list of station IDs used to query for proessing sites
 */
export function getSitesByStationList(input: StationIdListInput) {
    // Handle undefined input
    if (!input || !input.stationIds) {
        throw new Error('Unable to retrieve sites for undefined list of station IDs');
    }

    // Handle uninitialized data store
    handleUnitializedDataStore();

    return filter(dataStore.siteData, site => input.stationIds.indexOf(site.stationId) > -1);
}

/**
 * Retrieve a collection of processing channels matching the input list of processing site IDs.
 * @param input: The list of site IDs used to query for processing channels
 */
export function getChannelsBySiteList(input: SiteIdListInput) {
    // Handle undefined input
    if (!input || !input.siteIds) {
        throw new Error('Unable to retrieve channels for undefined list of site IDs');
    }

    // Handle uninitialized data store
    handleUnitializedDataStore();

    return filter(dataStore.channelData, channel => input.siteIds.indexOf(channel.siteId) > -1);
}

/**
 * Configure mock HTTP interfaces for a simulated set of station-related backend services.
 * @param httpMockWrapper The HTTP mock wrapper used to configure mock backend service interfaces
 */
export function initialize(httpMockWrapper: HttpMockWrapper) {

    logger.info('Initializing mock backend for station data');

    if (!httpMockWrapper) {
        throw new Error('Cannot initialize mock station services with undefined HTTP mock wrapper');
    }

    // Load test data from the configured data set
    dataStore = loadTestData();

    // Load the station backend service config settings
    const backendConfig = config.get('station.backend');

    // Configure mock service interfaces
    // Service: stationsByNetwork
    httpMockWrapper.onGet(backendConfig.services.networkByName.requestConfig.url, getSwolNetworkByName);
}

/**
 * Load test data into the mock backend data store from the configured test data set.
 */
function loadTestData(): StationDataStore {

    const dataPath = [config.get('testData.coiFormat.dataPath')];
    // If dataPath is a relative path, make it absolute by prefixing
    // it with the current working directory.
    if (!path.isAbsolute(dataPath[0])) {
        dataPath.unshift(process.cwd());
    }

    logger.info(`Loading station test data from path: ${dataPath}`);

    // Read the processing network definitions from the configured test set
    const networkData =
        readJsonData(dataPath.concat(config.get('testData.coiFormat.networkFileName')).join(path.sep));

    // Read the processing station definitions from the configured test set
    const stationData =
        readJsonData(dataPath.concat(config.get('testData.coiFormat.stationFileName')).join(path.sep));

    // Read the processing site definitions from the configured test set
    const siteData =
        readJsonData(dataPath.concat(config.get('testData.coiFormat.siteFileName')).join(path.sep));

    // Read the processing channel definitions from the configured test set
    const channelData =
        readJsonData(dataPath.concat(config.get('testData.coiFormat.channelFileName')).join(path.sep));

    return {
        networkData,
        stationData,
        siteData,
        channelData
    };
}

/**
 * Handle cases where the data store has not been initialized.
 */
function handleUnitializedDataStore() {
    // If the data store is uninitialized, throw an error
    if (!dataStore) {
        throw new Error('Mock backend station processing data store has not been initialized');
    }
}
