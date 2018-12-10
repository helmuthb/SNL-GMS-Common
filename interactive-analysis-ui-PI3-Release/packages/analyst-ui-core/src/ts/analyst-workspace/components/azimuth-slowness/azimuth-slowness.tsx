import { Intent, NonIdealState, Spinner } from '@blueprintjs/core';
import * as Gl from '@gms/golden-layout';
import { cloneDeep, filter, find } from 'lodash';
import * as React from 'react';
import { ChildProps, MutationFunc } from 'react-apollo';
import { userPreferences } from '../../config/';
import { TimeInterval } from '../../state';
import { addGlForceUpdateOnResize, addGlForceUpdateOnShow } from '../../util/gl-util';
import {
    detectionHypothesesRejectedSubscription
} from '../waveform-display/graphql/subscriptions';
import { WaveformSortType } from '../waveform-display/waveform-display-controls';
import { FkImageAndDetails } from './fk-image-and-details';
import { FkPlots } from './fk-plots';
import { FkThumbnails } from './fk-thumbnails';
import { FilterType, FkThumbnailsControls, FkThumbnailSize } from './fk-thumbnails-controls';
import { CreateFkInput, UpdateAzSlowFkInput } from './graphql/mutations';
import { EventFkDataQuery, FkData, FkDataInput, FrequencyBand, SdFkData, WindowParams } from './graphql/query';
import {
    detectionsUpdatedSubscription, DetectionsUpdatedSubscription,
    fkCreatedSubscription,
    FkCreatedSubscription, sdHypUpdatedSubscription
} from './graphql/subscriptions';

/**
 * Azimuth Slowness Redux Props
 */
export interface AzimuthSlownessReduxProps {
    // passed in from golden-layout
    glContainer?: Gl.Container;
    currentTimeInterval: TimeInterval;
    selectedSdIds: string[];
    openEventId: string;
    selectedSortType: WaveformSortType;
    setSelectedSdIds(ids: string[]): void;
}

/**
 * Azimuth Slowness State
 */
export interface AzimuthSlownessState {
    fkThumbnailSizePx: FkThumbnailSize;
    filterType: FilterType;
}

/**
 * Mutations used by the Az Slow display
 */
export interface AzimuthSlownessMutations {
    // {} because we don't care about mutation results for now, handling that through subscriptions
    createFk: MutationFunc<{}>;
    updateAzSlowFromFkMutation: MutationFunc<{}>;

}

/**
 * Consolidated props for Azimuth Slowness
 */
export type AzimuthSlownessProps =
    AzimuthSlownessReduxProps & ChildProps<AzimuthSlownessMutations, EventFkDataQuery>;

/**
 * Azimuth Slowness primary component
 */
export class AzimuthSlowness extends React.Component<AzimuthSlownessProps, AzimuthSlownessState> {

    /**
     * Used to constrain the max width of the thumbnail drag resize
     */
    private azimuthSlownessContainer: HTMLDivElement;

    /**
     * Used to drag & resize this element
     */
    private fkThumbnailsContainer: HTMLDivElement;

    /**
     * The inner container for the thumbnails
     */
    private fkThumbnailsInnerContainer: HTMLDivElement;

    private currentLeadLagPair: WindowParams;

    private currentFrequencyPair: FrequencyBand;

    /**
     * Indicate if fkData is available used to not show an NonIdealState,
     * when a filter, doesn't return any Fks.
     */
    private isFkData: boolean;

    /**
     * Handlers to unsubscribe from apollo subscriptions
     */
    private readonly unsubscribeHandlers: { (): void }[] = [];

    // ***************************************
    // BEGIN REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    /**
     * Constructor.
     * 
     * @param props The initial props
     */
    public constructor(props: AzimuthSlownessProps) {
        super(props);
        this.state = {
            fkThumbnailSizePx: FkThumbnailSize.MEDIUM,
            filterType: FilterType.firstP
        };
    }
    /**
     * Invoked when the componented mounted.
     */
    public componentDidMount() {
        addGlForceUpdateOnShow(this.props.glContainer, this);
        addGlForceUpdateOnResize(this.props.glContainer, this);

        this.setupSubscriptions(this.props);

        this.adjustFkInnerContainerWidth();
    }

    /**
     * Invoked when the componented mounted.
     * 
     * @param prevProps The previous props
     * @param prevState The previous state
     */
    public componentDidUpdate(prevProps: AzimuthSlownessProps) {
        this.adjustFkInnerContainerWidth();
        if (this.props.selectedSdIds[0] !== prevProps.selectedSdIds[0]) {
            const fkData = this.getSdFkData()
            .find(thumbnail =>
                thumbnail.signalDetectionHypothesis.id === this.props.selectedSdIds[0]);
            if (fkData) {
                this.currentFrequencyPair = {
                    minFrequencyHz:
                        fkData.signalDetectionHypothesis.azSlownessMeasurement.fkData.frequencyBand.minFrequencyHz,
                    maxFrequencyHz:
                        fkData.signalDetectionHypothesis.azSlownessMeasurement.fkData.frequencyBand.maxFrequencyHz
                };
                this.currentLeadLagPair = {
                    windowType:
                        fkData.signalDetectionHypothesis.azSlownessMeasurement.fkData.windowParams.windowType,
                    leadSeconds:
                        fkData.signalDetectionHypothesis.azSlownessMeasurement.fkData.windowParams.leadSeconds,
                    lengthSeconds:
                        fkData.signalDetectionHypothesis.azSlownessMeasurement.fkData.windowParams.lengthSeconds
                };
            }
        }
    }

    /**
     * Invoked when the componented will unmount.
     */
    public componentWillUnmount() {
        // unsubscribe from all current subscriptions
        this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
        this.unsubscribeHandlers.length = 0;
    }

    /**
     * Renders the component.
     */
    public render() {
        // if the golden-layout container is not visible, do not attempt to render
        // the compoent, this is to prevent JS errors that may occur when trying to
        // render the component while the golden-layout container is hidden
        if (this.props.glContainer) {
            if (this.props.glContainer.isHidden) {
                return (<NonIdealState />);
            }
        }

        if (!this.props.data.eventHypothesisById) {
            return (
                <NonIdealState
                    visual="heat-grid"
                    title="No FK Data Available"
                />
            );
        } else if (this.props.data.loading) {
            return (
                <NonIdealState
                    action={<Spinner intent={Intent.PRIMARY} />}
                    title="Loading:"
                    description="FK data for current event"
                />
            );
        }

        // Filter down to signal detection associations with valid FK data
        const sdFkDataToDraw = this.getSdFkData();

        if (!this.isFkData) {
            return (
                <NonIdealState
                    visual="heat-grid"
                    title="No FK Data Available"
                />
            );
        }
        return (
            <div
                ref={ref => this.azimuthSlownessContainer = ref}
                style={{
                    padding: '0.5rem',
                    height: '100%',
                    width: '100%',
                    overflow: 'hidden',
                    display: 'flex',
                    userSelect: 'none'
                }}
            >
                <div
                    ref={ref => this.fkThumbnailsContainer = ref}
                    style={{
                        flex: '0 0 auto',
                        display: 'flex',
                        flexDirection: 'column',
                        width: '255px',
                        overflow: 'hidden'
                    }}
                >
                    <div
                        style={{
                            position: 'relative',
                            marginBottom: '0.5rem',
                            height: 'auto'
                        }}
                    >
                        <FkThumbnailsControls
                            updateFkThumbnail={this.updateFkThumbnailSize}
                            updateFkFilter={this.updateFkFilter}
                            currentFilter={this.state.filterType}
                            acceptSelectedSdFks={this.acceptSelectedSdFks}
                        />
                    </div>
                    <div
                        style={{
                            flex: '1 1 auto',
                            position: 'relative',
                            height: 'auto',
                            overflow: 'auto'
                        }}
                    >
                        <div
                            ref={ref => this.fkThumbnailsInnerContainer = ref}
                            style={{
                                width: '100%',
                                display: 'flex',
                                flexDirection: 'row',
                                alignItems: 'center',
                                justifyContent: 'center',
                                margin: 'auto',
                            }}
                        >
                            <FkThumbnails
                                thumbnailSizePx={this.state.fkThumbnailSizePx}
                                data={sdFkDataToDraw}
                                selectedSdIds={this.props.selectedSdIds}
                                setSelectedSdIds={this.props.setSelectedSdIds}
                                selectedSortType={this.props.selectedSortType}
                                isMustUnAcceptedFk={this.isMustUnAcceptedFk}
                            />
                        </div>
                    </div>
                </div>
                {/* drag handle divider */}
                <div
                    className="gms-azimuth-slowness-divider"
                    onMouseDown={this.onThumbnailDividerDrag}
                    style={{
                        position: 'relative',
                        width: '0.5rem',
                        cursor: 'col-resize',
                        flex: '0 0 auto'
                    }}
                >
                    <div
                        style={{
                            position: 'absolute',
                            left: '0.2rem',
                            right: '0.2rem',
                            top: '0px',
                            bottom: '0px',
                        }}
                    />
                </div>

                    {this.displayFkData(sdFkDataToDraw)}
            </div>
        );
    }

    // ***************************************
    // END REACT COMPONENT LIFECYCLE METHODS
    // ***************************************

    /**
     * Initialize graphql subscriptions
     */
    private readonly setupSubscriptions = (props: AzimuthSlownessProps): void => {
        if (!props.data) return;

        // first, unsubscribe from all current subscriptions
        this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
        this.unsubscribeHandlers.length = 0;

        // don't register subscriptions if the current time interval is undefined/null
        // if (!props.currentTimeInterval) return;
        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                document: detectionsUpdatedSubscription,
                updateQuery: (prev: EventFkDataQuery, cur) => {
                    const data = (cur.subscriptionData.data as DetectionsUpdatedSubscription);
                    if (data) {
                        const detectionsUpdated = data.detectionsUpdated[0];
                        const selectedFkId = this.props.data.eventHypothesisById && this.props.selectedSdIds[0];
                        if (!selectedFkId) {
                            return;
                        }

                        const prevSdHyps = prev.eventHypothesisById.signalDetectionAssociations;
                        const newSdHyps = [...prevSdHyps];
                        prev.eventHypothesisById.signalDetectionAssociations.forEach((sdHyp, i) => {
                            if (sdHyp.signalDetectionHypothesis.signalDetection.id === selectedFkId) {
                                const newSdHyp = {
                                    ...sdHyp,
                                    signalDetectionHypothesis: {
                                        ...sdHyp.signalDetectionHypothesis,
                                        id: detectionsUpdated.id,
                                        phase: detectionsUpdated.currentHypothesis.phase,
                                        arrivalTimeMeasurement: {
                                            ...sdHyp.signalDetectionHypothesis.arrivalTimeMeasurement,
                                            timeSec: detectionsUpdated.currentHypothesis.arrivalTimeMeasurement.timeSec
                                        }
                                    }
                                };
                                newSdHyps[i] = newSdHyp;
                            }
                        });

                        return {
                            eventHypothesisById: {
                                signalDetectionAssociations: newSdHyps
                            }
                        };
                    }
                }
            })
        );
        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                document: fkCreatedSubscription,
                updateQuery: (prev: EventFkDataQuery, cur) => {
                    const data = (cur.subscriptionData.data as FkCreatedSubscription);
                    if (data) {
                        const fkCreated = (cur.subscriptionData.data as FkCreatedSubscription).fkCreated;
                        const selectedFkId = this.props.data.eventHypothesisById && this.props.selectedSdIds[0];
                        if (!selectedFkId) {
                            return;
                        }

                        const prevSdHyps = prev.eventHypothesisById.signalDetectionAssociations;
                        const newSdHyps = [...prevSdHyps];
                        prev.eventHypothesisById.signalDetectionAssociations.forEach((sdHyp, i) => {
                            if (sdHyp.signalDetectionHypothesis.signalDetection.id === selectedFkId) {
                                const newSdHyp = {
                                    ...sdHyp,
                                    signalDetectionHypothesis: {
                                        ...sdHyp.signalDetectionHypothesis,
                                        azSlownessMeasurement: {
                                            ...sdHyp.signalDetectionHypothesis.azSlownessMeasurement,
                                            fkData: fkCreated
                                        }
                                    }
                                };
                                newSdHyps[i] = newSdHyp;
                            }
                        });

                        return {
                            eventHypothesisById: {
                                signalDetectionAssociations: newSdHyps
                            }
                        };
                    }
                }
            })
        );
        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                // Since data in the existing SdFkData has changed
                // Redux with update the SdFkData entry
                document: sdHypUpdatedSubscription
            }
        ));
        this.unsubscribeHandlers.push(
            props.data.subscribeToMore({
                document: detectionHypothesesRejectedSubscription,
                updateQuery: (prev: EventFkDataQuery, cur) => {
                    // Set the SD list to prev if set else to empty
                    let newSdHyps = prev.eventHypothesisById ?
                        prev.eventHypothesisById.signalDetectionAssociations : [];
                    if (cur && cur.subscriptionData && prev && prev.eventHypothesisById &&
                        prev.eventHypothesisById.signalDetectionAssociations) {
                            const data = cur.subscriptionData.data;
                            if (data && data.detectionsRejected && data.detectionsRejected.length > 0) {
                                const deletedSDs = data.detectionsRejected;
                                const prevSdHyps = prev.eventHypothesisById.signalDetectionAssociations;
                                newSdHyps = [...prevSdHyps];
                                if (deletedSDs) {
                                    newSdHyps.forEach((sdHyp, i) => {
                                        const deletedId = deletedSDs.
                                            find(dId => dId.id === sdHyp.signalDetectionHypothesis.signalDetection.id);
                                        if (deletedId) {
                                            newSdHyps.splice(i, 1);
                                        }
                                    });
                                }
                            }
                    }
                    return {
                        eventHypothesisById: {
                            signalDetectionAssociations: newSdHyps
                        }
                    };
                }

            })
        );
    }

    /**
     * Update the fkThumbnailSizePx size
     */
    private readonly updateFkThumbnailSize = (size: FkThumbnailSize) => {
        this.setState({
            fkThumbnailSizePx: size
        });
    }

    /**
     * Return the filtered SdFkData list it futher filters the list based on firstPFilter
     */
    private readonly getSdFkData = (): SdFkData[] => {
        // Filter down to signal detection associations with valid FK data
        let sdFkDataToDraw = this.props.data.eventHypothesisById
        ? filter(this.props.data.eventHypothesisById.signalDetectionAssociations,
                 sdAssoc => sdAssoc.signalDetectionHypothesis &&
                    sdAssoc.signalDetectionHypothesis.azSlownessMeasurement &&
                    sdAssoc.signalDetectionHypothesis.azSlownessMeasurement.fkData)
        : [];

        // Further filter down the signal detection associations to first P phases
        // if the display is configured to do so
        this.isFkData = sdFkDataToDraw.length > 0 ? true : false;

        switch (this.state.filterType) {
            case FilterType.all: {
                // No action needs to be taken
                // Maybe refactor so it is in a method
                break;
            }
            // Further filter down the signal detection associations to first P phases
            // if the display is configured to do so
            case FilterType.firstP: {
                sdFkDataToDraw = this.firstPfilter(sdFkDataToDraw);
                break;
            }
            case FilterType.accept: {
                sdFkDataToDraw = this.mustBeAcceptedFkFilter(sdFkDataToDraw);
                break;
            }
            default: {
                sdFkDataToDraw = this.firstPfilter(sdFkDataToDraw);
            }
        }
        return sdFkDataToDraw;
    }

    /**
     * Update the filter
     */
    private readonly updateFkFilter = (filterType: FilterType) => {
        this.setState({
            filterType
        });
    }

    /**
     * Filter for First P FKs
     */
    private readonly firstPfilter = (fksToFilter: SdFkData[]) => {
        const seenStations: string[] = [];
        // Sort by arrival time then only take the first p for each station
        const filteredFks = fksToFilter.sort((fk1, fk2) =>
            fk1.signalDetectionHypothesis.arrivalTimeMeasurement.timeSec -
            fk2.signalDetectionHypothesis.arrivalTimeMeasurement.timeSec
        )
        .filter(fk => {
            const stationId = fk.signalDetectionHypothesis.signalDetection.station.id;
            const unseenStation = seenStations.indexOf(stationId) < 0;
            if (fk.signalDetectionHypothesis.phase[0] === 'P' && unseenStation) {
                seenStations.push(stationId);
                return true;
            }
            return false;
        });
        return filteredFks;
    }

    /**
     * Filter for Fks that MUST be accepted
     */
    private readonly mustBeAcceptedFkFilter = (fksToFilter: SdFkData[]) => {
        const filteredFks = fksToFilter.filter(fk => this.isMustUnAcceptedFk(fk));
        return filteredFks;
    }

    /**
     * is fk a MUST unaccepted FK
     * 
     * @param fk: an fk to be checked if MUST accept
     * @return boolean
     */
    private readonly isMustUnAcceptedFk = (fk: SdFkData): boolean => {
        const azOpType: String =
            fk.signalDetectionHypothesis.azSlownessMeasurement.azimuthDefiningRules[0].operationType;
        const slowOpType: String =
            fk.signalDetectionHypothesis.azSlownessMeasurement.slownessDefiningRules[0].operationType;
        const azIsDefining = fk.signalDetectionHypothesis.azSlownessMeasurement.azimuthDefiningRules[0].isDefining;
        const slowIsDefining = fk.signalDetectionHypothesis.azSlownessMeasurement.slownessDefiningRules[0].isDefining;
        const isAccepted = fk.signalDetectionHypothesis.azSlownessMeasurement.fkData.accepted;

        if (!isAccepted) {
            if (slowOpType.toLowerCase() === 'location' || azOpType.toLowerCase() === 'location') {
                if (azIsDefining || slowIsDefining) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Start a drag on mouse down on the divider
     */
    private readonly onThumbnailDividerDrag = (e: React.MouseEvent<HTMLDivElement>) => {
        let prevPosition = e.clientX;
        let currentPos = e.clientX;
        let diff = 0;
        const maxWidthPct = 0.8;
        const maxWidthPx = this.azimuthSlownessContainer.clientWidth * maxWidthPct;

        const onMouseMove = (e2: MouseEvent) => {
            currentPos = e2.clientX;
            diff = currentPos - prevPosition;
            prevPosition = currentPos;
            const widthPx = this.fkThumbnailsContainer.clientWidth + diff;
            if (widthPx < maxWidthPx) {
                this.fkThumbnailsContainer.style.width = `${widthPx}px`;
                this.adjustFkInnerContainerWidth();
            }
        };

        const onMouseUp = (e2: MouseEvent) => {
            document.body.removeEventListener('mousemove', onMouseMove);
            document.body.removeEventListener('mouseup', onMouseUp);
        };

        document.body.addEventListener('mousemove', onMouseMove);
        document.body.addEventListener('mouseup', onMouseUp);
    }

    /**
     * Adjusts the inner container width of the FK thumbnails to ensure that it
     * is always centered properly.
     */
    private readonly adjustFkInnerContainerWidth = () => {
        const scrollbarWidth = 15;
        if (this.fkThumbnailsContainer && this.fkThumbnailsInnerContainer) {
            // calculate the inner container to allow the container to be centered
            const outerContainerWidth: number = this.fkThumbnailsContainer.clientWidth;
            const innerContainerWidth: number = outerContainerWidth -
                (outerContainerWidth % (this.state.fkThumbnailSizePx + scrollbarWidth));
            this.fkThumbnailsInnerContainer.style.width = `${innerContainerWidth}px`;
        }
    }

    /**
     * Display fstat, azimuth, slowness plots for currently selected fk
     */
    private readonly displaySelectedFkPlots = (sdFkData: SdFkData): JSX.Element => {
        // const selectedFkId = this.props.data.eventHypothesisById && this.props.selectedSdIds[0];
        // const selectedSd = (selectedFkId
        //     && find(this.props.data.eventHypothesisById.signalDetectionAssociations,
        //             sdAssoc => sdAssoc.signalDetectionHypothesis.id === selectedFkId)
        //             .signalDetectionHypothesis);

        if (!sdFkData || !sdFkData.signalDetectionHypothesis) {
            return (
                <div/>
            );
        }

        const selectedFk = sdFkData.signalDetectionHypothesis.azSlownessMeasurement.fkData;
        const selectedSd = sdFkData.signalDetectionHypothesis;
        return (
            <FkPlots
                signalDetection={{
                     timeSecs: selectedSd.arrivalTimeMeasurement.timeSec,
                     id: selectedSd.id,
                     label: selectedSd.phase,
                     color: userPreferences.colors.events.inProgress
                }}
                fstatData={selectedFk.fstatData}
                windowParams={selectedFk.windowParams}
                contribChannels={selectedFk.contribChannels}
                leadLagUpdated={this.leadLagUpdated}
            />
        );
    }

    /**
     * Callback for changes in Lead/Lag in FK Plots
     */
    private readonly leadLagUpdated = (lead: number, length: number): void => {
        this.currentLeadLagPair = {
            ...this.currentLeadLagPair,
            leadSeconds: lead,
            lengthSeconds: length
        };
        if (this.currentLeadLagPair.leadSeconds && this.currentLeadLagPair.lengthSeconds) {
            this.onNewFkInfo();
        }
    }

    /**
     * Callback for changes in frequency band
     */
    private readonly frequencyBandUpdated = (lowFreq: number, highFreq: number): void => {
        this.currentFrequencyPair = {
            minFrequencyHz: lowFreq,
            maxFrequencyHz: highFreq
        };
        if (this.currentFrequencyPair.minFrequencyHz && this.currentFrequencyPair.maxFrequencyHz) {
            this.onNewFkInfo();
        }
    }

    /**
     * Handles new FK Request when frequency and/or window params change
     */
    private readonly onNewFkInfo = (): void => {
        if (this.currentLeadLagPair && this.currentFrequencyPair) {
            const selectedFkId = this.props.data.eventHypothesisById && this.props.selectedSdIds[0];
            const selectedSd = (selectedFkId
                && find(this.props.data.eventHypothesisById.signalDetectionAssociations,
                        sdAssoc => sdAssoc.signalDetectionHypothesis.id === selectedFkId)
                        .signalDetectionHypothesis);

            const contribChanIds = this.getContributingChannelIds(selectedSd.azSlownessMeasurement.fkData);
            const variables: CreateFkInput = {
                input: {
                    stationId: selectedSd.signalDetection.station.id,
                    contribChannelIds: contribChanIds,
                    frequencyBand: this.currentFrequencyPair,
                    windowParams: this.currentLeadLagPair
                },
                signalDetectionHypothesisId: selectedSd.id
            };
            this.props.createFk({
                variables
            })
            .catch(err => window.alert(err));
        }
    }

    /**
     * handles the rendering of the right side of the fk display including the
     * larger fk image, details, and the plots
     */
    private readonly displayFkData = (sdFkDataToDraw: SdFkData[]) => {
        const multiSelected = this.props.selectedSdIds.length > 1;
        const fkData = sdFkDataToDraw.find(thumbnail =>
            thumbnail.signalDetectionHypothesis.id === this.props.selectedSdIds[0]);
        if (multiSelected || !fkData) {
            const message = multiSelected ? 'Multiple FKs Selected' :
                this.props.selectedSdIds.length && this.props.selectedSdIds.length > 0 ?
                'No FK data for selected SD'
                : 'No SD selected';

            const icon = multiSelected ? 'multi-select' : 'heat-grid';

            return (
                <NonIdealState
                    visual={icon}
                    title={message}
                />
            );
        }

        return (
            <div
                style={{
                    flex: '1 1 auto',
                    display: 'flex',
                    flexDirection: 'column'
                }}
            >
                <div
                    style={{
                        flex: '0 0 auto',
                        display: 'flex',
                        marginBottom: '0.5rem',
                    }}
                >
                    <FkImageAndDetails
                        fkData={fkData}
                        updateAzSlowFromFkMutation={this.props.updateAzSlowFromFkMutation}
                        frequencyBandUpdated={this.frequencyBandUpdated}
                        acceptSelectedSdFks={this.acceptSelectedSdFks}
                    />
                </div>
                {this.displaySelectedFkPlots(fkData)}
            </div>
        );
    }

    /*
    * Prep and call update Sd FkData mutation for each SdFkData selected
    */
    private readonly acceptSelectedSdFks = (): void => {
        let sdFkDataToDraw = this.getSdFkData();
        // If no selected ids or SD Fk thumbnails then return
        if (!this.props.selectedSdIds || this.props.selectedSdIds.length === 0 ||
            sdFkDataToDraw.length === 0) {
            return;
        }

        // Get the list of SdFk Data based on the selected ids
        sdFkDataToDraw = filter(sdFkDataToDraw, sd => this.props.selectedSdIds.indexOf
            (sd.signalDetectionHypothesis.id) >= 0);

        // For now call each mutation seperately
        sdFkDataToDraw.forEach(sd => this.callFkDataMutation(sd));

        // Clear the selected Fk to have thumbnails update comp pick the next
        this.props.setSelectedSdIds([]);
    }

    /**
     *  Create and call mutation for FkData
     */
    private readonly callFkDataMutation = (sdFkData: SdFkData): void => {
        if (!sdFkData.signalDetectionHypothesis.azSlownessMeasurement ||
            !sdFkData.signalDetectionHypothesis.azSlownessMeasurement.fkData) {
            return;
        }
        const fk = sdFkData.signalDetectionHypothesis.azSlownessMeasurement.fkData;
        const fkInput = cloneDeep(fk) as FkDataInput;

        // Set the contribiting channel IDs
        fkInput.contribChannelIds = this.getContributingChannelIds(fk);

        // First strip the accepted and contrib channels from top level
        delete fkInput.accepted;
        delete fkInput.contribChannels;

        const variables: UpdateAzSlowFkInput = {
            fkDataInput: fkInput,
            sdHypothesisId: sdFkData.signalDetectionHypothesis.id
        };
        this.props.updateAzSlowFromFkMutation({
            variables
        })
        .catch(err => window.alert(err));
    }

    /**
     *  Helper function that returns a String[] of contributing channel ids
     *  that are properly populated (for now some channel.id are null)
     */
    private readonly getContributingChannelIds = (fk: FkData): string[] => {
        // Build list if populated else return an empty list
        const fkIds: string[] = [];
        if (fk.contribChannels && fk.contribChannels.length > 0) {
            fk.contribChannels.forEach(chan => {
                if (chan) {
                    fkIds.push(`${chan.site.name}/${chan.name}`);
                }
            });
        }
        return fkIds;
    }
}
