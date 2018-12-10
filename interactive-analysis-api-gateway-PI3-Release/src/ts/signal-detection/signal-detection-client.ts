import * as path from 'path';
import { find, filter, cloneDeep } from 'lodash';
import * as config from 'config';
import { readJsonData, readCsvData } from '../util/file-parse';
import * as uuid4 from 'uuid/v4';

import * as model from './model';
import { FkData } from '../fk/model';
import * as fkClient from '../fk/fk-client';
import { TimeRange, CreatorType } from '../common/model';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { pubsub } from './resolvers';

logger.info('Initializing the signal detection service');

// Build the CSS data file directory path
const cssDataPath = [config.get('testData.cssFormat.dataPath')];
// If dataPath is a relative path, make it absolute by prefixing
// it with the current working directory.
if (!path.isAbsolute(cssDataPath[0])) {
    cssDataPath.unshift(process.cwd());
}

// Build the COI data file directory path
const coiDataPath = [config.get('testData.coiFormat.dataPath')];
// If dataPath is a relative path, make it absolute by prefixing
// it with the current working directory.
if (!path.isAbsolute(coiDataPath[0])) {
    coiDataPath.unshift(process.cwd());
}

// Read the signal detections from the GMS COI JSON file
const signalDetections: model.SignalDetection[] =
    readJsonData(coiDataPath.concat(config.get('testData.coiFormat.signalDetectionFileName')).join(path.sep));

// Map the detection hypothesis into a separate collection for convenience
const signalDetectionHypotheses: model.SignalDetectionHypothesis[]
    = signalDetections.map(detection => detection.currentHypothesis);

// Read the CSS associations file for SDH<->EH associations
let associationInput = readCsvData(cssDataPath.concat(config.get('testData.cssFormat.assocFileName')).join(path.sep));

// Read in the event IDs from the CSS originf file. These will be used to filter down to valid associations
const eventIds = readCsvData(cssDataPath.concat(config.get('testData.cssFormat.originFileName')).join(path.sep))
    .map(origin => origin.ORID);

// Filter the association input data down to entries with an event ID (ORID) matching an ORID
// entry in the origin CSS file AND with a signal detection ID (ARID) matching an arid entry
// in the arrival CSS file
associationInput = filter(associationInput, assoc => eventIds.indexOf(assoc.ORID) > -1
    && signalDetections.map(detection => detection.id).indexOf(assoc.ARID) > -1);

// Overwrite the current signal detection hypotheses' phases to those in the association file
signalDetections.forEach(detection => {
    const assoc: any = find(associationInput, { ARID: detection.id });
    if (assoc) {
        detection.currentHypothesis.phase = assoc.PHASE;
        const currentInList = find(detection.hypotheses, {id: detection.currentHypothesis.id});
        if (currentInList) {
            currentInList.phase = assoc.PHASE;
        }
    }
});

// Load the signal detection associations
const sdAssociations: model.SignalDetectionEventAssociation[] = loadSdAssociations(associationInput);

// For each SDH ID -> FK ID mapping in the source data, set the FK ID in the correspondig signal detection
// hypothesis' azimuth/slowness feature measurement
const fkToSdhIds = fkClient.getFkToSdhIds();
fkToSdhIds.forEach(fkToSdId => {
    const detection = find(signalDetections, det => det.currentHypothesis.id === fkToSdId.sdhId);

    if (detection && fkToSdId.fks && fkToSdId.fks.length > 0) {
        // Use first fk id in the list of fks
        detection.currentHypothesis.azSlownessMeasurement.fkDataId = fkToSdId.fks[0].id;
    }
});

// Build the late-data CSS data file directory path
const lateCssDataPath = [config.get('testData.cssFormat.lateData.dataPath')];
// If dataPath is a relative path, make it absolute by prefixing
// it with the current working directory.
if (!path.isAbsolute(lateCssDataPath[0])) {
    lateCssDataPath.unshift(process.cwd());
}

// Build the COI data file directory path
const lateCoiDataPath = [config.get('testData.coiFormat.lateData.dataPath')];
// If dataPath is a relative path, make it absolute by prefixing
// it with the current working directory.
if (!path.isAbsolute(lateCoiDataPath[0])) {
    lateCoiDataPath.unshift(process.cwd());
}

// Read the late-arriving signal detections from the GMS COI JSON file
const lateSignalDetections: model.SignalDetection[] =
    readJsonData(lateCoiDataPath.concat(
        config.get('testData.coiFormat.lateData.signalDetectionFileName')).join(path.sep));

// Read the CSS associations file for late-arriving SDH<->EH associations
let lateAssociationInput =
    readCsvData(lateCssDataPath.concat(config.get('testData.cssFormat.lateData.assocFileName')).join(path.sep));

// Filter the association input data down to entries with an event ID (ORID) matching an ORID
// entry in the origin CSS file AND with a signal detection ID (ARID) matching an arid entry
// in the arrival CSS file
lateAssociationInput = filter(lateAssociationInput, assoc => eventIds.indexOf(assoc.ORID) > -1
    && lateSignalDetections.map(detection => detection.id).indexOf(assoc.ARID) > -1);

// Overwrite the current signal detection hypotheses' phases to those in the association file
lateSignalDetections.forEach(detection => {
    const assoc: any = find(lateAssociationInput, { ARID: detection.id });
    if (assoc) {
        detection.currentHypothesis.phase = assoc.PHASE;
    }
});

// Load the late-arriving signal detection associations
const lateSdAssociations = loadSdAssociations(lateAssociationInput);

// For each late-arriving SDH ID -> FK ID mapping in the source data, set the FK ID in the correspondig signal detection
// hypothesis' azimuth/slowness feature measurement
const lateFkToSdhIds = fkClient.getFkToSdhIds();
lateFkToSdhIds.forEach(fkToSdId => {
    const detection = find(lateSignalDetections, det => det.currentHypothesis.id === fkToSdId.sdhId);

    if (detection && fkToSdId.fks && fkToSdId.fks.length > 0) {
        // Use first fk id in the list of fks
        detection.currentHypothesis.azSlownessMeasurement.fkDataId = fkToSdId.fks[0].id;
    }
});

logger.info(`Late associations count: ${lateSdAssociations.length}`);

// ID of late-arriving data timer, initially undefined
let lateDataTimerId;

// Initialize the number of late-arriving signal detections to make available in one batch
// (from configuration)
const detectionCount = config.get('testData.lateData.detectionCount');

// Delay between release of late-arriving signal detections, read from configuration
const lateDataDelayMillis = config.get('testData.lateData.delayMillis');
// Delay before sending the first late-arriving signal detections
const preStartDelayMillis = config.get('testData.lateData.preStartDelay');
const lateDataPubSubChannel = config.get('signalDetection.subscriptions.channels.detectionsCreated');

/**
 * Build the collection of signal detection hypothesis <-> event hypothesis associations from the provided
 * association data set (loaded from the UEB association data file).
 * 
 * Note: this function should only be invoked after the loadSignalDetections function has been called
 * because this method assumes that the global collection of signal detections has been populated.
 * 
 * @param assocData The parsed signal detection hypothesis <-> event hypothesis association data loaded
 * from a UEB association data file
 */
export function loadSdAssociations(assocData: any): model.SignalDetectionEventAssociation[] {

    // Initialize the associations array to populate and return
    // const associations: model.SignalDetectionEventAssociation[] = [];

    // Build associations between signal detection hypotheses & event hypotheses
    // using the UEB data set arrivals
    const associations = assocData.map(row => ({
            id: uuid4().toString(),
            signalDetectionHypothesisId: row.ARID,
            eventHypothesisId: row.ORID,
            isRejected: false
        }));

    return associations;
}

/**
 * Remove the next block of late-arriving signal detections & hypotheses from the
 * late data collection if the collection isn't empty, and add it to the main data collection.
 * Publish the late-arriving signal detections and hypotheses to the GraphQL subscription channel
 */
function addLateSignalDetections() {
    // Clear the late data timer if it is set
    if (lateDataTimerId) {
        clearInterval(lateDataTimerId);
    }

    // If there are any more late-arriving signal detectinos to add to the data set,
    // add them and publish a notification
    logger.info(`Detection Count: ${lateSignalDetections.length}`);
    if (lateSignalDetections.length > 0) {

        const availableDetections = lateSignalDetections.splice(0, detectionCount);

        // For each late-arriving detection, add the detection to the collection,
        // add the corresponding hypothesis to the hypothesis collection,
        // and add the corresponding associations to the association data collection
        availableDetections.forEach(detection => {
            signalDetections.push(detection);
            detection.hypotheses.forEach(hypothesis => {
                hypothesis.creationInfo.creationTime = Date.now() / 1000.0;
                signalDetectionHypotheses.push(hypothesis);
                const associations: model.SignalDetectionEventAssociation[]
                    = filter(lateSdAssociations, { signalDetectionHypothesisId: hypothesis.id });
                associations.forEach(association => sdAssociations.push(association));
            });
        });

        // Publish the newly-available, late-arriving signal detections
        pubsub.publish(lateDataPubSubChannel, { detectionsCreated: availableDetections });

        logger.debug(`Publishing late-arriving signal detections
        Detection Count: ${availableDetections.length}`);

        // If there are more late-arriving detections to add, reset the timer
        if (lateSignalDetections.length > 0) {
            lateDataTimerId = setInterval(addLateSignalDetections, lateDataDelayMillis);
        }
    }
}

/**h
 * Retrieve the signal detection for the provided ID.
 * @param detectionId the ID of the signal detection to retrieve
 */
export function getSignalDetectionById(detectionId: string): model.SignalDetection {
    return find(signalDetections, { id: detectionId });
}

/**
 * Retrieve the signal detections matching the provided IDs.
 * @param degectionIds the IDs of the signal detections to retrieve
 */
export function getSignalDetectionsById(detectionIds: string[]): model.SignalDetection[] {
    return filter(signalDetections, detection => detectionIds.indexOf(detection.id) > -1);
}

/**
 * Retrieve the signal detection hypothesis for the provided ID.
 * @param hypothesisId the ID of the signal detection hypothesis to retrieve
 */
export function getSignalDetectionHypothesisById(hypothesisId: string): model.SignalDetectionHypothesis {
    return find(signalDetectionHypotheses, { id: hypothesisId});
}

/**
 * Retrieve the signal detection hypotheses matching the provided IDs.
 * @param hypothesisIds the IDs of the signal detection hypotheses to retrieve
 */
export function getSignalDetectionHypothesesById(hypothesisIds: string[]): model.SignalDetectionHypothesis[] {
    return filter(signalDetectionHypotheses, hypothesis => hypothesisIds.indexOf(hypothesis.id) > -1);
}

/**
 * Retrieve the signal detections associated with the provided list of station IDs, whose
 * arrival time feature measurements fall within the provided time range. Throws an error
 * if any of the hypotheses associated signal detections or arrival time measurements are missing.
 * @param stationIds The station IDs to find signal detections for
 * @param startTime The start of the time range for which to retrieve signal detections
 * @param endTime The end of the time range for which to retrieve signal detections
 */
export function getSignalDetectionsByStation(stationIds: string[],
                                             timeRange: TimeRange): model.SignalDetection[] {

    return filter(signalDetections, detection => {

        // Retrieve the arrival time feature measurement from the current hypothesis
        const arrivalTimeMeasurement = detection.currentHypothesis.arrivalTimeMeasurement;

        // Return true if the detection's station ID matches the input list
        // and the arrival time is in the input time range
        return stationIds.indexOf(detection.stationId) > -1
            && arrivalTimeMeasurement.timeSec >= timeRange.startTime
            && arrivalTimeMeasurement.timeSec < timeRange.endTime;
    });
}

/**
 * Retrieve the signal detection hypotheses associated with the provided list of station names, whose
 * arrival time feature measurements fall within the provided time range. Throws an error
 * if any of the hypotheses associated signal detections or arrival time measurements are missing.
 * @param stationIds The list of station IDs to find signal detections for
 * @param startTime The start of the time range for which to retrieve signal detections
 * @param endTime The end of the time range for which to retrieve signal detections
 */
export function getSignalDetectionHypothesesByStation(stationIds: string[],
                                                      timeRange: TimeRange): model.SignalDetectionHypothesis[] {
    return getSignalDetectionsByStation(stationIds, timeRange).map(detection => detection.currentHypothesis);
}

/**
 * Retrieve the SDH <-> EH associations from the collection of matching the provided list of IDs.
 * @param associationIds The collection of association IDs
 */
export function getAssociationsByIds(associationIds: string[]): model.SignalDetectionEventAssociation[] {

    return sdAssociations.filter(sdAssociation =>
        associationIds.indexOf(sdAssociation.id) > -1
    );
}

/**
 * Retrieve the SDH <-> EH association ID from the association collection for the provided event hypothesis 
 * and signal detection hypothesis ID pair.
 * @param eventHypothesisId The ID of the event hypothesis in the association
 * @param detectionHypothesisId The ID of the signal detection hypothesis in the association
 */
export function getAssociationId(eventHypothesisId: string, detectionHypothesisId: string): string {

    // Retrieve the association matching the input event and signal detection hypothesis IDs
    const associationMatch = find(sdAssociations, association =>
        association.signalDetectionHypothesisId === detectionHypothesisId &&
        association.eventHypothesisId === eventHypothesisId);

    // If an association was found, return its ID
    if (associationMatch) {
        return associationMatch.id;
    }

    // If no matching association was found, return undefined
    return undefined;
}

/**
 * Retrieve the SD <-> EH associations from the association collection for the
 * provided signal detection hypothesis ID
 * @param detectionHypothesisId The signal detection hypothesis ID for which to retrieve the associations
 */
export function  getAssociationsByDetectionHypothesis(detectionHypothesisId: string)
    : model.SignalDetectionEventAssociation[] {
    return filter(sdAssociations, { signalDetectionHypothesisId: detectionHypothesisId });
}

/**
 * Retrieve the SD <-> EH associations from the association collection for the
 * provided event hypothesis ID
 * @param eventHypothesisId The event hypothesis ID for which to retrieve the associations
 */
export function  getAssociationsByEventHypothesis(eventHypothesisId: string)
    : model.SignalDetectionEventAssociation[] {
    return filter(sdAssociations, { eventHypothesisId });
}

/**
 * Retrieve the signal detection hypotheses associated to the event hypothesis with the provided ID
 * @param eventHypothesisId The event hypothesis ID for which to retrieve the associations
 */
export function  getSdHypothesesByEventHypothesisId(eventHypothesisId: string)
    : model.SignalDetectionHypothesis[] {
    const sdHypothesisIds =
        filter(sdAssociations, { eventHypothesisId }).map(sdAssoc => sdAssoc.signalDetectionHypothesisId);
    return filter(signalDetectionHypotheses, sdh => sdHypothesisIds.indexOf(sdh.id) > -1);
}

/**
 * Retrieve the ID of the FK data set associated with the signal detection hypothesis
 * with the provided id. If the hypothesis cannot be found return undefined.
 * @param hypothesisId The ID of the signal detection hypothesis for which to retrieve the
 * FK data set ID.
 */
export function getFkIdforHypothesis(hypothesisId: string): string {

    // Retrieve the hypothesis matching the input ID
    const hypothesis = find(signalDetectionHypotheses, { id : hypothesisId });

    // If the hypothesis exists, return the ID of the FK data set associated with the az/slowness
    // feature masurement; otherwise return undefined
    return hypothesis
        ? hypothesis.azSlownessMeasurement.fkDataId
        : undefined;
}

/**
 * Creates a new signal detection with an initially hypothesis and time
 * feature measurement based on the provided input.
 * @param input The input parameters used to create the new detection
 */
export function createDetection(input: model.NewDetectionInput): model.SignalDetection {
    const detectionId = uuid4().toString();
    const hypothesisId = uuid4().toString();
    const timeMeasurementId = uuid4().toString();

    // Create a new signal detection hypothesis
    const newHypothesis: model.SignalDetectionHypothesis = {
        id: hypothesisId,
        phase: input.phase,
        isRejected: false,
        signalDetectionId: detectionId,
        arrivalTimeMeasurement: {
            id: timeMeasurementId,
            hypothesisId,
            featureType: model.FeatureType.ArrivalTime,
            definingRules: [
                {
                    operationType: model.DefiningOperationType.Location,
                    isDefining: true
                }
            ],
            timeSec: input.time,
            uncertaintySec: input.timeUncertaintySec,
        },
        // TODO call fk service to populate az/slowness measurement when available
        azSlownessMeasurement: undefined,
        // featureMeasurements: [],
        creationInfo: {
            id: uuid4.toString(),
            creationTime: new Date().getTime() / 1000.0,
            // TODO Add the analyst ID to the input and copy into the creation info creator ID
            creatorId: 'Analyst',
            creatorType: CreatorType.Analyst
        }
    };

    // Create a new signal detection
    const newDetection: model.SignalDetection = {
        id: detectionId,
        monitoringOrganization: 'CTBTO',
        stationId: input.stationId,
        hypotheses: [ newHypothesis ],
        currentHypothesis: newHypothesis
    };

    // Add the new detection to the list
    signalDetections.push(newDetection);

    // Add the new hypothesis to the list
    signalDetectionHypotheses.push(newHypothesis);

    return newDetection;
}

/**
 * Updates the signal detection with the provided unique ID if it exists, using
 * the provided UpdateDetectionInput object. This function creates a new signal detection hypothesis
 * and sets it to the 'current', reflecting the change.
 * This function throws an error if no signal detection hypothesis exists for the provided ID.
 * @param hypothesisId The unique ID identifying the signal detection hypothesis to update
 * @param input The UpdateDetectionInput object containing fields to update in the hypothesis
 */
export function updateDetection(detectionId: string,
                                input: model.UpdateDetectionInput): model.SignalDetection {

    const detection = find(signalDetections, { id: detectionId });

    // Throw an error if no detection exists for the provided ID
    if (!detection) {
        throw new Error(`Couldn't find Signal Detection with ID ${detectionId}`);
    }

    // Deep copy the current hypothesis as the starting point for the new hypothesis
    const newHypothesis: model.SignalDetectionHypothesis = cloneDeep(detection.currentHypothesis);

    // Update IDs
    const newHypothesisId = uuid4().toString();
    newHypothesis.id = newHypothesisId;
    newHypothesis.arrivalTimeMeasurement.hypothesisId = newHypothesisId;
    // newHypothesis.featureMeasurements.forEach(measurement => measurement.hypothesisId = newHypothesisId);

    newHypothesis.creationInfo.id = uuid4().toString();
    newHypothesis.creationInfo.creationTime = new Date().getTime() / 1000.0;
    newHypothesis.creationInfo.creatorType = CreatorType.Analyst;
    // TODO Add analyst ID to input, copy into creation info creator id

    // Add new association to the sdAssociations with the new hypothesis ID
    const associations = filter(sdAssociations, {signalDetectionHypothesisId: detection.currentHypothesis.id});
    associations.forEach(association => {
        sdAssociations.push({id: uuid4().toString(),
                            signalDetectionHypothesisId: newHypothesisId,
                            eventHypothesisId: association.eventHypothesisId,
                            isRejected: association.isRejected});
    });

    logger.info(`New assocation between SDH: ${newHypothesisId}`);

    // Boolean indicating whether any updates were applied to the new hypothesis (if not, we'll throw an error)
    let update = false;

    // Update the phase label if included in the input
    if (input.phase) {
        newHypothesis.phase = input.phase;
        update = true;
    }

    // Update the time if included in the input
    if (input.time) {
        newHypothesis.arrivalTimeMeasurement.id = uuid4().toString();
        newHypothesis.arrivalTimeMeasurement.timeSec = input.time;
        newHypothesis.arrivalTimeMeasurement.uncertaintySec = input.timeUncertaintySec;
        update = true;
    }

    // Throw an error if no updates were made (invalid input), since we don't want to publish a subscription callback
    // for no-op updates
    if (!update) {
        throw new Error(`No valid input provided to update detection with ID: ${detectionId}`);
    }

    // Add the new hypothesis to the detection
    detection.hypotheses.push(newHypothesis);
    detection.currentHypothesis = newHypothesis;

    // Add the new hypothesis to the list
    signalDetectionHypotheses.push(newHypothesis);

    return detection;
}

/**
 * Updates the collection of signal detections matching the provided list of unique IDs
 * using the provided UpdateDetectionInput object. A new hypothesis is created for each detection,
 * reflecting the updated content.
 * This function throws an error if no signal detection hypothesis exists for any of the provided IDs.
 * @param hypothesisIds The list of unique IDs identifying the signal detection hypothesis to update
 * @param input The UpdateDetectionInput object containing fields to update in the hypothesis
 */
export function updateDetections(detectionIds: string[],
                                 input: model.UpdateDetectionInput): model.SignalDetection[] {
    return detectionIds.map(detectionId => updateDetection(detectionId, input));
}

/**
 * Rejects the collection of signal detections matching the provided list of unique IDs
 * This function throws an error if no signal detection exists for any of the provided IDs.
 * @param detectionIds The list of unique IDs identifying the signal detections to update
 * @param input The UpdateDetectionInput object containing fields to update in the detections
 */
export function rejectDetections(detectionIds: string[]): model.SignalDetection[] {
    return detectionIds.map(detectionId => {
        const detection = find(signalDetections, { id: detectionId });

        // Deep copy the current hypothesis as the starting point for the new hypothesis
        const newHypothesis: model.SignalDetectionHypothesis = cloneDeep(detection.currentHypothesis);

        // Update IDs
        const newHypothesisId = uuid4().toString();
        newHypothesis.id = newHypothesisId;
        newHypothesis.arrivalTimeMeasurement.hypothesisId = newHypothesisId;
        // newHypothesis.featureMeasurements.forEach(measurement => measurement.hypothesisId = newHypothesisId);

        // Set the rejected flag on the new hypothesis
        newHypothesis.isRejected = true;

        // Add the new hypothesis to the detection
        detection.currentHypothesis = newHypothesis;
        detection.hypotheses.push(newHypothesis);

        return detection;
    });
}

/**
 * Add association objects to the collection between each signal detection hypothesis ID in the provided list
 * and the provided event hypothesis ID
 * @param detectionHypothesisIds The list of signal detection hypothesis IDs to build associations for
 * @param eventHypothesisId The event hypothesis ID to build associations for
 */
export function addDetectionAssociations(detectionHypothesisIds: string[], eventHypothesisId: string) {
    detectionHypothesisIds.forEach(detectionHypothesisId => sdAssociations.push({
        id: uuid4().toString(),
        signalDetectionHypothesisId: detectionHypothesisId,
        eventHypothesisId,
        isRejected: false
    }));
}

/**
 * Update the azimuth/slowness feature measurement for the signal detection hypothesis with the provided
 * ID, using the provided FK data set.
 * @param hypothesisId The ID of the signal detection hypothesis to update
 * @param fkDataInput The FK data set with which to update the azimuth/slowness feature measurement
 */
export function updateAzSlowFromFk(hypothesisId: string, fkDataInput: FkData): model.SignalDetectionHypothesis {

    const hypothesis = find(signalDetectionHypotheses, { id: hypothesisId });

    // Throw an error if the hypothesis can't be found
    if (!hypothesis) {
        throw new Error(`Unable to update az/slowness measurement for missing SD hypothesis with ID: ${hypothesisId}`);
    }

    // Update the FK data associated with the detection in the FK collection
    fkClient.updateFkDataById(hypothesis.azSlownessMeasurement.fkDataId, fkDataInput);

    // Create a new azimuth/slowness feature measurement from the input Fk data
    const newAzSlow: model.AzSlownessFeatureMeasurement = {
        id: uuid4().toString(),
        featureType: model.FeatureType.AzimuthSlowness,
        hypothesisId,
        azimuthDefiningRules: [
            {
                operationType: model.DefiningOperationType.Location,
                isDefining: hypothesis.phase.startsWith('P')
            }
        ],
        slownessDefiningRules: [
            {
                operationType: model.DefiningOperationType.Location,
                isDefining: hypothesis.phase.startsWith('P')
            }
        ],
        azimuthDeg: fkDataInput.peak.azimuthDeg,
        // TODO azimuthUncertainty: fkData.peak.azimuthUncertainty,
        azimuthUncertainty: 0.5,
        slownessSecPerDeg: fkDataInput.peak.radialSlowness,
        // TOOD slownessUncertainty: fkData.peak.slownessUncertainty,
        slownessUncertainty: 0.1,
        fkDataId: fkDataInput.id
    };

    // Update the signal detection hypothesis with the new az/slow measurement
    hypothesis.azSlownessMeasurement = newAzSlow;

    return hypothesis;
}

/**
 * Starts the signal detection service client, initializing a timer to add mock late-arriving
 * detection & hypothesis data, and to publish them based on a configuration-driven interval.
 */
export function start() {
    // Set a timer to add late-arriving waveform channel segments
    lateDataTimerId = setInterval(addLateSignalDetections, preStartDelayMillis * 1.2);
}
