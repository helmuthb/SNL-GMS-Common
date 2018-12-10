import * as d3 from 'd3';
import * as React from 'react';
import { MoveableMarker, MoveableMarkerObject } from '../moveable-marker';

/**
 * SelectionWindow Props
 */
export interface SelectionWindowProps {
    waveformDisplayStartTimeSecs: number;
    waveformDisplayEndTimeSecs: number;
    selectionWindowProps: SelectionWindowObject;
    waveformsViewportRef: HTMLDivElement | null;
    waveformsContainerRef: HTMLDivElement | null;
    updateMoveablelMarkersValue?(moveableMarkers: MoveableMarkerObject[]): void;
}

/**
 * SelectionWindow State
 */
// tslint:disable-next-line:no-empty-interface
export interface SelectionWindowState {

}

/**
 * UI Core level used interface, passed between weavess and UI-Core
 */
export interface SelectionWindowObject {
    startMoveableMarker: MoveableMarkerObject;
    endMoveableMarker: MoveableMarkerObject;
    color: string;
}

/**
 * SelectionWindow Component
 */
export class SelectionWindow extends React.Component<SelectionWindowProps, SelectionWindowState> {

    /**
     * Ref to the time window selection
     */
    private timeWindowSelectionRef: HTMLDivElement | null;

    /**
     * Ref to the lead marker
     */
    private leadBorderRef: MoveableMarker | null;

    /**
     * Ref to the lag marker
     */
    private endBorderRef: MoveableMarker | null;

    /**
     * constructor
     */
    public constructor(props: SelectionWindowProps) {
        super(props);
    }

    /**
     * React component lifecycle
     */
    public render() {
        const percent100 = 100;
        const leftPercent = this.calcLeftPercent(this.props.selectionWindowProps.startMoveableMarker.timeSecs);
        const rightPercent = percent100 -
            this.calcLeftPercent(this.props.selectionWindowProps.endMoveableMarker.timeSecs);
        return (
            <div
                style={{
                    height: '100%',
                    position: 'absolute',
                    width: '100%',
                    pointerEvents: 'none',
                    zIndex: 0
                }}
            >
                <div
                    ref={ref => this.timeWindowSelectionRef = ref}
                    className="weavess-time-window-selection"
                    style={{
                        position: 'absolute', top: '0px', bottom: '0px', display: 'initial',
                        backgroundColor: 'rgba(200,0,0,0.3)', left: `${leftPercent}%`, right: `${rightPercent}%`,
                        pointerEvents: 'auto',
                    }}
                    onMouseDown={e => this.onSelectionWindowClick(e)}
                />
                {this.createMoveableMarkers()};
            </div>
        );
    }

    /**
     * Selection window on click logic, creates mouse move and mouse down
     * Listeners to determine where to move the window and the markers. 
     */
    private readonly onSelectionWindowClick = (e: React.MouseEvent<HTMLDivElement>) => {
        if (e.button === 2 || e.altKey || e.ctrlKey || e.metaKey) return;
        if (!this.props.waveformsViewportRef || !this.props.waveformsContainerRef) return;

        e.stopPropagation();
        let isDragging = true;
        const htmlEle: HTMLDivElement = e.target as HTMLDivElement;
        const mouseXOffset = e.clientX - htmlEle.offsetLeft;
        const viewPortWidth = this.props.waveformsViewportRef.clientWidth;
        const zoomRatio = viewPortWidth / this.props.waveformsContainerRef.clientWidth;
        const precentFrac = 100;
        const percentDragGuard = 0.1;
        let startXPercent = (e.clientX - mouseXOffset) / viewPortWidth * zoomRatio;
        const onMouseMove = (event: MouseEvent) => {
            const currentXPercent = (event.clientX - mouseXOffset) / viewPortWidth * zoomRatio;
            const diffPercent = startXPercent - currentXPercent;

            // begin drag if moving more than 1 pixel
            if (Math.abs(diffPercent) > percentDragGuard && !isDragging) {
                isDragging = true;
            }
            if (isDragging) {
                let diffPct = startXPercent - currentXPercent;

                const diffTimeSecs = (this.props.waveformDisplayEndTimeSecs -
                    this.props.waveformDisplayStartTimeSecs) * diffPct;
                diffPct *= precentFrac;

                if (htmlEle && htmlEle.style.left && htmlEle.style.right) {
                    let divLeftPercent = htmlEle.style.left ? parseFloat(htmlEle.style.left) - diffPct : 0;
                    let divRightPercent = htmlEle.style.right ? parseFloat(htmlEle.style.right) + diffPct : 0;

                    if (this.leadBorderRef && this.leadBorderRef.containerRef &&
                        this.leadBorderRef.containerRef.style.left && this.endBorderRef &&
                        this.endBorderRef.containerRef && this.endBorderRef.containerRef.style.left) {

                        const leadPosition = parseFloat(this.leadBorderRef.containerRef.style.left);
                        const lagPosition = parseFloat(this.endBorderRef.containerRef.style.left);
                        const offsetPercentGuard = 99;
                        let leadPositionPercent = leadPosition - diffPct;
                        let lagPositionPercent = lagPosition - diffPct;

                        // Guard to ensure stays on waveform
                        if (leadPositionPercent < 0 || lagPositionPercent > offsetPercentGuard) {
                            leadPositionPercent = leadPosition;
                            lagPositionPercent = lagPosition;
                            divLeftPercent = parseFloat(htmlEle.style.left);
                            divRightPercent = parseFloat(htmlEle.style.right);
                        } else {
                            this.props.selectionWindowProps.startMoveableMarker.timeSecs -= diffTimeSecs;
                            this.props.selectionWindowProps.endMoveableMarker.timeSecs -= diffTimeSecs;
                            startXPercent = currentXPercent;
                        }

                        htmlEle.style.left = `${divLeftPercent}%`;
                        htmlEle.style.right = `${divRightPercent}%`;
                        this.leadBorderRef.containerRef.style.left = `${leadPositionPercent}%`;
                        this.endBorderRef.containerRef.style.left =  `${lagPositionPercent}%`;
                    }
                }
            }
        };

        const onMouseUp = (event: MouseEvent) => {
            isDragging = false;

            if (this.props.updateMoveablelMarkersValue) {
                this.props.updateMoveablelMarkersValue(
                    [this.props.selectionWindowProps.startMoveableMarker,
                     this.props.selectionWindowProps.endMoveableMarker]);
            }
            document.body.removeEventListener('mousemove', onMouseMove);
            document.body.removeEventListener('mouseup', onMouseUp);
        };
        document.body.addEventListener('mousemove', onMouseMove);
        document.body.addEventListener('mouseup', onMouseUp);
    }

    /**
     * Create boarder movable markers
     */
    private readonly createMoveableMarkers = (): JSX.Element[] => {
        if (!this.props.selectionWindowProps) return [];
        const borderMarkers: JSX.Element[] = [];
        borderMarkers.push(
            <MoveableMarker
                ref={ref => this.leadBorderRef = ref}
                key={'start'}
                markerName={'start'}
                color={this.props.selectionWindowProps.startMoveableMarker.color}
                lineStyle={this.props.selectionWindowProps.startMoveableMarker.lineStyle}
                percentageLocation={
                    this.calcLeftPercent(this.props.selectionWindowProps.startMoveableMarker.timeSecs)}
                waveformsContainerRef={this.props.waveformsContainerRef}
                waveformsViewportRef={this.props.waveformsViewportRef}
                updateTimeWindowSelection={this.updateTimeWindowSelection}
                moveableMarkers={[this.props.selectionWindowProps.startMoveableMarker,
                                        this.props.selectionWindowProps.endMoveableMarker]}
                waveformDisplayStartTimeSecs={this.props.waveformDisplayStartTimeSecs}
                waveformDisplayEndTimeSecs={this.props.waveformDisplayEndTimeSecs}
                updateMoveablelMarkersValue={this.props.updateMoveablelMarkersValue}
            />
        );
        borderMarkers.push(
            <MoveableMarker
                ref={ref => this.endBorderRef = ref}
                key={'end'}
                markerName={'end'}
                color={this.props.selectionWindowProps.endMoveableMarker.color}
                lineStyle={this.props.selectionWindowProps.endMoveableMarker.lineStyle}
                percentageLocation={
                    this.calcLeftPercent(this.props.selectionWindowProps.endMoveableMarker.timeSecs)}
                waveformsContainerRef={this.props.waveformsContainerRef}
                waveformsViewportRef={this.props.waveformsViewportRef}
                updateTimeWindowSelection={this.updateTimeWindowSelection}
                moveableMarkers={[this.props.selectionWindowProps.startMoveableMarker,
                                        this.props.selectionWindowProps.endMoveableMarker]}
                waveformDisplayStartTimeSecs={this.props.waveformDisplayStartTimeSecs}
                waveformDisplayEndTimeSecs={this.props.waveformDisplayEndTimeSecs}
                updateMoveablelMarkersValue={this.props.updateMoveablelMarkersValue}
            />
        );
        return borderMarkers;
    }

    /**
     * update time window div based on vertical markers moving
     */
    private readonly updateTimeWindowSelection = () => {
        if (! this.timeWindowSelectionRef || !this.endBorderRef || !this.leadBorderRef ||
             !this.leadBorderRef.containerRef || !this.endBorderRef.containerRef) return;

        const percent100 = 100;

        if (this.timeWindowSelectionRef.style.left &&
            this.leadBorderRef.containerRef.style.left && this.endBorderRef.containerRef.style.left) {
            this.timeWindowSelectionRef.style.left = this.leadBorderRef.containerRef.style.left;
            this.timeWindowSelectionRef.style.right =
                `${percent100 - parseFloat(this.endBorderRef.containerRef.style.left)}%`;
        }
    }

    /**
     * Convert from time seconds to percent space
     */
    private readonly calcLeftPercent = (timeSecs: number): number => {
        const scale = d3.scaleLinear()
            .domain([this.props.waveformDisplayStartTimeSecs, this.props.waveformDisplayEndTimeSecs])
            .range([0, 1]);
        const fracToPct = 100;
        return scale(timeSecs) * fracToPct;
    }
}
