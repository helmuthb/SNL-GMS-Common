import * as path from 'path';
import * as config from 'config';
import * as uuid4 from 'uuid/v4';
import { readJsonData } from '../util/file-parse';
import { find, findIndex } from 'lodash';
import * as model from './model';
import { gatewayLogger as logger } from '../log/gateway-logger';

logger.info('Initializing the FK Data service');

const testDataSettings = config.get('testData.coiFormat');

// Read the necessary UEB data set CSV files into arrays of objects
const dataPath = [testDataSettings.dataPath];
// If dataPath is a relative path, make it absolute by prefixing
// it with the current working directory.
if (!path.isAbsolute(dataPath[0])) {
    dataPath.unshift(process.cwd());
}

// Read the test data set from file; parse into nested object structure representing rows in the file
// Load signal detections and hypotheses from the test data set
const fkDataRaw =
    readJsonData(dataPath.concat(testDataSettings.fkDataFileName).join(path.sep));

const fkData: model.FkData[] = [];
const fkToSdhIds: model.FkToSdhIdMapping[] = [];

fkDataRaw.forEach(fkRaw => {

    // Set the id as a string (defined in raw data as a number)
    fkRaw.signalDetectionId = String(fkRaw.signalDetectionId);
    const fk: model.FkData = {
        id: uuid4().toString(),
        accepted: false, // Loaded FKs from the data are already accepted
        frequencyBand: {
            minFrequencyHz: fkRaw.freqBand[0],
            maxFrequencyHz: fkRaw.freqBand[1]
        },
        slownessScale: {
            maxValue: fkRaw.maxSlow,
            scaleValues: fkRaw.slow,
            scaleValueCount: fkRaw.numSlow
        },
        windowParams: {
            windowType: fkRaw.winType,
            leadSeconds: fkRaw.winLead,
            lengthSeconds: fkRaw.winLength
        },

        attenuation: fkRaw.atten,
        contribChannelIds: fkRaw.contribChannels,
        peak: {
            xSlowness: fkRaw.maxFk.xSlow,
            ySlowness: fkRaw.maxFk.ySlow,
            azimuthDeg: fkRaw.maxFk.az,
            radialSlowness: fkRaw.maxFk.slow,
            azimuthUncertainty: fkRaw.maxFk.azUnc,
            slownessUncertainty: fkRaw.maxFk.slowUnc,
            fstat: fkRaw.maxFk.fstat
        },
        theoretical:  {
            xSlowness: fkRaw.maxFk.theoXSlow,
            ySlowness: fkRaw.maxFk.theoYSlow,
            azimuthDeg: fkRaw.maxFk.theoAz,
            radialSlowness: fkRaw.maxFk.theoSlow,
            azimuthUncertainty: fkRaw.maxFk.theoAzUnc,
            slownessUncertainty: fkRaw.maxFk.theoSlowUnc,
            fstat: fkRaw.maxFk.fstat
        },
        fkGrid: fkRaw.fk,
        fstatData: {
            azimuthWf: {
                id: uuid4().toString(),
                startTime: fkRaw.fstatStruct.startTime,
                endTime: fkRaw.fstatStruct.endTime,
                sampleRate: fkRaw.fstatStruct.sampRate,
                sampleCount: fkRaw.fstatStruct.numSamp,
                waveformSamples: fkRaw.fstatStruct.az,
                calibration: undefined,
                fileOffset: 0,
                fileName: undefined
            },
            slownessWf: {
                id: uuid4().toString(),
                startTime: fkRaw.fstatStruct.startTime,
                endTime: fkRaw.fstatStruct.endTime,
                sampleRate: fkRaw.fstatStruct.sampRate,
                sampleCount: fkRaw.fstatStruct.numSamp,
                waveformSamples: fkRaw.fstatStruct.slow,
                calibration: undefined,
                fileOffset: 0,
                fileName: undefined
            },
            fstatWf: {
                id: uuid4().toString(),
                startTime: fkRaw.fstatStruct.startTime,
                endTime: fkRaw.fstatStruct.endTime,
                sampleRate: fkRaw.fstatStruct.sampRate,
                sampleCount: fkRaw.fstatStruct.numSamp,
                waveformSamples: fkRaw.fstatStruct.fstat,
                calibration: undefined,
                fileOffset: 0,
                fileName: undefined
            },
            beamWf: {
                id: uuid4().toString(),
                startTime: fkRaw.fstatStruct.beamStartTime,
                endTime: fkRaw.fstatStruct.beamEndTime,
                sampleRate: fkRaw.fstatStruct.beamSampRate,
                sampleCount: fkRaw.fstatStruct.beamNumSamp,
                waveformSamples: fkRaw.fstatStruct.beam,
                calibration: undefined,
                fileOffset: 0,
                fileName: undefined
            }
        }
    };

    fkData.push(fk);
    let fkToSdhId = find(fkToSdhIds, entry => entry.sdhId === fkRaw.signalDetectionId);

    if (!fkToSdhId) {
        // console.log("Adding new entry: " + fkRaw.signalDetectionId);
        fkToSdhId = {
            fks: [],
            sdhId: String(fkRaw.signalDetectionId),
            fkIdPos: 0
        };
        fkToSdhIds.push(fkToSdhId);
    }
    fkToSdhId.fks.push(fk);
});

let currentIdx = fkData.length - 1;

/**
 * Return ID mappings from all FK data to corresponding signal detection hypotheses
 */
export function getFkToSdhIds(): model.FkToSdhIdMapping[] {
    return fkToSdhIds;
}

/**
 * Returns the next FK in the test data set, incrementing a static index.
 * This method provides a basic capability for mock FK data on demand
 * (e.g. to simulate calculating a new FK for an updated set of inputs specified
 * in the UI).
 */
export function getNextFk(fkDataInput: model.NewFkInput, sdHypId: string): model.FkData {
    const fkToSdhId = find(fkToSdhIds, fk => fk.sdhId === sdHypId);

    if (fkToSdhId && fkToSdhId.fks && fkToSdhId.fks.length > 0) {
        const fkIndex = (fkToSdhId.fkIdPos + 1) % fkToSdhId.fks.length;
        ++fkToSdhId.fkIdPos;
        return fkToSdhId.fks[fkIndex];
    }
    logger.info("Couldn't find existing Signal Detection fk list returning random FkData entry.");
    currentIdx = (currentIdx + 1) % fkData.length;
    return fkData[currentIdx];
}

/**
 * Retrieve the FK data from the test collection in memory for the provided ID.
 * @param id The ID of the FK to retrieve
 */
export function getFkDataById(id: string): model.FkData {
    return find(fkData, { id });
}

/**
 * Create a new FK, not associated with any signal detection hypothesis
 * @param input Input values used to compute a new FK dat set
 */
export function createFk(input: model.NewFkInput, sdHypId: string): model.FkData {
    // Handle undefined input
    if (!input || !sdHypId) {
        throw new Error('Attempt to create new FK from null ' +
            'input or signal detection hypothesis id not set.');
    }

    // TODO: Until we have a working FK service, just get the next Fk Data for this
    // signal detection hypothesis and the next frequency band
    const fk = getNextFk(input, sdHypId);

    // If no existing signal detection exists, return undefined
    if (!fk) {
        return undefined;
    }

    // Build a new FkData object, blending the existing Fk data with the input
    return {
        id: uuid4().toString(),
        accepted: false,
        frequencyBand: input.frequencyBand,
        slownessScale: {
            maxValue: fk.slownessScale.maxValue,
            scaleValues: fk.slownessScale.scaleValues,
            scaleValueCount: fk.slownessScale.scaleValueCount
        },
        windowParams: input.windowParams,
        attenuation: fk.attenuation,
        contribChannelIds: input.contribChannelIds,
        peak: {
            xSlowness: fk.peak.xSlowness,
            ySlowness: fk.peak.ySlowness,
            azimuthDeg: fk.peak.azimuthDeg,
            radialSlowness: fk.peak.radialSlowness,
            azimuthUncertainty: fk.peak.azimuthUncertainty,
            slownessUncertainty: fk.peak.slownessUncertainty,
            fstat: fk.peak.fstat
        },
        theoretical:  {
            xSlowness: fk.theoretical.xSlowness,
            ySlowness: fk.theoretical.ySlowness,
            azimuthDeg: fk.theoretical.azimuthDeg,
            radialSlowness: fk.theoretical.radialSlowness,
            azimuthUncertainty: fk.theoretical.azimuthUncertainty,
            slownessUncertainty: fk.theoretical.slownessUncertainty,
            fstat: fk.theoretical.fstat
        },
        fkGrid: fk.fkGrid,
        fstatData: {
            azimuthWf: {
                id: uuid4().toString(),
                startTime: fk.fstatData.azimuthWf.startTime,
                endTime: fk.fstatData.azimuthWf.endTime,
                sampleRate: fk.fstatData.azimuthWf.sampleRate,
                sampleCount: fk.fstatData.azimuthWf.sampleCount,
                waveformSamples: fk.fstatData.azimuthWf.waveformSamples,
                calibration: fk.fstatData.azimuthWf.calibration,
                fileOffset: fk.fstatData.azimuthWf.fileOffset,
                fileName: fk.fstatData.azimuthWf.fileName
            },
            slownessWf: {
                id: uuid4().toString(),
                startTime: fk.fstatData.slownessWf.startTime,
                endTime: fk.fstatData.slownessWf.endTime,
                sampleRate: fk.fstatData.slownessWf.sampleRate,
                sampleCount: fk.fstatData.slownessWf.sampleCount,
                waveformSamples: fk.fstatData.slownessWf.waveformSamples,
                calibration: fk.fstatData.slownessWf.calibration,
                fileOffset: fk.fstatData.slownessWf.fileOffset,
                fileName: fk.fstatData.slownessWf.fileName
            },
            fstatWf: {
                id: uuid4().toString(),
                startTime: fk.fstatData.fstatWf.startTime,
                endTime: fk.fstatData.fstatWf.endTime,
                sampleRate: fk.fstatData.fstatWf.sampleRate,
                sampleCount: fk.fstatData.fstatWf.sampleCount,
                waveformSamples: fk.fstatData.fstatWf.waveformSamples,
                calibration: fk.fstatData.fstatWf.calibration,
                fileOffset: fk.fstatData.fstatWf.fileOffset,
                fileName: fk.fstatData.fstatWf.fileName
            },
            beamWf: {
                id: uuid4().toString(),
                startTime: fk.fstatData.beamWf.startTime,
                endTime: fk.fstatData.beamWf.endTime,
                sampleRate: fk.fstatData.beamWf.sampleRate,
                sampleCount: fk.fstatData.beamWf.sampleCount,
                waveformSamples: fk.fstatData.beamWf.waveformSamples,
                calibration: fk.fstatData.beamWf.calibration,
                fileOffset: fk.fstatData.beamWf.fileOffset,
                fileName: fk.fstatData.beamWf.fileName
            }
        }
    };
}

/**
 * Update the FK data collection to include the input FK data set. If an FK data set already exists
 * for the provided signal detection hypothesis ID, replace it with the input; otherwise add the new
 * FK to the data set.
 * @param sdHypothesisId The ID of the signal detection hypothesis to associate the input FK data with
 * in the collection.
 * @param fkDataInput The FK data set to store in the collection
 */
export function updateFkDataById(fkIdToUpdate: string, fkDataInput: model.FkData): model.FkData {

    // Handle undefined FK input
    if (!fkDataInput) {
        throw new Error(`Cannot update FK data collection with undefined FK input`);
    }

    // Retrieve the existing FK for the provided ID
    const idxToReplace = findIndex(fkData, fk => fk.id === fkIdToUpdate);

    // If the FK data to update does not exist in the collection, throw an error
    if (idxToReplace === -1) {
        throw new Error(`Cannot update missing FK Data with ID: ${fkIdToUpdate}`);
    }

    // If an existing FK is found, replace it with the new FK
    fkData[idxToReplace] = fkDataInput;

    // Updating an Fk means it has been accepted
    fkData[idxToReplace].accepted = true;

    return fkData[idxToReplace];
}
