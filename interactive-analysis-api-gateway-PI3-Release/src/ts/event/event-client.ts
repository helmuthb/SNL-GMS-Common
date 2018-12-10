import * as path from 'path';
import { find, filter } from 'lodash';
import * as config from 'config';
import * as uuid4 from 'uuid/v4';

import { readCsvData } from '../util/file-parse';
import * as model from './model';
import * as workflowClient from '../workflow/workflow-client';
import * as signalDetectionClient from '../signal-detection/signal-detection-client';
import { TimeRange, CreationInfo, CreatorType } from '../common/model';
import { gatewayLogger as logger } from '../log/gateway-logger';

const MILLIS_PER_SECOND = 1000;

logger.info('Initializing the event service');

const testDataSettings = config.get('testData.cssFormat');

// Read the necessary UEB data set CSV files into arrays of objects
const dataPath = [testDataSettings.dataPath];

// If dataPath is a relative path, make it absolute by prefixing
// it with the current working directory.
if (!path.isAbsolute(dataPath[0])) {
    dataPath.unshift(process.cwd());
}
const sourceData = {
    uebOrigin: readCsvData(dataPath.concat(testDataSettings.originFileName).join(path.sep)),
    uebAssoc: readCsvData(dataPath.concat(testDataSettings.assocFileName).join(path.sep))
};

// mock events and hypotheses
const events: model.Event[] = [];
const eventHypotheses: model.EventHypothesis[] = [];

// Build a collection event and event hypothesis objects from the UEB data set origins
sourceData.uebOrigin.forEach(row => {

    const processingStage = workflowClient.getStageByName('Auto Process');

    // Add an event, referencing the pre-allocated hypothesis ID
    events.push({
        id: row.ORID,
        monitoringOrganization: 'CTBTO',
        preferredHypotheses: [
            {
                processingStageId: processingStage.id,
                hypothesisId: row.ORID
            }
        ],
        preferredForStageHypotheses: [
            {
                processingStageId: processingStage.id,
                hypothesisId: row.ORID
            }
        ],
        hypothesisIds: [row.ORID],
        status: model.EventStatus.ReadyForRefinement,
        activeAnalystUserNames: []
    });

    const locationSolution: model.LocationSolution = {
        latDegrees: row.LAT,
        lonDegrees: row.LON,
        depthKm: row.DEPTH,
        timeSec: row.TIME,
        networkMagnitudeSolutions: []
    };

    // If defined in the data (non-negative), add the body wave magnitude
    // as a network mag estimate to the location solution
    if (row.MB >= 0) {
        locationSolution.networkMagnitudeSolutions.push(
            {
                magnitudeType: model.MagnitudeType.mb,
                magnitude: row.MB
            }
        );
    }
    // If defined in the data (non-negative), add the surface wave magnitude
    // as a network mag estimate to the location solution
    if (row.MS >= 0) {
        locationSolution.networkMagnitudeSolutions.push(
            {
                magnitudeType: model.MagnitudeType.ms,
                magnitude: row.MS
            }
        );
    }
    // If defined in the data (non-negative), add the local magnitude
    // as a network mag estimate to the location solution
    if (row.ML >= 0) {
        locationSolution.networkMagnitudeSolutions.push(
            {
                magnitudeType: model.MagnitudeType.ml,
                magnitude: row.ML
            }
        );
    }

    // Add a hypothesis, referencing the pre-allocated feature measurement IDs
    eventHypotheses.push({
        id: row.ORID,
        isRejected: false,
        eventId: row.ORID,
        locationSolutions: [locationSolution],
        preferredLocationSolution: {
            locationSolution,
            creationInfo: {
                id: uuid4.toString(),
                creationTime: 1274328000,
                creatorId: 'Auto',
                creatorType: CreatorType.System
            }
        }
    });
});

/**
 * Retrieve the event hypotheses whose event time (from the preferred location solution)
 * falls within the provided time range.
 * @param timeRange The time range to find event hypothesis within
 */
export function getEventHypothesesInTimeRange(timeRange: TimeRange): model.EventHypothesis[] {
    return filter(eventHypotheses, eventHypothesis => {
        const eventTime: number = eventHypothesis.preferredLocationSolution.locationSolution.timeSec;
        return eventTime >= timeRange.startTime && eventTime < timeRange.endTime;
    });
}

/**
 * Retrieve the preferred event hypothesis for the provided processing stage and
 * event with the provided ID.
 * @param eventId The ID of the event to locate the preferred hypothesis for
 * @param stageId The ID of the processing stage to locate the preferred hypothesis for
 */
export function getPreferredHypothesisForStage(eventId: string, stageId: string): model.PreferredEventHypothesis {

    // Find the preferred hypothesis object with the provided stage ID for the event with the provided ID
    const event = find(events, { id: eventId });
    if (event) {
        return find(event.preferredForStageHypotheses, { processingStageId: stageId });
    }
    return undefined;
}

/**
 * Retrieve the overall preferred event hypothesis for the event with the provided ID.
 * @param eventId The ID of the event to locate the preferred hypothesis for
 * @param stageId The ID of the processing stage to locate the preferred hypothesis for
 */
export function getPreferredHypothesis(eventId: string): model.PreferredEventHypothesis {

        // Find the preferred hypothesis object with the provided stage ID for the event with the provided ID
        const event = find(events, { id: eventId });
        if (event && event.preferredHypotheses.length > 0) {
            // Return the latest preferred hypothesis in the event's list
            return event.preferredHypotheses[event.preferredHypotheses.length - 1];
        }
        return undefined;
    }

/**
 * Retrieve the event hypotheses associated with the event whose ID matches the parameter
 * @param eventId The ID of the event to retrieve the hypotheses for
 */
export function getHypothesesForEvent(eventId: string): model.EventHypothesis[] {
    return filter(eventHypotheses, { eventId });
}

/**
 * Retrieve the event hypothesis with the provided ID.
 * @param id The ID of the event hypothesis to retrieve
 */
export function getEventHypothesisById(id: string) {
    return find(eventHypotheses, { id });
}

/**
 * Retrieve the event hypotheses matching the provided list of IDs.
 * @param ids The IDs of the event hypotheses to retrieve
 */
export function getEventHypothesesById(ids: string[]) {
    return filter(eventHypotheses, hypothesis => ids.indexOf(hypothesis.id) > -1);
}

/**
 * Retrieve the event with the provided ID.
 * @param id The ID of the event to retrieve
 */
export function getEventById(id: string) {
    return find(events, { id });
}

/**
 * Updates the events with the provided IDs using the provided input parameters. If no updates parameters are
 * included, this method will throw an error
 */
export function updateEvents(eventIds: string[], input: model.UpdateEventInput): model.Event[] {
    return eventIds.map(eventId => updateEvent(eventId, input));
}

/**
 * Updates the event with the provided ID using the provided input parameters. If no updates parameters are
 * included, this method will throw an error
 * @param eventId: The ID of the vent to update
 * @param input: The input parameters to update in the event
 */
export function updateEvent(eventId: string, input: model.UpdateEventInput): model.Event {

    // Try to retrieve the event with the provided ID; throw an error if it is missing
    const event = find(events, { id: eventId});

    if (!event) {
        throw new Error(`Attempt to update a missing event with ID ${eventId}`);
    }

    // Track whether any updates have been made
    let update = false;

    // Update the status if provided in the input
    if (input.status) {
        event.status = input.status;
        update = true;
    }

    // Update the preferred hypothesis if provided in the input
    if (input.preferredHypothesisId) {
        event.preferredHypotheses.push({
            processingStageId: input.processingStageId,
            hypothesisId: input.preferredHypothesisId
        });

        // TODO update preferred hypothesis for stage list
        update = true;
    }

    // Update the list of active analyst user names if provided in the input
    if (input.activeAnalystUserNames) {
        event.activeAnalystUserNames = input.activeAnalystUserNames;
        update = true;
    }

    // Throw an error if no updates were made (invalid input), since we don't want to publish a subscription callback
    // for no-op updates
    if (!update) {
        throw new Error(`No valid input provided to update event with ID: ${eventId}`);
    }

    return event;
}

/**
 * Creates a new list of event hypotheses from the input, and associates them with the events indicated in the input.
 * @param eventIds The IDs of the event to update
 * @param input The input used to create a new event hypothesis
 */
export function createEventHypotheses(eventIds: string[],
                                      input: model.CreateEventHypothesisInput): model.EventHypothesis[] {
    return eventIds.map(eventId => createEventHypothesis(eventId, input));
}

/**
 * Creates a new event hypothesis from the input, and associates it with the event with the provided ID.
 * @param eventId The ID of the event to update
 * @param input The input used to create a new event hypothesis
 */
export function createEventHypothesis(eventId: string, input: model.CreateEventHypothesisInput): model.EventHypothesis {

    // Find the event the new hypothesis will be associated with
    const event = find(events, {id: eventId });

    // Throw an error if the event can't be found
    if (!event) {
        throw new Error(`Attempt to create a new hypothesis for missing event with ID ${eventId}`);
    }

    // Add the new hypothesis' ID to the event's list
    const hypothesisId = uuid4().toString();
    event.hypothesisIds.push(hypothesisId);

    // Create a creation info object for the new hypothesis from the input
    const creationInfo: CreationInfo = {
        id: uuid4.toString(),
        creationTime: new Date().getTime() / MILLIS_PER_SECOND,
        creatorId: input.creatorId,
        creatorType: CreatorType.Analyst
    };

    // Create a location solution for the new hypothesis from the input
    const locationSolution: model.LocationSolution = {
        latDegrees: input.locationSolutionInput.latDegrees,
        lonDegrees: input.locationSolutionInput.lonDegrees,
        depthKm: input.locationSolutionInput.depthKm,
        timeSec: input.locationSolutionInput.timeSec,
        // TODO enable updates to magnitude solution
        networkMagnitudeSolutions: []
    };

    // Create the new hypothesis
    const hypothesis: model.EventHypothesis = {
        id: hypothesisId,
        isRejected: false,
        eventId: event.id,
        locationSolutions: [locationSolution],
        preferredLocationSolution: {
            locationSolution,
            creationInfo
        }
    };

    // Add the new hypothesis to the data set
    eventHypotheses.push(hypothesis);

    return hypothesis;
}

/**
 * Updates the list of event hypotheses with the provided IDs using the provided input values.
 * Only the fields that are defined in the input will be applied.
 * @param hypothesisIds The ilst of IDs of the hypotheses to update
 * @param input The input values to apply to the hypotheses' fields
 */
export function updateEventHypotheses(hypothesisIds: string[],
                                      input: model.UpdateEventHypothesisInput): model.EventHypothesis[] {
    return hypothesisIds.map(hypothesisId => updateEventHypothesis(hypothesisId, input));
}

/**
 * Updates the event hypothesis with the provided ID using the provided input values.
 * Only the fields that are defined in the input will be applied.
 * @param hypothesisId The ID of the hypothesis to update
 * @param input The input values to apply to the hypothesis fields
 */
export function updateEventHypothesis(hypothesisId: string,
                                      input: model.UpdateEventHypothesisInput): model.EventHypothesis {

    // Find the event hypothesis to update; throw an error if it can't be found
    const hypothesis = find(eventHypotheses, { id: hypothesisId });
    if (!hypothesis) {
        throw new Error(`Attempt to update a missing hypothesis with ID ${hypothesisId}`);
    }

    // Track whether any updates have been made
    let update = false;

    // Set the isRejected flag if it is defined in the input
    if (input.isRejected != undefined) {
        hypothesis.isRejected = input.isRejected;
        update = true;
    }

    // Add the signal detection associations if they are defined in the input
    if (input.associatedSignalDetectionIds && input.associatedSignalDetectionIds.length > 0) {
        signalDetectionClient.addDetectionAssociations(input.associatedSignalDetectionIds, hypothesis.id);
        update = true;
    }

    // Add a location solution to the list if the necessary input is defined
    if (input.locationSolutionInput) {

        // Create a location solution for the new hypothesis from the input
        // and add it to the hypothesis' list
        const locationSolution: model.LocationSolution = {
            latDegrees: input.locationSolutionInput.latDegrees,
            lonDegrees: input.locationSolutionInput.lonDegrees,
            depthKm: input.locationSolutionInput.depthKm,
            timeSec: input.locationSolutionInput.timeSec,
            // TODO enable updates to magnitude solution
            networkMagnitudeSolutions: []
        };
        hypothesis.locationSolutions.push(locationSolution);

        // Create a creation info object for the new hypothesis from the input
        const creationInfo: CreationInfo = {
            id: uuid4.toString(),
            creationTime: new Date().getTime() / MILLIS_PER_SECOND,
            creatorId: input.creatorId,
            creatorType: CreatorType.Analyst
        };

        // Set the new location solution as preferred
        hypothesis.preferredLocationSolution = {
            locationSolution,
            creationInfo
        };
        update = true;
    }

    // Throw an error if no updates were made (invalid input), since we don't want to publish a subscription callback
    // for no-op updates
    if (!update) {
        throw new Error(`No valid input provided to update event hypothesis with ID: ${hypothesisId}`);
    }

    return hypothesis;
}
