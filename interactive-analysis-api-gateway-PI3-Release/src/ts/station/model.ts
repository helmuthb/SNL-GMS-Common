import { Location, StationType } from '../common/model';

/**
 * Model definitions for the processing network/station/site/channel data API
 */

/**
 * Represents a group of stations used for monitoring.
 * This is a simplified interface used for processing. A richer interface modeling
 * station reference information is provided in the station reference API.
 */
export interface ProcessingNetwork {
    id: string;
    name: string;
    monitoringOrganization: string;
    stationIds: string[];
}

/**
 * Represents an installation of monitoring sensors for the purposes of processing.
 * Multiple sensors can be installed at the same station.
 * This is a simplified interface used for processing. A richer interface modeling
 * station reference information is provided in the station reference API.
 */
export interface ProcessingStation {
    id: string;
    name: string;
    stationType: StationType;
    location: Location;
    siteIds: string[];
    networkIds: string[];
}

/**
 * Represents a physical installation (e.g., building, underground vault, borehole)
 * containing a collection of Instruments that produce Raw Channel waveform data.
 * This is a simplified interface used for processing. A richer interface modeling
 * station reference information is provided in the station reference API.
 */
export interface ProcessingSite {
    id: string;
    name: string;
    location: Location;
    stationId: string;
    channelIds: string[];
}

/**
 * Represents a source for unprocessed (raw) or processed (derived) time series data
 * from a seismic, hydroacoustic, or infrasonic sensor.
 * This is a simplified interface used for processing. A richer interface modeling
 * station reference information is provided in the station reference API.
 */
export interface ProcessingChannel {
    id: string;
    name: string;
    channelType: string;
    locationCode: string;
    siteId: string;
    // TODO consider removing site name once channel IDs are stable in the OSD
    siteName: string;
    verticalAngleDegrees: number;
    horizontalAngleDegrees: number;
    nominalSampleRateHz: number;
}

/**
 * Represents information needed by the API gateway to retrieve the stations included
 * in the default set configured for the analyst user interface, as well as a default
 * channel to show by default for each station.
 */
export interface DefaultStationInfo {
    stationId: string;
    channelId: string;
}
