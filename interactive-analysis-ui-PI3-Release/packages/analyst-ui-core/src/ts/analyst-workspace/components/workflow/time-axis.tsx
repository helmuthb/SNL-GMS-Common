import * as d3 from 'd3';
import * as moment from 'moment';
import * as React from 'react';

/**
 * Props for the workflow time axis
 */
export interface TimeAxisProps {
    startTimeSecs: number;
    endTimeSecs: number;
    intervalDurationSecs: number;

    pixelsPerHour: number;
}

/**
 * Time axis for the Workflow display
 */
export class WorkflowTimeAxis extends React.Component<TimeAxisProps, {}> {

    /**
     * Handle to the dom element where the time axis will be created
     */

    private timeAxisContainer: HTMLDivElement;

    /**
     * The d3 time axis
     */
    private timeAxis: d3.Selection<Element | d3.EnterElement | Document | Window, {}, null, undefined>;

    /**
     * Display the time axis
     */
    public render() {
        return (
            <div
                style={{
                    marginLeft: '0px', width: 'calc(100% - 100px)',
                    height: '25px', flex: '0 0 auto'
                }}
            >
                <div
                    style={{ width: '100%', height: '25px', overflow: 'hidden' }}
                    ref={ref => { this.timeAxisContainer = ref; }}
                />
            </div>
        );
    }

    /**
     * On mount, create & render the d3 axis
     */
    public componentDidMount() {
        this.createAxis();
    }

    /**
     * re-draw axis on update.
     */
    public componentDidUpdate() {
        this.updateAxis();
    }

    /**
     * set the scrollLeft style attribute of the time axis
     * @param scrollLeft 
     */
    public setScrollLeft(scrollLeft: number) {
        this.timeAxisContainer.scrollLeft = scrollLeft;
    }

    /**
     * Create & render the d3 axis
     */
    private readonly createAxis = () => {

        const timeAxisHeight = 25;
        this.timeAxis = d3.select(this.timeAxisContainer)
            .append('svg')
            .attr('height', timeAxisHeight)
            .style('fill', '#ddd');
        this.timeAxis.append('g')
            .attr('class', 'gms-workflow-time-axis');

        this.updateAxis();
    }

    private readonly updateAxis = () => {
        const rightPadding = 110;
        const SECONDS_PER_HOUR = 3600;
        const axisWidthPx = (this.props.endTimeSecs - this.props.startTimeSecs)
            / SECONDS_PER_HOUR * this.props.pixelsPerHour;

        this.timeAxis.attr('width', axisWidthPx + rightPadding);

        const leftPadding = 35;
        const tickFormatter = (date: Date) =>
            d3.utcDay(date) < date ? d3.utcFormat('%H:%M')(date) : d3.utcFormat('%Y-%m-%d')(date);
        const scale = d3.scaleUtc()
            .domain([moment.unix(this.props.startTimeSecs)
                // tslint:disable-next-line:no-magic-numbers
                .subtract(10, 's')
                .toDate(),
            moment.unix(this.props.endTimeSecs)
                .toDate()])
            .rangeRound([leftPadding, axisWidthPx + leftPadding]);

        const tickSize = 7;
        const axis = d3.axisBottom(scale)
            .ticks(d3.utcHour.every(2))
            .tickSize(tickSize)
            .tickFormat(tickFormatter);
        this.timeAxis.select('.gms-workflow-time-axis')
            .transition()
            // tslint:disable-next-line:no-magic-numbers
            .duration(1500)
            .call(axis as any);

        this.timeAxis.select('.gms-workflow-time-axis')
            .selectAll('text')
            .each(function (tick: Date) {
                // tslint:disable-next-line:no-unused-expression
                tick.getUTCHours() === 0 ? (this as any).classList.add('day-label') : null;
            });
    }

}
