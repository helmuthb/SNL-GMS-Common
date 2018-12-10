import gql from 'graphql-tag';

export interface MapDataSignalDetection {
    signalDetection: {
        id: string;
        station: {
            id: string;
            name: string;
            location: {
                latDegrees: number;
                lonDegrees: number;
                elevationKm: number;
            };
        };
    };
    signalDetectionAssociations: {
        id: string;
        isRejected: boolean;
        eventHypothesis: {
            id: string;
            preferredLocationSolution: {
                locationSolution: {
                    latDegrees: number;
                    lonDegrees: number;
                    timeSec: number;
                };
            };
        };
    }[];
}

/**
 * Signal detection data for the map to draw great circle
 */
export interface MapSignalDetectionData {
    signalDetectionHypothesesByStation: MapDataSignalDetection[];
}

/**
 * Input variables for the Map signal detections query
 */
export interface MapSignalDetectionsQueryInput {
    stationIds: string[];
    timeRange: {
        startTime: number;
        endTime: number;
    };
}

export const mapSignalDetectionsQuery = gql`
query signalDetectionHypothesesByStation($stationIds: [String]!, $timeRange: TimeRange!) {
    signalDetectionHypothesesByStation(stationIds: $stationIds, timeRange: $timeRange) {
        id
        signalDetection {
            id
            station {
                id
                name
                location {
                    latDegrees
                    lonDegrees
                    elevationKm
                }
            }
        }
        signalDetectionAssociations {
            id
            isRejected
            eventHypothesis {
                id
                preferredLocationSolution {
                    locationSolution {
                        latDegrees
                        lonDegrees
                        timeSec
                    }
                }
            }
        }
    }
}
`;
