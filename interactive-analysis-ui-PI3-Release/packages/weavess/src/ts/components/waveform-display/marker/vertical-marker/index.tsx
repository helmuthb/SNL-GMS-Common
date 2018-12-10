import * as React from 'react';
import * as Entities from '../../../../entities';
import { MarkerProps } from '../marker';

/**
 * VerticalMarker State
 */
// tslint:disable-next-line:no-empty-interface
export interface VerticalMarkerState {

}

/**
 * VerticalMarkerObject
 */
export interface VerticalMarkerObject {
    color: string;
    lineStyle: Entities.LineStyle;
    timeSecs: number;
}

/**
 * VerticalMarker Component
 */
export class VerticalMarker extends React.Component<MarkerProps, VerticalMarkerState> {

    /**
     * Constructor
     */
    public constructor(props: MarkerProps) {
        super(props);
    }

    /**
     * React component lifecycle
     */
    public render() {
        return (
            <div
                style={{
                    position: 'absolute',
                    left: `${this.props.percentageLocation}%`,
                    width: '0px',
                    height: '100%',
                    border: `1px ${this.props.lineStyle} ${this.props.color}`,
                }}
            />
        );
    }
}
