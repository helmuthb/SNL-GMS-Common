import * as React from 'react';
import { WaveformDisplay, WaveformDisplayProps } from '../src/ts/components/waveform-display';
import { StationConfig, DisplayType,
    WeavessEvents, StationDefaultConfig, ChannelConfig, ChannelDefaultConfig, ChannelSegment,
    SignalDetectionConfig, TheoreticalPhaseWindow, Mask, WeavessSelections,
    HotKeyOverridesConfig, MeasureWindowSelection
} from '../src/ts/entities';
import {
    startTimeSeconds,
    endTimeSeconds
} from '../../../test/__test_runner_config__/analyst-ui-core/azimuth-slowness/test_util';

// function initialize(container: any, props: any): void {
// function initializeRecordSection(container: any, props: any): void {
// import { RecordSection } from '../src/ts/components/record-section-display';
// import { createDummyWaveform } from '../../weavess/src/ts/util/WaveformUtils';

const sampRate = 40;

const stationConfig: StationConfig[] = [
    // Beam
    {
        id: 'Beam',
        name: 'Beam',
        height: 75,
        defaultChannel: {
            id: 'Beam',
            name: 'Beam',
            color: 'dodgerblue',
            sampleRate: sampRate,
            dataSegments: [{
                startTimeSecs: startTimeSeconds,
                data: []
            }],
            displayType: [DisplayType.LINE],
            pointSize: 2,
        },
        nonDefaultChannels: []
    },
    // Fstat
    {
        id: 'Fstat',
        name: 'Fstat',
        height: 75,
        defaultChannel: {
            id: 'Fstat',
            name: 'Fstat',
            color: 'dodgerblue',
            sampleRate: sampRate,
            dataSegments: [{
                startTimeSecs: startTimeSeconds,
                data: []
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
        height: 75,
        defaultChannel: {
            id: 'Azimuth',
            name: 'Azimuth',
            color: 'dodgerblue',
            sampleRate: sampRate,
            dataSegments: [{
                startTimeSecs: startTimeSeconds,
                data:[]
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
        height: 75,
        defaultChannel: {
            id: 'Slowness',
            name: 'Slowness',
            color: 'dodgerblue',
            sampleRate: sampRate,
            dataSegments: [{
                startTimeSecs: startTimeSeconds,
                data: []
            }],
            displayType: [DisplayType.LINE, DisplayType.SCATTER],
            pointSize: 2,
        },
        nonDefaultChannels: []
    }
];

const waveformDisplayProps: WaveformDisplayProps = {
    startTimeSecs: startTimeSeconds,
    endTimeSecs: endTimeSeconds,
    stations: stationConfig,
    defaultZoomWindow: {
        startTimeSecs: startTimeSeconds,
        endTimeSecs: endTimeSeconds
    },
    verticalMarkers: {
        color: 'dodgerblue',
        timeSecs: 10
    }[0],
    flex: false
};

// This is the actual test written for Jest
it('test to see if the waveform display renders correctly', () => {
  const wrapper = shallow(
    <div
        style={{
            height: '100%',
            width: '100%',
            display: 'flex',
        }}
    >
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
                < WaveformDisplay
                    {...waveformDisplayProps}
                />
            </div>
        </div>
    </div>
    );
  expect(wrapper).toMatchSnapshot();
});
