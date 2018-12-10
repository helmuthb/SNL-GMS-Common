import * as React from 'react';
import { ChildProps, MutationFunc } from 'react-apollo';
import { AnalystCurrentFk, FkPrimary } from './fk-primary';
import { FkProperties } from './fk-properties';
import { FilterType, FkThumbnailSize } from './fk-thumbnails-controls';
import { EventFkDataQuery, SdFkData } from './graphql/query';

/**
 * Fk Image and Details Mutations
 */
// tslint:disable-next-line:no-empty-interface
export interface FkImageAndDetailsMutations {

}

/**
 * Azimuth Slowness Redux Props
 */
export interface FkImageAndDetailsReduxProps {
    fkData: SdFkData;
    updateAzSlowFromFkMutation: MutationFunc<{}>;

    acceptSelectedSdFks(): void;
    frequencyBandUpdated(minFreq: number, maxFreq: number): void;
}

/**
 * Azimuth Slowness State
 */
export interface FkImageAndDetailsState {
    fkThumbnailSizePx: FkThumbnailSize;
    filterType: FilterType;
    analystCurrentFk: AnalystCurrentFk;
}

/**
 * Consolidated props for Azimuth Slowness
 */
export type FkImageAndDetailsProps =
    FkImageAndDetailsReduxProps & ChildProps<FkImageAndDetailsMutations, EventFkDataQuery>;

/**
 * Azimuth Slowness primary component
 */
export class FkImageAndDetails extends React.Component<FkImageAndDetailsProps, FkImageAndDetailsState> {

    private FkImageAndDetailsContainer: HTMLDivElement;

    public constructor(props: FkImageAndDetailsProps) {
        super(props);
        this.state = {
            fkThumbnailSizePx: FkThumbnailSize.MEDIUM,
            filterType: FilterType.firstP,
            analystCurrentFk: undefined
        };
    }

    /**
     * React component lifecycle
     */
    public render() {

        return (
            <div
                ref={ref => this.FkImageAndDetailsContainer = ref}
                style={{
                    padding: '0.5rem',
                    height: '100%',
                    width: '100%',
                    overflow: 'hidden',
                    display: 'flex',
                    userSelect: 'none'
                }}
            >
                {this.displaySelectedThumbnail(this.props.fkData)}
                {this.displaySelectedFkDetails(this.props.fkData)}
            </div>
        );
    }

    /**
     * Update the current fk point
     */
    private readonly updateCurrentFk = (point: AnalystCurrentFk) => {
        this.setState({
            analystCurrentFk: point,
        });
    }

    /**
     * Display the currently selected thumbnail
     */
    private displaySelectedThumbnail(data: SdFkData): JSX.Element {
        return (
            <FkPrimary
                data={data}
                updateCurrentFk={this.updateCurrentFk}
            />
        );
    }

    /**
     * Display fstat, azimuth, slowness plots for currently selected fk
     */
    private displaySelectedFkDetails(data: SdFkData): JSX.Element {
        return (
            <FkProperties
                data={data}
                analystCurrentFk={this.state.analystCurrentFk}
                frequencyBandUpdated={this.props.frequencyBandUpdated}
                acceptSelectedSdFks={this.props.acceptSelectedSdFks}
            />
        );
    }
}
