import * as moment from 'moment';
import { MarkCompleteCellRenderer } from './mark-complete-cell-renderer';

/**
 * Definition of columns used in Render function
 */
export function getColumnDefs() {
    return [
        {
            headerName: 'Time',
            field: 'time',
            width: 100,
            cellStyle: { 'text-align': 'center' },
            valueFormatter: e => moment.unix(e.data.time)
                .utc()
                .format('HH:mm:ss')
        }, {
            headerName: 'ID',
            field: 'id',
            width: 75,
        }, {
            headerName: '#Det',
            field: 'numDetections',
            cellStyle: { 'text-align': 'right' },
            width: 50
        }, {
            headerName: 'mb',
            width: 50,
            cellStyle: { 'text-align': 'right' },
            valueFormatter: e => e.data.magMb ? e.data.magMb.toFixed(2) : ''
        }, {
            headerName: 'Lat',
            field: 'lat',
            width: 70,
            cellStyle: { 'text-align': 'right' },
            valueFormatter: e => e.data.lat.toFixed(3)
        }, {
            headerName: 'Lon',
            field: 'lon',
            width: 70,
            cellStyle: { 'text-align': 'right' },
            valueFormatter: e => e.data.lon.toFixed(3)
        }, {
            headerName: 'Depth',
            field: 'depth',
            width: 60,
            cellStyle: { 'text-align': 'right' },
            valueFormatter: e => e.data.depth.toFixed(2)
        },
    ];
}

/**
 * Additional Column Defs for 'Events to Work'
 */
export function getAdditionalColumnDefs() {
    const columnDefs = getColumnDefs();

    return [
        ...columnDefs,
        {
            headerName: 'Active analysts',
            field: 'activeAnalysts',
            enableCellChangeFlash: true,
            valueGetter: e => e.data.activeAnalysts.toString(),
            width: 115
        }, {
            headerName: 'Mark Complete',
            cellRendererFramework: MarkCompleteCellRenderer,
            width: 120,
        },
    ];
}
