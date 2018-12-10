import { Colors, Intent, NonIdealState, Spinner } from '@blueprintjs/core';
import * as GoldenLayout from '@gms/golden-layout';
import * as elementResizeEvent from 'element-resize-event';
import * as lodash from 'lodash';
import * as React from 'react';
import { Provider } from 'react-redux';
import * as Redux from 'redux';
declare var require;
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const electron = require('electron');
import { NormalizedCacheObject } from 'apollo-cache-inmemory';
import { ApolloClient } from 'apollo-client';
import { ApolloProvider } from 'react-apollo';
import { SubscriptionClient } from 'subscriptions-transport-ws';

import { createApolloClient } from './apollo-redux';
import { createStore } from './apollo-redux/create-store';
import { AzimuthSlowness, EventList, Map, SignalDetectionList, WaveformDisplay, Workflow } from './components';
import { analystUiConfig } from './config';
import { AnalystLogger, showLogPopup } from './util/log/analyst-logger';
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const logo = require('./img/gms-logo.png');

/**
 * Wrap the component in an apollo provider
 * @param Component // component
 */
export function ApolloProviderWrapped(Component: React.ComponentClass, store: Redux.Store<any>) {

    const { apolloClient } = createApolloClient();

    return class extends React.Component<any, any> {
        /**
         * Wrap the component in an apollo provider
         */
        public render() {
            return (
                <ApolloProvider
                    client={apolloClient}
                >
                    <Provider
                        store={store}
                    >
                        <Component {...this.props} />
                    </Provider>
                </ApolloProvider>
            );
        }
    };
}

export interface AnalystWorkspaceState {
    wsConnected: boolean;
}

/**
 * Primary analyst workspace component. Uses golden-layout to create a configurable display of multiple 
 * sub-components.
 */
export class AnalystWorkspace extends React.Component<{}, AnalystWorkspaceState> {

    /**
     * Handle to the dom element where we will render the golden-layout workspace
     */
    private glContainerRef: HTMLDivElement;
    private gl: GoldenLayout;

    private readonly store: Redux.Store<any>;
    private readonly client: ApolloClient<NormalizedCacheObject>;
    private readonly wsClient: SubscriptionClient;

    public constructor(props) {
        super(props);
        const { apolloClient, wsClient } = createApolloClient();
        this.client = apolloClient;
        this.wsClient = wsClient;
        this.store = createStore();
        this.state = {
            wsConnected: true
        };
    }

    /**
     * Create the analyst workspace
     */
    public render() {
        return (
            <div
                style={{
                    display: 'flex',
                    height: '100%',
                    width: '100%',
                    WebkitUserSelect: 'none',
                    flexDirection: 'column',
                }}
                className="pt-dark"
            >
                <nav
                    className="pt-navbar .modifier"
                    style={{
                        backgroundColor: Colors.DARK_GRAY2,
                        borderBottom: `2px solid ${Colors.BLACK}`,
                        height: '35px'
                    }}
                >
                    <div className="pt-navbar-group pt-align-left" style={{ height: '33px' }} >
                        <img
                            src={logo}
                            alt=""
                            // tslint:disable-next-line:no-magic-numbers
                            height={33}
                            style={{ filter: 'invert(100%)' }}
                        />
                        <span style={{ marginLeft: '0.25rem' }}>GMS</span>
                    </div>
                    <div className="pt-navbar-group pt-align-right" style={{ height: '35px' }}>
                        <button
                            className="pt-button pt-minimal"
                            onClick={showLogPopup}
                        >
                            Logs
                        </button>
                        <button
                            className="pt-button pt-minimal"
                            onClick={() => { localStorage.removeItem('gms-analyst-ui-layout'); location.reload(); }}
                        >
                            Clear stored layout
                        </button>
                    </div>
                </nav>
                <div
                    style={{
                        display: 'flex',
                        flex: '1 1 auto',
                        height: '100%',
                        width: '100%',
                        maxWidth: '100%',
                        position: 'relative',
                    }}
                    className="pt-dark"
                >
                    {
                        !this.state.wsConnected ?
                            (
                                <div
                                    style={{
                                        position: 'absolute',
                                        top: '0px',
                                        right: '0px',
                                        bottom: '0px',
                                        left: '0px',
                                        zIndex: 100,
                                        backgroundColor: 'rgba(0,0,0,0.85)',
                                        display: 'flex',
                                        justifyContent: 'center',
                                        alignItems: 'center'
                                    }}
                                >
                                    <NonIdealState
                                        visual="error"
                                        action={<Spinner intent={Intent.DANGER} />}
                                        className="pt-intent-danger"
                                        title="No connection to server..."
                                        description="Attempting to connect..."
                                    />
                                </div>
                            )
                            : null
                    }
                    <div
                        style={{
                            width: 'calc(100% - 50px)',
                            maxWidth: '100%',
                            flex: '1 1 auto',
                        }}
                        ref={ref => { this.glContainerRef = ref; }}
                    />
                </div>
            </div>
        );
    }

    /**
     * On mount, initialize the golden-layout workspace
     */
    public componentDidMount() {
        this.configureGoldenLayout();
        this.registerWsClientEvents();
        AnalystLogger.info('componentDidMount');
    }

    /**
     * configure & initialize the golden-layout workspace
     */
    private configureGoldenLayout() {
        if (this.gl) {
            this.destroyGl();
        }

        const savedConfig = localStorage.getItem('gms-analyst-ui-layout');
        if (savedConfig) {
            try {
                this.gl = new GoldenLayout(JSON.parse(savedConfig), this.glContainerRef);
                // if an update has changed the names of components, for example, need to start at default again
            } catch (e) {
                this.gl = new GoldenLayout(analystUiConfig.workspace, this.glContainerRef);
            }
        } else {
            this.gl = new GoldenLayout(analystUiConfig.workspace, this.glContainerRef);
        }

        const resizeDebounceMillis = 100;

        elementResizeEvent(this.glContainerRef, lodash.debounce(
            () => {
                this.gl.updateSize();
            },
            resizeDebounceMillis));

        this.gl.registerComponent(
            analystUiConfig.components.waveformDisplay.component,
            ApolloProviderWrapped(WaveformDisplay, this.store));
        this.gl.registerComponent(
            analystUiConfig.components.eventList.component,
            ApolloProviderWrapped(EventList, this.store));
        this.gl.registerComponent(
            analystUiConfig.components.signalDetectionList.component,
            ApolloProviderWrapped(SignalDetectionList, this.store));
        this.gl.registerComponent(
            analystUiConfig.components.workflow.component,
            ApolloProviderWrapped(Workflow, this.store));
        this.gl.registerComponent(
            analystUiConfig.components.map.component,
            ApolloProviderWrapped(Map, this.store));
        this.gl.registerComponent(
            analystUiConfig.components.azimuthSlowness.component,
            ApolloProviderWrapped(AzimuthSlowness, this.store));
        this.gl.init();
        this.gl.updateSize();

        (this.gl as any).on('stateChanged', () => {
            if (electron) electron.ipcRenderer.send('state-changed');
            if (this.gl.isInitialised) {
                const state = JSON.stringify(this.gl.toConfig());
                localStorage.setItem('gms-analyst-ui-layout', state);
            }
        });
    }

    private readonly destroyGl = () => {
        this.gl.destroy();
        this.client.resetStore()
            .catch();
    }

    private readonly registerWsClientEvents = () => {
        this.wsClient.on('disconnected', () => {
            this.setState({
                wsConnected: false
            });
        });
        this.wsClient.on('reconnected', () => {
            this.setState({
                wsConnected: true
            });
            // TODO be smarter about this, try and maintain user state & reload as necessary to make up for lost
            // subscription time
            window.location.reload();
            // this.configureGoldenLayout()
        });
    }
}
