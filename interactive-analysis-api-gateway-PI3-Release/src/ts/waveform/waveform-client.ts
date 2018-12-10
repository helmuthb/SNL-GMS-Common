import * as path from 'path';
import * as uuid4 from 'uuid/v4';
import * as model from './model';
import { TimeSeries, Waveform, CreatorType } from '../common/model';
import { uniq, find, filter } from 'lodash';
import * as config from 'config';
import * as express from 'express';
import * as msgpack from 'msgpack-lite';

import { gatewayLogger as logger } from '../log/gateway-logger';
import { readJsonData } from '../util/file-parse';
import { pubsub } from './resolvers';
import * as fs from 'fs';

const EVENT_AMPLITUDE = 1;
const NOISE_AMPLITUDE = 0.1;
const CONVERSION_WEIGHT_1000 = 1000;

logger.info('Initializing the waveform service');

// Retrieve the waveform data path
const dataPath = [config.get('testData.coiFormat.dataPath')];
// If dataPath is a relative path, make it absolute by prefixing
// it with the current working directory.
if (!path.isAbsolute(dataPath[0])) {
    dataPath.unshift(process.cwd());
}

// Retrieve the late-arriving waveform data path
const lateDataPath = [config.get('testData.coiFormat.lateData.dataPath')];
// If lateDataPath is a relative path, make it absolute by prefixing
// it with the current working directory.
if (!path.isAbsolute(lateDataPath[0])) {
    lateDataPath.unshift(process.cwd());
}

// Read the channel segment data from the test set, including late-arriving channel segments
const waveformChannelSegments: model.ChannelSegment[] =
    readJsonData(dataPath.concat(config.get('testData.coiFormat.channelSegmentFileName')).join(path.sep));
const lateWaveformChannelSegments: model.ChannelSegment[] =
    readJsonData(lateDataPath.concat(config.get('testData.coiFormat.lateData.channelSegmentFileName')).join(path.sep));

// const waveformChannelSegments: model.ChannelSegment[]  = loadChannelSegments(sourceData.uebWfdisc);
// const lateWaveformChannelSegments: model.ChannelSegment[] = loadChannelSegments(sourceData.uebWfdiscLate);

// Delay between release of late-arriving channel segments, read from configuration
const lateDataDelayMillis = config.get('testData.lateData.delayMillis');
// Delay before sending the first late-arriving signal detections
const preStartDelayMillis = config.get('testData.lateData.preStartDelay');
const lateDataPubSubChannel = config.get('waveform.subscriptions.channels.waveformChannelSegmentsAdded');

// The number of late-arriving channel segments to send after each delay
const channelSegmentCount = config.get('testData.lateData.channelSegmentCount');

// ID of late-arriving data timer, initially undefined
let lateDataTimerId;

/**
 * Loads a collection of WaveformChannelSegments from the provided collection of rows read from a wfdisc file.
 * @param wfdiscEntries A collection of wfdisc file entries parsed from a test data wfdisc file.
 */
/* function loadChannelSegments(wfdiscEntries: any) {

    // Create a list of waveform segments and associated waveforms from the UEB data set
    // wfdisc file. At this point, the samples field of the waveforms will be empty
    // Waveform samples will be added dynamically as needed by the graphql resolver in order
    // to avoid loading all of the waveform samples for all segments into memory
    const channelSegments: model.ChannelSegment[] = [];

    // For each wfdisc entry,
    wfdiscEntries.forEach(row => {

        // Create a waveform
        const timeSeries: Waveform = {
            id: uuid4().toString(),
            startTime: Number(row.TIME),
            endTime: Number(row.ENDTIME),
            waveformSamples: new Float32Array(0),
            sampleRate: Number(row.SAMPRATE),
            sampleCount: Number(row.NSAMP),
            calibration: {
                factor: Number(row.CALIB),
                factorError: 0,
                period: 0,
                timeShift: 0
            },
            fileOffset: Number(row.FOFF),
            fileName: row.DFILE
        };

        // Try to retrieve the segment with the channel ID matching the wfdisc row channel ID
        const waveformSegment = find(channelSegments, { channelId: row.CHANID });

        // If the segment already exists, add the new waveform to its timeseries list
        // and update the segment time range to account for the new waveform
        if (waveformSegment) {
            addTimeSeriesToSegment(waveformSegment, timeSeries);

        // Otherwise, create a new segment with the timeseries as the first entry in the list
        } else {
            channelSegments.push(
                {
                    id: uuid4().toString(),
                    segmentType: model.ChannelSegmentType.Raw,
                    startTime: Number(row.TIME),
                    endTime: Number(row.ENDTIME),
                    channelId: `${row.STA}/${row.CHAN}`,
                    timeseriesList: [timeSeries],
                    featureMeasurementIds: [],
                    creationInfo: {
                        id: uuid4.toString(),
                        creationTime: new Date().getTime() / CONVERSION_WEIGHT_1000,
                        creatorId: 'Auto',
                        creatorType: CreatorType.System
                    }
                }
            );
        }
    });

    return channelSegments;
} */

/**
 * Remove the next late-arriving channel segment from the late segment list if the list
 * isn't empty, and add it to the channel segment data set. Publish to the graphql subscription
 * channel notifying clients that additional channel segment data are available.
 * The late channel segment is added to the data set either by merging its timeseries
 * into an existing segment if one exists, or adding the late segment to the data set
 * directly.
 */
function addLateChannelSegment() {

    // Clear the late data timer if it is set
    if (lateDataTimerId) {
        clearInterval(lateDataTimerId);
    }

    // If there are any more late-arriving waveform channel segments to add to the data set,
    // add them and publish a notification
    if (lateWaveformChannelSegments.length > 0) {

        // Remove a segment from the list of late segments
        const lateSegments = lateWaveformChannelSegments.splice(0, channelSegmentCount);

        lateSegments.forEach(lateSegment => {
            // Retrieve the original (non-late) segment for the channel, if one exists
            const existingSegment = find(waveformChannelSegments, { channelId : lateSegment.channelId });

            // If the original segment exits, merge the late segments timeseries into the original
            if (existingSegment) {
                lateSegment.timeseriesList.forEach(ts => addTimeSeriesToSegment(existingSegment, ts));
            // Otherwise, add the late segment to the data set
            } else {
                waveformChannelSegments.push(lateSegment);
            }

            logger.debug(`Publishing late channel segment for
                channel ID: ${lateSegment.channelId},
                time range: ${lateSegment.startTime},
                - ${lateSegment.endTime}`);
        });

        // Publish the late channel segment
        pubsub.publish(lateDataPubSubChannel, {
            waveformChannelSegmentsAdded: lateSegments,
            });

        // If there are more late-arriving segments to add, reset the timer
        if (lateWaveformChannelSegments.length > 0) {
            lateDataTimerId = setInterval(addLateChannelSegment, lateDataDelayMillis);
        }
    }
}

/**
 * Adds the provided timeseries to the provided channel segment, updating the segment's start time
 * and end time to account for the added timeseries data.
 * @param channelSegment The channel segment to which the provided time series will be added
 * @param timeSeries The time series data to add to the provided channel segment
 */
function addTimeSeriesToSegment(channelSegment: model.ChannelSegment, timeSeries: TimeSeries) {
    if (channelSegment.startTime > timeSeries.startTime) {
        channelSegment.startTime = timeSeries.startTime;
    }
    if (channelSegment.endTime < timeSeries.endTime) {
        channelSegment.endTime = timeSeries.endTime;
    }
    channelSegment.timeseriesList.push(timeSeries);
}

/**
 * Functional interface for a method to populate waveform samples with in the provided waveform
 * This interface is used to pass the appropriate sample builder function to the buildWaveformSegments
 * function based on configuration settings (either generating mock sample data or reading from file)
 */
interface SamplePopulator {
    (waveform: Waveform, startTime: number, endTime: number): void;
}

/**
 * Creates collection of waveform channel segments corresponding to the input
 * channel IDs and time range, using the provided populateSamples method
 * to populate the samples in each waveform (e.g. generate or read from file).
 * This is a package private helper used by the exported function:
 * getWaveformSegmentsByChannel()
 * @param startTime The start time of the waveform data to retrieve
 * @param endTime The end time of the waveform data to retrieve
 * @param channelIds The list of channel IDs to retrieve waveform data for
 * @param populateSamples The function that will be used to populate samples
 * for each waveform object
 */
function createWaveformSegments(startTime: number,
                                endTime: number,
                                channelIds: string[],
                                populateSamples: SamplePopulator): model.ChannelSegment[] {

    channelIds = uniq(channelIds);
    logger.info(`Request for ${channelIds .length} waveforms for the range:
                ${startTime} to ${endTime}`);
    logger.debug(`Requested channels: ${channelIds}`);

    // Get the waveform channel segments for the provided channel IDs & time range from the
    // pre-loaded test set. Note that these reflect the available test data rather than the
    // requested time range; thus a new set of channel segments and waveforms will be built
    // with content from the test set appropriate to the requested time range
    const channelSegments: model.ChannelSegment[] = filter(waveformChannelSegments, segment =>
        channelIds.indexOf(segment.channelId) > -1
            && Math.min(segment.endTime, endTime)
             - Math.max(segment.startTime, startTime) >= 0
    );

    // Initialize the array of ChannelSegments to return
    const requestedChannelSegments: model.ChannelSegment[] = [];

    // For each matching channel segment in the pre-loaded test set,
    // create a new channel segment tailored to the requested time range,
    // (i.e. containing only the waveforms that fall within the requested time range)
    channelSegments.forEach(segment => {
        const channelId: string = segment.channelId;
        const newSegment: model.ChannelSegment = {
            id: uuid4().toString(),
            segmentType: segment.segmentType,
            startTime: Number.MAX_VALUE,
            endTime: Number.MIN_VALUE,
            channelId: segment.channelId,
            timeseriesList: [],
            featureMeasurementIds: [],
            creationInfo: {
                id: uuid4.toString(),
                creationTime: new Date().getTime() / CONVERSION_WEIGHT_1000,
                creatorId: 'Auto',
                creatorType: CreatorType.System
            }
        };

        // For each waveform in the pre-loaded test set segment,
        // create a copy and fill in the waveform samples using
        // the provided populateSamples method
        segment.timeseriesList.forEach(timeSeries => {
            const waveform: Waveform = {...timeSeries as Waveform};
            logger.debug(`Generating simulated waveform for channel ${channelId}`);
            populateSamples(waveform, startTime, endTime);
            if (waveform.waveformSamples.length > 1) {
                addTimeSeriesToSegment(newSegment, waveform);
            }
        });
        if (newSegment.timeseriesList.length > 0) {
            requestedChannelSegments.push(newSegment);
        }
    });

    return requestedChannelSegments;
}

/**
 * Creates collection of waveform channel segments corresponding to the input
 * channel IDs and time range.
 * @param startTime The start time of the waveform data to retrieve
 * @param endTime The end time of the waveform data to retrieve
 * @param channelIds The list of channel IDs to retrieve waveform data for
 */
export function getWaveformSegmentsByChannel(startTime: number,
                                             endTime: number,
                                             channelIds: string[]): model.ChannelSegment[] {

    // Read the waveform sample creation mode from configuration settings
    // (e.g. generate, load from file)
    const waveformMode: string = config.get('testData.waveform.waveformMode');

    // If configured for 'simulated' data, generate mock waveform data sets
    if (waveformMode.toLowerCase() === 'simulated') {
        return createWaveformSegments(startTime, endTime, channelIds, generateMockWaveformSamples);
    // If configured for data 'fromFile', read waveforms from the configured files
    } else if (waveformMode.toLowerCase() === 'fromfile') {
        return createWaveformSegments(startTime, endTime, channelIds, readWaveformSamples);
    } else {
        // Invalid waveformMode -- an empty list of waveforms will be returned.
        logger.info(`Invalid testData.waveformMode config value: ${waveformMode}`);
    }

    return [];
}

/**
 * Read in raw waveform data for a desired channel and time interval 
 * and assign the sample array to the provided waveform object.
 * @param waveform Waveform object to read in samples for
 * @param startTime The start time of the waveform data to retrieve.
 * @param endTime The end time of the waveform data to retrieve.
 * @param channelId The channel ID of the waveform.
 */
function readWaveformSamples(waveform: Waveform, startTime: number, endTime: number) {
    // Explicitly casting numeric values out of `wfd` to Number to
    // avoid weird string/number issues.
    const dataStartOffset: number = waveform.fileOffset;
    const dataStartTime: number   = waveform.startTime;
    const dataEndTime: number     = waveform.endTime;
    const dataSampleRate: number  = waveform.sampleRate;
    const calibration: number     = waveform.calibration.factor;
    const readStartTime: number   = Math.max(dataStartTime, startTime);
    const readEndTime: number     = Math.min(dataEndTime, endTime);
    const readStartOffset: number = Math.round(readStartTime - dataStartTime) * dataSampleRate * 4 + dataStartOffset;
    const readNumSamples: number  = Math.round((readEndTime - readStartTime) * dataSampleRate) + 1;

    // Exit without reading the waveform data from file if the waveform time range falls outside the
    // requested time range (i.e. if the number of samples to read is negative)
    if (readNumSamples <= 0) {
        return;
    }

    // Read the config value and replace any substrings of the form
    // `${VAR}` with the value of the `VAR` environment variable.
    const waveformDir = [config.get('testData.waveform.waveformPath')
                               .replace(/\$\{([^\}]+)\}/g, (_, v) => process.env[v])];
    // If waveformDir is a relative path, make it absolute by prefixing
    // it with the current working directory.
    if (!path.isAbsolute(waveformDir[0])) {
        waveformDir.unshift(process.cwd());
    }
    const waveformFile: string = waveformDir.concat(waveform.fileName).join(path.sep);

    logger.debug(`dataStartTime:   ${dataStartTime}`);
    logger.debug(`startTime:       ${startTime}`);
    logger.debug(`readStartTime:   ${readStartTime}`);
    logger.debug(`dataEndTime:     ${dataEndTime}`);
    logger.debug(`endTime:         ${endTime}`);
    logger.debug(`readEndTime:     ${readEndTime}`);
    logger.debug(`dataSampleRate   ${dataSampleRate}`);
    logger.debug(`readStartOffset: ${readStartOffset}`);
    logger.debug(`readNumSamples:  ${readNumSamples}`);
    logger.debug(`waveformFile:    ${waveformFile}`);

    waveform.waveformSamples =
        readWaveformSamplesFromFile(waveformFile, readStartOffset, readNumSamples * 4, calibration);
    waveform.startTime = readStartTime;
    waveform.endTime = readEndTime;
    waveform.sampleCount = readNumSamples;
}

/**
 * Read in raw waveform data from an "s4" formatted .w file.
 * @param filename Full or relative path to "s4" formatted waveform file on disk.
 * @param readOffsetInBytes Offset in bytes within the file to start reading at.
 * @param readLengthInBytes Number of bytes to read.
 * @param calibrationFactor Optional calibration factory to multiply each raw sample by (default = 1.0).
 * @return A Float32Array containing all of the read waveform samples.
 */
function readWaveformSamplesFromFile(filename: string,
                                     readOffsetInBytes: number,
                                     readLengthInBytes: number,
                                     calibrationFactor: number = 1
                                    ): Float32Array {

    // Read in the desired block of bytes from the specified s4 waveform file.
    const fd = fs.openSync(filename, 'r');
    const byteBuffer = new Buffer(readLengthInBytes);
    const bytesRead = fs.readSync(fd, byteBuffer, 0, readLengthInBytes, readOffsetInBytes);
    fs.closeSync(fd);

    // The bytes that were read in are actually 32-bit big-endian integer "samples".
    // Convert them and multiply the result by the calibrationFactor to get the
    // actual samples.
    const samples: Float32Array = new Float32Array(bytesRead/4);
    for (let i = 0; i < bytesRead; i += 4) {
        samples[i/4] = calibrationFactor * (byteBuffer[i] << 24 |
                                            byteBuffer[i+1] << 16 |
                                            byteBuffer[i+2] << 8 |
                                            byteBuffer[i+3]);
    }

    return samples;
}

/**
 * Generates a mock waveform, with generated samples.
 * @param sampleCount The number of samples in the mock waveform
 */
function generateMockWaveformSamples(waveform: Waveform, startTime: number, endTime: number) {
    const genStartTime = Math.max(waveform.startTime, startTime);
    const genEndTime = Math.min(waveform.endTime, endTime);
    const sampleCount = Math.round((genEndTime - genStartTime) * waveform.sampleRate) + 1;

    logger.debug(`waveform.StartTime: ${waveform.startTime}`);
    logger.debug(`startTime: ${startTime}`);
    logger.debug(`genStartTime: ${genStartTime}`);
    logger.debug(`waveform.EndTime: ${waveform.endTime}`);
    logger.debug(`endTime: ${endTime}`);
    logger.debug(`genEndTime: ${genEndTime}`);
    logger.debug(`dataSampleRate ${waveform.sampleRate}`);
    logger.debug(`sampleCount ${sampleCount}`);

    // Negative sample count indicates that the waveform falls outside the requested time range
    // in this case, do not generate waveform samples
    if (sampleCount > 0) {
        waveform.waveformSamples = createMockSamples(sampleCount, EVENT_AMPLITUDE, NOISE_AMPLITUDE);
        waveform.startTime = genStartTime;
        waveform.endTime = genEndTime;
        waveform.sampleCount = sampleCount;
    }
}

/**
 *  Generates a mock waveform sample set
 * @param samples 
 * @param eventAmplitude 
 * @param noiseAmplitude 
 */
function createMockSamples(samples: number, eventAmplitude: number,
                           noiseAmplitude: number): Float32Array {

    let currentEventAmplitude = 0;
    let currentEventPeak = 0;
    let eventBuildup = 0;
    const data = new Float32Array(samples);

    for (let i = 1; i < samples; i++) {

        if (i % Math.round(samples / (Math.random() * 10)) === 0) {
            currentEventAmplitude = 0.05;
            currentEventPeak = Math.random() * eventAmplitude;
            eventBuildup = 1;
        }
        if (currentEventAmplitude >= currentEventPeak) {
            eventBuildup = -1;
        }
        if (eventBuildup === 1) {
            currentEventAmplitude += currentEventAmplitude * (1 / samples) * 125;
        } else if (eventBuildup === -1) {
            currentEventAmplitude -= currentEventAmplitude * (1 / samples) * 62;
        }
        if (currentEventAmplitude < 0) {
            currentEventAmplitude = 0;
        }
        data[i - 1] = currentEventAmplitude + noiseAmplitude - Math.random() * 2 * noiseAmplitude
            - Math.random() * 2 * currentEventAmplitude;
    }

    return data;
}

/**
 * express request handler, 
 * @param req - should have query parameters start: number, end: number, channels: comma-separated list e.g. 1,2,3
 * @param res - a message pack encoded set of model.Waveforms
 */
export const waveformSegmentRequestHandler: express.RequestHandler = (req, res) => {

    const startTime = Number(req.query.start);
    const endTime = Number(req.query.end);
    const channels: string[] | null = req.query.channels && req.query.channels.split(',');

    // If channels is specified, return those channels
    const wfSegments = getWaveformSegmentsByChannel(startTime, endTime, channels);
    res.send(msgpack.encode(wfSegments));
};

/**
 * Starts the waveform service client, initializing a timer to add mock late-arriving
 * waveform data and publish mock notifications based on a configuration-driven interval.
 */
export function start() {
    // Set a timer to add late-arriving waveform channel segments
    lateDataTimerId = setInterval(addLateChannelSegment, preStartDelayMillis);
}
