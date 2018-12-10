import { Collapse, Colors, Icon, NonIdealState } from '@blueprintjs/core';
import { Column, Row, Table, TimeUtil } from '@gms/ui-core-components';
import { flatten } from 'lodash';
import * as React from 'react';
import { userPreferences } from '../../config/user-preferences';
import { MaskData } from './graphql/queries/qc-masks';

export interface Mask {
    id: string;
    channelId: string;
    currentVersion: MaskData;
    qcMaskVersions: MaskData[];
}

/**
 * QcMaskDetails Props
 */
export interface QcMaskDetailsProps {
    masks: Mask[];
}

/**
 * QcMaskDetails State
 */
export interface QcMaskDetailsState {
    showHistory: boolean;
    selectedMask: Mask;
}

/**
 * Interface that describes the QC Mask history
 * information.
 */
export interface QcMaskHistoryRow extends Row {
    id: string;
    versionId: string;
    color: number;
    creationTime: number;
    author: string;
    category: string;
    type: string;
    startTime: number;
    endTime: number;
    channelSegmentIds: string;
    rationale: string;
}

/**
 * Column definitions for the history table.
 */
const columnDefinitions: Column[] = [
    {
        headerName: '',
        field: 'color',
        cellStyle: { 'text-align': 'left', 'vertical-align': 'middle' },
        width: 30,
        cellRendererFramework: e => (
            <div
                style={{
                    height: '10px',
                    width: '20px',
                    backgroundColor: e.data.color,
                    marginTop: '4px'
                }}
            />
        ),
        enableCellChangeFlash: true
    },
    {
        headerName: 'Creation time',
        field: 'timestamp',
        cellStyle: { 'text-align': 'left' },
        width: 170,
        enableCellChangeFlash: true,
        valueFormatter: e => TimeUtil.toString(e.data.creationTime)
    },
    {
        headerName: 'Category',
        field: 'category',
        cellStyle: { 'text-align': 'left' },
        width: 130,
        enableCellChangeFlash: true
    },
    {
        headerName: 'Type',
        field: 'type',
        cellStyle: { 'text-align': 'left' },
        width: 130,
        enableCellChangeFlash: true
    },
    {
        headerName: 'Start time',
        field: 'startTime',
        cellStyle: { 'text-align': 'left' },
        width: 170,
        enableCellChangeFlash: true,
        valueFormatter: e => TimeUtil.toString(e.data.startTime)
    },
    {
        headerName: 'End time',
        field: 'endTime',
        cellStyle: { 'text-align': 'left' },
        width: 170,
        enableCellChangeFlash: true,
        valueFormatter: e => TimeUtil.toString(e.data.endTime)
    },
    {
        headerName: 'Author',
        field: 'author',
        cellStyle: { 'text-align': 'left' },
        width: 75,
        enableCellChangeFlash: true
    },
    {
        headerName: 'Rationale',
        field: 'rationale',
        cellStyle: { 'text-align': 'left' },
        width: 300,
        enableCellChangeFlash: true
    }
];

const labelStyle: React.CSSProperties = {
    color: Colors.GRAY5,
    margin: '0px 0.2rem .1rem 0',
    whiteSpace: 'nowrap',
    userSelect: 'none'
};

/**
 * Used to print out QC type more readable
 */

const QcMaskTypes = {
    SENSOR_PROBLEM: 'Sensor problem',
    STATION_PROBLEM: 'Station problem',
    CALIBRATION: 'Calibration',
    STATION_SECURITY: 'Station security',
    TIMING: 'Timing',
    REPAIRABLE_GAP: 'Repairable gap',
    LONG_GAP: 'Long gap',
    REPEATED_ADJACENT_AMPLITUDE_VALUE: 'Repeated adjacent amplitude value',
    SPIKE: 'Spike'
};

/**
 * QcMaskDetails Component
 */
export class QcMaskDetails extends React.Component<QcMaskDetailsProps, QcMaskDetailsState> {

    private containerRef: HTMLDivElement | null;

    /**
     * Constructor
     */
    public constructor(props: QcMaskDetailsProps) {
        super(props);
        this.state = {
            showHistory: false,
            selectedMask: undefined
        };
    }

    /**
     * React component lifecycle
     */
    public render() {
        return (
            <div
                ref={ref => this.containerRef = ref}
                style={{
                    width: '650px',
                }}
            >
                {this.renderMask(this.props.masks)}
            </div>
        );
    }

    /**
     * Render the mask.
     */
    private readonly renderMask = (masks: Mask[]): JSX.Element => {
        if (!masks && masks.length < 0) {
            return (
                <NonIdealState />
            );
        }

        // determine if the masks are completely overlapping
        const isDisplayMultipleMasks: boolean = (masks.length === 1 || this.state.selectedMask) ?
            false :
            (masks.every(m =>
                m.currentVersion.startTime === masks[0].currentVersion.startTime &&
                m.currentVersion.endTime === masks[0].currentVersion.endTime
            )
            );

        return (
            <div
                style={{
                    padding: '0.4rem',
                    userSelect: 'none'
                }}
            >
                {(isDisplayMultipleMasks) ?
                    this.renderMultipleOverlappingMasks() :
                    this.renderMaskDetails((this.state.selectedMask) ?
                        // take the highest rendered mask
                        this.state.selectedMask : masks[masks.length - 1])
                }
            </div>
        );
    }

    /**
     * Render table showing multiple overlapping masks.
     */
    private readonly renderMultipleOverlappingMasks = (): JSX.Element => (
        <div>
            <h6 style={labelStyle}>
                Select Mask to View
            </h6>
            {this.renderMaskTable({
                rowData: this.generateMaskTableRows(this.props.masks),
                overlayNoRowsTemplate: 'No Masks',
                onRowDoubleClicked: params => this.onRowDoubleClicked(params)
            })}
        </div>
    )

    /**
     * Render the mask details information (including its history).
     */
    private readonly renderMaskDetails = (mask: Mask): JSX.Element => {

        const tdStyle: React.CSSProperties = {
            paddingTop: '.2rem',
            verticalAlign: 'top'
        };

        const numberOfCols = 5;

        return (
            <div>
                <div
                    style={{
                        position: 'absolute',
                        right: '5px',
                        top: '5px',
                        height: '10px',
                        width: '20px',
                        backgroundColor: userPreferences.colors.waveforms.maskDisplayFilters
                        [mask.currentVersion.category].color,
                        marginLeft: '1rem'
                    }}
                />
                <div>
                    <table style={{ columns: numberOfCols, margin: 0, padding: 0 }}>
                        <tbody>
                            <tr>
                                <td style={tdStyle}><span style={labelStyle}>Category:</span></td>
                                <td style={tdStyle}>{userPreferences.colors.waveforms.maskDisplayFilters
                                     [mask.currentVersion.category].name}</td>
                                <td style={{ width: '1rem' }} />
                                <td style={tdStyle}><span style={labelStyle}>ID:</span></td>
                                <td style={tdStyle}>{mask.id}</td>
                            </tr>
                            <tr>
                                <td style={tdStyle}><span style={labelStyle}>Type:</span></td>
                                <td>{QcMaskTypes[mask.currentVersion.type]}</td>
                                <td style={{ width: '1rem' }} />
                                <td style={tdStyle}><span style={labelStyle}>Author:</span></td>
                                <td style={tdStyle}>{mask.currentVersion.creationInfo.creatorId}</td>
                            </tr>
                            <tr>
                                <td style={tdStyle}><span style={labelStyle}>Start time:</span></td>
                                <td style={tdStyle}>{TimeUtil.toString(mask.currentVersion.startTime)}</td>
                                <td style={{ width: '1rem' }} />
                                <td style={tdStyle}><span style={labelStyle}>Creation time:</span></td>
                                <td style={tdStyle}>
                                    {TimeUtil.toString(mask.currentVersion.creationInfo.creationTime)}
                                </td>
                            </tr>
                            <tr>
                                <td style={tdStyle}><span style={labelStyle}>End time:</span></td>
                                <td style={tdStyle}>{TimeUtil.toString(mask.currentVersion.endTime)}</td>
                            </tr>
                            <tr>
                                <td
                                    style={{
                                        ...tdStyle,
                                        paddingTop: '1rem'
                                    }}
                                >
                                    <span style={labelStyle}>Rationale:</span>
                                </td>
                                <td
                                    colSpan={4}
                                    style={{
                                        ...tdStyle,
                                        paddingTop: '1rem'
                                    }}
                                >
                                    {mask.currentVersion.rationale}
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <hr style={{ marginTop: '.5rem', marginBottom: '1rem', padding: 0 }} />
                <div>
                    <h6
                        style={labelStyle}
                        onClick={() => this.setState({ showHistory: !this.state.showHistory })}
                    >
                        Previous versions
                        <Icon icon={(this.state.showHistory) ? 'minus' : 'plus'} />
                    </h6>
                    <Collapse isOpen={this.state.showHistory}>
                        {this.renderMaskTable(
                            {
                                rowData: this.generateMaskHistoryTableRows(mask),
                                overlayNoRowsTemplate: 'No History'
                            })}
                    </Collapse>
                </div>
            </div>
        );
    }

    /**
     * Render the Mask table (used for history and overlapping masks).
     */
    private readonly renderMaskTable = (tableProps: {}) => (
        <div
            style={{
                height: '100%', width: '100%',
                padding: '0.5rem 0rem 0rem 0rem',
                overflow: 'auto'
            }}
            className="ag-dark"
        >
            <div
                style={{
                    height: '100%', display: 'flex', flexDirection: 'column',
                }}
            >
                <div style={{ flex: '1 1 auto', position: 'relative', minHeight: '150px' }}>
                    <div className="max">
                        <Table
                            columnDefs={columnDefinitions}
                            getRowNodeId={node => node.id}
                            rowSelection="single"
                            enableSorting={true}
                            enableFilter={true}
                            enableColResize={true}
                            {...tableProps}
                        />
                    </div>
                </div>
            </div>
        </div>
    )

    /**
     * Generate the table row data for masks.
     */
    private readonly generateMaskTableRows = (masks: Mask[]): QcMaskHistoryRow[] =>
        flatten(masks.map(m => ({
            id: m.id,
            versionId: m.currentVersion.version,
            color: userPreferences.colors.waveforms.maskDisplayFilters[m.currentVersion.category].color,
            creationTime: m.currentVersion.creationInfo.creationTime,
            author: m.currentVersion.creationInfo.creatorId,
            category: userPreferences.colors.waveforms.maskDisplayFilters[m.currentVersion.category].name,
            type: QcMaskTypes[m.currentVersion.type],
            startTime: m.currentVersion.startTime,
            endTime: m.currentVersion.endTime,
            channelSegmentIds: m.currentVersion.channelSegmentIds.join(', '),
            rationale: m.currentVersion.rationale
        })))

    /**
     * Generate the table row data for the mask hisory.
     */
    private readonly generateMaskHistoryTableRows = (mask: Mask): QcMaskHistoryRow[] =>
        flatten(mask.qcMaskVersions.
            filter(m => m.version !== mask.currentVersion.version)
            .map(m => ({
                id: mask.id,
                versionId: m.version,
                color: userPreferences.colors.waveforms.maskDisplayFilters[m.category].color,
                creationTime: m.creationInfo.creationTime,
                author: m.creationInfo.creatorId,
                category: userPreferences.colors.waveforms.maskDisplayFilters[m.category].name,
                type: QcMaskTypes[m.type],
                startTime: m.startTime,
                endTime: m.endTime,
                channelSegmentIds: m.channelSegmentIds.join(', '),
                rationale: m.rationale
            })))

    /**
     * Event handler for a row being double clicked on the table.
     */
    private readonly onRowDoubleClicked = (params: any) => {
        this.setState({
            selectedMask: this.props.masks.find(m => m.id === params.data.id)
        });
    }
}
