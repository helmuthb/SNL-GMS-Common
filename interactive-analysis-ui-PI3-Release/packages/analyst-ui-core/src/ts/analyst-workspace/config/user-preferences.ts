import { Colors } from '@blueprintjs/core';

export interface MaskDisplayFilter {
    color: string;
    visible: boolean;
    name: string;
}

export interface QcMaskDisplayFilters {
    ANALYST_DEFINED: MaskDisplayFilter;
    CHANNEL_PROCESSING: MaskDisplayFilter;
    DATA_AUTHENTICATION: MaskDisplayFilter;
    REJECTED: MaskDisplayFilter;
    STATION_SOH: MaskDisplayFilter;
    WAVEFORM_QUALITY: MaskDisplayFilter;
}

export interface UserPreferences {
    colors: {
        events: {
            toWork: string;
            inProgress: string;
            complete: string;
        };
        signalDetections: {
            unassociated: string;
            newDetection: string;
        };
        waveforms: {
            raw: string;
            maskDisplayFilters: QcMaskDisplayFilters;
        };
    };
    map: {
        icons: {
            event: string;
            eventScale: number;
            station: string;
            stationScale: number;
            scaleFactor: number;
            displayDistance: number;
            pixelOffset: number;
        };
        colors: {
            openEvent: string;
            unselectedStation: string;
            selectedEvent: string;
            completeEvent: string;
            toWorkEvent: string;
            outOfIntervalEvent: string;
        };
        widths: {
            unselectedSignalDetection: number;
            selectedSignalDetection: number;
        };
        defaultTo3D: boolean;
    };
    defaultSignalDetectionPhase: string;
    signalDetectionList: {
        autoFilter: boolean;
    };
}

export const userPreferences: UserPreferences = {
    colors: {
        events: {
            toWork: '#ff3333',
            inProgress: Colors.GOLD5,
            complete: Colors.FOREST5
        },
        signalDetections: {
            unassociated: 'lightgray',
            newDetection: '#9b59b6'
        },
        waveforms: {
            raw: Colors.COBALT4,
            maskDisplayFilters: {
                ANALYST_DEFINED: {
                    color: Colors.RED3,
                    visible: true,
                    name: 'Analyst defined'
                },
                CHANNEL_PROCESSING: {
                    color: Colors.TURQUOISE3,
                    visible: true,
                    name: 'Channel processing'
                },
                DATA_AUTHENTICATION: {
                    color: Colors.INDIGO3,
                    visible: true,
                    name: 'Data authentication'
                },
                REJECTED: {
                    color: Colors.LIME3,
                    visible: false,
                    name: 'Rejected'
                },
                STATION_SOH: {
                    color: Colors.ORANGE3,
                    visible: true,
                    name: 'Station SOH'
                },
                WAVEFORM_QUALITY: {
                    color: Colors.VIOLET3,
                    visible: true,
                    name: 'Waveform quality'
                }
            }
        }
    },
    map: {
        icons: {
            event: 'circle-transition.png',
            eventScale: 0.07,
            station: 'outlined-triangle.png',
            stationScale: 0.12,
            scaleFactor: 1.5,
            displayDistance: 1e6,
            pixelOffset: 15
        },
        colors: {
            openEvent: '#ffd800',
            unselectedStation: '#ffffff',
            selectedEvent: '#00ffff',
            completeEvent: '#00ff00',
            toWorkEvent: '#f50a37',
            outOfIntervalEvent: '#cccccc'
        },
        widths: {
            unselectedSignalDetection: 1,
            selectedSignalDetection: 3
        },
        defaultTo3D: false
    },
    defaultSignalDetectionPhase: 'P',
    signalDetectionList: {
        autoFilter: true
    }
};
