import * as React from 'react';
import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';

import { AnalystWorkspaceState } from '../../state';
import * as Actions from '../../state/actions';
import { createDetectionMutation, rejectDetections, updateDetectionsMutation } from './graphql/mutations';
import { waveformDisplayQuery, WaveformDisplayQueryInput } from './graphql/queries';
import { WaveformDisplay, WaveformDisplayReduxProps } from './waveform-display';
import { WaveformSortType } from './waveform-display-controls';

// map parts of redux state into this component as props
const mapStateToProps = (state: AnalystWorkspaceState): Partial<WaveformDisplayReduxProps> => ({
    currentTimeInterval: state.app.currentTimeInterval,
    currentOpenEventHypId: state.app.openEventHypId,
    selectedSdIds: state.app.selectedSdIds,
    waveformSortType: state.app.selectedSortType
});

// map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<WaveformDisplayReduxProps> => ({
    setSelectedSdIds: (ids: string[]) => {
        dispatch(Actions.setSelectedSdIds(ids));
    },
    setSelectedSortType: (selectedSortType: WaveformSortType) => {
        dispatch(Actions.setSelectedSortType(selectedSortType));
    }
});

/**
 * higher-order component react-redux(react-apollo(WaveformDisplay))
 */
export const ReduxApolloWaveformDisplay: React.ComponentClass<Pick<{}, never>> = compose(
    ReactRedux.connect(mapStateToProps, mapDispatchToProps),
    graphql(waveformDisplayQuery, {
        options: (props: WaveformDisplayReduxProps) => {
            const skip = props.currentTimeInterval === undefined;
            const variables: WaveformDisplayQueryInput | {} = skip ? {}
                : {
                    timeRange: {
                        startTime: String(props.currentTimeInterval.startTimeSecs),
                        endTime: String(props.currentTimeInterval.endTimeSecs),
                    },
                    distanceToSourceInput: {
                        sourceId: props.currentOpenEventHypId,
                        sourceType: 'Event'
                    }
                };
            // work-around to only fetch based on props, if the current time interval is undefined
            return {
                variables,
                fetchPolicy: skip ? 'cache-only' : undefined
            };
        },
    }),
    graphql(createDetectionMutation, { name: 'createDetection' }),
    graphql(updateDetectionsMutation, { name: 'updateDetections' }),
    graphql(rejectDetections, { name: 'rejectDetectionHypotheses' })
)(WaveformDisplay);

export { WaveformDisplay, WaveformDisplayProps } from './waveform-display';
