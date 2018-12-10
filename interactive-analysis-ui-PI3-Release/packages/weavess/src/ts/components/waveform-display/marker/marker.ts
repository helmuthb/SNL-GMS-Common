import * as Entities from '../../../entities';
import { MoveableMarkerObject } from './moveable-marker';
import { SelectionWindowObject } from './selection-window';
import { VerticalMarkerObject } from './vertical-marker';

export interface MarkerProps {
    color: string;
    lineStyle: Entities.LineStyle;
    percentageLocation: number;
}

export interface MarkerObjects {
    verticalMarkers?: VerticalMarkerObject[];
    moveableMarkers?: MoveableMarkerObject[];
    selectionWindows?: SelectionWindowObject[];
}
