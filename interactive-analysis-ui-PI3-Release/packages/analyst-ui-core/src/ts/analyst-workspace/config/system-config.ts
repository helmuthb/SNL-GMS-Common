export interface SystemConfig {
    defaultSdPhases: string[];
    defaultWeavessHotKeyOverrides: WeavesHotKeyOverrides;
    defaultFkConfig: FkConfig;
}

export interface WeavesHotKeyOverrides {
    amplitudeScale: string;
    amplitudeScaleSingleReset: string;
    amplitudeScaleReset: string;
}

export interface FkConfig {
    fkVelocityRadii: number[];
}

// TODO hard-coded here for now, eventually this will be read in from the server.
export const systemConfig: SystemConfig = {
    defaultSdPhases: [
        'P', 'Pn', 'PKP', 'PKPbc', 'PcP', 'pP',
        'S', 'Sn', 'LR', 'Lg'
    ],
    defaultWeavessHotKeyOverrides: {
        amplitudeScale: 'a',
        amplitudeScaleSingleReset: 'Control+a',
        amplitudeScaleReset: 'Control+Shift+A',
    },
    defaultFkConfig: {
        // tslint:disable-next-line:no-magic-numbers
        // Fk slowness guide circle radii in s/km (6, 8 & 12 km/s velocity)
        // tslint:disable-next-line:no-magic-numbers
        fkVelocityRadii: [0.1667, 0.125, 0.08333],
    },
};
