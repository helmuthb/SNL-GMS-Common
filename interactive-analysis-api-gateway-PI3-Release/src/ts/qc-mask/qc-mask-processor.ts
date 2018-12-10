
import * as model from './model';
import * as config from 'config';
import * as qcMaskMockBackend from './qc-mask-mock-backend';
import { TimeRange, CreatorType, CreationInfo } from '../common/model';
import { HttpClientWrapper } from '../util/http-wrapper';
import { gatewayLogger as logger } from '../log/gateway-logger';

// List of channels with masks. This shouldn't be necessary but the
// OSD errors out when we query each channel individually
const channelsWithMasks = [
    'f523a54a-7154-4949-8528-dc1442bd8dbb',
    'f5cf30cf-2190-40fb-b62c-795ac3066579',
    'ec34ea99-2d14-4602-812d-84078ca11e12',
    '17159e6e-680f-45f7-8923-11cbdaad4452',
    'bf231063-0eac-4056-a585-876caaf1653c',
    '97e833cc-f42c-4c9e-a736-523bfc3fafe8',
    '1d12d67d-b20b-439d-ace4-9e1c1184513d',
    '6df3c096-0178-48a0-9049-b58087276904',
    '7564658f-747b-4d6a-8f4e-c0323be4ffc1',
    '357e757a-6acc-4aab-ac7f-d960f8d31d65',
    '3c878800-1d06-4718-9c62-2ec0b80e2e61',
    'f9169f4d-bedf-4c58-94dc-b7d8b04d31c1',
    '9ebee82a-8955-4d8d-946e-eb2443330634',
    'f72ae9c4-1284-494b-b717-cd67f597cf14',
    '75b78e01-0dec-4dca-997c-ab2c74db56ad',
    'cafbbfbe-fa34-4200-bed4-7f38a4c7b5ab',
    '92680ae4-1090-4cd7-988c-e9fa5057ab90',
    '7deac738-a4a1-4199-81e5-34da974af120',
    '42dbf1ac-1a31-4019-be21-837f9e733b95',
    'ce300637-03f2-4a07-93ab-d470a129c344',
    '48b5f841-b232-4f94-95bf-b935117f74bd'
];

// TODO replace with a more robust caching solution
/**
 * Encapsulates QC mask data cached in memory
 */
interface QcMaskDataCache {
    qcMasks: model.QcMask[];
    creationInfo: CreationInfo[];
    noMaskChannelIds: string[];
}

/**
 * API gateway processor for QC mask data APIs. This class supports:
 * - data fetching & caching from the backend service interfaces
 * - mocking of backend service interfaces based on test configuration
 * - session management
 * - GraphQL query resolution from the user interface client
 */

class QcMaskProcessor {

    // Local configuration settings
    private settings: any;

    // HTTP client wrapper for communicationg with backend services
    private httpWrapper: HttpClientWrapper;

    // Local cache of data fetched from the backend
    private dataCache: QcMaskDataCache = {
        qcMasks: [],
        creationInfo: [],
        noMaskChannelIds: []
    };

    /**
     * Constructor - initialize the processor, loading settings and initializing
     * the HTTP client wrapper.
     */
    public constructor() {

        // Load configuration settings
        this.settings = config.get('qcMask');

        // Initialize an http client
        this.httpWrapper = new HttpClientWrapper();
    }

    /**
     * Initialize the QC mask processor, setting up a mock backend if configured to do so.
     */
    public initialize(): void {

        logger.info('Initializing the QcMask processor');

        // If service mocking is enabled, initialize the mock backend
        if (this.settings.backend.mock.enable) {
            qcMaskMockBackend.initialize(this.httpWrapper.createHttpMockWrapper(
                { delayResponse: this.settings.backend.mock.serviceDelayMs }));
        }

        // Rather than fetch all (like stations) we will only fetch as needed on
        // new open interval
    }

    /**
     * Retrieve QC Masks from the cache, filtering the results
     * down to those masks overlapping the input time range and matching an entry in
     * the input list of channel IDs.
     * 
     * @param timeRange The time range in which to retreive QC masks 
     * @param channelIds The list of channel IDs for which to retrieve QC masks
     */
    public async getQcMasks(timeRange: TimeRange, channelIds: string[]): Promise<model.QcMask[]> {
        const millSec = 1000;
        // Handle undefined input time range
        if (!timeRange) {
            throw new Error('Unable to retrieve cached QC masks for undefined time range');
        }

        // Handle undefined input channel ID list
        if (!channelIds || channelIds.length === 0) {
            throw new Error('Unable to retrieve cached QC masks for undefined channel ID list');
        }

        // Retrieve the requested masks via backend service calls
        // TODO update the cache with results when available

        // Retrieve the request configuration for the service call
        const requestConfig = this.settings.backend.services.masksByChannelIds.requestConfig;
        let qcMasks = [];

        // First call for cached masks then execute OSD queries for each channel id not found
        channelIds = this.findCachedMasks(qcMasks, channelIds);

        // Query OSD for qcMasks not found in the cache (channelIds returned in findCachedMasks)
        const promises = channelsWithMasks.map(async chanId => {
            if (channelIds.find(cid => cid === chanId)) {
                const query = {
                    'channel-id': chanId,
                    'start-time': new Date(timeRange.startTime * millSec).toISOString(),
                    'end-time': new Date(timeRange.endTime * millSec).toISOString()
                };
                const responseData = await this.httpWrapper.request(requestConfig, query);
                const procChannelIdKey = 'processingChannelId';
                if (responseData && responseData.length > 0) {
                    responseData.map(mask => {

                        // FIXME Add new mask to cache for now PI 4 need to rework how all this works
                        // If not in dataCache list add it
                        if (!this.dataCache.qcMasks.find(qcM => qcM === mask.id)) {
                            this.dataCache.qcMasks.push(mask);
                        }

                        // Transform results returned from test data/ OSD
                        // for translate processingChannelId to channelId in mask model
                        mask.channelId = mask.processingChannelId;
                        delete mask[procChannelIdKey];

                        // walk thru the mask to lookup the CreationInfo for each mask version
                        mask.qcMaskVersions.map(maskVersion => {
                            // Add CreationInfo to datacache if not already present
                            const creationInfo = this.dataCache.creationInfo.find(ci => ci.id ===
                                    maskVersion.creationInfoId);
                            if (!creationInfo) {
                                this.dataCache.creationInfo.
                                    push(this.createCreationInfo(maskVersion.creationInfoId));
                            }
                        });
                    });
                    // Add masks to list from each promise
                    qcMasks = qcMasks.concat(responseData);
                }  else {
                    this.dataCache.noMaskChannelIds.push(chanId);
                }
            }
        });
        await Promise.all(promises);
        return qcMasks;
    }

    public getCreationInfo(creationInfoId: string) {
        return this.dataCache.creationInfo.find(ci => ci.id === creationInfoId);
    }

    private createCreationInfo(creationInfoId: string) {
        return {
            id: creationInfoId,
            creationTime: 1274428800,
            creatorId: 'Spencer',
            creatorType: CreatorType.Analyst
        };
    }

    private findCachedMasks = (qcMasks: model.QcMask[], channelIds: string[]): string[] => {
        const osdChannelIds = [];
        channelIds.forEach(chanId => {
            const filteredMasks = this.dataCache.qcMasks.filter(qcM => qcM.channelId === chanId);
            // If found mask add it to list else add the chanId to OSD channel id query list
            if (filteredMasks && filteredMasks.length > 0) {
                qcMasks.push(...filteredMasks);
            } else if (this.dataCache.noMaskChannelIds.find(cid => cid === chanId) === undefined) {
                osdChannelIds.push(chanId);
            }
        });
        // console.log("!!!! findCachedMasks returning OSD channel ids length: " + osdChannelIds.length);
        return osdChannelIds;
    }
}

// Export an initialized instance of the processor
export const qcMaskProcessor: QcMaskProcessor = new QcMaskProcessor();
qcMaskProcessor.initialize();
