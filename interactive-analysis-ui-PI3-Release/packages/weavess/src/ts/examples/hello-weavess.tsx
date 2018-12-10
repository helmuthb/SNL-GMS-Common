import * as React from 'react';
import { WaveformDisplay } from '../components/waveform-display';
import { StationConfig } from '../entities';

function generateDummyData(startTimeSecs: number): StationConfig[] {
    const SAMPLE_RATE = 40;
    // tslint:disable-next-line:no-magic-numbers
    const NUM_SAMPLES = SAMPLE_RATE * 600; // 10 minutes of data
    const stations: StationConfig[] = [];

    // tslint:disable-next-line:no-magic-numbers
    for (let i = 0; i < 50; i++) {
        const data = new Float32Array(NUM_SAMPLES);
        for (let samp = 0; samp < NUM_SAMPLES; samp++) {
            // tslint:disable-next-line:no-magic-numbers
            data[samp] = Math.pow(Math.random() - 0.5, 3) * 4;
        }

        const data2 = new Float32Array(NUM_SAMPLES);
        for (let samp = 0; samp < NUM_SAMPLES; samp++) {
            // tslint:disable-next-line:no-magic-numbers
            data2[samp] = Math.pow(Math.random() - 0.5, 3) * 4;
        }

        stations.push({
            id: String(i),
            name: `station${i}`,
            height: 50,
            defaultChannel: {
                id: String(i),
                color: '#2965CC',
                name: `channel${i}`,
                sampleRate: SAMPLE_RATE,
                description: 'simulated data',
                theoreticalPhaseWindows: [{
                    color: 'red',
                    // tslint:disable-next-line:no-magic-numbers
                    startTimeSecs: startTimeSecs + 700,
                    // tslint:disable-next-line:no-magic-numbers
                    endTimeSecs: startTimeSecs + 950,
                    id: '1',
                    label: 'PKP'
                }],
                dataSegments: [{
                    data,
                    startTimeSecs
                }, {
                    data: data2,
                    // tslint:disable-next-line:no-magic-numbers
                    startTimeSecs: startTimeSecs + 900
                }],
                signalDetections: [{
                    id: '1',
                    // tslint:disable-next-line:no-magic-numbers
                    timeSecs: startTimeSecs + 450,
                    color: 'red',
                    label: 'P'
                }]
            },
            nonDefaultChannels: [],
        });
    }

    return stations;
}

export const TestCompoennt = () => (
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
                <WaveformDisplay
                    // tslint:disable-next-line:no-magic-numbers
                    startTimeSecs={1507593600}
                    // tslint:disable-next-line:no-magic-numbers
                    endTimeSecs={1507593600 + 1800}
                    // tslint:disable-next-line:no-magic-numbers
                    stations={generateDummyData(1507593600)}
                />
            </div>
        </div>
    </div>
);
