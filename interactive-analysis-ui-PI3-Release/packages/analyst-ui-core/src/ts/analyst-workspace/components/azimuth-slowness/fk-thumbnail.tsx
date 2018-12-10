import { Colors, NonIdealState } from '@blueprintjs/core';
import * as d3 from 'd3';
import * as React from 'react';
import { systemConfig } from '../../config';
import { computeMinMaxFkValues, createFkImageBitmap, drawCircle } from './fk-util';
import { SdFkData } from './graphql/query';

/**
 * Fk Thumbnail Props.
 */
export interface FkThumbnailProps {
    data: SdFkData;
    sizePx: number;
    selected: boolean;

    onClick(e: React.MouseEvent<HTMLDivElement>): void;
    isMustUnAcceptedFk(fk: SdFkData): boolean;
}

/**
 * Fk Thumbnail State
 */
// tslint:disable-next-line:no-empty-interface
export interface FkThumbnailState {

}

/**
 * Fk Thumbnail component.
 */
export class FkThumbnail extends React.Component<FkThumbnailProps, FkThumbnailState> {

    /**
     * destination to draw the fk.
     */
    private canvasRef: HTMLCanvasElement | undefined;

    /**
     * Used to resize the canvas to fit the container.
     */
    private containerRef: HTMLDivElement;

    /**
     * Canvas rendering context used to draw the fk.
     */
    private ctx: CanvasRenderingContext2D;

    /**
     * The current fk represented as an ImageBitmap.
     */
    private currentImage: ImageBitmap;

    public constructor(props: FkThumbnailProps) {
        super(props);
    }

    /**
     * React component lifecycle.
     */
    public render() {
        const classNames = [
            'gms-fk-thumbnail',
            this.props.selected ? 'selected' : undefined].join(' ');
        if (!this.props.data) {
            return (
                <NonIdealState
                    visual="heat-grid"
                    title="All Fks Filtered Out"
                />
            );
        }
        const accepted = !this.props.isMustUnAcceptedFk(this.props.data);
        const label = `${this.props.data.signalDetectionHypothesis.signalDetection.station.name}` +
            ` ${this.props.data.signalDetectionHypothesis.phase}`;
        return (
            <div
                ref={ref => this.containerRef = ref}
                className={classNames}
                style={{
                    position: 'relative',
                    width: `${this.props.sizePx}px`,
                    height: `${this.props.sizePx}px`,
                    marginLeft: '0.22rem',
                    marginRight: '0.22rem',
                    marginBottom: '0.44rem',
                }}
                onClick={this.props.onClick}
            >
                <div
                    className={accepted ?
                        'gms-fk-thumbnail-label-accepted' : 'gms-fk-thumbnail-label'}
                    style={{
                        position: 'absolute',
                        top: '0px',
                        left: '0px',
                        padding: '0.15rem',
                        zIndex: 1,
                        fontSize: '0.6rem'
                    }}
                >
                    {label}
                </div>
                <canvas
                    style={{
                        position: 'absolute',
                        left: '0px',
                        right: '0px',
                        top: '0px',
                        bottom: '0px',
                        zIndex: 0
                    }}
                    ref={ref => this.canvasRef = ref}
                />

            </div>
        );
    }

    /**
     * React component lifecycle.
     */
    public async componentDidMount() {
        const fkData = this.props.data.signalDetectionHypothesis.azSlownessMeasurement.fkData;
        this.ctx = this.canvasRef.getContext('2d');
        this.ctx.imageSmoothingEnabled = false;
        const [min, max] = computeMinMaxFkValues(fkData.fkGrid);
        this.currentImage = await createFkImageBitmap(fkData.fkGrid, min, max);
        this.draw();
    }

    /**
     * React component lifecycle.
     */
    public async componentDidUpdate(prevProps: FkThumbnailProps) {
        if (prevProps.data !== this.props.data) {
            const fkData = this.props.data.signalDetectionHypothesis.azSlownessMeasurement.fkData;
            const [min, max] = computeMinMaxFkValues(fkData.fkGrid);
            this.currentImage = await createFkImageBitmap(fkData.fkGrid, min, max);
            this.draw();
        } else if (prevProps.sizePx !== this.props.sizePx) {
            this.draw();
        }
    }

    /**
     * Resize & draw the fk on the canvas.
     */
    private draw() {
        const fkData = this.props.data.signalDetectionHypothesis.azSlownessMeasurement.fkData;
        // resize if necessary
        this.canvasRef.width = this.containerRef.clientWidth;
        this.canvasRef.height = this.containerRef.clientHeight;

        this.ctx.drawImage(this.currentImage, 0, 0, this.canvasRef.width, this.canvasRef.height);

        const scale = d3.scaleLinear()
            .domain([0, fkData.slownessScale.maxValue])
            .range([0, this.canvasRef.height / 2]);

        const scaledRadii = systemConfig.defaultFkConfig.fkVelocityRadii.map(scale);

        drawCircle(this.ctx, this.canvasRef.width / 2, this.canvasRef.height / 2, scaledRadii, Colors.WHITE);
    }
}
