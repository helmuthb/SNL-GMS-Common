/**
 * Mock backend HTTP services providing access to configuration data used by the API gateway.
 * If mock services are enabled in the configuration file, this module loads a test configuration
 * data set specified in the configuration file, and configures mock HTTP interfaces for API
 * gateway backend service calls.
 */
import { get } from 'lodash';
import * as path from 'path';
import * as config from 'config';
import { readJsonData } from '../util/file-parse';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { HttpMockWrapper } from '../util/http-wrapper';

/**
 * Encapsulates the query parameters used to retrieve a configuration item by key.
 */
interface ConfigKeyInput {
    key: string;
}

// Declare a backend data store for the mock config backend
let configDataStore: any;

/**
 * Retrieve a configuration value for the provided key.
 * @param input The query parameters used to retrieve configuration data by key.
 */
export function getConfigByKey(input: ConfigKeyInput): any {

    // Handle undefined input
    if (!input || !input.key) {
        throw new Error('Unable to retrieve configuration item for undefined key');
    }

    // Handle cases where the config data store has not been initialized
    handleUnitializedDataStore();

    let configItem: any;

    // Use lodash to retrieve and return the requested property from the configuration object
    try {
        configItem = get(configDataStore, input.key);
    } catch (error) {
        logger.error(error);
    }

    // Return undefined if an exception is thrown accessing the configuration object
    return configItem;
}

/**
 * Configure mock HTTP interfaces for a simulated set of station-related backend services.
 * @param httpMockWrapper The HTTP mock wrapper used to configure mock backend service interfaces
 */
export function initialize(httpMockWrapper: HttpMockWrapper) {

    logger.info('Initializing mock backend for config data');

    if (!httpMockWrapper) {
        throw new Error('Cannot initialize mock config services with undefined HTTP client');
    }

    // Load test data from the configured data set
    configDataStore = loadTestData();

    // Load the station backend service config settings
    const backendConfig = config.get('config.backend');

    // Create a mock wrapper from the HTTP client wrapper
    // Provide the configured response delay to simulate service latency
    // const mockWrapper: HttpMockWrapper =
    //     httpMockWrapper.createHttpMockWrapper({ delayResponse: backendConfig.mock.serviceDelayMs });

    // Configure mock service interfaces

    // Service: networksByIdList
    httpMockWrapper.onGet(backendConfig.services.configByKey.requestConfig.url, getConfigByKey);
}

/**
 * Load test data into the mock backend data store from the configured test data set.
 */
function loadTestData(): any {

    const dataPath = [config.get('testData.coiFormat.dataPath')];
    // If dataPath is a relative path, make it absolute by prefixing
    // it with the current working directory.
    if (!path.isAbsolute(dataPath[0])) {
        dataPath.unshift(process.cwd());
    }

    // Read the network definitions from the test data file set
    return readJsonData(dataPath.concat(config.get('testData.coiFormat.uiConfigFileName')).join(path.sep));
}

/**
 * Handle cases where the data store has not been initialized.
 */
function handleUnitializedDataStore() {
    // If the data store is uninitialized, throw an error
    if (!configDataStore) {
        throw new Error('Mock backend config data store has not been initialized');
    }
}
