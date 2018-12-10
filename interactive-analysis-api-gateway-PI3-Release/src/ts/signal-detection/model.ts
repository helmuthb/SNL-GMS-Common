
import { CreationInfo } from '../common/model';

/**
 * Model definitions for the signal detection data API
 */

/**
 * Represents a signal detection marking the arrival of a signal of interest on
 * channel within a time interval.
 */
export interface SignalDetection {
    id: string;
    monitoringOrganization: string;
    stationId: string;
    hypotheses: SignalDetectionHypothesis[];
    currentHypothesis: SignalDetectionHypothesis;
}

/**
 * Represents a proposed explanation for a Signal Detection
 */
export interface SignalDetectionHypothesis {
    id: string;
    phase: string;
    isRejected: boolean;
    signalDetectionId: string;
    arrivalTimeMeasurement: TimeFeatureMeasurement;
    azSlownessMeasurement: AzSlownessFeatureMeasurement;
    // featureMeasurements: FeatureMeasurement[];
    creationInfo: CreationInfo;
}

/**
 * Represents the linkage between Event Hypotheses and Signal Detection Hypotheses.
 * The rejected attribute is used to ensure that any rejected associations will not
 * be re-formed in subsequent processing stages.
 */
export interface SignalDetectionEventAssociation {
    id: string;
    signalDetectionHypothesisId: string;
    eventHypothesisId: string;
    isRejected: boolean;
}

/**
 * Enumeration of feature measurement types
 */
export enum FeatureType {
    ArrivalTime = 'ArrivalTime',
    AzimuthSlowness = 'AzimuthSlowness',
    Amplitude = 'Amplitude'
}

/**
 * Enumeration of operation types used in defining rules
 */
export enum DefiningOperationType {
    Location = 'Location',
    Magnitude = 'Magnitude'
}

/**
 * Represents the defining relationship (isDefining: true|false) for an operation type (e.g. location, magnitude)
 */
export interface DefiningRule {
    operationType: DefiningOperationType;
    isDefining: boolean;
}

/**
 * Represents a measurement of a signal detection feature,
 * including measurement uncertainty.
 */
/* export interface FeatureMeasurement {
    id: string;
    hypothesisId: string;
    featureType: FeatureType;
} */

/**
 * Concrete feature measurement type for signal detection arrival time
 */
// export interface TimeFeatureMeasurement extends FeatureMeasurement {
export interface TimeFeatureMeasurement {
    id: string;
    hypothesisId: string;
    featureType: FeatureType;
    definingRules: DefiningRule[];
    timeSec: number;
    uncertaintySec: number;
}

/**
 * Concrete feature measurement type for signal detection azimuth/slowness
 */
// export interface AzSlownessFeatureMeasurement extends FeatureMeasurement {
export interface AzSlownessFeatureMeasurement {
    id: string;
    hypothesisId: string;
    featureType: FeatureType;
    azimuthDefiningRules: DefiningRule[];
    slownessDefiningRules: DefiningRule[];
    azimuthDeg: number;
    slownessSecPerDeg: number;
    azimuthUncertainty: number;
    slownessUncertainty: number;
    fkDataId: string;
}

/**
 * Input used to create a new signal detection with an initial hypothesis
 * and time feature measurement
 */
export interface NewDetectionInput {
    stationId: string;
    phase: string;
    time: number;
    timeUncertaintySec: number;
}

/**
 * Input used to update an existing signal detection
 */
export interface UpdateDetectionInput {
    time: number;
    timeUncertaintySec: number;
    phase: string;
}
