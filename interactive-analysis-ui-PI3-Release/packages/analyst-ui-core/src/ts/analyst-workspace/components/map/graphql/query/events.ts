import gql from 'graphql-tag';

export interface MapEventHypothesis {
    id: string;
    isRejected: boolean;
    event: {
        id: string;
        status: string;
    };
    preferredLocationSolution: {
        locationSolution: {
            latDegrees: number;
            lonDegrees: number;
            depthKm: number;
            timeSec: number;
            networkMagnitudeSolutions: {
                magnitudeType: string;
                magnitude: number;
            }[];
        };
    };
}

/**
 * Event data necessary for the map to know about
 */
export interface MapEventData {
    eventHypothesesInTimeRange: MapEventHypothesis[];
}

/**
 * Input variables for the Map events query
 */
export interface MapEventsQueryInput {
    timeRange: {
        startTime: number;
        endTime: number;
    };
}

export const mapEventsQuery = gql`
query eventHypothesesInTimeRange($timeRange: TimeRange!) {
    eventHypothesesInTimeRange(timeRange: $timeRange) {
        id
        isRejected
        event {
            id
            status
        }
        preferredLocationSolution {
            locationSolution {
                latDegrees
                lonDegrees
                depthKm
                timeSec
                networkMagnitudeSolutions {
                    magnitudeType
                    magnitude
                }
            }
        }
    }
}
`;
