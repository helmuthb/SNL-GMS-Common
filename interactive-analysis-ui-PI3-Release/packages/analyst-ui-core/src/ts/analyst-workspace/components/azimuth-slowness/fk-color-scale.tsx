import * as d3 from 'd3';
import * as React from 'react';
import { createColorScaleImageBitmap } from './fk-util';

/**
 * FkColorScale Props
 */
// tslint:disable-next-line:no-empty-interface
export interface FkColorScaleProps {
    minSlow: number;
    maxSlow: number;
}

/**
 * FkColorScale State
 */
// tslint:disable-next-line:no-empty-interface
export interface FkColorScaleState {
}

/**
 * The color scale size.
 */
export interface ColorScaleSize {
    width: number;
    height: number;
}

/**
 * FkColorScale Component
 */
export class FkColorScale extends React.Component<FkColorScaleProps, FkColorScaleState> {

    public static readonly padding: number = 25;

    /**
     * The color scale size.
     */
    private static readonly colorScaleSize: ColorScaleSize = { width: 80, height: 240 };

    /**
     * Reference to the canvas to draw the color scale.
     */
    private canvasRef: HTMLCanvasElement | undefined;

    /**
     * Used to resize the canvas to fit the container.
     */
    private containerRef: HTMLDivElement | undefined;

    /**
     * Canvas rendering context used to draw the color scale.
     */
    private ctx: CanvasRenderingContext2D;

    /**
     * The current color scale represented as an ImageBitmap.
     */
    private currentImage: ImageBitmap | undefined;

    /**
     * The x-axis div container.
     */
    private xAxisContainerRef: HTMLDivElement | undefined;

    /**
     * constructor
     */
    public constructor(props: FkColorScaleProps) {
        super(props);
    }

    /**
     * React component lifecycle
     */
    public render() {
        return (
            <div
                ref={ref => this.containerRef = ref}
                style={{
                    position: 'relative',
                    width: '100%',
                    height: '100%',
                }}
            >
                <div
                    ref={ref => this.xAxisContainerRef = ref}
                    style={{
                        position: 'absolute',
                        right: '0px',
                        left: '0px',
                        bottom: '0px',
                        height: '35px',
                        top: '18px'
                    }}
                />
                <canvas
                    style={{
                        position: 'absolute',
                        right: '25px',
                        left: '25px',
                        top: '0px',
                        zIndex: 0
                    }}
                    ref={ref => this.canvasRef = ref}
                />
            </div>
        );
    }

    /**
     * React component lifecycle
     */
    public async componentDidMount() {
        this.ctx = this.canvasRef.getContext('2d');
        this.ctx.imageSmoothingEnabled = false;
        this.currentImage =
            await createColorScaleImageBitmap(
                FkColorScale.colorScaleSize.width,
                FkColorScale.colorScaleSize.height);
        this.draw();
    }

    /**
     * React component lifecycle
     */
    public async componentDidUpdate(prevProps: FkColorScaleProps) {
        this.ctx = this.canvasRef.getContext('2d');
        this.ctx.imageSmoothingEnabled = false;
        this.currentImage =
            await createColorScaleImageBitmap(
                FkColorScale.colorScaleSize.width,
                FkColorScale.colorScaleSize.height);
        this.draw();
    }

    private draw() {
        const height = 50;
        this.canvasRef.width = this.xAxisContainerRef.clientWidth - (FkColorScale.padding * 2);
        this.canvasRef.height = height;
        this.ctx.drawImage(this.currentImage, 0, 0, this.canvasRef.width, height);

        this.createXAxis();
    }

    /**
     * Create and draw the x-axis.
     */
    private createXAxis() {
        if (!this.xAxisContainerRef) return;
        this.xAxisContainerRef.innerHTML = '';

        const svg = d3.select(this.xAxisContainerRef)
            .append('svg')
            .attr('width', this.xAxisContainerRef.clientWidth)
            .attr('height', this.xAxisContainerRef.clientHeight)
            .style('fill', '#ddd');

        const svgAxis = svg.append('g')
            .attr('class', 'fk-axis');

        const x = d3.scaleLinear()
            .domain([this.props.minSlow, this.props.maxSlow])
            .range([FkColorScale.padding, this.xAxisContainerRef.clientWidth - FkColorScale.padding]);

        const range = this.props.maxSlow - this.props.minSlow;
        const tickSize = 7;
        const xAxis = d3.axisBottom(x)
            .tickSize(tickSize)
            .tickValues([
                this.props.minSlow,
                this.props.minSlow + range / 4,
                this.props.minSlow + range / 2,
                (this.props.minSlow + range / 4) * 3,
                this.props.maxSlow
            ])
            .tickFormat(d3.format('.1s'));
        svgAxis.call(xAxis);
    }
}
