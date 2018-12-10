// API for wrapping all Map calls which would allow for easier replacement of map tech stack
import { TimeInterval } from '../../state';
import { MapProps } from '../map';
import * as DefaultStations from './default-stations';
import * as Events from './events';
import { MapDataDefaultStation, MapDataSignalDetection, MapEventHypothesis } from './graphql/query';
import { MapAPI, MapOptions, MapState } from './map-api';
import * as SDs from './signal-detections';

(window as any).CESIUM_BASE_URL = './build/cesium';
import 'cesium/Build/Cesium/Widgets/widgets.css';
import 'cesium/Build/CesiumUnminified/Cesium.js';
declare var Cesium;
// TODO find a real map server on premise or something
Cesium.BingMapsApi.defaultKey = 'AsdrA8Fa6E_pOjFiQLbMDqdYpvKBlhZMvHvpJCzRJb6iIJDYvp2xvcNoXjT7zDD_';
// Set Default Camera View
Cesium.Camera.DEFAULT_VIEW_RECTANGLE = new Cesium.Rectangle(-Math.PI, -Math.PI / 2, Math.PI, Math.PI / 2);

const LEFT_CLICK = Cesium.ScreenSpaceEventType.LEFT_CLICK;
const CTRL_KEY = Cesium.KeyboardEventModifier.CTRL;
const LEFT_DOUBLE_CLICK = Cesium.ScreenSpaceEventType.LEFT_DOUBLE_CLICK;

/**
 * Implmentation of MapAPI Interface
 */
export class CesiumMap implements MapAPI {

    public state: MapState;
    private readonly options: MapOptions;
    /**
     * handle to the Map.Viewer object.
     */
    private viewer: any;

    public constructor(options: MapOptions) {
        this.state = {
            layers: {
                Events: this.createDataSource('Events'),
                Stations: this.createDataSource('Stations'),
                SDs: this.createDataSource('Sds')
            }
        };
        this.options = options;
    }

    /**
     * Public accessor for the data layers/datasources
     */
    public getDataLayers = () => this.state.layers;

    /**
     * Initialize Map viewer
     */
    public initialize = (containerDomElement: HTMLDivElement) => {
        this.viewer = this.createMapViewer(containerDomElement);
        this.viewer.screenSpaceEventHandler.setInputAction(this.onMapClick, LEFT_CLICK);
        this.viewer.screenSpaceEventHandler.setInputAction(this.onMapCtrlClick, LEFT_CLICK, CTRL_KEY);
        this.viewer.screenSpaceEventHandler.setInputAction(this.onMapDoubleClick, LEFT_DOUBLE_CLICK);
        this.viewer.camera.flyHome(0);
        this.setupLayers(this.viewer);
    }

    /**
     * Creates a Map View
     * @param containerElement element in which the map will be displayed
     */
    public createMapViewer = (containerElement: any) => {

        const baseViewerSettings = {
            sceneMode: this.options.analystUiConfig.userPreferences.map.defaultTo3D ?
                        Cesium.SceneMode.SCENE3D : Cesium.SceneMode.SCENE2D,
            fullscreenButton: false,
            fullscreenElement: false,
            animation: false,
            timeline: false,
        };

        const viewerSettings = this.options.analystUiConfig.environment.map.online ?
            baseViewerSettings :
            {
                ...baseViewerSettings,
                imageryProvider : Cesium.createTileMapServiceImageryProvider({
                    url : Cesium.buildModuleUrl(this.options.analystUiConfig.environment.map.offlineImagery.url),
                    maximumLevel : this.options.analystUiConfig.environment.map.offlineImagery.maxResolutionLevel
                }),
                baseLayerPicker : false,
                geocoder : false
            };

        return new Cesium.Viewer(containerElement, viewerSettings);
    }

    /**
     * Add datasources to the viewer held by the map/index
     * @param viewer viewer created by CreateMapViewer that is owned by map/index
     */
    public setupLayers = (viewer: any) => {
        viewer.dataSources.add(this.state.layers.Events);
        viewer.dataSources.add(this.state.layers.Stations);
        viewer.dataSources.add(this.state.layers.SDs);
    }

    /**
     * Draws signal detections on the map
     * @param signalDetections List of signal detections to draw
     * @param nextOpenEventHypId ID for the next open event Hypothesis
     */
    public drawSignalDetections = (signalDetections: MapDataSignalDetection[], nextOpenEventHypId: string) => {
        SDs.draw(this.state.layers.SDs, signalDetections, nextOpenEventHypId);
    }

    public highlightSelectedSignalDetections(selectedSignalDetections: string[]) {
        SDs.highlightSelectedSignalDetections(this.state.layers.SDs,
                                              selectedSignalDetections);
    }

    /**
     * Draw default stations on the the map
     * @param currentDefaultStations list of stations previously drawn
     * @param nextdefaultStations list of next stations
     */
    public drawDefaultStations = (currentDefaultStations: MapDataDefaultStation[],
                                  nextdefaultStations: MapDataDefaultStation[]) => {
        DefaultStations.draw(this.state.layers.Stations, currentDefaultStations, nextdefaultStations);
    }

    /**
     * Updates stations when data or selections change
     * @param currentSignalDetections list of current signal detections
     * @param currentOpenEventHypId current open event
     * @param nextSignalDetections next detections to be drawn
     * @param nextOpenEventHypId next open event
     */
    public updateStations = (currentSignalDetections: MapDataSignalDetection[],
                             currentOpenEventHypId: string,
                             nextSignalDetections: MapDataSignalDetection[],
                             nextOpenEventHypId: string) => {
            DefaultStations.update(this.state.layers.Stations,
                                   currentSignalDetections,
                                   currentOpenEventHypId,
                                   nextSignalDetections,
                                   nextOpenEventHypId);
    }

    /**
     * Draws events on the map
     * @param currentProps currentprops - passed in entire props to use data
     * @param nextProps nextprops - passed in entire props to use data
     */
    public drawEvents(currentProps: MapProps, nextProps: MapProps) {
        Events.draw(this.state.layers.Events, currentProps, nextProps);
    }

    /**
     * Highlights the current open event
     * @param currentTimeInterval current open time interval
     * @param currentOpenEvent current open event
     * @param nextOpenEvent next open event
     * @param selectedEventIds currently selected event IDs
     */
    public highlightOpenEvent = (currentTimeInterval: TimeInterval,
                                 currentOpenEvent: MapEventHypothesis,
                                 nextOpenEvent: MapEventHypothesis,
                                 selectedEventIds: string[]) => {
            Events.highlightOpenEvent(this.state.layers.Events,
                                      currentTimeInterval,
                                      currentOpenEvent,
                                      nextOpenEvent,
                                      selectedEventIds);

            // fly to the event, maintaining current camera height
            if (nextOpenEvent) {
                const loc = nextOpenEvent.preferredLocationSolution.locationSolution;
                const currentCameraHeight = this.viewer.camera.positionCartographic.height;
                this.viewer.camera.flyTo({
                    destination: Cesium.Cartesian3.fromDegrees(loc.lonDegrees, loc.latDegrees, currentCameraHeight),
                    duration: 2
                });
            }
    }

    /**
     * Resets view when the interval is changed. 
     * @param nextProps props getting passed in for the next interval
     */
    public resetView(nextProps: any) {
        SDs.resetView(this.state.layers.SDs);
        DefaultStations.resetView(nextProps.data.defaultStations, this.state.layers.Stations);
        this.viewer.homeButton.viewModel.command();
    }

    /**
     * Creates a Map Datasource with the passed in name
     * @param name of the datasource being started
     */
    private readonly createDataSource = (name: string) => new Cesium.CustomDataSource(name);

    /**
     * Handles map single click
     */
    private readonly onMapClick = (e: any) => {
        const entityWrapper = this.viewer.scene.pick(e.position);
        if (Cesium.defined(entityWrapper)) {
            this.options.events.onMapClick(e, entityWrapper.id);
        } else {
            this.options.events.onMapClick(e, undefined);
        }
    }

    /**
     * Handles map ctrl+click
     */
    private readonly onMapCtrlClick = (e: any) => {
        const entityWrapper = this.viewer.scene.pick(e.position);
        if (Cesium.defined(entityWrapper)) {
            this.options.events.onMapShiftClick(e, entityWrapper.id);
        } else {
            this.options.events.onMapShiftClick(e, undefined);
        }
    }

    /**
     * Handles map double click
     */
    private readonly onMapDoubleClick = (e: any) => {
        const entityWrapper = this.viewer.scene.pick(e.position);
        if (Cesium.defined(entityWrapper)) {
            this.options.events.onMapDoubleClick(e, entityWrapper.id);
        } else {
            this.options.events.onMapDoubleClick(e, undefined);
        }
    }
}
