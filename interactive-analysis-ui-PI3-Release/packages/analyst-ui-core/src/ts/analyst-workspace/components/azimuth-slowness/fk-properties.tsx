import { Button, NumericInput, Position } from '@blueprintjs/core';
import { Row, Table } from '@gms/ui-core-components';
import * as React from 'react';
import { AnalystCurrentFk } from './fk-primary';
import { FrequencyBand, SdFkData } from './graphql/query';

// TODO this should be in config most likely
const digitPrecision = 3;

const FrequencyFilters: FrequencyBand[] = [
    {
        minFrequencyHz: 0.5,
        maxFrequencyHz: 2
    },
    {
        minFrequencyHz: 1,
        maxFrequencyHz: 2.5
    },
    {
        minFrequencyHz: 1.5,
        maxFrequencyHz: 3
    },
    {
        minFrequencyHz: 2,
        maxFrequencyHz: 4
    },
    {
        minFrequencyHz: 3,
        maxFrequencyHz: 6
    }
];

/**
 * FkProperties Props
 */
export interface FkPropertiesProps {
    data: SdFkData;
    analystCurrentFk: AnalystCurrentFk;

    acceptSelectedSdFks(): void;
    frequencyBandUpdated(minFreq: number, maxFreq: number): void;
}

/**
 * FkProperties State
 */
export interface FkPropertiesState {
    presetFrequency: boolean;
}

/**
 * FkProperties Component
 */
export class FkProperties extends React.Component<FkPropertiesProps, FkPropertiesState> {

    private lowFreqControl: NumericInput;

    private highFreqControl: NumericInput;

    // ***************************************
    // BEGIN REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    /**
     * Constructor.
     * 
     * @param props The initial props
     */
    public constructor(props: FkPropertiesProps) {
        super(props);
        this.state = {
            presetFrequency: FkProperties.isPresetFrequency(props.data.signalDetectionHypothesis
                .azSlownessMeasurement.fkData.frequencyBand)
        };
    }

    /**
     * Updates the derived state from the next props.
     * 
     * @param nextProps The next (new) props
     * @param prevState The previous state
     */
    public static getDerivedStateFromProps(nextProps: FkPropertiesProps, prevState: FkPropertiesState) {
        return {
            presetFrequency: FkProperties.isPresetFrequency(nextProps.data.signalDetectionHypothesis
                .azSlownessMeasurement.fkData.frequencyBand)
        };
    }

    /**
     * Renders the component.
     */
    public render() {
        const stationName = this.props.data.signalDetectionHypothesis.signalDetection.station.name;
        const channelName = this.props.data.signalDetectionHypothesis.signalDetection.station.defaultChannel.name;
        const phase = this.props.data.signalDetectionHypothesis.phase;

        return (
            <div
                style={{ height: '100%', width: '100%', overflow: 'auto', maxWidth: '427px' }}
                className="ag-dark"
            >
                <div
                    style={{
                        height: '100%', display: 'flex', flexDirection: 'column',
                    }}
                >
                    <div
                        style={{
                            display: 'flex',
                            flexDirection: 'row',
                            alignItems: 'baseline',
                            justifyContent: 'space-between'
                        }}
                    >
                        <div>
                            Station:
                                <span
                                    style={{
                                        paddingLeft: '4px'
                                    }}
                                >
                                    {stationName}
                                </span>
                        </div>
                        <div>
                            Channel:
                                <span
                                    style={{
                                        paddingLeft: '4px'
                                    }}
                                >
                                    {channelName}
                                </span>
                        </div>
                        <div>
                            Phase:
                                <span
                                    style={{
                                        paddingLeft: '4px'
                                    }}
                                >
                                    {phase}
                                </span>
                        </div>
                    </div>
                    <div style={{ flex: '1 1 auto', position: 'relative', minHeight: '120px', maxWidth: '100%' }}>
                        <div className="max">
                            <Table
                                columnDefs={this.getColumnDefs()}
                                rowData={this.getRowData(this.props)}
                                getRowNodeId={node => node.id}
                                deltaRowDataMode={true}
                                overlayNoRowsTemplate="No data available"
                                enableSorting={false}
                                enableFilter={false}
                                enableColResize={false}
                            />
                        </div>
                    </div>
                    <div
                        style={{
                            height: '100%',
                            display: 'flex',
                            flexDirection: 'row',
                            paddingTop: '8px',
                        }}
                    >
                        <div>
                            <div
                                style={{
                                    display: 'grid',
                                    gridTemplateColumns: 'auto auto',
                                    justifyContent: 'space-between',
                                    gridColumnGap: '8px',
                                    gridRowGap: '4px',
                                    alignItems: 'baseline',
                                }}
                                className="grid-container"
                            >
                                <div className="grid-item">Frequency:</div>
                                <div className="grid-item pt-select pt-fill">
                                    <select
                                        value={this.state.presetFrequency ?
                                                this.frequencyBandToString(
                                                    this.props.data.signalDetectionHypothesis.
                                                    azSlownessMeasurement.fkData.frequencyBand
                                                ) :
                                                'Custom'}
                                        onChange={this.onClickFreqencyMenu}
                                    >
                                        {this.getFrequencyBands()}
                                    </select>
                                </div>
                                <div className="grid-item">Low:</div>
                                <div className="grid-item">
                                    <NumericInput
                                        ref={ref => this.lowFreqControl = ref}
                                        className="pt-fill"
                                        allowNumericCharactersOnly={true}
                                        buttonPosition={Position.RIGHT}
                                        value={this.props.data.signalDetectionHypothesis
                                               .azSlownessMeasurement.fkData.frequencyBand.minFrequencyHz}
                                        onValueChange={this.onChangeLowFrequency}
                                        selectAllOnFocus={true}
                                        // tslint:disable-next-line:no-magic-numbers
                                        stepSize={0.1}
                                        // tslint:disable-next-line:no-magic-numbers
                                        minorStepSize={0.01}
                                        majorStepSize={1}
                                    />
                                </div>
                                <div className="grid-item">High:</div>
                                <div className="grid-item">
                                    <NumericInput
                                        ref={ref => this.highFreqControl = ref}
                                        className="pt-fill"
                                        allowNumericCharactersOnly={true}
                                        buttonPosition={Position.RIGHT}
                                        value={this.props.data.signalDetectionHypothesis
                                               .azSlownessMeasurement.fkData.frequencyBand.maxFrequencyHz}
                                        onValueChange={this.onChangeHighFrequency}
                                        selectAllOnFocus={true}
                                        // tslint:disable-next-line:no-magic-numbers
                                        stepSize={0.1}
                                        // tslint:disable-next-line:no-magic-numbers
                                        minorStepSize={0.01}
                                        majorStepSize={1}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div
                    style={{
                        marginTop: '24px',
                    }}
                >
                    <Button
                        text="Accept"
                        onClick={this.onAcceptClick}
                    />
                </div>
            </div>
        );
    }

    // ***************************************
    // END REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    /**
     * Checks if the passed in frequency is in the list of preset filters
     * @param freq frequency to check if it is in the preset list
     */
    private static isPresetFrequency(freq: FrequencyBand) {
            return FrequencyFilters.filter(freqs =>
                    freqs.minFrequencyHz === freq.minFrequencyHz &&
                    freqs.maxFrequencyHz === freq.maxFrequencyHz).length > 0;
    }

    private getFrequencyBands(): JSX.Element[] {
        const items = [];
        FrequencyFilters.forEach(frequency => {
            items.push(
                <option
                    key={this.frequencyBandToString(frequency)}
                    value={this.frequencyBandToString(frequency)}
                >
                    {this.frequencyBandToString(frequency)}
                </option>
            );
        });

        items.push(
            <option
                key={'Custom'}
                value={'Custom'}
            >
                {`Custom`}
            </option>
        );
        return items;
    }

    /**
     * Validates numeric entries in the numeric control
     */
    private readonly validNumericEntry = (valueAsString: string, prevValue: number, controlReference: NumericInput) => {
        if (valueAsString === '') {
            // tslint:disable-next-line:no-parameter-reassignment
            valueAsString = String(prevValue);
        }

        // tslint:disable-next-line:no-parameter-reassignment
        valueAsString = valueAsString.replace(/e|\+/, '');

        controlReference.setState((prev: any) => ({
            value: valueAsString
        }));

        const newValue = isNaN(parseFloat(valueAsString)) ?
            prevValue : parseFloat(valueAsString);

        return ({
            valid: (!valueAsString.endsWith('.')
                && !isNaN(parseFloat(valueAsString))
                && newValue !== prevValue),
            value: newValue
        });
    }

    private readonly onChangeHighFrequency = (highFreq: number, numberAsString: string): void => {
        const currentHigh = this.props.data.signalDetectionHypothesis.
                         azSlownessMeasurement.fkData.frequencyBand.maxFrequencyHz;
        const result = this.validNumericEntry(numberAsString, currentHigh, this.highFreqControl);

        if (result.valid) {
            this.props.frequencyBandUpdated(this.props.data.signalDetectionHypothesis
                                            .azSlownessMeasurement.fkData.frequencyBand.minFrequencyHz,
                                            result.value);
        }
    }

    private readonly onChangeLowFrequency = (lowFreq: number, numberAsString: string): void => {
        const currentLow = this.props.data.signalDetectionHypothesis.
                         azSlownessMeasurement.fkData.frequencyBand.minFrequencyHz;
        const result = this.validNumericEntry(numberAsString, currentLow, this.lowFreqControl);

        if (result.valid) {
            this.props.frequencyBandUpdated(result.value,
                                            this.props.data.signalDetectionHypothesis
                                            .azSlownessMeasurement.fkData.frequencyBand.maxFrequencyHz);
        }
    }

    private readonly onClickFreqencyMenu = (event: React.ChangeEvent<HTMLSelectElement>) => {
        const newFreq = FrequencyFilters.filter(pair =>
                        this.frequencyBandToString(pair) === event.currentTarget.value)[0];
        if (newFreq) {
            this.props.frequencyBandUpdated(newFreq.minFrequencyHz, newFreq.maxFrequencyHz);
        }
    }

    /*
    * Call the Signal Detection Fk Data Mutation in the Azimuth Slowness class
    * for all selected SD Ids this is also called by Accept All in the Thumbnail Controls class
    */
    private readonly onAcceptClick = (event: React.MouseEvent<HTMLButtonElement>): void => {
        this.props.acceptSelectedSdFks();
    }

    private readonly getRowData = (props: FkPropertiesProps): Row[] => {
        const rowKeys = ['Azimuth', 'Slowness', 'Fstat'];

        const peakFk = props.data.signalDetectionHypothesis.azSlownessMeasurement.fkData.peak;
        const theoreticalFk = props.data.signalDetectionHypothesis.azSlownessMeasurement.fkData.theoretical;

        // CONVERTS XY TO POLAR - doesn't seem quite right
        const convertedSelected = this.convertXYtoPolar(props);

        const dataRows: Row[] = [];
        rowKeys.forEach(rowKey => {
            const row = {
                id: rowKey,
                key: rowKey,
                peak: rowKey === rowKeys[0] ?
                    this.formatWithUnc(peakFk.azimuthDeg, peakFk.azimuthUncertainty) :
                    rowKey === rowKeys[1] ?
                        this.formatWithUnc(peakFk.radialSlowness, peakFk.slownessUncertainty) :
                        peakFk.fstat.toFixed(digitPrecision),
                theoretical: rowKey === rowKeys[0] ?
                    this.formatWithUnc(theoreticalFk.azimuthDeg, theoreticalFk.azimuthUncertainty) :
                    rowKey === rowKeys[1] ?
                        this.formatWithUnc(theoreticalFk.radialSlowness, theoreticalFk.slownessUncertainty) :
                        '---',
                selected: rowKey === rowKeys[0] ? convertedSelected.azimuthDeg :
                    rowKey === rowKeys[1] ? convertedSelected.radialSlowness :
                        '---'
            };
            dataRows.push(row);
        });

        return dataRows;
    }

    private readonly getColumnDefs = (): any =>
        [
            {
                headerName: '',
                field: 'key',
                width: 75
            },
            {
                headerName: 'Peak',
                field: 'peak',
                width: 130
            },
            {
                headerName: 'Theoretical',
                field: 'theoretical',
                width: 130
            },
            {
                headerName: 'Selected',
                field: 'selected',
                width: 90
            }
        ]

    private convertXYtoPolar(props: FkPropertiesProps) {
        if (props.analystCurrentFk !== undefined) {
            // Set the angle based on the south axis moving counter-clockwise (this is the current way the
            // data given to us is formatted - subject to change)
            let theta = (Math.atan2(props.analystCurrentFk.y, props.analystCurrentFk.x) +
                // tslint:disable-next-line:no-magic-numbers
                (Math.PI / 2)) * (180 / Math.PI);
            // tslint:disable-next-line:no-magic-numbers
            theta = theta < 0 ? theta + 360 : theta;

            // Calculate radius from center
            const radius = Math.sqrt(props.analystCurrentFk.x ** 2 + props.analystCurrentFk.y ** 2);

            return {
                azimuthDeg: theta.toFixed(digitPrecision),
                radialSlowness: radius.toFixed(digitPrecision)
            };
        } else {
            return {
                azimuthDeg: '',
                radialSlowness: ''
            };
        }
    }

    /*
    * Number format helper
    */
    private formatWithUnc(value: number, unc: number) {
        return `${value.toFixed(digitPrecision)} (\u00B1 ${unc.toFixed(digitPrecision)})`;
    }

    /**
     * Formats a frequency band into a string for the drop down
     */
    private readonly frequencyBandToString = (band: FrequencyBand): string =>
        `${band.minFrequencyHz} - ${band.maxFrequencyHz} Hz`
}
