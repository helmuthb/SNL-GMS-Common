import * as React from 'react';
import * as ReactDOM from 'react-dom';
import '../css/style.scss';

import { RecordSection } from './components/record-section-display';
import { WaveformDisplay } from './components/waveform-display';

export function initialize(container: any, props: any): void {
    return ReactDOM.render(<WaveformDisplay {...props} />, container) as any;
}

export function initializeRecordSection(container: any, props: any): void {
    return ReactDOM.render(<RecordSection {...props} />, container) as any;
}

export { WaveformDisplay, WaveformDisplayProps } from './components/waveform-display';
export { RecordSection } from './components/record-section-display';
export { createDummyWaveform } from './util/WaveformUtils';

export * from './entities';
