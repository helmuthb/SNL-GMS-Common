import { GridOptions } from 'ag-grid';
import { AgGridReact } from 'ag-grid-react';
import * as React from 'react';

export {
    GridOptions as TableProps,
    GridApi as TableApi,
    ColumnApi,
    ColDef as Column,
} from 'ag-grid';

/**
 * Row Interface
 */
export interface Row {
    id: string;
}

/**
 * Table component that wraps AgGridReact.
 */
export class Table extends React.Component<GridOptions, {}> {

    /**
     * Currently, just wrapping a Ag-grid table.
     */
    public render() {
        return (
            <AgGridReact
                {...this.props}
            />
        );
    }

}
