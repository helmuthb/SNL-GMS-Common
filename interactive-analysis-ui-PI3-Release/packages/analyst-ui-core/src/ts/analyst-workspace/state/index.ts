import { WaveformSortType } from '../components/waveform-display/waveform-display-controls';

/**
 * a simple time interval
 */
export interface TimeInterval {
    startTimeSecs: number;
    endTimeSecs: number;
}

/**
 * Top-level redux state for the Analyst UI
 */
export interface AnalystWorkspaceState {
    app: AppState;
}

export interface AppState {
    currentTimeInterval: TimeInterval;
    currentProcessingStageIntervalId: string;
    selectedEventHypIds: string[];
    openEventHypId: string;
    selectedSdIds: string[];
    selectedSortType: WaveformSortType;
}
