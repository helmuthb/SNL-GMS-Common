import { Menu, MenuItem } from '@blueprintjs/core';
import * as React from 'react';
declare var require;
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const electron = require('electron');

import { systemConfig } from '../../../config';
import { DetectionRePhaser } from './';

export const generateSetPhaseElectronMenuTemplate = (sdIds: string[], rePhaseDetections: DetectionRePhaser) =>
    systemConfig.defaultSdPhases.map(phase => ({
        label: phase,
        click: () => rePhaseDetections(sdIds, phase)
    }));

export const generateSetPhaseBlueprintMenuItems = (sdIds: string[], rePhaseDetections: DetectionRePhaser) => {
    const MenuItems: JSX.Element[] = systemConfig.defaultSdPhases.map(phase => (
        <MenuItem
            key={phase}
            text={phase}
            onClick={() => rePhaseDetections(sdIds, phase)}
        />
    ));
    return MenuItems;
};

export const SetPhaseElectronContextMenu = (sdIds: string[], rePhaseDetections: DetectionRePhaser) => {
    const ElectronMenu = electron.remote.Menu;
    return ElectronMenu.buildFromTemplate(generateSetPhaseElectronMenuTemplate(sdIds, rePhaseDetections));
};

export const SetPhaseBlueprintContextMenu = (sdIds: string[], rePhaseDetections: DetectionRePhaser) => (
    <Menu>
        {generateSetPhaseBlueprintMenuItems(sdIds, rePhaseDetections)}
    </Menu>
);
