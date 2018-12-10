export interface FkImageData {
    id: string;
    label: string;
    fk: number[][];
    maxSlowness: number;
    distanceKm: number; // Comes from SignalDetection populated by graphQL
    accepted: boolean;
    peakFk: {
        xSlowness: number;
        ySlowness: number;
        azimuthDeg: number;
        radialSlowness: number;
        azimuthUncertainty: number;
        slownessUncertainty: number;
        fstat: number;
    };
    theoreticalFk: {
        xSlowness: number;
        ySlowness: number;
        azimuthDeg: number;
        radialSlowness: number;
        azimuthUncertainty: number;
        slownessUncertainty: number;
        fstat: number;
    };
    contribChannels: {
        id: string;
    } [];
    frequencyBand: {
        minFrequencyHz: number;
        maxFrequencyHz: number;
    };
}
