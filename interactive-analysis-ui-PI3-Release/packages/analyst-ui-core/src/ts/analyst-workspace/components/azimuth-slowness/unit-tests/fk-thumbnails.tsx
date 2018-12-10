import * as React from 'react';
import { FkThumbnails } from '../fk-thumbnails';

/**
 * Standalone Fk Thumbnails Props
 */
// tslint:disable-next-line:no-empty-interface
export interface StandaloneFkThumbnailsProps {

}

/**
 * Standalone Fk Thumbnails State
 */
// tslint:disable-next-line:no-empty-interface
export interface StandaloneFkThumbnailsState {

}

/**
 * Standalone Fk Thumbnails
 */
export class StandaloneFkThumbnails extends React.Component<StandaloneFkThumbnailsProps, StandaloneFkThumbnailsState> {

    public constructor(props: StandaloneFkThumbnailsProps) {
        super(props);
    }

    /**
     * React component lifecycle
     */
    public render() {
        return (
            <div
                style={{
                    border: '1px solid',
                    resize: 'both',
                    overflow: 'auto',
                    height: '700px',
                    width: '400px',
                }}
            >
                <FkThumbnails
                    // tslint:disable-next-line:no-magic-numbers
                    thumbnailSizePx={200}
                    data={[]}
                    selectedSdIds={undefined}
                    setSelectedSdIds={undefined}
                    selectedSortType={undefined}
                    isMustUnAcceptedFk={undefined}
                />
            </div>
        );
    }
}
