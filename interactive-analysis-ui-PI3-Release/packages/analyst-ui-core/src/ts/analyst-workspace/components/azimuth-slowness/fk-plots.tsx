import { Button, NumericInput, Popover, Position } from '@blueprintjs/core';
import { DisplayType, LineStyle, SignalDetectionConfig, StationConfig, WaveformDisplay as Weavess } from '@gms/weavess';
import * as React from 'react';
import { FkFstatData, WindowParams } from './graphql/query';

// TODO this should be in config most likely
const LeadLagPairs: WindowParams[] = [
    {
        windowType: 'Hanning',
        leadSeconds: 1,
        lengthSeconds: 4
    },
    {
        windowType: 'Hanning',
        leadSeconds: 1,
        lengthSeconds: 6
    },
    {
        windowType: 'Hanning',
        leadSeconds: 1,
        lengthSeconds: 9
    },
    {
        windowType: 'Hanning',
        leadSeconds: 1,
        lengthSeconds: 11
    }
];

/**
 * FkPlots Props
 */
export interface FkPlotsProps {
    signalDetection: SignalDetectionConfig;
    fstatData: FkFstatData;
    windowParams: WindowParams;
    contribChannels: {
        id: string;
    }[];

    leadLagUpdated(lead: number, lag: number): void;
}

/**
 * FkPlots State
 */
export interface FkPlotsState {
    presetLeadLagPair: boolean;
}

/**
 * FkPlots Component
 */
export class FkPlots extends React.Component<FkPlotsProps, FkPlotsState> {

    private leadControl: NumericInput;

    private lagControl: NumericInput;

    private readonly digitPrecision: number = 1;

    private readonly waveformPanelHeight: number = 70;

    // ***************************************
    // BEGIN REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    /**
     * Constructor.
     * 
     * @param props The initial props
     */
    public constructor(props: FkPlotsProps) {
        super(props);
        this.state = {
            presetLeadLagPair: FkPlots.isPresetLeadLag(props.windowParams)
        };
    }

    /**
     * Updates the derived state from the next props.
     * 
     * @param nextProps The next (new) props
     * @param prevState The previous state
     */
    public static getDerivedStateFromProps(nextProps: FkPlotsProps, prevState: FkPlotsState) {
        return {
            presetLeadLagPair: FkPlots.isPresetLeadLag(nextProps.windowParams)
        };
    }

    /**
     * Renders the component.
     */
    public render() {
        // az, slowness, and fstat have the same rate and num sumples
        // but we need to calculate the data to send to weavess for beam
        const stations: StationConfig[] = [
            // Beam
            {
                id: 'Beam',
                name: 'Beam',
                height: this.waveformPanelHeight,
                defaultChannel: {
                    id: 'Beam',
                    name: 'Beam',
                    color: 'dodgerblue',
                    sampleRate: this.props.fstatData.beamWf.sampleRate,
                    dataSegments: [{
                        startTimeSecs: this.props.fstatData.beamWf.startTime,
                        data: this.props.fstatData.beamWf.waveformSamples
                    }],
                    displayType: [DisplayType.LINE],
                    pointSize: 2,
                    signalDetections: [this.props.signalDetection]
                },
                nonDefaultChannels: []
            },
            // Fstat
            {
                id: 'Fstat',
                name: 'Fstat',
                height: this.waveformPanelHeight,
                defaultChannel: {
                    id: 'Fstat',
                    name: 'Fstat',
                    color: 'dodgerblue',
                    sampleRate: this.props.fstatData.fstatWf.sampleRate,
                    dataSegments: [{
                        startTimeSecs: this.props.fstatData.fstatWf.startTime,
                        data: this.props.fstatData.fstatWf.waveformSamples
                    }],
                    displayType: [DisplayType.LINE, DisplayType.SCATTER],
                    pointSize: 2,
                },
                nonDefaultChannels: []
            },
            // Azimuth
            {
                id: 'Azimuth',
                name: 'Azimuth',
                height: this.waveformPanelHeight,
                defaultChannel: {
                    id: 'Azimuth',
                    name: 'Azimuth',
                    color: 'dodgerblue',
                    sampleRate: this.props.fstatData.azimuthWf.sampleRate,
                    dataSegments: [{
                        startTimeSecs: this.props.fstatData.azimuthWf.startTime,
                        data: this.props.fstatData.azimuthWf.waveformSamples
                    }],
                    displayType: [DisplayType.LINE, DisplayType.SCATTER],
                    pointSize: 2,
                },
                nonDefaultChannels: []
            },
            // Slowness
            {
                id: 'Slowness',
                name: 'Slowness',
                height: this.waveformPanelHeight,
                defaultChannel: {
                    id: 'Slowness',
                    name: 'Slowness',
                    color: 'dodgerblue',
                    sampleRate: this.props.fstatData.slownessWf.sampleRate,
                    dataSegments: [{
                        startTimeSecs: this.props.fstatData.slownessWf.startTime,
                        data: this.props.fstatData.slownessWf.waveformSamples
                    }],
                    displayType: [DisplayType.LINE, DisplayType.SCATTER],
                    pointSize: 2,
                },
                nonDefaultChannels: []
            }
        ];
        return (
            <div
                className="ag-dark"
                style={{
                    height: '100%',
                    width: '100%',
                    display: 'flex',
                }}
            >
                <div
                    style={{
                        height: '100%',
                        width: '100%',
                        display: 'flex',
                        flexDirection: 'column'
                    }}
                >
                    <div
                        style={{
                            display: 'inline-flex',
                            flexDirection: 'row',
                            alignItems: 'baseline',
                            flex: '0 0 auto',
                            paddingBottom: '0.25rem',
                            maxWidth: '750px'
                        }}
                    >
                        <div
                            style={{
                                display: 'flex',
                                flexDirection: 'row',
                                alignItems: 'baseline',
                                flex: '0 1 auto',
                                paddingRight: '6px',
                            }}
                        >
                            <span style={{ paddingRight: '4px' }}>
                                FK window:
                            </span>
                            <div
                                className="pt-select"
                                style={{
                                    flex: '0 1 auto',
                                }}
                            >
                                <select
                                    value={this.state.presetLeadLagPair ?
                                        this.leadLagToString(this.props.windowParams) :
                                        'Custom'}
                                    onChange={this.onClickLeadLagMenu}
                                >
                                    {this.getLeadLagPairs()}
                                </select>
                            </div>
                        </div>
                        <div
                            style={{
                                display: 'flex',
                                flexDirection: 'row',
                                alignItems: 'baseline',
                                flex: '0 1 auto',
                                paddingLeft: '6px',
                                paddingRight: '6px',
                                width: '140px'
                            }}
                        >
                            <span style={{ paddingRight: '4px' }}>
                                Lead:
                            </span>
                            <NumericInput
                                ref={ref => this.leadControl = ref}
                                className="pt-fill"
                                allowNumericCharactersOnly={true}
                                buttonPosition={Position.RIGHT}
                                value={this.props.windowParams.leadSeconds.toFixed(this.digitPrecision)}
                                onValueChange={this.onLeadTimeChanged}
                                selectAllOnFocus={true}
                                // tslint:disable-next-line:no-magic-numbers
                                stepSize={0.5}
                                // tslint:disable-next-line:no-magic-numbers
                                minorStepSize={0.1}
                                majorStepSize={1}
                            />
                        </div>
                        <div
                            style={{
                                display: 'flex',
                                flexDirection: 'row',
                                alignItems: 'baseline',
                                flex: '0 1 auto',
                                paddingLeft: '6px',
                                paddingRight: '6px',
                                width: '140px'
                            }}
                        >
                            <span style={{ paddingRight: '4px' }}>
                                Lag:
                            </span>
                            <NumericInput
                                ref={ref => this.lagControl = ref}
                                className="pt-fill"
                                allowNumericCharactersOnly={true}
                                buttonPosition={Position.RIGHT}
                                value={(this.props.windowParams.lengthSeconds - this.props.windowParams.leadSeconds)
                                    .toFixed(this.digitPrecision)}
                                onValueChange={this.onLagTimeChanged}
                                selectAllOnFocus={true}
                                // tslint:disable-next-line:no-magic-numbers
                                stepSize={0.5}
                                // tslint:disable-next-line:no-magic-numbers
                                minorStepSize={0.1}
                                majorStepSize={1}
                            />
                        </div>
                        <div
                            style={{
                                paddingLeft: '6px',
                            }}
                        >
                            <span style={{ paddingRight: '4px' }}>
                                Duration: {(this.props.windowParams.lengthSeconds).toFixed(this.digitPrecision)} s
                            </span>
                        </div>
                        {this.createChannelList()}
                    </div>
                    <div
                        style={{
                            flex: '1 1 auto',
                            position: 'relative'
                        }}
                    >
                        <div
                            style={{
                                position: 'absolute',
                                top: '0px', bottom: '0px', left: '0px', right: '0px',
                            }}
                        >
                            <Weavess
                                startTimeSecs={this.props.fstatData.beamWf.startTime}
                                endTimeSecs={this.props.fstatData.beamWf.endTime}
                                stations={stations}
                                flex={false}
                                events={{
                                    updateMoveableMarkersValue: this.updateMoveableMarkers
                                }}
                                markers={{
                                    selectionWindows:
                                        [
                                            {
                                                startMoveableMarker:
                                                    {
                                                        color: 'red',
                                                        lineStyle: LineStyle.DASHED,
                                                        timeSecs: this.props.signalDetection.timeSecs -
                                                            this.props.windowParams.leadSeconds,
                                                    },
                                                endMoveableMarker:
                                                    {
                                                        color: 'red',
                                                        lineStyle: LineStyle.DASHED,
                                                        timeSecs: Number((Number(this.props.signalDetection.timeSecs) +
                                                            (Number(this.props.windowParams.lengthSeconds) -
                                                                Number(this.props.windowParams.leadSeconds)))),
                                                    },
                                                color: 'red'
                                            }
                                        ]
                                }}
                            />
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    // ***************************************
    // END REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    /**
     * Checks if the passed in lead/lag is in the list of preset lead/lag
     * @param windowParams params to check if it is in the preset list
     */
    private static isPresetLeadLag(windowParams: WindowParams) {
        return LeadLagPairs.filter(pair =>
            pair.leadSeconds === windowParams.leadSeconds &&
            pair.lengthSeconds === windowParams.lengthSeconds).length > 0;
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

    /**
     * Handles change on Lead time control
     */
    private readonly onLeadTimeChanged = (newLeadTime: number, leadTimeAsString: string) => {
        const result = this.validNumericEntry(leadTimeAsString, this.props.windowParams.leadSeconds, this.leadControl);
        if (result.valid) {
            const lagTime = this.props.windowParams.lengthSeconds - this.props.windowParams.leadSeconds;
            this.props.leadLagUpdated(result.value, result.value + lagTime);
        }
    }

    /**
     * Handles change on lag time control
     */
    private readonly onLagTimeChanged = (newLagTime: number, lagTimeAsString: string) => {
        const lagTime = this.props.windowParams.lengthSeconds - this.props.windowParams.leadSeconds;
        const result = this.validNumericEntry(lagTimeAsString, lagTime, this.lagControl);
        if (result.valid) {
            const leadTime = this.props.windowParams.leadSeconds;
            this.props.leadLagUpdated(leadTime, leadTime + result.value);
        }
    }

    /**
     * Call back for drag and drop change of the moveable lead lag markers
     */
    private readonly updateMoveableMarkers = (verticalMarkers: any[]) => {
        const lagTime = this.props.windowParams.lengthSeconds - this.props.windowParams.leadSeconds;
        const newLeadTime = parseFloat((this.props.signalDetection.timeSecs - (verticalMarkers[0].timeSecs))
            .toFixed(this.digitPrecision));
        const newLagTime = parseFloat(((verticalMarkers[1].timeSecs) - this.props.signalDetection.timeSecs)
            .toFixed(this.digitPrecision));
        const minimumStepSize = 0.01;

        if ((newLeadTime > this.props.windowParams.leadSeconds + minimumStepSize ||
            newLeadTime < this.props.windowParams.leadSeconds - minimumStepSize) ||
            (newLagTime > lagTime + minimumStepSize ||
                newLagTime < lagTime - minimumStepSize)) {
            this.props.leadLagUpdated(newLeadTime, newLeadTime + newLagTime);
        }
    }

    /**
     * Handles the change in the drop down menu
     */
    private readonly onClickLeadLagMenu = (event: React.ChangeEvent<HTMLSelectElement>) => {
        const newPair = LeadLagPairs.filter(pair => this.leadLagToString(pair) === event.currentTarget.value)[0];

        if (newPair) {
            this.props.leadLagUpdated(newPair.leadSeconds, newPair.lengthSeconds);
        }
    }

    /**
     * Creates the drop down of lead/lag pairs
     */
    private getLeadLagPairs(): JSX.Element[] {
        const items = [];
        LeadLagPairs.forEach(pair => {
            items.push(
                <option
                    key={this.leadLagToString(pair)}
                    value={this.leadLagToString(pair)}
                >
                    {this.leadLagToString(pair)}
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
     * Creates the popover for channel list
     */
    private createChannelList(): JSX.Element {
        return (
            <Popover
                position={Position.BOTTOM}
                content={(
                    <div
                        style={{
                            padding: '1rem',
                            overflowY: 'auto',
                            maxHeight: '200px',
                        }}
                    >
                        <table>
                            <tbody>
                                {
                                    Object.keys(this.props.contribChannels)
                                        .filter(channel => this.props.contribChannels[channel])
                                        .map(channel =>
                                            <tr
                                                // tslint:disable-next-line:max-line-length
                                                key={`${this.props.contribChannels[channel].site.name}/${this.props.contribChannels[channel].name}`}
                                            >
                                                <td
                                                    style={{
                                                        verticalAlign: 'middle',
                                                    }}
                                                >
                                                    {
                                                        // tslint:disable-next-line:max-line-length
                                                        `${this.props.contribChannels[channel].site.name}/${this.props.contribChannels[channel].name}`}
                                                </td>
                                            </tr>
                                        )
                                }
                            </tbody>
                        </table>
                    </div>
                )}
                target={
                    <Button
                        className="pt-minimal"
                        text="Channels"
                        style={{
                            marginLeft: '8px',
                            marginRight: '0.5rem'
                        }}
                    />
                }
            />
        );
    }

    /**
     * Handles the string representation of a lead/lag pair
     */
    private readonly leadLagToString =
        (pair: WindowParams): string =>
            `Lead ${pair.leadSeconds.toFixed(this.digitPrecision)} s,
             Lag ${(pair.lengthSeconds - pair.leadSeconds).toFixed(this.digitPrecision)} s`
}
