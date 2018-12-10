import { Colors } from '@blueprintjs/core';
import * as d3 from 'd3';

export const createColorScale: any = (min: number, max: number) => d3
    .scaleSequential(t => {
        if (t < 0 || t > 1) {
            // tslint:disable-next-line:no-parameter-reassignment
            t -= Math.floor(t);
        }
        // tslint:disable-next-line:no-magic-numbers
        const ts = Math.abs(t - 0.5);
        // map to range [240, 0] hue
        // tslint:disable-next-line:no-magic-numbers binary-expression-operand-order
        return d3.hsl(240 - (240 * t), 1.5 - 1.5 * ts, 0.8 - 0.9 * ts);
    })
    .domain([min, max]);

/**
 * Convert fk data to an ImageBitmap
 */
export const createFkImageBitmap = async (fkData: number[][], min: number, max: number): Promise<ImageBitmap> => {
    const dim = fkData.length;
    const size = dim * dim;
    // tslint:disable-next-line:no-magic-numbers
    const buffer = new Uint8ClampedArray(size * 4); // r, g, b, a for each point
    const uInt8Max = 255;

    const colorScale = createColorScale(min, max);
    for (let row = 0; row < fkData.length; row++) {
        for (let col = 0; col < fkData[0].length; col++) {
            const value = fkData[row][col];
            // tslint:disable-next-line:no-magic-numbers
            const pos = (row * fkData.length + col) * 4;

            const color = d3.rgb(colorScale(value));
            buffer[pos] = color.r;
            buffer[pos + 1] = color.g;
            buffer[pos + 2] = color.b;
            buffer[pos + 3] = uInt8Max;
        }
    }

    const imgData = new ImageData(buffer, fkData.length, fkData.length);
    const imgBitmap = window.createImageBitmap(imgData);

    return imgBitmap;
};

/**
 * Create heat map color scale.
 */
export const createColorScaleImageBitmap = async (heightPx: number, widthPx: number): Promise<ImageBitmap> => {
    const size = heightPx * widthPx;
    const buffer = new Uint8ClampedArray(size * 4); // r, g, b, a for each point
    const uInt8Max = 255;

    const colorScale = createColorScale(0, heightPx + 1);
    for (let row = 0; row < heightPx; row++) {
        for (let col = 0; col < widthPx; col++) {
            const pos = (row * heightPx + col) * 4;

            const color = d3.rgb(colorScale(col));
            buffer[pos] = color.r;
            buffer[pos + 1] = color.g;
            buffer[pos + 2] = color.b;
            buffer[pos + 3] = uInt8Max;
        }
    }

    const imgData = new ImageData(buffer, heightPx, widthPx);
    const imgBitmap = window.createImageBitmap(imgData);

    return imgBitmap;
};

export const drawCircle = (
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    radii: number[],
    strokeColor: string = Colors.RED3,
    isFilled: boolean = false) => {
    ctx.strokeStyle = strokeColor;

    radii.forEach(radius => {
        ctx.beginPath();
        ctx.arc(x, y, radius, 0, Math.PI * 2);
        if (isFilled) {
            ctx.fillStyle = strokeColor;
            ctx.fill();
        } else {
            ctx.stroke();
        }
    });
};

export const computeMinMaxFkValues = (fkData: number[][]): [number, number] => {
    let max = -Infinity;
    let min = Infinity;

    for (const row of fkData) {
        for (const val of row) {
            if (val > max) max = val;
            if (val < min) min = val;
        }
    }

    return [min, max];
};
