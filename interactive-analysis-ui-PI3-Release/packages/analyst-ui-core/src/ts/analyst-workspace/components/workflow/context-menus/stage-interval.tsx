import { Menu, MenuDivider, MenuItem } from '@blueprintjs/core';
import * as React from 'react';
declare var require;
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const electron = require('electron');
import { IntervalStatus } from '../';

/**
 * function which modifies an interval status
 */
export interface IntervalMarker {
    (status: IntervalStatus): void;
}

export const StageIntervalElectronContextMenu = (markInterval: IntervalMarker): any => {

    const ElectronMenu = electron.remote.Menu;
    const template = [
        {
            label: 'Open Stage Interval',
            click: () => markInterval(IntervalStatus.InProgress)
        }, {
            label: 'Mark Stage Interval Complete',
            click: () => markInterval(IntervalStatus.Complete)
        }, {
            type: 'separator'
        }, {
            label: 'Add Note...',
            enabled: false
        }
    ];
    return ElectronMenu.buildFromTemplate(template);
};

export const StageIntervalBlueprintContextMenu = (markInterval: IntervalMarker): JSX.Element => (
    <Menu>
        <MenuItem
            text="Open Stage Interval"
            onClick={() => markInterval(IntervalStatus.InProgress)}
        />
        <MenuItem
            text="Mark Stage Interval Complete"
            onClick={() => markInterval(IntervalStatus.Complete)}
        />
        <MenuDivider />
        <MenuItem
            text="Add Note..."
            disabled={true}
        />
    </Menu>
);
