import * as React from 'react';
import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';

import { AnalystWorkspaceState } from '../../state';
import * as Actions from '../../state/actions';
import { AzimuthSlowness, AzimuthSlownessReduxProps } from './azimuth-slowness';
import { createFkMutation, updateAzSlowFromFkMutation } from './graphql/mutations';
import { eventFkDataQuery, EventFkDataQueryInput } from './graphql/query';

// map parts of redux state into this component as props
const mapStateToProps = (state: AnalystWorkspaceState): Partial<AzimuthSlownessReduxProps> => ({
    currentTimeInterval: state.app.currentTimeInterval,
    selectedSdIds: state.app.selectedSdIds,
    openEventId: state.app.openEventHypId,
    selectedSortType: state.app.selectedSortType
});

// map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<AzimuthSlownessReduxProps> => ({
    setSelectedSdIds: (ids: string[]) => {
        dispatch(Actions.setSelectedSdIds(ids));
    }
});

/**
 * higher-order component react-redux(react-apollo(AzimuthSlowness))
 */
export const ReduxApolloAzimuthSlowness: React.ComponentClass<Pick<{}, never>> = compose(
    ReactRedux.connect(mapStateToProps, mapDispatchToProps),
    graphql(eventFkDataQuery, {
        options: (props: AzimuthSlownessReduxProps) => {
            const skip = props.currentTimeInterval == undefined || props.openEventId == undefined;
            const variables: EventFkDataQueryInput | {} = skip ? {}
            : {
                eventId: Number(props.openEventId) // TODO swtich to string
            };
            // work-around to only fetch based on props, if the current time interval is undefined
            return {
                variables,
                fetchPolicy: skip ? 'cache-only' : undefined
            };
        },
    }),
    graphql(createFkMutation, { name: 'createFk' }),
    graphql(updateAzSlowFromFkMutation, { name: 'updateAzSlowFromFkMutation' })
)(AzimuthSlowness);

export { FkThumbnails } from './fk-thumbnails';
export { AzimuthSlowness } from './azimuth-slowness';
