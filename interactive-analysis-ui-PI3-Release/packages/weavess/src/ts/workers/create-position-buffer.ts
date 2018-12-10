import * as d3 from 'd3';

/**
 * Input required to create the position buffer
 */
export interface CreatePositionBufferParams {
    glMin: number;
    glMax: number;
    data: number[];
    displayStartTimeSecs: number;
    displayEndTimeSecs: number;
    startTimeSecs: number;
    sampleRate: number;
}

/**
 * Convert number[] + startTime + sample rate into a position buffer of [x,y,z,x,y,z,...]
 */
export const createPositionBuffer = (params: CreatePositionBufferParams): Float32Array => {
    const vertices: Float32Array = new Float32Array(params.data.length * 3);

    const timeToGlScale = d3.scaleLinear()
        .domain([params.displayStartTimeSecs, params.displayEndTimeSecs])
        .range([params.glMin, params.glMax]);

    let time = params.startTimeSecs;
    let i = 0;
    for (const sampleValue of params.data) {
        const x = timeToGlScale(time);
        vertices[i] = x;
        vertices[i + 1] = sampleValue;
        vertices[i + 3] = 0;

        i += 3;

        time += (1 / params.sampleRate);
    }

    return vertices;
};
