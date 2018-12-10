import { CreationInfo } from '../common/model';

/**
 * Model definitions for the event-related data API
 */

/**
 * The enumerated status of an event
 */
export enum EventStatus {
    ReadyForRefinement = 'ReadyForRefinement',
    OpenForRefinement = 'OpenForRefinement',
    AwaitingReview = 'AwaitingReview',
    Complete = 'Complete'
}
/**
 * Represents an event marking the occurrence of some transient
 * source of energy in the ground, oceans, or atmosphere
 */
export interface Event {
    id: string;
    monitoringOrganization: string;
    preferredHypotheses: PreferredEventHypothesis[];
    preferredForStageHypotheses: PreferredEventHypothesis[];
    hypothesisIds: string[];
    status: EventStatus;
    activeAnalystUserNames: string[];
}

/**
 * The preferred hypothesis for the event at a given processing stage
 */
export interface PreferredEventHypothesis {
    processingStageId: string;
    hypothesisId: string;
}

/**
 * Represents a proposed explanation for an event, such that the set of
 * event hypotheses grouped by an Event represents the history of that event.
 */
export interface EventHypothesis {
    id: string;
    isRejected: boolean;
    eventId: string;
    locationSolutions: LocationSolution[];
    preferredLocationSolution: PreferredLocationSolution;
}

/**
 * Represents an estimate of the location of an event, defined as latitude, longitude, depth, and time.
 * A location solution is often determined by a location algorithm that minimizes the difference between
 * feature measurements (usually arrival time, azimuth, and slowness) and corresponding feature predictions.
 */
export interface LocationSolution {
    latDegrees: number;
    lonDegrees: number;
    depthKm: number;
    timeSec: number;
    networkMagnitudeSolutions: NetworkMagnitudeSolution[];
    // TODO
    // locationUncertainty: LocationUncertainty
}

/**
 * Enumerated type of magnitude solution (surface wave, body wave, local, etc.)
 */
export enum MagnitudeType {
    mb = 'mb',
    mbMLE = 'mbMLE',
    mbrel = 'mbrel',
    ms = 'ms',
    msMLE = 'msMLE',
    msVMAX = 'msVMAX',
    ml = 'ml'
}

/**
 * Represents an estimate of an event's magnitude based on detections from multiple stations.
 */
export interface NetworkMagnitudeSolution {
    magnitudeType: MagnitudeType;
    magnitude: number;
}

/**
 * Represents a preference relationship between an event hypothesis and a location solution.
 * Creation information is included in order to capture provenance of the preference.
 */
export interface PreferredLocationSolution {
    locationSolution: LocationSolution;
    creationInfo: CreationInfo;
}

/**
 * Encapsulates a set of event field values to apply to an event.
 */
export interface UpdateEventInput {
    creatorId: string;
    processingStageId: string;
    status: EventStatus;
    preferredHypothesisId: string;
    activeAnalystUserNames: string[];
}

/**
 * Encapsulates input used to create a location solution for an event hypothesis.
 */
export interface LocationSolutionInput {
    latDegrees: number;
    lonDegrees: number;
    depthKm: number;
    timeSec: number;

    // TODO
    // locationUncertainty: number
}

/**
 * Encapsulates input used to create a new event hypothesis.
 */
export interface CreateEventHypothesisInput {
    associatedSignalDetectionIds: string[];
    locationSolutionInput: LocationSolutionInput;
    creatorId: string;
    processingStageId: string;
}

/**
 * Encapsulates input used to update an existing event hypothesis.
 */
export interface UpdateEventHypothesisInput {
    isRejected: boolean;
    associatedSignalDetectionIds: string[];
    locationSolutionInput: LocationSolutionInput;
    creatorId: string;
    processingStageId: string;
}
