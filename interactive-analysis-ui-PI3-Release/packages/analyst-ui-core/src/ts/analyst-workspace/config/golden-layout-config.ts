import * as GoldenLayout from '@gms/golden-layout';
declare var require;
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const electron = require('electron');

export interface ComponentList {
    [componentKey: string]: {
        type: string;
        title: string;
        component: string;
    };
}

export const componentList: ComponentList = {
    waveformDisplay: {
        type: 'react-component',
        title: 'Waveform Display',
        component: 'waveform-display'
    },
    eventList: {
        type: 'react-component',
        title: 'Event List',
        component: 'event-list'
    },
    signalDetectionList: {
        type: 'react-component',
        title: 'Signal Detection List',
        component: 'signal-detection-list'
    },
    workflow: {
        type: 'react-component',
        title: 'Workflow',
        component: 'workflow'
    },
    map: {
        type: 'react-component',
        title: 'Map',
        component: 'map'
    },
    azimuthSlowness: {
        type: 'react-component',
        title: 'Azimuth Slowness',
        component: 'azimuth-slowness'
    }
};

export const defaultGoldenLayoutConfig: GoldenLayout.Config = {
    settings: {
        showPopoutIcon: Boolean(electron),
        showMaximiseIcon: true,
        showCloseIcon: true,
    },
    content: [{
        type: 'row',
        content: [
            {
                type: 'column',
                content: [{
                    ...componentList.map,
                    height: 60
                }, {
                    type: 'stack',
                    content: [{
                        ...componentList.eventList
                    },
                    {
                        ...componentList.signalDetectionList
                    },
                    {
                        ...componentList.azimuthSlowness
                    }]
                }],
                width: 60
            }, {
                type: 'column',
                content: [{
                    ...componentList.workflow
                }, {
                    ...componentList.waveformDisplay,
                    height: 70,
                }]
            }
        ]

    }],
    dimensions: {
        borderWidth: 2,
        minItemHeight: 30,
        minItemWidth: 30,
    }
};
