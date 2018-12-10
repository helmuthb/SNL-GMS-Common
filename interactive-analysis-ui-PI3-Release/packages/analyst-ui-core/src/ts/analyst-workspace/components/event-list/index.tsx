import * as React from 'react';
import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';

import { analystUiConfig } from '../../config';
import { AnalystWorkspaceState } from '../../state';
import * as Actions from '../../state/actions';
import { EventList, EventListReduxProps } from './event-list';
import { updateEventsMutation } from './graphql/mutations';
import { eventListQuery, EventListQueryInput } from './graphql/query';
// import { createDetectionMutation, updateDetectionsMutation, rejectDetectionHypotheses } from './graphql/mutations';

// map parts of redux state into this component as props
const mapStateToProps = (state: AnalystWorkspaceState): Partial<EventListReduxProps> => ({
    currentTimeInterval: state.app.currentTimeInterval,
    openEventHypId: state.app.openEventHypId,
    selectedEventHypIds: state.app.selectedEventHypIds
});

// map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<EventListReduxProps> => ({
    setOpenEventHypId: (id: string) => {
        dispatch(Actions.setOpenEventHypId(id));
    },
    setSelectedEventHypIds: (ids: string[]) => {
        dispatch(Actions.setSelectedEventHypIds(ids));
    }
});

/**
 * higher-order component react-redux(react-apollo(EventList))
 */
export const ReduxApolloEventList: React.ComponentClass<Pick<{}, never>> = compose(
    ReactRedux.connect(mapStateToProps, mapDispatchToProps),
    graphql(eventListQuery, {
        options: (props: EventListReduxProps) => {
            const skip = props.currentTimeInterval === undefined;
            // get events before & after the current interval as well
            const deltaSecs: number = analystUiConfig.environment.additionalTimeToLoad;
            const variables: EventListQueryInput | {} = skip ? {}
            : {
                timeRange: {
                    startTime: String(props.currentTimeInterval.startTimeSecs - deltaSecs),
                    endTime: String(props.currentTimeInterval.endTimeSecs + deltaSecs),
                }
            };
            // work-around to only fetch based on props, if the current time interval is undefined
            return {
                variables,
                fetchPolicy: skip ? 'cache-only' : undefined
            };
        },
    }),
    graphql(updateEventsMutation, { name: 'updateEvents' }),
)(EventList);

export { EventList, EventListProps} from './event-list';
