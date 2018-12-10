import '@blueprintjs/core/src/blueprint.scss';
import '@blueprintjs/datetime/src/blueprint-datetime.scss';
import '@blueprintjs/icons/src/blueprint-icons.scss';
import 'cesium/Build/Cesium/Widgets/widgets.css';
import * as JQuery from 'jquery';
import * as React from 'react';
import * as ReactDom from 'react-dom';
import '../css/ag-grid-blueprint-theme.scss';
import '../css/ag-grid.scss';
import '../css/azimuth-slowness.scss';
import '../css/event-list.scss';
import '../css/goldenlayout-base.scss';
import '../css/goldenlayout-blueprint-theme.scss';
import '../css/style.scss';
import '../css/workflow.scss';
// required for golden-layout
(window as any).React = React;
(window as any).ReactDOM = ReactDom;
(window as any).$ = JQuery;
import { Colors } from '@blueprintjs/core';
import { HashRouter, Route, Switch } from 'react-router-dom';
declare var require;
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const electron = require('electron');

import { AnalystWorkspace, ApolloProviderWrapped } from './analyst-workspace';
import { createStore } from './analyst-workspace/apollo-redux/create-store';
import {
    AzimuthSlowness,
    EventList,
    LoadingScreen,
    Map,
    SignalDetectionList,
    WaveformDisplay,
    Workflow
} from './analyst-workspace/components';
import {
    StandaloneAzimuthSlowness,
    StandaloneFkThumbnails
} from './analyst-workspace/components/azimuth-slowness/unit-tests';

import { AnalystLogger } from './analyst-workspace/util/log/analyst-logger';

window.onload = () => {
    if (!window.navigator.userAgent.includes('Chrome')) {
        window.alert(`GMS Interactive Analysis currently supports
            Google Chrome > v59. You will likely experience degraded performance`);
    }

    ReactDom.render(
        (
            <HashRouter>
                <Switch>
                    <Route
                        path="/loading"
                        component={LoadingScreen}
                    />
                    <Route
                        path="/waveform-display"
                        component={props => createPopoutComponent(WaveformDisplay, props)}
                    />
                    <Route
                        path="/event-list"
                        component={props => createPopoutComponent(EventList, props)}
                    />
                    <Route
                        path="/signal-detection-list"
                        component={props => createPopoutComponent(SignalDetectionList, props)}
                    />
                    <Route
                        path="/workflow"
                        component={props => createPopoutComponent(Workflow, props)}
                    />
                    <Route
                        path="/map"
                        component={props => createPopoutComponent(Map, props)}
                    />
                    <Route
                        path="/azimuth-slowness"
                        component={props => createPopoutComponent(AzimuthSlowness, props)}
                    />
                    <Route
                        path="/test/fk-thumbnails"
                        component={StandaloneFkThumbnails}
                    />
                    <Route
                        path="/test/fk"
                        component={StandaloneAzimuthSlowness}
                    />
                    <Route
                        path="/"
                        component={AnalystWorkspace}
                    />
                </Switch>
            </HashRouter>
        ),
        document.getElementById('app')
    );
};

if (electron) {
    electron.ipcRenderer.on('load-path', (event, newHash: string) => {
        window.location.hash = newHash;
    });
}

/**
 * Wrap the component with everything it needs to live standalone as a popout
 */
function createPopoutComponent(Component: any, props: any) {
    AnalystLogger.info('createPopoutComponent');
    AnalystLogger.debug('createPopoutComponent');
    AnalystLogger.warn('createPopoutComponent');
    AnalystLogger.error('createPopoutComponent');

    // const { apolloClient } = createApolloClient();
    const store = createStore();

    const WrappedComponent: any = ApolloProviderWrapped(Component, store);

    const PopoutComponent = class extends React.Component<any, {}> {
        /**
         * Create the pop-out wrapper component
         */
        public render() {
            return (
                <div
                    style={{
                        width: '100%',
                        height: '100%',
                        backgroundColor: Colors.DARK_GRAY2
                    }}
                    className="pt-dark"
                >
                    <WrappedComponent
                        {...this.props}
                    />
                    {
                        // only show pop-in button if running in electron
                        electron ?
                            <div
                                className="lm_popin"
                                title="pop-in"
                                onClick={() => {
                                    electron.ipcRenderer.send(
                                        'popin-window', electron.remote.getCurrentWebContents().popoutConfig);
                                    electron.remote.getCurrentWindow()
                                        .close();
                                }}
                            >
                                <div className="lm_icon" />
                                <div className="lm_bg" />
                            </div>
                            : undefined
                    }
                </div>
            );
        }
    };

    return (
        <PopoutComponent {...props} />
    );
}
