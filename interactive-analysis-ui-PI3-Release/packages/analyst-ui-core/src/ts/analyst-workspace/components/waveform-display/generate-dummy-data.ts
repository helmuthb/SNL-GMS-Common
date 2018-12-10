import { createDummyWaveform, StationConfig } from '@gms/weavess';
/**
 * Generate some dummy data for initial display
 */
export function generateDummyData(): StationConfig[] {
    const waveforms = [];
    const numWaveforms = 30;
    const samples = 5000;
    const eventAmplitude = 1;
    const noiseAmplitude = 0.05;
    for (let i = 0; i < numWaveforms; i++) {
        const waveform = createDummyWaveform(samples, eventAmplitude, noiseAmplitude);
        waveform.name = 'channel ' + String(i);
        waveforms.push(waveform);
    }

    return waveforms;
}
