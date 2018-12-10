import { WaveformSortType } from '../components/waveform-display/waveform-display-controls';
import { AppState } from './';
import * as Actions from './actions';

const defaultState = (): AppState => ({
    currentTimeInterval: undefined,
    currentProcessingStageIntervalId: undefined,
    selectedEventHypIds: [],
    openEventHypId: undefined,
    selectedSdIds: [],
    selectedSortType: WaveformSortType.distance
});

export const Reducer = (state: AppState = defaultState(), action: Actions.Action<any>): AppState => {
    switch (action.type) {
        case Actions.SET_CURRENT_TIME_INTERVAL:
            return {
                ...state,
                currentTimeInterval: action.payload
            };
        case Actions.SET_CURRENT_PROCESSING_STAGE_INTERVAL_ID:
            return {
                ...state,
                currentProcessingStageIntervalId: action.payload
            };
        case Actions.SET_SELECTED_EVENT_HYP_IDS:
            return {
                ...state,
                selectedEventHypIds: action.payload
            };
        case Actions.SET_OPEN_EVENT_HYP_ID:
            return {
                ...state,
                openEventHypId: action.payload
            };
        case Actions.SET_SELECTED_SD_IDS:
            return {
                ...state,
                selectedSdIds: action.payload
            };
            case Actions.SET_SELECTED_SORT_TYPE:
            return {
                ...state,
                selectedSortType: action.payload
            };
        default:
            return state;
    }
};
