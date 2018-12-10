import * as d3 from 'd3';
import * as React from 'react';

import * as moment from 'moment';

/**
 * Props for the time axis
 */
export interface TimeAxisProps {
    endTimeSecs: number;
    startTimeSecs: number;
    borderTop: boolean;

    getViewRange(): [number, number];
}

const MILLIS_TO_S = 1000;

/**
 * a D3-based Time Axis component
 */
export class TimeAxis extends React.Component<TimeAxisProps, {}> {

    /**
     * A handle to the axis wrapper HTML element
     */
    public axisRef: HTMLElement | null;

    /**
     * A handle to the svg selection d3 returns, where the axis will be created
     */
    private svgAxis: d3.Selection<Element | d3.EnterElement | Document | Window | null, {}, null, undefined>;

    /**
     * react component lifecycle
     */
    public render() {
        return (
            <div
                ref={axis => { this.axisRef = axis; }}
                style={{
                    backgroundColor: '#202B33',
                    borderTop: this.props.borderTop ? '1px solid' : undefined,
                    height: '35px',
                }}
            />
        );
    }

    /**
     * react component lifecycle
     */
    public componentDidMount() {

        const svg = d3.select(this.axisRef)
            .append('svg')
            .attr('width', '100%')
            // tslint:disable-next-line:no-magic-numbers
            .attr('height', 35)
            .style('fill', '#ddd');

        this.svgAxis = svg.append('g')
            .attr('class', 'weavess-axis');
        this.update();
    }

    /**
     * React component lifecycle
     */
    public componentDidUpdate() {
        this.update();
    }

    /**
     * re-draw the axis based on new parameters
     * Not a react life cycle method. Used to manually update the time axis
     * This is done to keep it performant, and not have to rerender the DOM 
     */
    public update = () => {

        if (!this.axisRef) return;

        const durationSecs = this.props.endTimeSecs - this.props.startTimeSecs;
        const axisStart = this.props.startTimeSecs + (durationSecs * this.props.getViewRange()[0]);
        const axisEnd = this.props.startTimeSecs + (durationSecs * this.props.getViewRange()[1]);
        const x = d3.scaleUtc()
            .domain([new Date(axisStart * MILLIS_TO_S), new Date(axisEnd * MILLIS_TO_S)])
            // tslint:disable-next-line:no-magic-numbers
            .range([184, this.axisRef.clientWidth - 10]);

        // adding in some time axis date label formatting
        const tickFormatter = (date: Date) => moment.utc(date)
            .format('HH:mm:ss.SS');
        // d3.utcDay(date) < date ? d3.utcFormat('%Y-%m-%d %H:%M:%S')(date) : d3.utcFormat('%Y-%m-%d')(date);
        // tslint:disable-next-line:no-magic-numbers
        const numTicks = Math.floor((this.axisRef.clientWidth - 200) / 135);
        const tickSize = 7;
        const xAxis = d3.axisBottom(x)
            .ticks(numTicks)
            .tickSize(tickSize)
            .tickFormat(tickFormatter);
        this.svgAxis.call(xAxis);
    }
}
