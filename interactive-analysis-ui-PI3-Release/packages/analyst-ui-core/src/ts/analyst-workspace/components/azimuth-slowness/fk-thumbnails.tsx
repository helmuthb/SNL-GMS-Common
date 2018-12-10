import { findIndex, findLastIndex, orderBy, sortBy } from 'lodash';
import * as React from 'react';
import { WaveformSortType } from '../../components/waveform-display/waveform-display-controls';
import { FkThumbnail } from './fk-thumbnail';
import { SdFkData } from './graphql/query';

/**
 * Fk Thumbnails Props
 */
export interface FkThumbnailsProps {
    data: SdFkData[];
    thumbnailSizePx: number;
    selectedSortType: WaveformSortType;
    selectedSdIds: string[];

    setSelectedSdIds(ids: string[]): void;
    isMustUnAcceptedFk(fk: SdFkData): boolean;
}

/**
 * Fk Thumbnails State
 */
// tslint:disable-next-line:no-empty-interface
export interface FkThumbnailsState {
}

/**
 * FK Thumbnails Component
 */
export class FkThumbnails extends React.Component<FkThumbnailsProps, FkThumbnailsState> {

    private lastSelectedSd: string[] = [];

    // ***************************************
    // BEGIN REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    /**
     * Constructor.
     * 
     * @param props The initial props
     */
    public constructor(props: FkThumbnailsProps) {
        super(props);
    }

    /**
     * Invoked when the componented mounted.
     */
    public componentDidMount() {
        this.setSelectedThumbnail();
    }

    /**
     * Invoked when the componented mounted.
     * 
     * @param prevProps The previous props
     * @param prevState The previous state
     */
    public componentDidUpdate() {
        this.setSelectedThumbnail();
    }

    /**
     * Renders the component.
     */
    public render() {
        const sortedFkData = this.getSortedFkData();

        // HACK for testing set the selected list to latest
        if (this.props.selectedSdIds && this.props.selectedSdIds.length > 0) {
            this.lastSelectedSd = this.props.selectedSdIds;
        }
        return (
            <div
                style={{
                    padding: '0.25rem',
                    display: 'flex',
                    flexWrap: 'wrap',
                    alignContent: 'flex-start',
                }}
                onKeyDown={this.onKeyDown}
                tabIndex={0}
            >
                {
                    sortedFkData.map(fkData => (
                        <FkThumbnail
                            key={fkData.signalDetectionHypothesis.id}
                            data={fkData}
                            sizePx={this.props.thumbnailSizePx}
                            selected={this.props.selectedSdIds.indexOf(fkData.signalDetectionHypothesis.id) >= 0}
                            onClick={(e: React.MouseEvent<HTMLDivElement>) =>
                                this.onThumbnailClick(e, fkData.signalDetectionHypothesis.id)}
                            isMustUnAcceptedFk={this.props.isMustUnAcceptedFk}
                        />
                    ))
                }
            </div>
        );
    }

    // ***************************************
    // END REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    private readonly setSelectedThumbnail = (): void => {
        // If no SD are selected set it to the first unaccepted sdFkDataToDraw
        if (!this.props.selectedSdIds || this.props.selectedSdIds.length === 0) {
            // Walk thru the list to find first not accepted
            const firstUnacceptSd = this.getSortedFkData()
            .find(sd => !sd.signalDetectionHypothesis.
                azSlownessMeasurement.fkData.accepted &&
                this.lastSelectedSd.indexOf(sd.signalDetectionHypothesis.id) === -1);

            // If found a new one set the selected id
            if (firstUnacceptSd) {
                // console.log('Setting SD id to: ' + firstUnacceptSd.signalDetectionHypothesis.id);
                this.props.setSelectedSdIds([firstUnacceptSd.signalDetectionHypothesis.id]);
            }
        }
    }

    /**
     * Returns sorted FK Data based on sort type (Distance, Alphabetical)
     */
    private readonly getSortedFkData = (): SdFkData[] => {
        if (!this.props.data) return [];
        // apply sort based on sort type
        let data = [];

        // Sort by distance
        if (this.props.selectedSortType === WaveformSortType.distance) {
            data = sortBy(this.props.data, [sd =>
                sd.signalDetectionHypothesis.signalDetection.station.distanceToSource.distanceKm]);
        } else {
            // apply sort if a sort comparator is passed in
            data = this.props.selectedSortType ?
                orderBy(this.props.data, ['signalDetectionHypothesis.signalDetection.station.name'],
                        ['asc']) : this.props.data;
        }
        return data;
    }

    /**
     * onKeyDown event handler
     */
    private readonly onKeyDown = (e: React.KeyboardEvent<HTMLDivElement>): void => {
        if (!e.repeat) {
            if (e.shiftKey && (e.ctrlKey || e.metaKey) && (e.key === 'a' || e.key === 'A')) {
                // Shift + CmrOrCtrl + a ==> add all to current selection
                const selectedIds: string[] = [...this.props.selectedSdIds];
                this.getSortedFkData()
                .forEach(fk => {
                    if (selectedIds.indexOf(fk.signalDetectionHypothesis.id) === -1) {
                        selectedIds.push(fk.signalDetectionHypothesis.id);
                    }
                });
                this.props.setSelectedSdIds(selectedIds);
            } else if ((e.ctrlKey || e.metaKey) && (e.key === 'a' || e.key === 'A')) {
                // CmrOrCtrl + a ==> select all
                const selectedIds: string[] = [...this.getSortedFkData()
                    .map(fk => fk.signalDetectionHypothesis.id)];
                this.props.setSelectedSdIds(selectedIds);
            } else if (e.key === 'Escape') {
                // Escape ==> deselect all
                this.props.setSelectedSdIds([]);
            }
        }
    }

    /**
     * When clicking a thumbnail, select it
     */
    private readonly onThumbnailClick = (e: React.MouseEvent<HTMLDivElement>, id: string): void => {
        let selectedIds: string[];
        if (e.shiftKey && this.props.selectedSdIds.length > 0) {
            // shift range selection
            selectedIds = [...this.props.selectedSdIds];
            const fkIds: string[] = this.getSortedFkData()
                .map(fk => (fk.signalDetectionHypothesis.id));
            const selectedIndex: number = fkIds.indexOf(id);
            const minIndex: number = findIndex(fkIds, i => (selectedIds.indexOf(i) !== -1));
            const maxIndex: number = findLastIndex(fkIds, i => (selectedIds.indexOf(i) !== -1));
            fkIds.forEach(i => {
                if (selectedIds.indexOf(i) === -1) {
                    const index: number = fkIds.indexOf(i);
                    if (index >= selectedIndex && index < minIndex) {
                        selectedIds.push(i);
                    } else if (index > maxIndex && index <= selectedIndex) {
                        selectedIds.push(i);
                    }
                }
            });

        } else if (e.ctrlKey || e.metaKey) {
            // add to current selection
            selectedIds = [...this.props.selectedSdIds];
            if (selectedIds.indexOf(id) >= 0) {
                selectedIds.splice(selectedIds.indexOf(id), 1);
            } else {
                selectedIds.push(id);
            }
        } else {
            selectedIds = [id];
        }

        this.props.setSelectedSdIds(selectedIds);
    }
}
