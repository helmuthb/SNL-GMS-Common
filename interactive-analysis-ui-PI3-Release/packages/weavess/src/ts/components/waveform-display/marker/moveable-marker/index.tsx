import * as React from 'react';
import * as Entities from '../../../../entities';

/**
 * MoveableMarker Props
 */
export interface MoveableMarkerBaseProps {
    color: string;
    lineStyle: Entities.LineStyle;
    percentageLocation: number;
    waveformsViewportRef: HTMLDivElement | null;
    waveformsContainerRef: HTMLDivElement | null;
    waveformDisplayStartTimeSecs: number;
    waveformDisplayEndTimeSecs: number;
    moveableMarkers: MoveableMarkerObject[];
    markerName?: string;

    updateMoveablelMarkersValue?(moveableMarkers: MoveableMarkerObject[]): void;
    updateTimeWindowSelection?(): void;

}

/**
 * MoveableMarker State
 */
// tslint:disable-next-line:no-empty-interface
export interface MoveableMarkerState {

}

/**
 * UI Core level used interface, passed between weavess and UI-Core
 */
export interface MoveableMarkerObject {
    color: string;
    lineStyle: Entities.LineStyle;
    timeSecs: number;
}

/**
 * Consolidated props type for Moveable Marker.
 */
export type MoveableMarkerProps = MoveableMarkerBaseProps;

/**
 * MoveableMarker Component
 */
export class MoveableMarker extends React.Component<MoveableMarkerProps, MoveableMarkerState> {

    /**
     * Ref to the marker container element
     */
    public containerRef: HTMLElement | null;

    /**
     * constructor
     */
    public constructor(props: MoveableMarkerProps) {
        super(props);
    }

    /**
     * React component lifecycle
     */
    public render() {
        return (
            <div
                ref={ref => this.containerRef = ref}
                className={'weavess-moveable-marker'}
                style={{
                    position: 'absolute',
                    left: `${this.props.percentageLocation}%`,
                    width: '0px',
                    height: '100%',
                    border: `1px ${this.props.lineStyle} ${this.props.color}`,
                    pointerEvents: 'auto'
                }}
                onMouseDown={this.onMoveableMarkerClick}
            />
        );
    }

    /**
     * Move logic for the markers. Creates mouse move and up listeners to determine
     * Where it should be moved. Only works for pairs currently, if more than two markers
     * Depend on each other, will need to be refactored. 
     */
    private readonly onMoveableMarkerClick = (e: React.MouseEvent<HTMLDivElement>) => {
        if (!this.props.waveformsViewportRef || !this.props.waveformsContainerRef) return;

        e.stopPropagation();
        const htmlEle: HTMLDivElement = e.target as HTMLDivElement;
        const mouseXOffset = e.clientX - htmlEle.offsetLeft; // Beggining X position of waveform display
        const fracPrecentage = 100;
        const viewPortWidth = this.props.waveformsViewportRef.clientWidth;
        const zoomRatio = viewPortWidth / this.props.waveformsContainerRef.clientWidth;

        const onMouseMove = (event: MouseEvent) => {
            if (!htmlEle) return;
            const mouseXPrct = (event.clientX - mouseXOffset) / viewPortWidth * zoomRatio;
            const offsetPercentGuard = 0.99;
            // Get the limited position based on the other moveable div (if exist)
            let newPosPercent = mouseXPrct;
            const timeWindow = this.props.waveformDisplayEndTimeSecs - this.props.waveformDisplayStartTimeSecs;

            if (this.props.moveableMarkers && this.props.moveableMarkers.length > 1) {
                // The method limits start and end divs from crossing each other
                newPosPercent = this.getMoveableMarkerLimit(mouseXPrct);
            }

            // Guard to ensure stays on waveform
            newPosPercent = newPosPercent < 0 ? 0 : newPosPercent;
            newPosPercent = newPosPercent > offsetPercentGuard ? offsetPercentGuard : newPosPercent;
            htmlEle.style.left = `${newPosPercent * fracPrecentage}%`;
            const timeSecs = (newPosPercent) * timeWindow + this.props.waveformDisplayStartTimeSecs;

            if (this.props.moveableMarkers && this.props.moveableMarkers.length === 1) {
                this.props.moveableMarkers[0].timeSecs = timeSecs;
            } else {
                this.props.markerName === 'start' ? this.props.moveableMarkers[0].timeSecs = timeSecs :
                this.props.moveableMarkers[1].timeSecs = timeSecs;
            }

            if (this.props.updateTimeWindowSelection) {
                this.props.updateTimeWindowSelection();
            }
        };

        const onMouseUp = (event: MouseEvent) => {
            // Call the lead lag time pickers for selected times
            if (this.props.updateMoveablelMarkersValue) {
                this.props.updateMoveablelMarkersValue(this.props.moveableMarkers);
            }
            document.body.removeEventListener('mouseup', onMouseUp);
            document.body.removeEventListener('mousemove', onMouseMove);
        };
        document.body.addEventListener('mousemove', onMouseMove);
        document.body.addEventListener('mouseup', onMouseUp);
    }

    /**
     * If moveable marker is part of a pair, and has a name, ensures the marker
     * cannot go pasted its partner. A function that is for the selection window
     * May need to be revisted for other marker pairs. 
     */
    private readonly getMoveableMarkerLimit = (currentPercent: number): number => {
        if (!this.props.markerName) return currentPercent;
        const halfPercent = 0.005;
        const isStartMarker: boolean = this.props.markerName.startsWith('start');
        const otherMarker: MoveableMarkerObject = isStartMarker ?
            this.props.moveableMarkers[1] : this.props.moveableMarkers[0];
        if (otherMarker) {
            const timeWindow = this.props.waveformDisplayEndTimeSecs - this.props.waveformDisplayStartTimeSecs;
            const relMarkerTime = otherMarker.timeSecs - this.props.waveformDisplayStartTimeSecs;
            const otherPercent = (relMarkerTime / timeWindow);

            if (isStartMarker && currentPercent + halfPercent  > otherPercent) {
                return (otherPercent - halfPercent);
            } else if (!isStartMarker && currentPercent - halfPercent  < otherPercent) {
                return (otherPercent + halfPercent);
            }
        }
        return currentPercent;
    }
}
