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

export const ActivityIntervalElectronContextMenu = (markInterval: IntervalMarker): any => {

    const ElectronMenu = electron.remote.Menu;
    const template = [
        {
            label: 'Open Activity',
            click: () => markInterval(IntervalStatus.InProgress)
        }, {
            label: 'Mark Activity Complete',
            click: () => markInterval(IntervalStatus.Complete)
        }, {
            label: 'Mark Activity Not Complete',
            click: () => markInterval(IntervalStatus.NotComplete)
        }, {
            type: 'separator'
        }, {
            label: 'Subdivide Activity',
            enabled: false
        }, {
            label: 'Add Note...',
            enabled: false
        }
    ];
    return ElectronMenu.buildFromTemplate(template);
};

export const ActivityIntervalBlueprintContextMenu = (markInterval: IntervalMarker): JSX.Element => (
    <Menu>
        <MenuItem
            text="Open Activity"
            onClick={() => markInterval(IntervalStatus.InProgress)}
        />
        <MenuItem
            text="Mark Activity Complete"
            onClick={() => markInterval(IntervalStatus.Complete)}
        />
        <MenuItem
            text="Mark Activity Not Complete"
            onClick={() => markInterval(IntervalStatus.NotComplete)}
        />
        <MenuDivider />
        <MenuItem
            text="Subdivide Activity..."
            disabled={true}
        />
        <MenuItem
            text="Add Note..."
            disabled={true}
        />
    </Menu>
);
