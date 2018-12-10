import * as d3 from 'd3';
import * as React from 'react';

/**
 * YAxis Props
 */
export interface YAxisProps {
    maxAmplitude: number;
    minAmplitude: number;
}

const paddingPx = 8;

/**
 * Y axis for an individual waveform
 */
export class YAxis extends React.Component<YAxisProps, {}> {

    /**
     * Handle to the axis wrapper HTMLElement
     */
    private axisRef: HTMLElement | null;

    /**
     * Handle to the d3 svg selection, where the axis will be created.
     */
    private svgAxis: d3.Selection<Element | d3.EnterElement | Document | Window | null, {}, null, undefined>;

    /**
     * todo
     */

    /**
     * React lifecycle
     */
    public render() {
        return (
            <div
                style={{ height: '100%', width: '50px' }}
                ref={axisRef => { this.axisRef = axisRef; }}
            />
        );
    }

    /**
     * React lifecycle
     */
    public componentDidMount() {
        if (!this.axisRef) return;

        const svg = d3.select(this.axisRef)
            .append('svg');
        svg.attr('height', this.axisRef.clientHeight)
            // tslint:disable-next-line:no-magic-numbers
            .attr('width', 50);
        this.svgAxis = svg.append('g')
            .attr('class', 'weavess-axis')
            // tslint:disable-next-line:no-magic-numbers
            .attr('transform', `translate(${49},0)`);
        this.display();
    }

    /**
     * React lifecycle
     * @param nextProps props for the axis of type YAxisProps
     */
    public shouldComponentUpdate(nextProps: YAxisProps) {
        const hasChanged = !(this.props.maxAmplitude === nextProps.maxAmplitude
            && this.props.minAmplitude === nextProps.minAmplitude);
        return hasChanged;
    }

    /**
     * React lifecycle
     */
    public componentDidUpdate() {
        this.display();
    }

    /**
     * Draw the axis
     */
    public display = () => {
        if (!this.axisRef) return;

        d3.select(this.axisRef)
            .select('svg')
            .attr('height', this.axisRef.clientHeight)
            // tslint:disable-next-line:no-magic-numbers
            .attr('width', 50);
        const heightPx = this.axisRef.clientHeight;
        const min = this.props.minAmplitude * ((heightPx - paddingPx) / heightPx);
        const max = this.props.maxAmplitude * ((heightPx - paddingPx) / heightPx);
        const totalTicks = 5;
        const yAxisScale = d3.scaleLinear()
            .domain([min, max])
            .range([this.axisRef.clientHeight - paddingPx, paddingPx]);
        const tickValues = [min, 0, max];
        const yAxis = d3.axisLeft(yAxisScale)
            .tickValues(tickValues);
        yAxisScale.ticks(totalTicks);
        this.svgAxis.call(yAxis);
    }
}
