import { Menu, MenuItem } from '@blueprintjs/core';
import * as React from 'react';
declare var require;
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const electron = require('electron');

import { generateSetPhaseBlueprintMenuItems, generateSetPhaseElectronMenuTemplate } from './set-phase';

/**
 * a callback which executes re-phase logic
 */
export interface DetectionRePhaser {
    (sdIds: string[], phase: string): void;
}

export interface DetectionRejecter {
    (sdIds: string[]): void;
}

export const WfDisplayElectronContextMenu = (selectedSdIds: string[], rePhaseDetections: DetectionRePhaser,
                                             rejectDetections: DetectionRejecter): any => {

    const ElectronMenu = electron.remote.Menu;
    const template = [
        {
            label: 'Set Phase...',
            accelerator: 'CommandOrControl+S',
            // TODO this doesn't appear to disable it... figure out why
            enabled: selectedSdIds.length > 0,
            submenu: selectedSdIds.length > 0 ?
                generateSetPhaseElectronMenuTemplate(selectedSdIds, rePhaseDetections)
                : undefined
        }, {
            label: 'Reject',
            accelerator: 'Delete',
            enabled: selectedSdIds.length > 0,
            click: () => rejectDetections(selectedSdIds)
        }
    ];
    return ElectronMenu.buildFromTemplate(template);
};

export const WfDisplayBlueprintContextMenu = (selectedSdIds: string[], rePhaseDetections: DetectionRePhaser,
                                              rejectDetections: DetectionRejecter): JSX.Element => (

        <Menu>
            <MenuItem
                text="Set Phase..."
                label={'Ctrl+s'}
                disabled={selectedSdIds.length === 0}
            >
                {
                    generateSetPhaseBlueprintMenuItems(selectedSdIds, rePhaseDetections)
                }
            </MenuItem>
            <MenuItem
                text="Reject"
                label={'Delete'}
                disabled={selectedSdIds.length === 0}
                onClick={() => rejectDetections(selectedSdIds)}
            />
        </Menu>
    );

export { SetPhaseElectronContextMenu, SetPhaseBlueprintContextMenu } from './set-phase';
