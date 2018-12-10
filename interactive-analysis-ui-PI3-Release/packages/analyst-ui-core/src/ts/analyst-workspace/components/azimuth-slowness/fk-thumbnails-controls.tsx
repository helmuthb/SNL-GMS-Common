import { Button, Menu, MenuDivider, MenuItem, Popover, Position } from '@blueprintjs/core';
import * as React from 'react';

/**
 * Plan of attack:
 * will add accept to filter type 
 * "must accept"
 * Yellow = Not accecpted yet, but needs to be
 * Grey = accepted 
 * Case ? what if not accepted, but not a manditory accept : new color or grey
 * 
 * 
 * Update query and model (kinda did query) look why just 'P' is the only is true for isDefining
 * Look at accepted flag, also look at the defning rule for location
 * 
 * if accepted flag === flase and opreration type is 'location' or
 * 
 */

/**
 * Fk Thumbnails Controls Props
 */
export interface FkThumbnailsControlsProps {
    currentFilter: FilterType;

    acceptSelectedSdFks(): void;
    updateFkThumbnail(px: number): void;
    updateFkFilter(filter: FilterType): void;
}

/**
 * Different filters that are available
 */
export enum FilterType {
    firstP = 'First P',
    all = 'All',
    accept = 'Must accept'
}

/**
 * Pixels widths of available thumbnail sizes
 */
export enum FkThumbnailSize {
    SMALL = 70,
    MEDIUM = 110,
    LARGE = 150
}

/**
 * Fk Thumbnails Controls State
 */
// tslint:disable-next-line:no-empty-interface
export interface FkThumbnailsControlsState {

}

/**
 * FK Thumbnails Controls Component
 */
export class FkThumbnailsControls extends React.Component<FkThumbnailsControlsProps, FkThumbnailsControlsState> {

    public constructor(props: FkThumbnailsControlsProps) {
        super(props);
    }

    /**
     * React component lifecycle
     */
    public render() {
        return (
            <div
                style={{
                    display: 'inline-flex',
                    flexDirection: 'row',
                    flex: '0 1 auto',
                }}
            >
                <div>
                    <Popover
                        content={<Menu>
                            <MenuItem
                                onClick={e => this.props.updateFkThumbnail(FkThumbnailSize.SMALL)}
                                text="Small"
                            />
                            <MenuItem
                                onClick={e => this.props.updateFkThumbnail(FkThumbnailSize.MEDIUM)}
                                text="Medium"
                            />
                            <MenuItem
                                onClick={e => this.props.updateFkThumbnail(FkThumbnailSize.LARGE)}
                                text="Large"
                            />
                            <MenuDivider />
                            <MenuItem
                                onClick={e => this.onAcceptAllClick()}
                                text="Accept selected"
                            />
                        </Menu>}
                        position={Position.RIGHT_TOP}
                    >
                        <Button className="pt-minimal" icon="cog" />
                    </Popover>
                </div>
                <div
                    style={{
                        display: 'inline-flex',
                        flexDirection: 'row',
                        flex: '0 1 auto',
                        alignItems: 'baseline',
                    }}
                >
                    <div
                        style={{
                            paddingLeft: '10px',
                            paddingRight: '6px',
                        }}
                    >
                        <h6>Show:</h6>
                    </div>
                    <div
                        className="pt-select pt-fill"
                    >
                        <select
                            value={this.props.currentFilter}
                            onChange={e => this.props.updateFkFilter(e.currentTarget.value as FilterType)}
                        >
                            {this.createDropdownItems()}
                        </select>
                    </div>
                </div>
            </div>
        );
    }

    /*
    * Call the Signal Detection Fk Data Mutation in the Azimuth Slowness class
    * for all selected SD Ids this is also called by Accept in Fk Properties class
    */
    private readonly onAcceptAllClick = (): void => {
        this.props.acceptSelectedSdFks();
    }

    /**
     * Creates the HTML for the dropwdown items for the filter
     */
    private readonly createDropdownItems = (): JSX.Element[] =>
        Object.keys(FilterType)
            .map(type => (
            <option
                key={type}
                value={FilterType[type]}
            >
                {FilterType[type]}
            </option>
        ))
}
