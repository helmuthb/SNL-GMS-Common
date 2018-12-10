import { Collapse, Colors, Icon, NonIdealState } from '@blueprintjs/core';
import { Column, Row, Table, TimeUtil } from '@gms/ui-core-components';
import { flatten } from 'lodash';
import * as React from 'react';
import { HypothesisData } from './graphql/queries/station-detections';

export interface Detection {
    id: string;
    currentHypothesis: HypothesisData;
    hypotheses: HypothesisData[];
}

/**
 * SignalDetectionDetails Props
 */
export interface SignalDetectionDetailsProps {
    detection: Detection;
}

/**
 * SignalDetectionDetails State
 */
export interface SignalDetectionDetailsState {
    showHistory: boolean;
}

/**
 * Interface that describes the Detection history
 * information.
 */
export interface SignalDetectionHistoryRow extends Row {
    id: string;
    versionId: string;
    phase: string;
    isRejected: boolean;
    arrivalTimeMeasurementFeatureType: string;
    arrivalTimeMeasurementTimestamp: number;
    arrivalTimeMeasurementUncertaintySec: number;
    author: string;
    creationTime: number;
}

/**
 * Column definitions for the history table.
 */
const columnDefinitions: Column[] = [
    {
        headerName: 'Creation time',
        field: 'timestamp',
        cellStyle: { 'text-align': 'left' },
        width: 165,
        enableCellChangeFlash: true,
        valueFormatter: e => TimeUtil.toString(e.data.creationTime)
    },
    {
        headerName: 'Phase',
        field: 'phase',
        cellStyle: { 'text-align': 'left' },
        width: 70,
        enableCellChangeFlash: true
    },
    {
        headerName: 'Detection time',
        field: 'arrivalTimeMeasurementUncertaintySec',
        cellStyle: { 'text-align': 'left' },
        width: 165,
        enableCellChangeFlash: true,
        valueFormatter: e => TimeUtil.toString(e.data.arrivalTimeMeasurementTimestamp)
    },
    {
        headerName: 'Time uncertainty',
        field: 'arrivalTimeMeasurementUncertaintySec',
        cellStyle: { 'text-align': 'left' },
        width: 125,
        enableCellChangeFlash: true,
        valueFormatter: e => formatUncertainty(e.data.arrivalTimeMeasurementUncertaintySec)
    },
    {
        headerName: 'Rejected',
        field: 'isRejected',
        cellStyle: { 'text-align': 'left' },
        width: 75,
        valueFormatter: e => (e.data.isRejected) ? 'Yes' : 'No'
    },
    {
        headerName: 'Author',
        field: 'author',
        cellStyle: { 'text-align': 'left' },
        width: 90,
        enableCellChangeFlash: true
    },
];

const labelStyle: React.CSSProperties = {
    color: Colors.GRAY5,
    margin: '0px 0.2rem .1rem 0',
    whiteSpace: 'nowrap',
    userSelect: 'none'
};

/**
 * Reusable formatter for time uncertainty 
 * @param unc the uncertainty value in seconds
 */
function formatUncertainty(unc: number) {
    return `${unc.toFixed(2)} s`;
}

/**
 * SignalDetectionDetails Component
 */
export class SignalDetectionDetails extends React.Component<SignalDetectionDetailsProps, SignalDetectionDetailsState> {

    private containerRef: HTMLDivElement | null;

    /**
     * Constructor
     */
    public constructor(props: SignalDetectionDetailsProps) {
        super(props);
        this.state = {
            showHistory: false
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
                {this.renderDetection(this.props.detection)}
            </div>
        );
    }

    /**
     * Render the detection.
     */
    private readonly renderDetection = (detection: Detection): JSX.Element => {
        if (!detection) {
            return (
                <NonIdealState />
            );
        }

        return (
            <div
                style={{
                    padding: '0.4rem',
                    userSelect: 'none'
                }}
            >
                {this.renderDetectionDetails(detection)}
            </div>
        );
    }

    /**
     * Render the detection details information (including its history).
     */
    private readonly renderDetectionDetails = (detection: Detection): JSX.Element => {

        const tdStyle: React.CSSProperties = {
            paddingTop: '.2rem',
            verticalAlign: 'top'
        };

        const numberOfCols = 5;

        return (
            <div>
                <div>
                    <table style={{ columns: numberOfCols, margin: 0, padding: 0 }}>
                        <tbody>
                            <tr>
                                <td style={tdStyle}><span style={labelStyle}>Phase:</span></td>
                                <td style={tdStyle}>{detection.currentHypothesis.phase}</td>
                                <td style={{ width: '1rem' }} />
                                <td style={tdStyle}><span style={labelStyle}>ID:</span></td>
                                <td style={tdStyle}>{detection.id}</td>
                            </tr>
                            <tr>
                                <td style={tdStyle}><span style={labelStyle}>Detection time:</span></td>
                                <td style={tdStyle}>
                                    {TimeUtil.toString(
                                        detection.currentHypothesis.arrivalTimeMeasurement.timeSec)}
                                </td>
                                <td style={{ width: '1rem' }} />
                                <td style={tdStyle}><span style={labelStyle}>Author:</span></td>
                                <td style={tdStyle}>{detection.currentHypothesis.creationInfo.creatorId}</td>
                            </tr>
                            <tr>
                                <td style={tdStyle}><span style={labelStyle}>Time uncertainty:</span></td>
                                <td style={tdStyle}>
                                    {formatUncertainty(
                                        detection.currentHypothesis.arrivalTimeMeasurement.uncertaintySec
                                    )}
                                </td>
                                <td style={{ width: '1rem' }} />
                                <td style={tdStyle}><span style={labelStyle}>Creation time:</span></td>
                                <td style={tdStyle}>
                                    {TimeUtil.toString(detection.currentHypothesis.creationInfo.creationTime)}
                                </td>
                            </tr>
                            <tr>
                                <td style={tdStyle}><span style={labelStyle}>Rejected:</span></td>
                                <td style={tdStyle}>{(detection.currentHypothesis.isRejected) ? 'Yes' : 'No'}
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
                        {this.renderTable(
                            {
                                rowData: this.generateDetectionHistoryTableRows(detection),
                                overlayNoRowsTemplate: 'No History'
                            })}
                    </Collapse>
                </div>
            </div>
        );
    }

    /**
     * Render the Detection table.
     */
    private readonly renderTable = (tableProps: {}) => (
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
     * Generate the table row data for the detection hisory.
     */
    private readonly generateDetectionHistoryTableRows = (detection: Detection): SignalDetectionHistoryRow[] =>
        flatten(detection.hypotheses.
            filter(m => m.id !== detection.currentHypothesis.id)
            .map(d => ({
                id: d.id,
                versionId: d.id,
                phase: d.phase,
                isRejected: d.isRejected,
                arrivalTimeMeasurementFeatureType: d.arrivalTimeMeasurement.featureType,
                arrivalTimeMeasurementTimestamp: d.arrivalTimeMeasurement.timeSec,
                arrivalTimeMeasurementUncertaintySec: d.arrivalTimeMeasurement.uncertaintySec,
                creationTime: d.creationInfo.creationTime,
                author: d.creationInfo.creatorId
            })))
}
