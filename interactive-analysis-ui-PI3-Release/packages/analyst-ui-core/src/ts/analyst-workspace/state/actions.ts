import { WaveformSortType } from '../components/waveform-display/waveform-display-controls';
import { TimeInterval } from './';

export const SET_CURRENT_TIME_INTERVAL = 'SET_CURRENT_TIME_INTERVAL';
export const SET_CURRENT_PROCESSING_STAGE_INTERVAL_ID = 'SET_CURRENT_PROCESSING_STAGE_INTERVAL_ID';
export const SET_SELECTED_EVENT_HYP_IDS = 'SET_SELECTED_EVENT_HYP_IDS';
export const SET_OPEN_EVENT_HYP_ID = 'SET_OPEN_EVENT_HYP_ID';
export const SET_SELECTED_SD_IDS = 'SET_SELECTED_SD_IDS';
export const SET_SELECTED_SORT_TYPE = 'SET_SORT_TYPE';

/**
 * definition of a redux action
 */
export interface Action<T> {
    type: String;
    payload: T;
}

// ACTION CREATORS
// *******************************************************

export const setCurrentTimeInterval = (startTimeSecs: number, endTimeSecs: number): Action<TimeInterval> => {
    const payload: TimeInterval = {
        startTimeSecs,
        endTimeSecs
    };
    return {
        type: SET_CURRENT_TIME_INTERVAL,
        payload
    };
};

export const setCurrentProcessingStageIntervalId = (id: string): Action<string> => ({
    type: SET_CURRENT_PROCESSING_STAGE_INTERVAL_ID,
    payload: id
});

export const setSelectedEventHypIds = (ids: string[]): Action<string[]> => ({
    type: SET_SELECTED_EVENT_HYP_IDS,
    payload: ids
});

export const setOpenEventHypId = (id: string): Action<string> => ({
    type: SET_OPEN_EVENT_HYP_ID,
    payload: id
});

export const setSelectedSdIds = (ids: string[]): Action<string[]> => ({
    type: SET_SELECTED_SD_IDS,
    payload: ids
});

export const setSelectedSortType = (selectedSortType: WaveformSortType): Action<WaveformSortType> => ({
    type: SET_SELECTED_SORT_TYPE,
    payload: selectedSortType
});
