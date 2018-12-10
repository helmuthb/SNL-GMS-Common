import { Colors, Popover, PopoverInteractionKind, Position } from '@blueprintjs/core';
import * as d3 from 'd3';
import * as React from 'react';
import { systemConfig } from '../../config';
import { FkColorScale } from './fk-color-scale';
import { computeMinMaxFkValues, createFkImageBitmap, drawCircle } from './fk-util';
import { FkData, SdFkData } from './graphql/query';

/**
 * FkPrimary Props
 */
export interface FkPrimaryProps {
    data: SdFkData;

    updateCurrentFk(point: AnalystCurrentFk): void;
}

/**
 * FkPrimary State
 */
export interface FkPrimaryState {
    analystCurrentFk: AnalystCurrentFk;

    // the min/max value in the fk data
    minFkValue: number;
    maxFkValue: number;
}

export interface AnalystCurrentFk {
    x: number;
    y: number;
}

/**
 * FkPrimary Component
 */
export class FkPrimary extends React.Component<FkPrimaryProps, FkPrimaryState> {

    /**
     * The radius size of the markers.
     */
    private static readonly markerRadiusSize: number = 5;

    /**
     * Reference to the canvas to draw the fk.
     */
    private canvasRef: HTMLCanvasElement | undefined;

    /**
     * Used to resize the canvas to fit the container.
     */
    private containerRef: HTMLDivElement | undefined;

    /**
     * canvas rendering context used to draw the fk.
     */
    private ctx: CanvasRenderingContext2D | undefined;

    /**
     * The current fk represented as an ImageBitmap.
     */
    private currentImage: ImageBitmap | undefined;

    /**
     * The y-axis div container.
     */
    private yAxisContainerRef: HTMLDivElement | undefined;

    /**
     * The x-axis div container.
     */
    private xAxisContainerRef: HTMLDivElement | undefined;

    public constructor(props: FkPrimaryProps) {
        super(props);
        this.state = {
            analystCurrentFk: {
                x: undefined,
                y: undefined
            },
            minFkValue: 0,
            maxFkValue: 1
        };
    }

    /**
     * React Component Lifecycle
     */
    public render() {
        return (
            <div
                style={{
                    display: 'flex',
                    flex: '0 0 auto',
                    flexDirection: 'column',
                    width: '300px',
                    height: '300px',
                    marginRight: '1rem'
                }}
            >
                <div
                    ref={ref => this.containerRef = ref}
                    style={{
                        position: 'relative',
                        width: '100%',
                        height: '100%',
                        marginRight: '0.5rem',
                        marginBottom: '0.5rem',
                    }}
                >
                    <div
                        ref={ref => this.yAxisContainerRef = ref}
                        style={{
                            position: 'absolute',
                            top: '-10px',
                            left: '0px',
                            bottom: '25px',
                            width: '35px'
                        }}
                    />
                    <div
                        ref={ref => this.xAxisContainerRef = ref}
                        style={{
                            position: 'absolute',
                            right: '-10px',
                            left: '25px',
                            bottom: '0px',
                            height: '35px'
                        }}
                    />
                    <div
                        style={{
                            position: 'absolute',
                            left: '35px',
                            right: '0px',
                            bottom: '0px',
                            fontSize: '0.5rem',
                            textAlign: 'center',
                            zIndex: 1
                        }}
                    >
                        <Popover
                            interactionKind={PopoverInteractionKind.CLICK}
                            position={Position.BOTTOM}
                            modifiers={{
                                constraints: [{ attachment: 'together', to: 'scrollParent' }]
                            }}
                        >
                            <div>{'slowness (s/km)'}</div>
                            <div
                                style={{
                                    height: 'calc(40px + 0.5rem)',
                                    width: '260px',
                                    padding: '0.25rem',
                                }}
                            >
                                <FkColorScale
                                    minSlow={this.state.minFkValue}
                                    maxSlow={this.state.maxFkValue}
                                />
                            </div>
                        </Popover>
                    </div>
                    <canvas
                        style={{
                            position: 'absolute',
                            right: '0px',
                            top: '0px',
                            zIndex: 0
                        }}
                        ref={ref => this.canvasRef = ref}
                        onClick={this.onPrimaryFkClick}
                    />
                </div>
            </div>
        );
    }

    /**
     * React component lifecycle
     */
    public async componentDidMount() {
        const fkGrid = this.props.data.signalDetectionHypothesis.azSlownessMeasurement.fkData.fkGrid;
        this.ctx = this.canvasRef.getContext('2d');
        this.ctx.imageSmoothingEnabled = false;
        const [min, max] = computeMinMaxFkValues(fkGrid);
        this.currentImage = await createFkImageBitmap(fkGrid, min, max);
        this.draw();
        this.setState({
            maxFkValue: max,
            minFkValue: min
        });
    }

    /**
     * React component lifecycle
     */
    public async componentDidUpdate(prevProps: FkPrimaryProps) {
        const prevFkGrid = prevProps.data.signalDetectionHypothesis.
            azSlownessMeasurement.fkData.fkGrid;
        const currentFkGrid = this.props.data.signalDetectionHypothesis.
            azSlownessMeasurement.fkData.fkGrid;
        this.ctx = this.canvasRef.getContext('2d');
        this.ctx.imageSmoothingEnabled = false;
        if (prevFkGrid !== currentFkGrid) {
            const [min, max] = computeMinMaxFkValues(currentFkGrid);
            this.currentImage = await createFkImageBitmap(currentFkGrid, min, max);
            this.draw();
            this.props.updateCurrentFk(undefined);
            this.setState({
                maxFkValue: max,
                minFkValue: min,
                analystCurrentFk: {
                    x: undefined,
                    y: undefined
                }
            });
        }
    }

    /**
     * Draw the fk on the canvas.
     */
    private readonly draw = () => {
        const fkData: FkData = this.props.data.signalDetectionHypothesis.azSlownessMeasurement.fkData;
        this.createXAxis(fkData);
        this.createYAxis(fkData);

        const padding = 35;
        this.canvasRef.width = this.containerRef.clientWidth - padding;
        this.canvasRef.height = this.containerRef.clientHeight - padding;
        this.ctx.drawImage(this.currentImage, 0, 0, this.canvasRef.width, this.canvasRef.height);

        this.drawFkCrossHairs();
        this.drawVelocityRings();
        this.drawMaxFk(fkData);
        this.drawTheoreticalFk(fkData);
    }

    /**
     * Draw the crosshairs.
     */
    private readonly drawFkCrossHairs = () => {
        this.ctx.strokeStyle = Colors.WHITE;
        this.ctx.moveTo(this.canvasRef.width / 2, 0);
        this.ctx.lineTo(this.canvasRef.width / 2, this.canvasRef.height);
        this.ctx.stroke();

        this.ctx.moveTo(0, this.canvasRef.height / 2);
        this.ctx.lineTo(this.canvasRef.width, this.canvasRef.height / 2);
        this.ctx.stroke();
    }

    /**
     * Draw velocity radius indicators
     */
    private readonly drawVelocityRings = () => {
        const scale = d3.scaleLinear()
            .domain([0, this.props.data.signalDetectionHypothesis.azSlownessMeasurement.fkData.slownessScale.maxValue])
            .range([0, this.canvasRef.height / 2]);

        const radii = systemConfig.defaultFkConfig.fkVelocityRadii
            .sort()
            .reverse();
        const scaledRadii = radii.map(scale);

        const center: any = {
            x: (this.canvasRef.width / 2),
            y: (this.canvasRef.height / 2)
        };

        // add labels for each ring
        scaledRadii.forEach((value: number, index) => {
            this.ctx.fillStyle = Colors.WHITE;
            const label = (index === 0)
                ? `${Math.round(1 / (radii[index]))} k/s`
                : `${Math.round(1 / (radii[index]))}`;
            this.ctx.fillText(label, Number(center.x) + 3, Number(center.y) - (value + 3));
        });

        drawCircle(this.ctx, center.x, center.y, scaledRadii, Colors.WHITE);

    }

    /**
     * Draw the Max FK marker.
     */
    private readonly drawMaxFk = (fkData: FkData) => {
        const scale = d3.scaleLinear()
            .domain([-fkData.slownessScale.maxValue, fkData.slownessScale.maxValue])
            .range([0, this.canvasRef.height]);

        drawCircle(
            this.ctx,
            scale(fkData.peak.xSlowness),
            scale(fkData.peak.ySlowness), [FkPrimary.markerRadiusSize - 1], Colors.WHITE, true);
    }

    /**
     * Draw the theoretical DK marker.
     */
    private readonly drawTheoreticalFk = (fkData: FkData) => {
        const scale = d3.scaleLinear()
            .domain([-fkData.slownessScale.maxValue, fkData.slownessScale.maxValue])
            .range([0, this.canvasRef.height]);

        const x = scale(fkData.theoretical.xSlowness);
        const y = scale(fkData.theoretical.ySlowness);
        const length = FkPrimary.markerRadiusSize - 1;

        this.ctx.strokeStyle = Colors.BLACK;

        this.ctx.beginPath();
        this.ctx.moveTo(x - length, y - length);
        this.ctx.lineTo(x + length, y + length);

        this.ctx.moveTo(x + length, y - length);
        this.ctx.lineTo(x - length, y + length);
        this.ctx.stroke();

        drawCircle(this.ctx, x, y, [FkPrimary.markerRadiusSize], Colors.BLACK, false);
    }

    /**
     * Create and draw the x-axis.
     */
    private readonly createXAxis = (fkData: FkData) => {
        if (!this.xAxisContainerRef) return;
        this.xAxisContainerRef.innerHTML = '';

        const svg = d3.select(this.xAxisContainerRef)
            .append('svg')
            .attr('width', this.xAxisContainerRef.clientWidth)
            .attr('height', this.xAxisContainerRef.clientHeight)
            .style('fill', '#ddd');

        const svgAxis = svg.append('g')
            .attr('class', 'fk-axis');

        const padding = 10;
        const x = d3.scaleLinear()
            .domain([-fkData.slownessScale.maxValue, fkData.slownessScale.maxValue])
            .range([padding, this.xAxisContainerRef.clientWidth - padding - 1]);

        const tickSize = 7;
        const xAxis = d3.axisBottom(x)
            .tickSize(tickSize);
        svgAxis.call(xAxis);
    }

    /**
     * Create and draw the y-axis.
     */
    private readonly createYAxis = (fkData: FkData) => {
        if (!this.yAxisContainerRef) return;
        this.yAxisContainerRef.innerHTML = '';

        const svg = d3.select(this.yAxisContainerRef)
            .append('svg')
            .attr('width', this.yAxisContainerRef.clientWidth)
            .attr('height', this.yAxisContainerRef.clientHeight)
            .style('fill', '#ddd');

        const svgAxis = svg.append('g')
            .attr('class', 'fk-axis')
            .attr('transform', 'translate(34, 0)');

        const padding = 10;
        const y = d3.scaleLinear()
            .domain([-fkData.slownessScale.maxValue, fkData.slownessScale.maxValue])
            .range([this.yAxisContainerRef.clientHeight - padding - 1, padding]);

        const tickSize = 7;
        const yAxis = d3.axisLeft(y)
            .tickSize(tickSize);
        svgAxis.call(yAxis);
    }

    /**
     * When primary fk is clicked, will draw black circle
     */
    private readonly onPrimaryFkClick = (e: React.MouseEvent<HTMLCanvasElement>) => {
        const x = e.clientX - e.currentTarget.getBoundingClientRect().left;
        const y = e.clientY - e.currentTarget.getBoundingClientRect().top;

        const slownessY = e.currentTarget.getBoundingClientRect().bottom - e.clientY;
        const fkData: FkData = this.props.data.signalDetectionHypothesis.azSlownessMeasurement.fkData;
        const scale = d3.scaleLinear()
            .domain([0, this.canvasRef.height])
            .range([-fkData.slownessScale.maxValue, fkData.slownessScale.maxValue]);

        this.ctx.clearRect(0, 0, this.canvasRef.clientWidth, this.canvasRef.clientHeight);
        this.draw();
        drawCircle(this.ctx, x, y, [FkPrimary.markerRadiusSize - 1], Colors.BLACK, true);

        const selectedPoint: AnalystCurrentFk = {
            x: scale(x),
            y: scale(slownessY)
        };

        // Void method call to pass out parameters
        this.props.updateCurrentFk(selectedPoint);

        this.setState({
            analystCurrentFk: selectedPoint
        });
    }
}
