/**
 * Interfaces for GMS Map
 * Currently cesium-map.ts implements this interface
 */

import { AnalystUiConfig } from '../../config';
import { TimeInterval } from '../../state';

export interface MapState {
    layers: {
        Events: any;
        Stations: any;
        SDs: any;
    };
}

export interface MapOptions {
    analystUiConfig: AnalystUiConfig;
    events: {
        onMapClick(e: any, entity?: any): void;
        onMapShiftClick(e: any, entity?: any): void;
        onMapDoubleClick(e: any, entity?: any): void;
    };
}

/**
 * Interface/API for GMS Map. Currently, at a minimum a map must implement the methods below
 */
export interface MapAPI {
    state: MapState;

    getDataLayers();
    initialize(containerElement: any);
    createMapViewer(containerElement: any);
    setupLayers(viewer: any);
    drawSignalDetections(signalDetections: any[], nextOpenEventHypId: string);
    highlightSelectedSignalDetections(selectedSignalDetection: string[]);
    drawDefaultStations(currentStations: any[], nextStations: any[]);
    updateStations(currentSignalDetections: any[], currentOpenEventHypId: string,
                   nextSignalDetections: any[], nextOpenEventHypId: string);
    drawEvents(currenProps: any, nextProps: any);
    highlightOpenEvent(currentTimeInterval: TimeInterval, currentOpenEvent: any,
                       nextOpenEvent: any, selectedEventIds: string[]);
    resetView(nextProps: any);
}
