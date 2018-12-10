/**
 * Common model definitions shared across gateway data APIs
 */

/**
 * Represents a location specified using latitude (degrees), longitude (degrees),
 * and altitude (kilometers).
 */
export interface Location {
    latDegrees: number;
    lonDegrees: number;
    elevationKm: number;
}

/**
 * Represents a frequency range
 */
export interface FrequencyBand {
    minFrequencyHz: number;
    maxFrequencyHz: number;
}

/**
 * Frequency band input type
 */
export interface FrequencyBandInput {
    minFrequencyHz: number;
    maxFrequencyHz: number;
}

/**
 * Time range
 */
export interface TimeRange {
    startTime: number;
    endTime: number;
}

/**
 * Enumeration representing the different types of stations in the monitoring network.
 */
export enum StationType {
    Seismic3C = 'Seismic3C',
    SeismicArray = 'SeismicArray',
    HydroacousticArray = 'HydroacousticArray',
    InfrasoundArray = 'InfrasoundArray',
    Invalid = 'Invalid'
}

/**
 * Enumerated set of channel types
 */
export enum ChannelType {
    BroadbandHighGainVertical = 'BroadbandHighGainVertical',
    ShortPeriodLowGainVertical = 'ShortPeriodLowGainVertical',
    ShortPeriodHighGainVertical = 'ShortPeriodHighGainVertical',
    BroadbandHighGainEastWest = 'BroadbandHighGainEastWest',
    ShortPeriodLowGainEastWest = 'ShortPeriodLowGainEastWest',
    ShortPeriodHighGainEastWest = 'ShortPeriodHighGainEastWest',
    BroadbandHighGainNorthSouth = 'BroadbandHighGainNorthSouth',
    ShortPeriodLowGainNorthSouth = 'ShortPeriodLowGainNorthSouth',
    ShortPeriodHighGainNorthSouth = 'ShortPeriodHighGainNorthSouth',
    ExtremelyShortPeriodHydrophone = 'ExtremelyShortPeriodHydrophone',
    ExtremelyShortPeriodHighGainEastWest = 'ExtremelyShortPeriodHighGainEastWest',
    ExtremelyShortPeriodHighGainNorthSouth = 'ExtremelyShortPeriodHighGainNorthSouth',
    ExtremelyShortPeriodHighGainVertical = 'ExtremelyShortPeriodHighGainVertical',
    HighBroadbandHighGainEastWest = 'HighBroadbandHighGainEastWest',
    HighBroadbandHighGainNorthSouth = 'HighBroadbandHighGainNorthSouth',
    HighBroadbandHighGainVertical = 'HighBroadbandHighGainVertical',
    MidPeriodHighGainEastWest = 'MidPeriodHighGainEastWest',
    MidPeriodHighGainNorthSouth = 'MidPeriodHighGainNorthSouth',
    MidPeriodHighGainVertical = 'MidPeriodHighGainVertical'
}

/**
 * Enumerated type of the actor (e.g. analyst, system) associated with a CreationInfo object
 */
export enum CreatorType {
    Analyst = 'Analyst',
    System = 'System'
}

/**
 * Provenance information about the results of data processing completed by the System
 * and actions completed by users of the system
 */
export interface CreationInfo {
    id: string;
    creationTime: number;
    creatorId: string;
    creatorType: CreatorType;

    // TODO
    // The processing step associated with the processing result
    // processingStepId: string;

    // TODO
    // The software component associated with the processing result
    // softwareComponentId: string!
}

/**
 * Represents calibration information associated with a waveform
 */
export interface ProcessingCalibration {
    factor: number;
    factorError: number;
    period: number;
    timeShift: number;
}

/**
 * Represents a generic time series data set output from a channel
 * (derived or raw) within a specified time range.
 */
export interface TimeSeries {
    id: string;
    startTime: number;
    endTime: number;
    sampleRate: number;
    sampleCount: number;
}

/**
 * Represents a time-ordered set of waveform samples output from a channel
 * (derived or raw) within a specified time range.
 */
export interface Waveform extends TimeSeries {
    waveformSamples: Float32Array;
    calibration: ProcessingCalibration;
    // TODO: remove file offset and file path fields
    // once the API gateway is connected to
    // to the OSD back-end (should not be needed)
    fileOffset: number;
    fileName: string;
}

/**
 * Represents the configured type of data source the API Gateway provides access to - values:
 * Local - The API gateway loads data from local file storage for testing purposes
 * Service - The API gateway uses services to provide access to backend (e.g. OSD) data
 */
export enum AccessorDataSource {
    Local = 'Local',
    Service = 'Service'
}

/*
 * Enumerated list of source types used to compute distances to
 */
export enum DistanceSourceType {
    Event = 'Event',
    UserDefined = 'UserDefined'
}

/*
 * Represents a distance measurement relative to a specified source location
 */
export interface DistanceToSource {

    // The distance in kilometers to the source
    distanceKm: number;

    // The source location
    sourceLocation: Location;

    // The type of the source the distance is measured to (e.g. and event)
    sourceType: DistanceSourceType;

    // Optional: the unique ID of the source object
    sourceId?: string;
}
