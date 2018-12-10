import * as moment from 'moment';
import * as React from 'react';
import { systemConfig } from '../../config';
import { DetectionColorCellRenderer } from './detection-color-cell-renderer';
import { FilterType } from './signal-detection-list';

/**
 * Column Definitions for Signal Detection List
 */
export function getColumnDefs() {
    return [
        {
            cellStyle: { display: 'flex', 'justify-content': 'center' },
            width: 50,
            field: 'color',
            headerName: '',
            cellRendererFramework: DetectionColorCellRenderer,
        }, {
            headerName: 'Hyp ID',
            field: 'hypothesisId',
            cellStyle: { 'text-align': 'center' },
            enableCellChangeFlash: true,
            width: 100,
        }, {
            headerName: 'Time',
            field: 'time',
            cellStyle: { 'text-align': 'center' },
            width: 100,
            enableCellChangeFlash: true,
            editable: true,
            valueFormatter: e => moment.unix(e.data.time)
                .utc()
                .format('HH:mm:ss')
        }, {
            headerName: 'Station',
            field: 'station',
            width: 70,
            cellStyle: { 'text-align': 'left' },
        }, {
            headerName: 'Phase',
            field: 'phase',
            editable: true,
            cellEditor: 'agSelectCellEditor',
            cellEditorParams: {
                values: systemConfig.defaultSdPhases
            },
            cellStyle: { 'text-align': 'left' },
            enableCellChangeFlash: true,
            width: 60
        }, {
            headerName: 'Time Unc',
            field: 'timeUnc',
            editable: true,
            cellStyle: { 'text-align': 'right' },
            enableCellChangeFlash: true,
            width: 80,
        }, {
            headerName: 'Assoc Evt ID',
            field: 'assocEventId',
            cellStyle: { 'text-align': 'center' },
            enableCellChangeFlash: true,
            width: 100,
            valueFormatter: e => e.data.assocEventId ? e.data.assocEventId : 'N/A'
        }, {
            headerName: 'Conflict',
            width: 100
        }
    ];
}

/**
 * Creates the HTML for the dropwdown items for the filter
 */
export function createDropdownItems(): JSX.Element[] {
    const items = [];
    Object.keys(FilterType)
        .forEach(filter => {
            items.push(<option key={filter} value={FilterType[filter]}>{FilterType[filter]}</option>);
        });
    return items;
}
