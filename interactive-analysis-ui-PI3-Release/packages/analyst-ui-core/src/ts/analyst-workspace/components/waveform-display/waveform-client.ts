import * as msgpack from 'msgpack-lite';

export enum ChannelSegmentType {
    Acquired = 'Acquired',
    Raw = 'Raw',
    DetectionBeam = 'DetectionBeam',
    FkBean = 'FkBean',
    Filter = 'Filter'
}

export interface ChannelSegment {
    id: string;
    segmentType: ChannelSegmentType;
    startTime: number;
    endTime: number;
    channelId: string;
    timeseriesList: Waveform[];
    featureMeasurementIds: string[];
}

/**
 * A waveform returned from the API-gateway
 */
export interface Waveform {
    id: string;
    startTime: number;
    endTime: number;
    sampleRate: number;
    sampleCount: number;

    waveformSamples: Float32Array;
}

/**
 * Retrieve messagepack-encoded waveforms from the api-gateway
 * @param start startime number
 * @param end endtime number
 * @param channelIds list of channelIds strings
 */

export const fetchWaveforms = async (start: number, end: number, channelIds: string[]) => {
    const resp = await window.fetch(`waveforms?start=${start}&end=${end}&channels=${channelIds.join(',')}`);
    const buffer = await resp.arrayBuffer();
    return msgpack.decode(new Uint8Array(buffer)) as ChannelSegment[];
};
