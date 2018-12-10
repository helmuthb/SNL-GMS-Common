/**
 * Mock backend HTTP services providing access to processing station data. If mock services are enabled in the
 * configuration file, this module loads a test data set specified in the configuration file and configures
 * mock HTTP interfaces for the API gateway backend service calls.
 */
import * as uuid4 from 'uuid/v4';
import { filter } from 'lodash';
import * as path from 'path';
import * as config from 'config';
import { readJsonData } from '../util/file-parse';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { HttpMockWrapper } from '../util/http-wrapper';

/**
 * Encapsulates the query parameters used to retrieve QC masks by time range and channel ID
 */
interface QcMaskByChannelIdInput {
    'start-time': string;
    'end-time': string;
    'channel-id': string;
}

/**
 * Encapsulates backend data supporting retrieval by the API gateway.
 */
interface QcMaskDataStore {
    qcMasks: any[];
}

// Declare a data store for the mock QC mask backend
let dataStore: QcMaskDataStore;

/**
 * Retrieve QC Masks from the mock data store. Filter the results
 * down to those masks overlapping the input time range and matching
 * the input channel ID.
 * 
 * @param timeRange The time range in which to retreive QC masks 
 * @param channelId The channel ID to retrieve QC masks for
 */
function getQcMasksByChannelId(input: QcMaskByChannelIdInput): any[] {
    const milliSec = 1000;
    // logger.info('Retrieving QC masks from the mock data store by channel ID and time');
    logger.debug(`QC mask query inputs - ${JSON.stringify(input, undefined, 2)}`);

    // Handle uninitialized data store
    handleUnitializedDataStore();

    // Handle undefined input
    if (!input) {
        throw new Error('Unable to retrieve QC masks for undefined input');
    }

    // Handle undefined input time range
    if (!input['start-time'] || !input['end-time']) {
        throw new Error('Unable to retrieve QC masks for undefined time range');
    }

    // Handle undefined input channel ID
    if (!input['channel-id']) {
        throw new Error('Unable to retrieve QC masks for undefined channel ID');
    }
    // Retrieve the masks from the data store with channel ID matching the
    // input ID, and time range overlapping the input range.
    const startTime: number = new Date(input['start-time']).valueOf() / milliSec;
    const endTime: number = new Date(input['end-time']).valueOf() / milliSec;
    return filter(dataStore.qcMasks, mask =>
        mask.processingChannelId === input['channel-id'] &&
        mask.currentVersion.startTime < endTime &&
        mask.currentVersion.endTime > startTime
    );
}

/**
 * Configure mock HTTP interfaces for a simulated set of QC mask backend services.
 * @param httpMockWrapper The HTTP mock wrapper used to configure mock backend service interfaces
 */
export function initialize(httpMockWrapper: HttpMockWrapper) {

    logger.info('Initializing mock backend for QC mask data');

    if (!httpMockWrapper) {
        throw new Error('Cannot initialize mock QC mask services with undefined HTTP mock wrapper');
    }

    // Load test data from the configured data set
    dataStore = loadTestData();

    // Load the QC mask backend service config settings
    const backendConfig = config.get('qcMask.backend');

    // Configure mock service interfaces
    // Service: masksByChannelId
    httpMockWrapper.onGet(backendConfig.services.masksByChannelIds.requestConfig.url, getQcMasksByChannelId);
}

/**
 * Load test data into the mock backend data store from the configured test data set.
 */
function loadTestData(): QcMaskDataStore {

    // Get test data configuration settings
    const testDataSettings = config.get('testData.coiFormat');

    // Read the necessary files into arrays of objects
    const dataPath = [testDataSettings.dataPath];

    // If dataPath is a relative path, make it absolute by prefixing
    // it with the current working directory.
    if (!path.isAbsolute(dataPath[0])) {
        dataPath.unshift(process.cwd());
    }

    logger.info(`Loading QC mask test data from path: ${dataPath}`);

    // Read the test data set from file; parse into object structure representing rows in the file
    const qcMasks =
        readJsonData(dataPath.concat(testDataSettings.qcMaskFileName).join(path.sep));

    qcMasks.forEach(mask => {
        // If the mask version list is not empty, set the
        // current mask version to the last entry in the list
        const maskVersionCount = mask.qcMaskVersions.length;
        if (maskVersionCount > 0) {
            mask.currentVersion = mask.qcMaskVersions[maskVersionCount - 1];
        }
        // For each mask read from the test data set, add a creation info object to each of its versions
        mask.qcMaskVersions.forEach(version => {
            version.creationInfoId =  uuid4().toString();
        });
    });

    return {
        qcMasks
    };
}

/**
 * Handle cases where the data store has not been initialized.
 */
function handleUnitializedDataStore() {
    // If the data store is uninitialized, throw an error
    if (!dataStore) {
        throw new Error('Mock backend QC mask processing data store has not been initialized');
    }
}
