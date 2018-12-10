import { Waveform, FrequencyBand, FrequencyBandInput } from '../common/model';

/**
 * Represents the slowness scale used to calculate FK data
 */
export interface SlownessScale {
    maxValue: number;
    scaleValues: number[];
    scaleValueCount: number;
}

/**
 * Represents the time window parameters used in the Fk cacluation
 */
export interface WindowParameters {
    windowType: string;
    leadSeconds: number;
    lengthSeconds: number;
}

/**
 * Represents a point within the Fk data, including both cartesian and polar coordinates
 */
export interface FkPoint {
    xSlowness: number;
    ySlowness: number;
    azimuthDeg: number;
    radialSlowness: number;
    azimuthUncertainty: number;
    slownessUncertainty: number;
    fstat: number;
}

/** 
 * Fstat Structure of data
 */
export interface FstatData {
    azimuthWf: Waveform;
    slownessWf: Waveform;
    fstatWf: Waveform;
    beamWf: Waveform;
}

/**
 * Represents an FK calculated for a single detection input set,
 * including e.g. phase, time range, frequency band, channel list, etc.
 */
export interface FkData {
    id: string;
    accepted: boolean;
    frequencyBand: FrequencyBand;
    slownessScale: SlownessScale;
    windowParams: WindowParameters;
    attenuation: number;
    contribChannelIds: string[];
    peak: FkPoint;
    theoretical: FkPoint;
    fkGrid: number[][];
    fstatData: FstatData;
}

/**
 * Input window parameters used to create new Fks
 */
export interface WindowParametersInput {
    windowType: string;
    leadSeconds: number;
    lengthSeconds: number;
}

/**
 * Input type for creating new Fks
 */
export interface NewFkInput {
    sdHypothesisId: string;
    stationId: string;
    phase: string;
    frequencyBand: FrequencyBandInput;
    windowParams: WindowParametersInput;
    contribChannelIds: string[];
}

export interface FkToSdhIdMapping {
    fks: FkData[];
    sdhId: string;
    fkIdPos: number; // ToDo remove this when OSD hooked up
}
