/* tslint:disable:no-bitwise*/
import * as moment from 'moment';

import * as Entities from '../entities';

export function UUIDv4(a?: number) {
    // tslint:disable-next-line:no-magic-numbers
    return a ? (a ^ Math.random() * 16 >> a / 4).toString(16) :
        // tslint:disable-next-line
        (([1e7] as any) + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, UUIDv4);
}

export function createDummyWaveform(samples: number, eventAmplitude: number,
    noiseAmplitude: number): Entities.StationConfig {

    let currentEventAmplitude = 0;
    let currentEventPeak = 0;
    let eventBuildup = 0;
    const data: any[] = [];
    const signalDetections: any[] = [];
    const theoreticalPhaseWindows: any[] = [];
    const startTime = moment('2016-01-01T00:00:00Z');
    const currentTime = startTime.clone();
    const sampleRate = 40;

    const theoreticalPhaseWindowColors = ['gold', 'plum', 'cyan'];

    for (let i = 1; i < samples; i++) {

        // tslint:disable-next-line:no-magic-numbers
        if (i % Math.round(samples / (Math.random() * 10)) === 0) {
            // tslint:disable-next-line:no-magic-numbers
            currentEventAmplitude = 0.05;
            currentEventPeak = Math.random() * eventAmplitude;
            eventBuildup = 1;
            signalDetections.push({
                color: 'red',
                id: UUIDv4()
                    .toString(),
                label: 'P',
                time: currentTime.toISOString(),
            });
            theoreticalPhaseWindows.push({
                color: theoreticalPhaseWindowColors[Math.floor(Math.random() * theoreticalPhaseWindowColors.length)],
                endTime: currentTime.clone()
                    .add(1, 's')
                    .toISOString(),
                id: UUIDv4()
                    .toString(),
                label: 'P',
                startTime: currentTime.clone()
                    .subtract(1, 's')
                    .toISOString(),
            });
        }
        if (currentEventAmplitude >= currentEventPeak) {
            eventBuildup = -1;
        }
        if (eventBuildup === 1) {
            // tslint:disable-next-line:no-magic-numbers
            currentEventAmplitude += currentEventAmplitude * (1 / samples) * 125;
        } else if (eventBuildup === -1) {
            // tslint:disable-next-line:no-magic-numbers
            currentEventAmplitude -= currentEventAmplitude * (1 / samples) * 62;
        }
        if (currentEventAmplitude < 0) {
            currentEventAmplitude = 0;
        }
        data.push(currentEventAmplitude + noiseAmplitude - Math.random() * noiseAmplitude * 2
            - Math.random() * currentEventAmplitude * 2);
        currentTime.add(1 / sampleRate, 's');
    }

    return {
        id: UUIDv4()
            .toString(),
        name: 'dummy station',
        height: 75,
        defaultChannel: {
            id: UUIDv4()
                .toString(),
            name: 'dummy channel',
            sampleRate,
            dataSegments: [{
                data,
                startTimeSecs: new Date().valueOf() / 1000
            }],
            description: `eventAmplitude: ${eventAmplitude.toFixed(2)}, noiseAmplitude: ${noiseAmplitude.toFixed(2)}`,
            signalDetections,
            theoreticalPhaseWindows,
        },
        nonDefaultChannels: [],
    };

}
