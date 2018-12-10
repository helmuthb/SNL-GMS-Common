import * as React from 'react';

import * as Entities from '../../../entities';
import { YAxis } from '../axes';

const channelLabelStyle: React.CSSProperties = {
    display: 'flex',
    flex: '0 0 auto',
    flexDirection: 'column',
    justifyContent: 'center',
    maxWidth: '100px',
    paddingLeft: '5px',
    paddingRight: '5px',
    textAlign: 'center',
    width: '100px',
    wordWrap: 'break-word',
    position: 'relative'
};

/**
 * ChannelLabel Props
 */
export interface ChannelLabelProps {
    channelConfig: Entities.ChannelConfig;
    subChannel: boolean;
    // [min, max];
    yAxisBounds: [number, number];
    selectedChannels: string[] | undefined;
    showMaskIndicator: boolean;
    distanceToSource: number;

    onChannelLabelClick?(e: React.MouseEvent<HTMLDivElement>, channelName: string): void;
}

/**
 * Label for a channel row in the waveform display.
 * Includes the channel name + y-axis.
 */
export class ChannelLabel extends React.Component<ChannelLabelProps, {}> {

    private yAxisRef: YAxis | null;

    /**
     * react lifecycle method
     */
    public render() {
        const isSelected = this.props.selectedChannels
            && this.props.selectedChannels.indexOf(this.props.channelConfig.id) > -1;

        const labelStyle = {
            ...channelLabelStyle,
            filter: this.props.subChannel ? 'brightness(0.7)' : undefined,
            textShadow: isSelected ? '0px 1px 15px' : 'initial'
        };

        const distString = this.props.distanceToSource === 0 ? '' : this.props.distanceToSource;
        return (
            <div
                style={{
                    display: 'flex',
                    borderBottom: '1px solid'
                }}
                className="weavess-channel-label"
            >
                <div
                    style={labelStyle}
                    onClick={e => {
                        if (this.props.onChannelLabelClick) {
                            this.props.onChannelLabelClick(e, this.props.channelConfig.id);
                        }
                    }}
                >
                    {this.props.channelConfig.name}
                    <div>
                        <p>{distString}{' '}
                            <span
                                style={{
                                    color: 'red'
                                }}
                            >
                                {this.props.showMaskIndicator ? 'M' : null}
                            </span>
                        </p>
                    </div>
                </div>
                <YAxis
                    ref={ref => this.yAxisRef = ref}
                    maxAmplitude={this.props.yAxisBounds[1]}
                    minAmplitude={this.props.yAxisBounds[0]}
                />
            </div>
        );
    }

    /**
     * React component lifecycle
     */
    public componentDidUpdate() {
        this.refreshYAxis();
    }

    public refreshYAxis = () => {
        if (this.yAxisRef) {
            this.yAxisRef.display();
        }
    }
}

/**
 * StationLabel Props
 */
export interface StationLabelProps {
    stationConfig: Entities.StationConfig;

    expanded: boolean;
    selectedChannels: string[] | undefined;

    // [min, max];
    yAxisBounds: {
        channelId: string;
        bounds: [number, number];
    }[];

    toggleExpansion(stationId: string): void;
    onChannelLabelClick?(e: React.MouseEvent<HTMLDivElement>, channelName: string): void;
}

export interface StationLabelState {
    showMaskIndicator: boolean | false;
}

/**
 * Label for a station. contains one or more child ChannelLabels
 */
export class StationLabel extends React.Component<StationLabelProps, StationLabelState> {
    /**
     * default config for the station, if none has been provided
     */
    public static defaultProps: Entities.StationDefaultConfig = {
        height: 75
    };

    public constructor(props: StationLabelProps) {
        super(props);

        // check to see if there are any masks on the default channel or any of its non-default channels
        const showMaskIndicator =
            Boolean((this.props.stationConfig.nonDefaultChannels && this.props.stationConfig.nonDefaultChannels
                .map(channel => ((channel.masks !== undefined) && (channel.masks.length > 0)))
                .reduce((c1, c2) => c1 || c2, false)));
        this.state = {
            showMaskIndicator
        };
    }

    /**
     * react lifecycle
     */
    public render() {
        const height = this.props.expanded && this.props.stationConfig.nonDefaultChannels ?
            (this.props.stationConfig.height || StationLabel.defaultProps.height) *
            (this.props.stationConfig.nonDefaultChannels.length + 1)
            : (this.props.stationConfig.height || StationLabel.defaultProps.height);

        const defaultChannelBoundsData = this.props.yAxisBounds
            .find(bounds => bounds.channelId === this.props.stationConfig.defaultChannel.id);

        const defaultChannelBounds: [number, number] = defaultChannelBoundsData ?
            defaultChannelBoundsData.bounds
            : [-1, 1];

        return (
            <div
                style={{
                    height,
                    transition: 'height 0.1s linear',
                    display: 'flex',
                    flexDirection: 'column',
                    flex: '0 0 auto',
                    backgroundColor: '#202B33',
                }}
            >
                <div
                    style={{
                        display: 'flex',
                        height: `${(this.props.stationConfig.height || StationLabel.defaultProps.height)}px`,
                        flex: '0 0 auto'
                    }}
                >
                    <div
                        style={{
                            width: '24px',
                            flex: '0 0 auto',
                            display: 'flex',
                            justifyContent: 'center',
                            alignItems: 'center',
                            borderBottom: '1px solid'
                        }}
                    >
                        {
                            /* show expansion button if there are additional channels */
                            !this.props.stationConfig.nonDefaultChannels ||
                                this.props.stationConfig.nonDefaultChannels.length !== 0 ?
                                (

                                    <div
                                        style={{
                                            padding: '2px',
                                            cursor: 'pointer',
                                        }}
                                        onClick={e => this.props.toggleExpansion(this.props.stationConfig.id)}
                                    >
                                        {this.props.expanded ? '-' : '+'}
                                    </div>
                                )
                                : <></>
                        }
                    </div>
                    {/* The default channel */}
                    <ChannelLabel
                        subChannel={false}
                        selectedChannels={this.props.selectedChannels}
                        onChannelLabelClick={this.props.onChannelLabelClick}
                        yAxisBounds={defaultChannelBounds}
                        channelConfig={{ ...this.props.stationConfig.defaultChannel }}
                        showMaskIndicator={this.state.showMaskIndicator}
                        distanceToSource={this.props.stationConfig.distanceKm ?
                            this.props.stationConfig.distanceKm : 0}
                    />
                </div>
                {
                    this.props.expanded && this.props.stationConfig.nonDefaultChannels ?
                        (
                            this.props.stationConfig.nonDefaultChannels.map(channelConfig => {

                                const channelBoundsData = this.props.yAxisBounds
                                    .find(bounds => bounds.channelId === channelConfig.id);

                                const channelBounds: [number, number] = channelBoundsData ?
                                    channelBoundsData.bounds
                                    : [-1, 1];

                                return (
                                    <div
                                        key={channelConfig.id}
                                        style={{
                                            display: 'flex',
                                            height: `${(this.props.stationConfig.height
                                                || StationLabel.defaultProps.height)}px`,
                                            flex: '0 0 auto'
                                        }}
                                    >
                                        <div
                                            style={{
                                                width: '0px',
                                                flex: '0 0 auto',
                                                display: 'flex',
                                                justifyContent: 'center',
                                                alignItems: 'center',
                                                borderLeft: '24px solid dodgerblue',
                                            }}
                                        />
                                        {/* Additional, non-default channels */}
                                        <ChannelLabel
                                            channelConfig={{ ...channelConfig }}
                                            subChannel={true}
                                            selectedChannels={this.props.selectedChannels}
                                            onChannelLabelClick={this.props.onChannelLabelClick}
                                            yAxisBounds={channelBounds}
                                            showMaskIndicator={false}
                                            distanceToSource={0}
                                        />
                                    </div>
                                );
                            })
                        )
                        : <></>
                }
            </div>
        );
    }
}
