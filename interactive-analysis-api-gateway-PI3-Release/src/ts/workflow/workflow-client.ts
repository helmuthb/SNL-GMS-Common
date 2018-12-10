import * as config from 'config';
import { find, filter } from 'lodash';
import * as uuid4 from 'uuid/v4';
import * as path from 'path';

import { gatewayLogger  as logger } from '../log/gateway-logger';
import { readJsonData } from '../util/file-parse';
import { intervalCreated } from './resolvers';
import * as model from './model';

logger.info('Initializing the interval service');

const testDataSettings = config.get('testData.coiFormat');
// Read the file path to the workflow-related data sets from configuration
const dataPath = [testDataSettings.dataPath];
// If dataPath is a relative path, make it absolute by prefixing
// it with the current working directory.
if (!path.isAbsolute(dataPath[0])) {
    dataPath.unshift(process.cwd());
}

logger.info(`path to data: ${dataPath.toString()}`);

// Read the workflow-related data sets from JSON files
const stages: model.ProcessingStage[] =
    readJsonData(dataPath.concat(testDataSettings.stageFileName).join(path.sep));
const activities: model.ProcessingActivity[] =
    readJsonData(dataPath.concat(testDataSettings.activityFileName).join(path.sep));
const intervals: model.ProcessingInterval[] =
    readJsonData(dataPath.concat(testDataSettings.intervalFileName).join(path.sep));
const stageIntervals: model.ProcessingStageInterval[] =
    readJsonData(dataPath.concat(testDataSettings.stageIntervalFileName).join(path.sep));
const analysts: model.Analyst[] =
    readJsonData(dataPath.concat(testDataSettings.analystFileName).join(path.sep));
const activityIntervals: model.ProcessingActivityInterval[] =
    readJsonData(dataPath.concat(testDataSettings.activityIntervalFileName).join(path.sep));

// Load service configuration settings
const serviceConfig = config.get('workflow.intervalService');

let intervalCreationStartTimeSec: number = serviceConfig.intervalCreationStartTimeSec;
const intervalDurationSec: number = serviceConfig.intervalDurationSec;
const intervalCreationFrequencyMillis: number = serviceConfig.intervalCreationFrequencyMillis;

/**
 * Retrieves the list of analysts defined for the interactive analysis.
 */
export function getAllAnalysts(): model.Analyst[] {
    return analysts;
}

/**
 * Retrieves the list of analysts whose user names are in the provided list.
 * @param analystUserNames The list of user names for which to retrieve analysts
 */
export function getAnalysts(analystUserNames: string[]): model.Analyst[] {
    return filter(analysts, (analyst: model.Analyst) =>
        analystUserNames.indexOf(analyst.userName) > -1);
}

/**
 * Retrieves the analyst with the provided user name.
 * @param userName The unique username of the analyst to retrieve
 */
export function getAnalyst(userName: string): model.Analyst {
    return find(analysts, { userName });
}

/**
 * Retrieves the list of processing stages defined for the interactive analysis.
 */
export function getStages(): model.ProcessingStage[] {
    return stages;
}

/**
 * Retrieves the processing stage with the provided ID string.
 * @param id The unique ID of the processing stage to retrieve
 */
export function getStage(id: string): model.ProcessingStage {
    return find(stages, { id });
}

/**
 * Retrieves the processing stage with the provided name string.
 * @param name The name of the processing stage to retrieve
 */
export function getStageByName(name: string): model.ProcessingStage {
    return find(stages, { name });
}

/**
 * Retrieves the processing intervals in the provided time range.
 * @param startTime The start time of the range for which to retrieve intervals
 * @param endTime The end time of the range for which to retrieve intervals
 */
export function getIntervalsInRange(startTime: number, endTime: number): model.ProcessingInterval[] {
    return filter(intervals, function (interval) {
        return (interval.startTime >= startTime &&
            interval.endTime <= endTime);
    });
}

/**
 * Retrieves the processing interval with the provided ID string.
 * @param id The unique ID of the processing interval to retrieve
 */
export function getInterval(id: string): model.ProcessingInterval {
    return find(intervals, { id });
}

/**
 * Retrieves the list of processing stage intervals.
 */
export function getStageIntervals(): model.ProcessingStageInterval[] {
    return stageIntervals;
}

/**
 * Retrieves the processing stage interval with the provided ID string.
 * @param id The unique ID of the processing stage interval to retrieve
 */
export function getStageInterval(id: string): model.ProcessingStageInterval {
    return find(stageIntervals, { id });
}

/**
 * Retrieves the list of processing activities defined for interactive processing.
 */
export function getActivities(): model.ProcessingActivity[] {
    return activities;
}

/**
 * Retrieves the processing activity with the provided ID string.
 * @param id The unique ID of the processing activity to retrieve
 */
export function getActivity(id: string): model.ProcessingActivity {
    return find(activities, { id });
}

/**
 * Retrieves the list of processing activity intervals.
 */
export function getActivityIntervals(): model.ProcessingActivityInterval[] {
    return activityIntervals;
}

/**
 * Retrieves the processing activity interval with the provided ID string.
 * @param id The unique ID of the processing activity interval to retrieve
 */
export function getActivityInterval(id: string): model.ProcessingActivityInterval {
    return find(activityIntervals, { id });
}

/**
 * Retrieves the processing stage intervals in the provided time range.
 * @param startTime The start time of the range for which to retrieve stage intervals
 * @param endTime The end time of the range for which to retrieve stage intervals
 */
export function getStageIntervalsInRange(startTime: number, endTime: number): model.ProcessingStageInterval[] {
    return filter(stageIntervals, function (stageInterval) {
        return (stageInterval.startTime >= startTime && stageInterval.endTime <= endTime);
    });
}

/**
 * Updates the processing stage interval object with the provided unique ID
 * to reflect the status information in the input parameter, including
 * interval status and the id of the Analyst requesting the update.
 * @param stageIntervalId The unique ID of the stage interval to mark
 * @param input The marking input to apply to the stage interval - includes:
 *  - Updated status value
 *  - Analyst id string
 */
export function markStageInterval(stageIntervalId: string, input: any): model.ProcessingStageInterval {
    const stageInterval = find(stageIntervals, { id: stageIntervalId });
    if (!stageInterval) {
        throw new Error(`Couldn't find Processing Stage Interval with ID ${stageIntervalId}`);
    }

    // Check that the status update is valid for the provided ProcessingStageInterval and
    // associated ProcessingActivityIntervals (throw an error otherwise)
    validateStageIntervalStatus(stageInterval, input.status);

    // If the status is InProgress, update the status of each activity interval and
    // Add the provided analyst user name to the list of active analysts (if not already in the list)
    if (input.status === model.IntervalStatus.InProgress) {
        filter(activityIntervals, { stageIntervalId: stageInterval.id })
            .forEach(currentValue => {
                updateActivityIntervalStatus(currentValue, input.status, input.analystUserName);
            });
    }

    // Update the stage status
    updateStageIntervalStatus(stageInterval, input.status, input.analystUserName);

    return stageInterval;
}

/**
 * Updates the processing activity interval object with the provided unique ID
 * to reflect the status information in the input parameter, including
 * interval status and the id of the Analyst requesting the update.
 * @param stageIntervalId The unique ID of the stage interval to mark
 * @param input The marking input to apply to the stage interval - includes:
 *  - Updated status value
 *  - Analyst id string
 */
export function markActivityInterval(activityIntervalId: string, input: any): model.ProcessingActivityInterval {
    const activityInterval = find(activityIntervals, { id: activityIntervalId });
    if (!activityInterval) {
        throw new Error(`Couldn't find Processing Activity Interval with ID ${activityIntervalId}`);
    }

    // Check that the transition to the input status is valid for the provided ProcessingActivityInterval
    validateActivityIntervalStatus(activityInterval, input.status);

    // Update the activity interval status
    updateActivityIntervalStatus(activityInterval, input.status, input.analystUserName);

    // Update the parent stage for activities moving to InProgress status
    if (input.status === model.IntervalStatus.InProgress) {

        // Find the parent stage and update its status
        const stage = find(stageIntervals, { id: activityInterval.stageIntervalId });
        updateStageIntervalStatus(stage, input.status, input.analystUserName);
    }

    return activityInterval;
}

/**
 * Retrieves the processing activities for the processing stage with the provided unique ID.
 * @param stageId The unique ID of the processing stage to retrieve activities for
 * 
 */
export function getActivitiesByStage(stageId: string): model.ProcessingActivity[] {
    return filter(activities, { stageId });
}

/**
 * Retrieves the processing stage intervals for the processing stage with the provided unique ID.
 * If provided, the optional timeRange parameter constrains the results to those
 * intervals falling between timeRange.startTime (inclusive) and timeRange.endTime (exclusive)
 * @param stageId The unique ID of the stage to retrieve intervals for
 * @param timeRange The time range object for which to retrieve intervals
 */
export function getIntervalsByStage(stageId: string, timeRange: any): model.ProcessingStageInterval[] {
    if (timeRange) {
        return filter(stageIntervals, function (stageInterval: model.ProcessingStageInterval) {
            return (stageInterval.stageId === stageId &&
                stageInterval.startTime >= timeRange.startTime &&
                stageInterval.endTime < timeRange.endTime);
        });
    } else {
        return filter(stageIntervals, { stageId });
    }
}

/**
 * Retrieves the activity intervals for the processing activity with the provided unique ID.
 * @param activityId: The unique ID of the processing activity to retrieve activity intervals for
 */
export function getIntervalsByActivity(activityId: string): model.ProcessingActivityInterval[] {
    return filter(activityIntervals, { activityId });
}

/**
 * Retrieves the processing stage intervals for the provided interval ID
 * @param intervalId The unique ID of the interval for which to retrieve processing intervals
 */
export function getStageIntervalsByInterval(intervalId: string): model.ProcessingStageInterval[] {
    return filter(stageIntervals, { intervalId });
}

/**
 * Retrieves the processing activity intervals for the provided processing stage interval
 * with the provided unique ID.
 * @param stageIntervalId The unique ID of the processing stage interval for which to
 * retrieve activity intervals
 */
export function getActivityIntervalsByStageInterval(stageIntervalId: string): model.ProcessingActivityInterval[] {
    return filter(activityIntervals, { stageIntervalId });
}

/**
 * This method enforces status transition rules for ProcessingStageIntervals and associated
 * ProcessingActivityIntervals, throwing an Error for invalid transitions, which include:
 * ProcessingStageInterval:
 *  - NotStarted -> Complete
 * ProcessingActivityInterval
 *  - NotStarted -> Complete
 *  - NotStarted -> NotComplete
 *  - Complete -> NotComplete
 *  - NotComplete -> Complete
 * @param stageInterval The ProcessingStageInterval the status update would be applied to
 * @param status The ProcessingStageInterval the status update would be applied to
 */
export function validateStageIntervalStatus(stageInterval: model.ProcessingStageInterval,
                                            status: model.IntervalStatus) {

    // Prevent all stage transitions to NotStarted (only valid for activity intervals)
    if (status === model.IntervalStatus.NotComplete) {
        throw new Error(`Invalid stage status transition (* to NotComplete)
                         for stage with ID: ${stageInterval.id}`);

        // Prevent status transitions from NotStarted directly to Complete
    } else if (status === model.IntervalStatus.Complete) {
        if (stageInterval.status === model.IntervalStatus.NotStarted) {
            throw new Error(`Invalid stage status transition (NotStarted to Complete)
                             for stage with ID: ${stageInterval.id}`);
        }

        // Prevent stage status transitions to Complete if any of the associated activities
        // has a status other than Complete or NotComplete (i.e. InProgress or NotStarted)
        filter(activityIntervals, { stageIntervalId: stageInterval.id })
            .forEach(currentValue => {
                if (currentValue.status !== model.IntervalStatus.Complete &&
                    currentValue.status !== model.IntervalStatus.NotComplete) {
                    throw new Error(`Cannot transition stage to Complete because associated activity
                                with ID ${currentValue.id} is not complete (${currentValue.status})`);
                }
            });

        // Validate the status transition for each associated ProcessingActivityInterval
    } else if (status === model.IntervalStatus.InProgress) {
        filter(activityIntervals, { stageIntervalId: stageInterval.id })
            .forEach(currentValue => {
                validateActivityIntervalStatus(currentValue, status);
            });
    }
}

/**
 * Update the status of the provided ProcessingStageInterval to the provided status
 * @param stageInterval The ProcessingStageInterval to update
 * @param status The new status to apply to the ProcessingStageInterval
 * @param analystUserName The username of the Analyst to associate with the status update
 */
export function updateStageIntervalStatus(
        stageInterval: model.ProcessingStageInterval,
        status: model.IntervalStatus,
        analystUserName: string) {

    // Set the completed by field if the input status is Complete
    if (status === model.IntervalStatus.Complete) {
        stageInterval.completedByUserName = analystUserName;
    }

    // Update the status
    stageInterval.status = status;
}

/**
 *
 * This method enforces status transition rules, throwing an Error for invalid transitions, which include:
 * NotStarted -> Complete
 * NotStarted -> NotComplete
 * Complete -> NotComplete
 * NotComplete -> Complete
 * @param activityInterval The ProcessingActivityInterval the status update would be applied to
 * @param status The ProcessingActivityInterval the status update would be applied to
 */
function validateActivityIntervalStatus(activityInterval: model.ProcessingActivityInterval,
                                        status: model.IntervalStatus) {

    // Prevent status transitions from NotStarted or NotComplete to Complete
    if (status === model.IntervalStatus.Complete) {
        if (activityInterval.status === model.IntervalStatus.NotStarted ||
            activityInterval.status === model.IntervalStatus.NotComplete) {
            throw new Error(`Invalid activity status transition from ${activityInterval.status}
                            to ${status} for activity with ID: ${activityInterval.id}`);
        }

        // Prevent status transitions from NotStarted or Complete to NotComplete
    } else if (status === model.IntervalStatus.NotComplete) {
        if (activityInterval.status === model.IntervalStatus.NotStarted ||
            activityInterval.status === model.IntervalStatus.Complete) {
            throw new Error(`Invalid activity status transition from ${activityInterval.status}
                            to ${status} for activity with ID: ${activityInterval.id}`);
        }
    }
}

/**
 * Update the status of the provided ProcessingActivityInterval to the provided status
 * @param activityInterval The ProcessingActivityInterval to update
 * @param status The new status to apply to the ProcessingActivityInterval
 * @param analystUserName The username of the Analyst to associate with the status update
 */
export function updateActivityIntervalStatus(activityInterval: model.ProcessingActivityInterval,
                                             status: model.IntervalStatus,
                                             analystUserName: string) {
    if (status === model.IntervalStatus.Complete ||
        status === model.IntervalStatus.NotComplete) {
        // Set the completed by field to the input analyst user name
        // Note: NotComplete is an alternative for activities where Complete doesn't make sense,
        // so set the completed by field for this case too
        activityInterval.completedByUserName = analystUserName;

    } else if (status === model.IntervalStatus.InProgress) {
        // Add the provided analyst user name to the list of active analysts (if not already in the list)
        if (activityInterval.activeAnalystUserNames.indexOf(analystUserName) === -1) {
            activityInterval.activeAnalystUserNames.push(analystUserName);
        }
    }

    // Update the status
    activityInterval.status = status;
}

/**
 * Creates a new stage interval ending at the current date/time spanning the time range defined
 * in the configuration.
 */
function createInterval() {

    // Determine the new interval start and end time (based on configured interval span)
    const startTime = intervalCreationStartTimeSec;
    const endTime = intervalCreationStartTimeSec + intervalDurationSec;

    // Start the next interval at the current end time
    intervalCreationStartTimeSec = endTime.valueOf();

    logger.info(`Creating stage and activity intervals for the time span: ${startTime} to ${endTime}`);

    // Create a new ProcessingInterval with the start and end time
    const interval = {id: uuid4().toString(),
        startTime,
        endTime,
        stageIntervalIds: []
    };
    intervals.push(interval);

    // Create a new ProcessingStageInterval for each stage defined, and add it to the canned data array
    stages.forEach(stage => {
        const stageInterval = { id: uuid4().toString(),
            startTime,
            endTime,
            completedByUserName: '',
            stageId: stage.id,
            intervalId: interval.id,
            eventCount: 0,
            status: model.IntervalStatus.NotStarted,
            activityIntervalIds: [] };

        stageIntervals.push(stageInterval);
        interval.stageIntervalIds.push(stageInterval.id);

        logger.info(`Created new processing stage interval with ID: ${stageInterval.id}, for stage: ${stage.name}`);

        // Create a new ProcessingActivityInterval for each activity associated with the stage (by ID), add it
        // to the canned data array, and update the stage interval array of activity interval IDs
        stage.activityIds.forEach(activityId => {
            const activityIntervalId = uuid4().toString();

            activityIntervals.push({ id: activityIntervalId,
                activeAnalystUserNames: [],
                completedByUserName: '',
                timeStarted: undefined,
                eventCount: 0,
                status: model.IntervalStatus.NotStarted,
                activityId,
                stageIntervalId: stageInterval.id });

            logger.info(`Created new processing activity interval with ID: ${activityIntervalId}`);

            stageInterval.activityIntervalIds.push(activityIntervalId);
        });
    });

    // Publish the created interval to GraphQL subscribers
    intervalCreated(interval);
}

/**
 * Starts the interval service stub, starting a timer to create and publish new ProcessingStageIntervals periodically.
 */
export function start() {
    // Set a timer to create stage intervals periodically based on the configured interval span
    setInterval(createInterval, intervalCreationFrequencyMillis);
}
