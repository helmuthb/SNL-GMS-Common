import * as React from 'react';

/**
 * Waveform Display Controls Props
 */
export interface WaveformDisplayControlsProps {
    currentSortType: WaveformSortType;
    currentOpenEventHypId: string;
    setSelectedSortType(sortType: WaveformSortType): void;
}

/**
 * Different sort types that are available
 */
export enum WaveformSortType {
    distance = 'Distance',
    stationName = 'Station Name'
}

/**
 * Waveform Display Controls State
 */
// tslint:disable-next-line:no-empty-interface
export interface WaveformDisplayControlsState {
}

/**
 *  Waveform Display Controls Component
 */
export class  WaveformDisplayControls extends
    React.Component<WaveformDisplayControlsProps, WaveformDisplayControlsState> {

    public constructor(props: WaveformDisplayControlsProps) {
        super(props);
    }

    /**
     * React component lifecycle
     */
    public render() {
        return (
            <div
                style={{
                    flex: '0 0 auto',
                    marginBottom: '0.5rem',
                    marginRight: '0.5rem',
                    height: '8px',
                }}
            >
                <div
                    className="pt-select"
                    style={{
                        marginBottom: '0.5rem'
                    }}
                >
                    <select
                        value={this.props.currentSortType}
                        onChange={e => {
                            const key = Object.keys(WaveformSortType)
                                .find(type => (WaveformSortType[type] === e.target.value));
                            this.props.setSelectedSortType(WaveformSortType[key]);
                        }}
                    >
                        {this.createDropdownItems()}
                    </select>
                </div>
            </div>
        );
    }

    /**
     * Creates the HTML for the dropwdown items for the filter
     */
    private readonly createDropdownItems = (): JSX.Element[] =>
            Object.keys(WaveformSortType)
                .map(type => (
                // If no event has been selected disable the distance selection
                <option
                    key={type}
                    value={WaveformSortType[type]}
                    disabled={(!this.props.currentOpenEventHypId &&
                        WaveformSortType[type] === WaveformSortType.distance ? true : false)}
                >
                    {WaveformSortType[type]}
                </option>
            )
    )
}
