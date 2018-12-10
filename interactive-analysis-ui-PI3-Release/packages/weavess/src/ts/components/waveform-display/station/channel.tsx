import * as d3 from 'd3';
import * as lodash from 'lodash';
import * as React from 'react';
import * as THREE from 'three';
import * as Entities from '../../../entities';
import { isHotKeyCommandSatisfied } from '../../../util/HotKeyUtils';
import { WorkerOperations } from '../../../workers/operations';
import { SignalDetection } from '../signal-detection';
import { TheoreticalPhaseWindow } from '../theoretical-phase-window';
// import { YAxis } from '../axes';

const channelWrapperStyle: React.CSSProperties = {
    display: 'flex',
    height: '50px',
    flex: '1 1 auto',
};

/**
 * Channel Props
 */
export interface ChannelProps {

    stationId: string;
    channelConfig: Entities.ChannelConfig;

    subChannel: boolean;
    height: number;

    displayStartTimeSecs: number;
    displayEndTimeSecs: number;
    workerRpcs: any[];
    selectedSignalDetections: string[] | undefined;
    selectedChannels: string[] | undefined;
    isMeasureWindow: boolean;

    // hotkeys
    amplitudeScaleHotKey: string;
    amplitudeScaleSingleResetHotKey: string;

    // callbacks
    setYAxisBounds(channelId: string, min: number, max: number);
    getViewRange(): [number, number];
    onLoad(): void;

    onMouseMove(e: React.MouseEvent<HTMLDivElement>, xPct: number): void;
    onMouseDown(e: React.MouseEvent<HTMLDivElement>, xPct: number): void;
    onMouseUp(e: React.MouseEvent<HTMLDivElement>, xPct: number, channelId: string, timeSecs: number): void;
    onContextMenu?(e: React.MouseEvent<HTMLDivElement>, channelId: string, sdId?: string): void;
    onChannelLabelClick?(e: React.MouseEvent<HTMLDivElement>, channelName: string): void;
    onSignalDetectionClick?(e: React.MouseEvent<HTMLDivElement>, sdId: string): void;
    onSignalDetectionDragEnd?(sdId: string, timeSecs: number): void;
    onKeyPress?(e: React.KeyboardEvent<HTMLDivElement>, clientX: number, clientY: number,
        channelId: string, timeSecs: number): void;
    updateMeasureWindow(measureWindowSelection: Entities.MeasureWindowSelection): void;
    onMaskClick?(event: React.MouseEvent<HTMLDivElement>, channelId: string, maskId: string[]): void;
}

/**
 * Channel State
 */
// tslint:disable-next-line:no-empty-interface
export interface ChannelState {
}

/**
 * Primary Channel component
 */
export class Channel extends React.Component<ChannelProps, ChannelState> {

    /**
     * default channel props, if not provided
     */
    public static readonly defaultProps: Entities.ChannelDefaultConfig = {
        displayType: [Entities.DisplayType.LINE],
        pointSize: 2,
        color: '#4580E6'
    };

    /**
     * Ref to the element where this channel will be rendered
     */
    public containerRef: HTMLElement | null;

    /**
     * Ref to drag indicator element
     */
    private dragIndicatorRef: HTMLDivElement | null;

    /**
     * THREE.Scene which holds the waveforms for this channel
     */
    public scene: THREE.Scene;

    /**
     * References to the masks drawn on the scene.
     */
    private readonly renderedMaskRefs: THREE.Mesh[] = [];

    /**
     * orthographic camera used to zoom/pan around the waveform
     */
    public camera: THREE.OrthographicCamera;

    /**
     * current min for all waveforms in gl units
     */
    public glMin: number = 0;

    /**
     * current max for all waveforms in gl units
     */
    public glMax: number = 100;

    /**
     * current mouse position in [0,1]
     */
    private mouseXPosition: number = 0;

    /**
     * current mouse x position in pixels from the left of the window
     */
    private mouseClientX: number;

    /**
     * current mouse y position in pixels from the top of the window
     */
    private mouseClientY: number;

    /**
     * currently cached data segments, to compare against new/deleted ones
     */
    private readonly cachedDataSegments: Map< Entities.ChannelSegment, THREE.Object3D[]> = new Map();

    /**
     * Camera max top value for specific channel.
     */
    private cameraTopMax: number = -Infinity;

    /**
     * Camera max bottom value for specific channel
     */
    private cameraBottomMax: number = Infinity;

    /**
     * The amplitude adjustment that has been applied to the channel.
     */
    private cameraAmplitudeAdjustment: number = 0;

    /**
     * Is Amplitude Increase Hotkey Down
     */
    private isAmplitudeIncreaseHotkeyDown: boolean = false;

    /**
     * Ref to the translucent selection brush-effect region, which is updated manually for performance reasons 
     */
    private measureWindowSelectionAreaRef: HTMLDivElement | null;

    /**
     * Is measure window being dragged
     */

    // private isMeasureWindowDragged: boolean = false;

    // ************************************
    // BEGIN REACT COMPONENT LIFECYCLE METHODS
    // ************************************

    /**
     * React component constructor 
     */
    public constructor(props: ChannelProps) {
        super(props);
    }

    /**
     * React component lifecycle
     */
    public render() {
        const isSelected = this.props.selectedChannels &&
            this.props.selectedChannels.indexOf(this.props.channelConfig.id) > -1;

        const style = {
            ...channelWrapperStyle,
            height: `${this.props.height}px`,
            backgroundColor: isSelected ? 'rgba(150,150,150,0.2)' : 'initial'
        };

        return (
            <div
                className="weavess-channel"
                style={style}
                ref={channel => this.containerRef = channel}
                tabIndex={0}
                onKeyDown={this.onKeyDown}
                onMouseEnter={this.onMouseEnter}
                onContextMenu={e => {
                    if (this.props.onContextMenu) this.props.onContextMenu(e, this.props.channelConfig.id, undefined);
                }}
                onMouseMove={this.onMouseMove}
                onMouseDown={this.onMouseDown}
                onMouseUp={this.onMouseUp}
            >
                <div style={{ overflowX: 'hidden', overflowY: 'initial', position: 'relative', width: '100%' }}>
                    <div
                        ref={ref => this.dragIndicatorRef = ref}
                        style={{
                            position: 'absolute', top: '0px', bottom: '0px',
                            borderLeft: '1px solid red', display: 'none'
                        }}
                    />
                    <div
                        ref={ref => { this.measureWindowSelectionAreaRef = ref; }}
                        className="weavess-measure-window-selection"
                        style={{
                            position: 'absolute', top: '0px', bottom: '0px', display: 'none',
                            backgroundColor: 'rgba(255,183,51,0.3)', left: '0px', right: '0px'
                        }}
                        onMouseDown={e => this.onMeasureWindowClick(e)}
                    />
                    {this.createSignalDetectionDomElements()}
                    {this.createTheoreticalPhaseWindowDomElements()}
                </div>
                <div
                    style={{ position: 'absolute', bottom: '0px', right: '0.2rem', fontSize: '0.6rem' }}
                >
                    {/*this.props.description*/}
                </div>
            </div>
        );
    }

    /**
     * React component lifecycle
     */
    public componentDidMount() {
        this.initialize();
    }

    /**
     * React component lifecycle
     */
    public componentDidUpdate(prevProps: ChannelProps, prevState: ChannelState) {
        if (!prevProps.channelConfig.dataSegments && this.props.channelConfig.dataSegments) {
            this.initialize();
        } else if (prevProps.channelConfig.dataSegments !== this.props.channelConfig.dataSegments
            || prevProps.displayStartTimeSecs !== this.props.displayStartTimeSecs
            || prevProps.displayEndTimeSecs !== this.props.displayEndTimeSecs) {
            this.updateCameraBounds(prevProps);
            this.loadData(this.props.channelConfig.dataSegments)
                .catch(e => console.error(e)); // tslint:disable-line
            if (this.props.channelConfig.masks) {
                this.renderChannelMasks(this.props.channelConfig.masks);
            }
        } else if (this.props.channelConfig.masks &&
                   prevProps.channelConfig.masks !== this.props.channelConfig.masks) {
            this.renderChannelMasks(this.props.channelConfig.masks);
        }
    }

    // ************************************
    // END REACT COMPONENT LIFECYCLE METHODS
    // ************************************

    /**
     * Reset the amplitude to the default.
     */
    public resetAmplitude = () => {
        // Check that the amplitude needs resetting
        if (this.camera.top !== this.cameraTopMax ||
            this.camera.bottom !== this.cameraBottomMax) {
            // reset the amplitude to the window default for this channel
            this.camera.top = this.cameraTopMax;
            this.camera.bottom = this.cameraBottomMax;
            this.cameraAmplitudeAdjustment = 0;

            this.props.setYAxisBounds(this.props.channelConfig.id, this.camera.bottom, this.camera.top);
            this.camera.updateProjectionMatrix();
        }
    }

    /**
     * sets the measure window selection div display to none
     */
    public removeMeasureWindowSelection = () => {
        if (this.measureWindowSelectionAreaRef) {
            this.measureWindowSelectionAreaRef.style.display = 'none';
        }
    }

    /**
     * First time setup. Create scene, camera, then load data.
     */
    private initialize(): void {
        this.scene = new THREE.Scene();

        const cameraZDepth = 5;
        this.camera = new THREE.OrthographicCamera(this.glMin, this.glMax, 1, -1, cameraZDepth, -cameraZDepth);
        this.camera.position.z = 0;

        this.loadData(this.props.channelConfig.dataSegments)
            .catch(e => console.error(e)); // tslint:disable-line

        if (this.props.channelConfig.masks) {
            this.renderChannelMasks(this.props.channelConfig.masks);
        }
    }
    /**
     * Creates SignalDetection components
     */
    private createSignalDetectionDomElements(): JSX.Element[] {
        if (!this.props.channelConfig.signalDetections) return [];

        const scale = d3.scaleLinear()
            .domain([this.props.displayStartTimeSecs, this.props.displayEndTimeSecs])
            .range([0, 1]);

        return this.props.channelConfig.signalDetections.map(signalDetection => {

            const signalDetectionPosition = scale(signalDetection.timeSecs);

            return (
                <SignalDetection
                    key={signalDetection.id}
                    channelId={this.props.channelConfig.id}
                    {...signalDetection}
                    position={signalDetectionPosition}
                    selectedSignalDetections={this.props.selectedSignalDetections}

                    getTimeSecsForClientX={this.getTimeSecsForClientX}
                    onClick={this.props.onSignalDetectionClick}
                    onContextMenu={this.props.onContextMenu}
                    onSignalDetectionDragEnd={this.props.onSignalDetectionDragEnd}
                    toggleDragIndicator={this.toggleDragIndicator}
                    positionDragIndicator={this.positionDragIndicator}
                />
            );
        });
    }

    /**
     * Create Theoretical Phase Window
     */
    private createTheoreticalPhaseWindowDomElements(): JSX.Element[] {
        if (!this.props.channelConfig.theoreticalPhaseWindows) return [];

        const diff = this.props.displayEndTimeSecs - this.props.displayStartTimeSecs;
        const left = this.props.displayStartTimeSecs + (diff * this.props.getViewRange()[0]);
        const right = this.props.displayStartTimeSecs + (diff * this.props.getViewRange()[1]);
        const scale = d3.scaleLinear()
            .domain([left, right])
            .range([0, 1]);

        return this.props.channelConfig.theoreticalPhaseWindows.map(theoreticalPhaseWindow => {

            const leftPos = scale(theoreticalPhaseWindow.startTimeSecs);
            const rightPos = scale(theoreticalPhaseWindow.endTimeSecs);

            return (
                <TheoreticalPhaseWindow
                    key={theoreticalPhaseWindow.id}
                    color={theoreticalPhaseWindow.color}
                    left={leftPos}
                    right={rightPos}
                    label={theoreticalPhaseWindow.label}
                />
            );
        });
    }

    /**
     * mouseXPosition in [0,1]
     */
    private readonly computeTimeSecsForMouseXPosition = (mouseXPosition: number): number => {
        const timeRangeSecs = this.props.displayEndTimeSecs - this.props.displayStartTimeSecs;
        const left = this.props.displayStartTimeSecs + (timeRangeSecs * this.props.getViewRange()[0]);
        const right = this.props.displayStartTimeSecs + (timeRangeSecs * this.props.getViewRange()[1]);
        const scale = d3.scaleLinear()
            .domain([0, 1])
            .range([left, right]);

        return scale(mouseXPosition);
    }

    /**
     * return undefined if clientX is out of the channel's bounds on screen.
     */
    private readonly getTimeSecsForClientX = (clientX: number): number | undefined => {
        if (!this.containerRef) return;

        const boundingRect = (this.containerRef.offsetParent as HTMLDivElement).offsetParent.getBoundingClientRect();
        if (clientX < boundingRect.left && clientX > boundingRect.right) return undefined;

        // position in [0,1] in the current channel bounds.
        const scrollBarWidth = 10;
        const position = (clientX - boundingRect.left) / (boundingRect.width - scrollBarWidth);
        const time = this.computeTimeSecsForMouseXPosition(position);
        return time;
    }

    /**
     * Update the min,max in gl units where we draw waveforms, if the view bounds have changed.
     */
    private readonly updateCameraBounds = (prevProps: ChannelProps) => {
        const scale = d3.scaleLinear()
            .domain([prevProps.displayStartTimeSecs, prevProps.displayEndTimeSecs])
            .range([this.glMin, this.glMax]);

        const min = scale(this.props.displayStartTimeSecs);
        const max = scale(this.props.displayEndTimeSecs);
        this.glMin = min;
        this.glMax = max;
        this.camera.left = this.glMin;
        this.camera.right = this.glMax;

    }

    /**
     * Add and load data Segments to the display
     */
    private readonly loadData = async (dataSegments: Entities.ChannelSegment[]) => {
        // determine the new data segments that need to be added to the scene
        const unloadedDataSegments = lodash.difference(dataSegments, Array.from(this.cachedDataSegments.keys()));
        unloadedDataSegments.forEach(dataSegment => this.cachedDataSegments.set(dataSegment, []));

        // Removes any data segment from the cache that is no longer in data
        const segmentsToRemove = lodash.difference(Array.from(this.cachedDataSegments.keys()), dataSegments);
        segmentsToRemove.forEach(dataSegment => {
            const entry = this.cachedDataSegments.get(dataSegment);
            if (entry) {
                entry.forEach(obj => this.scene.remove(obj));
            }
            this.cachedDataSegments.delete(dataSegment);
        });

        // for each new data segment, create a THREE.js line and add it to the scene.
        unloadedDataSegments.forEach(async dataSegment => {
            if (dataSegment.data.length === 0) return;
            // have the web worker convert the number[] into [x,y,z,x,y,z,...]
            const float32Array = await this.props.workerRpcs[Math.floor(Math.random() * this.props.workerRpcs.length)]
                .rpc(WorkerOperations.CREATE_POSITION_BUFFER, {
                    data: dataSegment.data,
                    displayEndTimeSecs: this.props.displayEndTimeSecs,
                    displayStartTimeSecs: this.props.displayStartTimeSecs,
                    glMax: this.glMax,
                    glMin: this.glMin,
                    sampleRate: this.props.channelConfig.sampleRate,
                    startTimeSecs: dataSegment.startTimeSecs,
                });

            // re-use this geometry for lines & points
            const geometry = new THREE.BufferGeometry();
            geometry.addAttribute('position', new THREE.BufferAttribute(float32Array, 3));
            const threeData: THREE.Object3D[] = [];

            (this.props.channelConfig.displayType || Channel.defaultProps.displayType).map(displayType => {
                const color: string = this.props.channelConfig.color || Channel.defaultProps.color;
                if (displayType === Entities.DisplayType.LINE) {
                    const lineMaterial = new THREE.LineBasicMaterial({ color, linewidth: 1});
                    const line = new THREE.Line(geometry, lineMaterial);

                    this.scene.add(line);
                    threeData.push(line);
                } else if (displayType === Entities.DisplayType.SCATTER) {
                    const pointsMaterial = new THREE.PointsMaterial({
                        color,
                        size: this.props.channelConfig.pointSize || Channel.defaultProps.pointSize,
                        sizeAttenuation: false
                    });
                    const points = new THREE.Points(geometry, pointsMaterial);

                    this.scene.add(points);
                    threeData.push(points);
                }
                this.cachedDataSegments.set(dataSegment, threeData);

            });
            this.props.onLoad();
        });

        // calculate the new axis based on all of the loaded segments
        const loadedSegments: Entities.ChannelSegment[] = Array.from(this.cachedDataSegments.keys());
        if (loadedSegments) {
            let topMax = -Infinity;
            let bottomMax = Infinity;
            let channelAvg = 0;
            let offset = 0;

            loadedSegments.forEach(segment => {
                if (segment.data.length === 0) {
                    // When there is no data in the channel set offset to 1 (to avoid infinity)
                    this.cameraTopMax = 1;
                    this.cameraBottomMax = -1;
                    return;
                }
                let segmenToptMax = -Infinity;
                let segmentBottomMax = Infinity;
                let segmentAvg = 0;

                // tslint:disable-next-line:prefer-for-of
                for (let i = 0; i < segment.data.length; i++) {
                    const sample = segment.data[i];
                    segmentAvg += sample;
                    if (sample > segmenToptMax) segmenToptMax = sample;
                    if (sample < segmentBottomMax) segmentBottomMax = sample;
                }
                segmentAvg = segmentAvg / segment.data.length;

                topMax = Math.max(topMax, segmenToptMax);
                bottomMax = Math.min(bottomMax, segmentBottomMax);
                channelAvg += segmentAvg;

                offset = Math.max(Math.abs(topMax), Math.abs(bottomMax));
            });

            // Set channel average and set default camera top/bottom based on average
            // calculate the average using the unloaded data segments and the previous loaded segments
            const yAvg = channelAvg / loadedSegments.length;

            // Set axis offset and default view but account for the zero (empty channel)
            const axisOffset = offset !== 0 ? offset : 1;
            this.cameraTopMax = yAvg + axisOffset;
            this.cameraBottomMax = yAvg - axisOffset;

            if (this.cameraTopMax !== -Infinity && this.cameraBottomMax !== Infinity)  {
                // update the camera and apply the any ampliitude adjustment to the camera
                this.camera.top = this.cameraTopMax - this.cameraAmplitudeAdjustment;
                this.camera.bottom = this.cameraBottomMax + this.cameraAmplitudeAdjustment;

                this.props.setYAxisBounds(this.props.channelConfig.id, this.camera.bottom, this.camera.top);
                this.camera.updateProjectionMatrix();
            }
        }
    }

    /**
     * Render the Masks to the display.
     */
    private readonly renderChannelMasks = (masks: Entities.Mask[]) => {
        // clear out any existing masks
        this.renderedMaskRefs.forEach(m => this.scene.remove(m));
        this.renderedMaskRefs.length = 0; // delete all references

        // if we're being passed empty data, don't try to add masks
        if (this.props.channelConfig.dataSegments.length === 1
            && this.props.channelConfig.dataSegments[0].data.length === 0) return;

        const timeToGlScale = d3.scaleLinear()
            .domain([this.props.displayStartTimeSecs, this.props.displayEndTimeSecs])
            .range([this.glMin, this.glMax]);

        // TODO move sorting to happen elsewhere and support re-sorting when new masks are added
        // TODO consider passing comparator for mask sorting as an argument to weavess
        lodash.sortBy(masks, (mask: Entities.Mask) => mask.endTimeSecs - mask.startTimeSecs)
            .forEach((mask, i, arr) => {
                const width = timeToGlScale(mask.endTimeSecs) - timeToGlScale(mask.startTimeSecs);
                const midoint = timeToGlScale(mask.startTimeSecs + (mask.endTimeSecs - mask.startTimeSecs) / 2);
                const planeGeometry = new THREE.PlaneBufferGeometry(width, this.cameraTopMax * 2);
                const planeMaterial = new THREE.MeshBasicMaterial({
                    color: new THREE.Color(mask.color),
                    side: THREE.DoubleSide,
                    transparent: true
                });
                planeMaterial.blending = THREE.CustomBlending;
                planeMaterial.blendEquation = THREE.AddEquation;
                planeMaterial.blendSrc = THREE.DstAlphaFactor;
                planeMaterial.blendDst = THREE.SrcColorFactor;
                planeMaterial.depthFunc = THREE.NotEqualDepth;

                const plane: THREE.Mesh = new THREE.Mesh(planeGeometry, planeMaterial);
                const depth = -2;
                plane.position.x = midoint;
                plane.position.z = depth;
                plane.renderOrder = i / arr.length;

                this.renderedMaskRefs.push(plane);
            });

        if (this.renderedMaskRefs.length > 0) {
            this.scene.add(...this.renderedMaskRefs);
        }
    }

    /**
     * onMouseEnter event handler
     */
    private readonly onMouseEnter = (): void => {
        if (!this.containerRef) return;

        this.containerRef.focus();
    }

    /**
     * onKeyDown event handler
     */
    private readonly onKeyDown = (e: React.KeyboardEvent<HTMLDivElement>): void => {
        const timeSecs = this.computeTimeSecsForMouseXPosition(this.mouseXPosition);
        if (this.props.onKeyPress) {
            this.props.onKeyPress(e, this.mouseClientX, this.mouseClientY, this.props.channelConfig.id, timeSecs);
        }

        if (!e.repeat) {
            if (isHotKeyCommandSatisfied(e.nativeEvent, this.props.amplitudeScaleSingleResetHotKey)) {
                this.resetAmplitude();
            } else if (isHotKeyCommandSatisfied(e.nativeEvent, this.props.amplitudeScaleHotKey)) {
                const onKeyUp = (e2: KeyboardEvent) => {
                    if (isHotKeyCommandSatisfied(e2, this.props.amplitudeScaleHotKey)) {
                        this.isAmplitudeIncreaseHotkeyDown = false;
                        document.removeEventListener('keyup', onKeyUp);
                    }
                };

                this.isAmplitudeIncreaseHotkeyDown = true;
                document.addEventListener('keyup', onKeyUp, true);
            }
        }
    }

    /**
     * onMouseMove event handler
     */
    private readonly onMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
        if (!this.containerRef) return;

        const scrollBarWidth = 10;
        this.mouseXPosition = (e.clientX - (this.containerRef.offsetParent as HTMLDivElement)
            .offsetParent.getBoundingClientRect().left) /
            ((this.containerRef.offsetParent as HTMLDivElement).offsetParent.
                getBoundingClientRect().width - scrollBarWidth);
        this.mouseClientX = e.clientX;
        this.mouseClientY = e.clientY;
        this.props.onMouseMove(e, this.mouseXPosition);
    }

    private readonly onMeasureWindowClick = (e: React.MouseEvent<HTMLDivElement>): void => {
        if (e.button === 2 || e.altKey || e.ctrlKey || e.metaKey) return;
        e.stopPropagation();
        const startClientX = e.clientX;
        const start = this.mouseXPosition;
        let isDragging = false;
        let diff = 0;
        const fracPrecentage = 100;
        const startDivLeft = this.measureWindowSelectionAreaRef &&
            this.measureWindowSelectionAreaRef.style.left ?
            parseFloat(this.measureWindowSelectionAreaRef.style.left) : 0;

        const startDivRight = this.measureWindowSelectionAreaRef &&
            this.measureWindowSelectionAreaRef.style.right ?
            parseFloat(this.measureWindowSelectionAreaRef.style.right) : 0;

        const onMouseMove = (event: MouseEvent) => {
            if (!this.measureWindowSelectionAreaRef) return;
            diff = Math.abs(startClientX - event.clientX);
            // begin drag if moving more than 1 pixel
            if (diff > 1 && !isDragging) {
                isDragging = true;
            }
            if (isDragging) {
                // current mouse position in [0,1]
                if (!this.containerRef) return;
                const scrollBarWidth = 10;
                const currentMouseXFrac = (event.clientX - (this.containerRef.offsetParent as HTMLDivElement)
                    .offsetParent.getBoundingClientRect().left) /
                    ((this.containerRef.offsetParent as HTMLDivElement).offsetParent.
                        getBoundingClientRect().width - scrollBarWidth);
                const mouseStartEndDifference = start - currentMouseXFrac;
                const scale = this.props.getViewRange()[1] - this.props.getViewRange()[0];
                const diffPct = fracPrecentage * mouseStartEndDifference * scale;

                this.measureWindowSelectionAreaRef.style.left = `${startDivLeft - diffPct}%`;
                this.measureWindowSelectionAreaRef.style.right = `${startDivRight + diffPct}%`;
            }
        };

        const onMouseUp = (event: MouseEvent) => {
            if (!this.measureWindowSelectionAreaRef) return;
            isDragging = false;

            const curDivLeft = this.measureWindowSelectionAreaRef &&
                this.measureWindowSelectionAreaRef.style.left ?
                parseFloat(this.measureWindowSelectionAreaRef.style.left) : 0;

            const curDivRight = this.measureWindowSelectionAreaRef &&
                this.measureWindowSelectionAreaRef.style.right ?
                parseFloat(this.measureWindowSelectionAreaRef.style.right) : 0;

            const scale = d3.scaleLinear()
                .range([0, 1])
                .domain([this.props.getViewRange()[0], this.props.getViewRange()[1]]);
            const left = scale(curDivLeft / fracPrecentage);
            const right = scale(1 - curDivRight / fracPrecentage);
            // todo fix below, need to get back mouse location from new div location
            const startTimeSecs = this.computeTimeSecsForMouseXPosition(left);
            const endTimeSecs = this.computeTimeSecsForMouseXPosition(right);
            const measureWindowSelection: Entities.MeasureWindowSelection = {
                stationId: this.props.stationId,
                channelId: this.props.channelConfig.id,
                channelRef: this,
                startTimeSecs,
                endTimeSecs
            };
            this.props.updateMeasureWindow(measureWindowSelection);
            document.body.removeEventListener('mousemove', onMouseMove);
            document.body.removeEventListener('mouseup', onMouseUp);
            // this.isMeasureWindowDragged = false;
        };

        document.body.addEventListener('mousemove', onMouseMove);
        document.body.addEventListener('mouseup', onMouseUp);
    }

    /**
     * onMouseDown event handler, may have to move the measureWindow logic to keydown
     * to distingush between command click and regular click
     */
    private readonly onMouseDown = (e: React.MouseEvent<HTMLDivElement>): void => {
        e.stopPropagation();
        if (e.button === 2) return;
        this.props.onMouseDown(e, this.mouseXPosition);

        if (this.isAmplitudeIncreaseHotkeyDown) {
            this.beginScaleAmplitudeDrag(e);
        } else if (e.altKey && !this.props.isMeasureWindow) { // Below is handing measure window.
            // Prevent propagation of these events so that the underlying channel click doesn't register
            e.stopPropagation();
            const start = this.mouseXPosition;
            let currentPos = e.clientX;
            let isDragging = false;
            let diff = 0;
            const fracToPct = 100;
            const scale = d3.scaleLinear()
                .domain([0, 1])
                .range(
                [this.props.getViewRange()[0], this.props.getViewRange()[1]]);

            const onMouseMove = (event: MouseEvent) => {
                if (!this.measureWindowSelectionAreaRef) return;
                currentPos = event.clientX;
                diff = Math.abs(currentPos - start);
                // begin drag if moving more than 1 pixel
                if (diff > 1 && !isDragging) {
                    isDragging = true;
                    this.measureWindowSelectionAreaRef.style.display = 'initial';
                }
                if (isDragging) {
                    const left = scale(Math.min(start, this.mouseXPosition));
                    const right = scale(Math.max(start, this.mouseXPosition));
                    this.measureWindowSelectionAreaRef.style.left = `${left * fracToPct}%`;
                    this.measureWindowSelectionAreaRef.style.right = `${(1 - right) * fracToPct}%`;
                }
            };

            const onMouseUp = (event: MouseEvent) => {
                if (!this.measureWindowSelectionAreaRef) return;
                isDragging = false;
                const left = this.computeTimeSecsForMouseXPosition(Math.min(start, this.mouseXPosition));
                const right = this.computeTimeSecsForMouseXPosition(Math.max(start, this.mouseXPosition));
                const measureWindowSelection: Entities.MeasureWindowSelection = {
                    stationId: this.props.stationId,
                    channelId: this.props.channelConfig.id,
                    channelRef: this,
                    startTimeSecs: left,
                    endTimeSecs: right
                };
                this.props.updateMeasureWindow(measureWindowSelection);
                document.body.removeEventListener('mousemove', onMouseMove);
                document.body.removeEventListener('mouseup', onMouseUp);
                // this.isMeasureWindowDragged = false;
            };

            document.body.addEventListener('mousemove', onMouseMove);
            document.body.addEventListener('mouseup', onMouseUp);
        } else if (e.shiftKey) {
            if (this.props.onMaskClick) {
                // determine if any masks were click
                const position = this.computeTimeSecsForMouseXPosition(this.mouseXPosition);
                const masks = lodash.sortBy(
                    this.props.channelConfig.masks,
                    (m: Entities.Mask) => m.endTimeSecs - m.startTimeSecs)
                    .filter(m => (m.startTimeSecs <= position && position <= m.endTimeSecs))
                    .map(m => m.id);

                if (masks) {
                    this.props.onMaskClick(e, this.props.channelConfig.id, masks);
                }
            }
        }
    }

    /**
     * onMouseUp event handler
     */
    private readonly onMouseUp = (e: React.MouseEvent<HTMLDivElement>) => {
        const timeSecs = this.computeTimeSecsForMouseXPosition(this.mouseXPosition);
        this.props.onMouseUp(e, this.mouseXPosition, this.props.channelConfig.id, timeSecs);
    }

    /**
     * toggle display of the drag indicator for this channel
     */
    private readonly toggleDragIndicator = (show: boolean, color: string): void => {
        if (!this.dragIndicatorRef) return;

        this.dragIndicatorRef.style.borderColor = color;
        this.dragIndicatorRef.style.display = show ? 'initial' : 'none';
    }

    /**
     * Set the position for the drag indicator
     */
    private readonly positionDragIndicator = (clientX: number): void => {
        if (!this.containerRef || !this.dragIndicatorRef) return;

        const fracToPct = 100;
        const boundingRect = this.containerRef.getBoundingClientRect();
        // position in [0,1] in the current channel bounds.
        const position = (clientX - boundingRect.left) / boundingRect.width;
        this.dragIndicatorRef.style.left = `${position * fracToPct}%`;
    }

    /**
     * Scales the amplitude of the single waveform.
     */
    private readonly beginScaleAmplitudeDrag = (e: React.MouseEvent<HTMLDivElement>): void => {
        // prevent propagation of these events so that the underlying channel click doesn't register
        // const start = e.clientY;
        let previousPos = e.clientY;
        let currentPos = e.clientY;
        let diff = 0;

        const onMouseMove = (e2: MouseEvent) => {
            currentPos = e2.clientY;
            diff = previousPos - currentPos;
            previousPos = currentPos;

            const currentCameraRange = Math.abs(this.camera.top - this.camera.bottom);

            // calculate the amplitude adjustment
            const percentDiff = 0.05;
            const amplitudeAdjustment: number = (currentCameraRange * percentDiff);
            // apply the any ampliitude adjustment to the camera
            if (diff > 0) {
                this.camera.top -= amplitudeAdjustment;
                this.camera.bottom += amplitudeAdjustment;
                this.cameraAmplitudeAdjustment += amplitudeAdjustment;
            } else if (diff < 0) {
                this.camera.top += amplitudeAdjustment;
                this.camera.bottom -= amplitudeAdjustment;
                this.cameraAmplitudeAdjustment -= amplitudeAdjustment;
            }

            this.props.setYAxisBounds(this.props.channelConfig.id, this.camera.bottom, this.camera.top);
            this.camera.updateProjectionMatrix();
        };

        const onMouseUp = (e2: MouseEvent) => {
            document.body.removeEventListener('mousemove', onMouseMove);
            document.body.removeEventListener('mouseup', onMouseUp);
        };

        document.body.addEventListener('mousemove', onMouseMove);
        document.body.addEventListener('mouseup', onMouseUp);
    }
}
