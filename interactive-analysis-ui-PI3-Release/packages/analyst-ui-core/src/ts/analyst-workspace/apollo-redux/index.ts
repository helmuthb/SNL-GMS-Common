import { InMemoryCache, NormalizedCacheObject } from 'apollo-cache-inmemory';
import { ApolloClient } from 'apollo-client';
import { split } from 'apollo-link';
import { HttpLink } from 'apollo-link-http';
import { WebSocketLink } from 'apollo-link-ws';
import { getMainDefinition } from 'apollo-utilities';
import { SubscriptionClient } from 'subscriptions-transport-ws';

// we can't initialize the websocket client if we aren't running in the browser or a renderer process.
// this shouldn't run in the main electron process.
let windowIsDefined: boolean;
try {
    windowIsDefined = Boolean(window);
} catch (e) {
    windowIsDefined = false;
}

const wsClient = !windowIsDefined ?
    undefined
    : new SubscriptionClient(`ws://${window.location.host}/subscriptions`, {
        reconnect: true
    });

// Create a WebSocket link:
const wsLink = !windowIsDefined ?
    undefined
    : new WebSocketLink(wsClient);

// Create an http link:
const httpLink = new HttpLink({
    uri: '/graphql'
});

// using the ability to split links, you can send data to each link
// depending on what kind of operation is being sent
const link = split(
    // split based on operation type
    ({ query }) => {
        const definition = getMainDefinition(query);
        return definition.kind === 'OperationDefinition' && definition.operation === 'subscription';
    },
    windowIsDefined ? wsLink : httpLink,
    httpLink,
);

/**
 * Create an apollo client with support for subscriptions
 */
export const createApolloClient = () => {

    const client = new ApolloClient<NormalizedCacheObject>({
        link,
        cache: new InMemoryCache({
            addTypename: false,
            dataIdFromObject: (object: any) => object.id || null
        }),
    });

    return {
        apolloClient: client,
        wsClient
    };
};
