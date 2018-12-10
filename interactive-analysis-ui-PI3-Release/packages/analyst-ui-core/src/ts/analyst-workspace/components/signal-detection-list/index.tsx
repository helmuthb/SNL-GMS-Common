import * as React from 'react';
import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';

import { AnalystWorkspaceState } from '../../state';
import * as Actions from '../../state/actions';
import { updateDetectionsMutation } from './graphql/mutations';
import { signalDetectionListQuery, SignalDetectionListQueryInput } from './graphql/query';
import { SignalDetectionList, SignalDetectionListReduxProps } from './signal-detection-list';

// map parts of redux state into this component as props
const mapStateToProps = (state: AnalystWorkspaceState): Partial<SignalDetectionListReduxProps> => ({
    currentTimeInterval: state.app.currentTimeInterval,
    selectedSdIds: state.app.selectedSdIds,
    openEventHypId: state.app.openEventHypId
});

// map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<SignalDetectionListReduxProps> => ({
    setSelectedSdIds: (ids: string[]) => {
        dispatch(Actions.setSelectedSdIds(ids));
    }
});

/**
 * higher-order component react-redux(react-apollo(SignalDetectionList))
 */
export const ReduxApolloSignalDetectionList: React.ComponentClass<Pick<{}, never>> = compose(
    ReactRedux.connect(mapStateToProps, mapDispatchToProps),
    graphql(signalDetectionListQuery, {
        options: (props: SignalDetectionListReduxProps) => {
            const skip = props.currentTimeInterval === undefined;
            // get signal detections in the current interval
            const variables: SignalDetectionListQueryInput | {} = skip ? {}
                : {
                    timeRange: {
                        startTime: String(props.currentTimeInterval.startTimeSecs),
                        endTime: String(props.currentTimeInterval.endTimeSecs),
                    }
                };
            // work-around to only fetch based on props, if the current time interval is undefined
            return {
                variables,
                fetchPolicy: skip ? 'cache-only' : undefined
            };
        },
    }),
    graphql(updateDetectionsMutation, { name: 'updateDetections' })
)(SignalDetectionList);

export { SignalDetectionList, SignalDetectionListProps } from './signal-detection-list';
