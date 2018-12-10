import gql from 'graphql-tag';
import { MaskData } from './qc-masks';

/**
 * Location structure used by DistanceToSource
 */
export interface Location {
    latDegrees: number;
    lonDegrees: number;
    elevationKm: number;
}

export interface HypothesisData {
    id: string;
    phase: string;
    isRejected: boolean;
    arrivalTimeMeasurement: {
        id: string;
        featureType: string;
        timeSec: number;
        uncertaintySec: number;
    };
    signalDetectionAssociations: {
        id: string;
        isRejected: boolean;
        eventHypothesis: {
            id: string;
            event: {
                id: string;
            };
        };
    }[];
    creationInfo: {
        id: string;
        creationTime: number;
        creatorId: string;
        creatorType: string;
    };
}

export interface SignalDetection {
    id: string;
    currentHypothesis: HypothesisData;
    hypotheses: HypothesisData[];
}

/**
 * Data to populate the waveform display
 */
export interface WaveformDisplayData {
    defaultStations: {
        id: string;
        name: string;
        defaultChannel: {
            id: string;
            name: string;
            channelType: string;
        };
        sites: {
            name: string;
            id: string;
            channels: {
                id: string;
                name: string;
                channelType: string;
                qcMasks: {
                    id: string;
                    channelId: string;
                    currentVersion: MaskData;
                    qcMaskVersions: MaskData[];
                }[];
            }[];
        }[];
        signalDetections: SignalDetection[];
        distanceToSource: {
            distanceKm: number;
            sourceType: string;
            sourceId: string;
            sourceLocation: Location;
        };
    }[];
    eventHypothesesInTimeRange: {
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
            };
        };
        signalDetectionAssociations: {
            isRejected: boolean;
            signalDetectionHypothesis: {
                id: string;
                phase: string;
                isRejected: boolean;
                arrivalTimeMeasurement: {
                    id: string;
                    featureType: string;
                    timeSec: number;
                    uncertaintySec: number;
                };
            };
        }[];
    }[];
}

/**
 * Input variables for the Waveform display query
 */
export interface WaveformDisplayQueryInput {
    timeRange: {
        startTime: number;
        endTime: number;
    };
    distanceToSourceInput?: DistanceToSourceInput;
}

/**
 * Input variables for the Station Distance Source query
 */
export interface DistanceToSourceInput {
    sourceType?: string;
    sourceId?: string;
    sourceLocation?: Location;
}

export const waveformDisplayQuery = gql`
    query defaultStations($timeRange: TimeRange!, $distanceToSourceInput: DistanceToSourceInput) {
        defaultStations {
        id
        name
        defaultChannel {
            id
            name
            channelType
        }
        sites {
            id
            name
            channels {
                id
                name
                channelType
                qcMasks(timeRange: $timeRange) {
                    id,
                    channelId,
                    currentVersion {
                        version
                        creationInfo {
                            id
                            creationTime
                            creatorId
                            creatorType
                        },
                        channelSegmentIds,
                        category,
                        type,
                        startTime,
                        endTime,
                        rationale
                    },
                    qcMaskVersions {
                        version
                        creationInfo {
                            id
                            creationTime
                            creatorId
                            creatorType
                        },
                        channelSegmentIds,
                        category,
                        type,
                        startTime,
                        endTime,
                        rationale
                    }
                }
            }
        }
        signalDetections(timeRange: $timeRange) {
            id
            currentHypothesis {
                id
                phase
                isRejected
                arrivalTimeMeasurement {
                    id
                    featureType
                    timeSec
                    uncertaintySec
                }
                signalDetectionAssociations {
                    id
                    isRejected
                    eventHypothesis {
                        id
                        event {
                            id
                        }
                    }
                }
                creationInfo {
                    id
                    creationTime
                    creatorId
                    creatorType
                }
            }
            hypotheses {
                id
                phase
                isRejected
                arrivalTimeMeasurement {
                    id
                    featureType
                    timeSec
                    uncertaintySec
                }
                signalDetectionAssociations {
                    id
                    isRejected
                    eventHypothesis {
                        id
                        event {
                            id
                        }
                    }
                }
                creationInfo {
                    id
                    creationTime
                    creatorId
                    creatorType
                }
            }
        }
        distanceToSource(distanceToSourceInput: $distanceToSourceInput) {
            distanceKm
            sourceType
            sourceId
            sourceLocation {
              latDegrees
              lonDegrees
              elevationKm
            }
        }
    }
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
            }
        }
        signalDetectionAssociations {
            isRejected
            signalDetectionHypothesis {
                id
                phase
                isRejected
                arrivalTimeMeasurement {
                    id
                    featureType
                    timeSec
                    uncertaintySec
                }
            }
        }
    }
}
`;
