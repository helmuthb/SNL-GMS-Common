import { ChannelSegment } from './waveform-client';

interface Cache {
    [channelId: string]: CacheEntry;
}

/**
 * Encapsulates an entry in the waveform data cache
 */
export interface CacheEntry {
    sampleRate: number;
    dataSegments: {
        data: Float32Array;
        startTimeSecs: number;
    }[];
}

/**
 * Provides an in-memory cache for waveform data used in rendering the waveform display
 */
export class WfDataCache {
    private cache: Cache = {};

    public getChannelIds = () =>  Object.keys(this.cache);

    /**
     * Retrieve the cache entry associated with the provided channel ID. This method returns
     * undefined if no cache entry exists for the provided channel ID.
     * @param channelId The channel ID associated with the cache entry to retrieve
     */
    public get(channelId: string): CacheEntry {
        return this.cache[channelId];
    }

    /**
     * Insert the provided entry in the cache replacing the existing entry
     * if one exists
     * @param channelId The channel ID used as the key for the entry in the cache 
     * @param value The entry to insert into the cache associated to the provided channel ID
     */
    public set(channelId: string, value: CacheEntry): CacheEntry {
        this.cache[channelId] = value;
        return value;
    }

    /**
     * Updates the cache from the provided list of ChannelSegments, adding
     * new cache entries for channel IDs not already in the cache, and
     * merging in timeseries data into existing cache entries where they exist.
     * If the overwrite parameter is set to true, this method will replace
     * existing cache entries associated with the channel IDs, rather than merging
     * in the new timeseries data.
     * @param channelSegments The list of ChannelSegments from which to update
     * the cache
     * @param overwrite Flag indicating whether to replace the cache entries
     * if they exist
     */
    public updateFromChannelSegments(channelSegments: ChannelSegment[], overwrite: boolean) {

        // Merge each provided channel segment in to the cache
        channelSegments.forEach(channelSegment => {
            let existingData = this.get(channelSegment.channelId);
            // if the cache entry exists but it set to undefined, initialize it first
            if (!existingData || overwrite) {
                const entry: CacheEntry = {
                    sampleRate: channelSegment.timeseriesList[0].sampleRate,
                    dataSegments: []
                };
                existingData = this.set(channelSegment.channelId, entry);
            }
            // add the data segment to the cache entry for this channel
            channelSegment.timeseriesList.forEach(timeSeries => {
                existingData.dataSegments.push({
                    data: timeSeries.waveformSamples,
                    startTimeSecs: timeSeries.startTime
                });
            });
            existingData.dataSegments = [...existingData.dataSegments];
        });
    }

    /**
     * clear a specific channelId from the cache, or clear the whole cache
     * @param channelId The channel ID to clear the cache entry for
     */
    public clear(channelId?: string) {
        if (channelId) {
            // tslint:disable-next-line:no-dynamic-delete
            delete this.cache[channelId];
            this.cache[channelId] = undefined;
        } else {
            delete this.cache;
            this.cache = {};
        }
    }
}
