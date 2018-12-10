import * as lodash from 'lodash';
import { userPreferences } from '../../config';
import { MapDataDefaultStation, MapDataSignalDetection } from './graphql/query';

declare var Cesium;
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const stationPng = require('./img/' + userPreferences.map.icons.station);
const selectedStationColor = Cesium.Color.fromCssColorString(userPreferences.map.colors.openEvent);
const stationColor = Cesium.Color.fromCssColorString(userPreferences.map.colors.unselectedStation);
const imageScale = userPreferences.map.icons.stationScale;

(window as any).CESIUM_BASE_URL = './build/cesium';
import 'cesium/Build/Cesium/Widgets/widgets.css';
import 'cesium/Build/CesiumUnminified/Cesium.js';

/**
 * construct a Cesium DataSource with the default stations
 * @returns a Cesium.DataSource
 */
export function draw(dataSource: any,
    currentDefaultStations: MapDataDefaultStation[],
    nextdefaultStations: MapDataDefaultStation[]) {

    const newStations = lodash.differenceBy(nextdefaultStations, currentDefaultStations, 'id');
    addDefaultStations(dataSource, newStations);

    // TODO handle modified & removed stations
}

/**
 * create new map entities for a list of events
 * @param dataSource 
 * @param defaultStations 
 * @param currentTimeInterval 
 */
function addDefaultStations(dataSource: any, defaultStations: MapDataDefaultStation[]) {
    const pixelOffset = 15;
    const sitePixelOffset = -15;
    const defaultZoomDistance = 1e7;
    const closeLabelZoomDistance = 1.5e3;
    const closeBillboardZoomDistance = 1e4;

    defaultStations.forEach(defaultStation => {
        const siteLon = defaultStation.location.lonDegrees;
        const siteLat = defaultStation.location.latDegrees;
        const siteElev = defaultStation.location.elevationKm;

        dataSource.entities.add({
            name: 'station: ' + defaultStation.name,
            position: Cesium.Cartesian3.fromDegrees(siteLon, siteLat),
            id: defaultStation.id,
            billboard: {
                image: stationPng,
                scale: imageScale,
                color: stationColor
            },
            label: {
                text: defaultStation.name,
                font: '14px sans-serif',
                outlineColor: Cesium.Color.BLACK,
                pixelOffset: new Cesium.Cartesian2(0, pixelOffset),
                distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, defaultZoomDistance),
            },
            description: `<ul>
                            <li>Name: ${defaultStation.name}</li>
                            <li>Networks: ${defaultStation.networks.map(network => network.name)
                    .join(' ')}</li>
                            <li>Latitude: ${siteLat.toFixed(3)}</li>
                            <li>Lognitude: ${siteLon.toFixed(3)}</li>
                            <li>Elevation: ${siteElev.toFixed(3)}</li></ul>`,
        });

        const stationSites = defaultStation.sites;
        const siteStationID = defaultStation.id;

        stationSites.forEach(stationSite => {
            const stationSiteID: string = stationSite.id;
            const stationSiteLon = stationSite.location.lonDegrees;
            const stationSiteLat = stationSite.location.latDegrees;
            const stationSiteElev = stationSite.location.elevationKm;
            const stationSiteName = defaultStation.name;

            const siteEntity = new Cesium.Entity({
                name: 'site: ' + stationSiteID,
                position: Cesium.Cartesian3.fromDegrees(stationSiteLon, stationSiteLat),
                id: stationSiteID,
                billboard: {
                    image: stationPng,
                    scale: imageScale,
                    distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, closeBillboardZoomDistance),
                    color: stationColor
                },
                label: {
                    text: stationSiteID,
                    font: '12px sans-serif',
                    outlineColor: Cesium.Color.BLACK,
                    pixelOffset: new Cesium.Cartesian2(0, sitePixelOffset),
                    distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, closeLabelZoomDistance),
                },
                description: `<ul>
                                    <li>Name: ${stationSiteID}</li>
                                    <li>Station ID: ${siteStationID}</li>
                                    <li>Station: ${stationSiteName}</li>
                                    <li>Latitude: ${stationSiteLat.toFixed(3)}</li>
                                    <li>Lognitude: ${stationSiteLon.toFixed(3)}</li>
                                    <li>Elevation: ${stationSiteElev.toFixed(3)}</li></ul>`,
            });

            // Apparently, there are duplicate site id's in the data set so
            // we don't want to add it if it exists
            // Cesium will complain LOUDLY
            // May need to handle in future?
            if (!dataSource.entities.getById(stationSiteID)) {
                dataSource.entities.add(siteEntity);
            }
        });

    });
}

/**
 * Update the map entities for a list of Stations
 * @param dataSource 
 * @param eventHypotheses 
 * @param currentTimeInterval 
 */
export function update(dataSource: any,
    currentSignalDetections: MapDataSignalDetection[],
    currentOpenEventHypId: string,
    nextSignalDetections: MapDataSignalDetection[],
    nextOpenEventHypId: string) {

    // Reset stations from last open event
    currentSignalDetections
        .filter(sd => {
            const associatedEvent = sd.signalDetectionAssociations.find(sda =>
                !sda.isRejected &&
                sda.eventHypothesis != undefined);
            return associatedEvent ?
                associatedEvent.eventHypothesis.id === currentOpenEventHypId
                : false;
        })
        .forEach(signalDetection => {
            const eventEntity = dataSource.entities.getById(signalDetection.signalDetection.station.id);
            eventEntity.billboard.color = stationColor;
            eventEntity.billboard.scale = imageScale;
        });

    // Update new stations for next event
    nextSignalDetections
        .filter(sd => {
            const associatedEvent = sd.signalDetectionAssociations.find(sda =>
                !sda.isRejected &&
                sda.eventHypothesis != undefined);
            return associatedEvent ?
                associatedEvent.eventHypothesis.id === nextOpenEventHypId
                : false;
        })
        .forEach(signalDetection => {
            const eventEntity = dataSource.entities.getById(signalDetection.signalDetection.station.id);
            eventEntity.billboard.color = selectedStationColor;
            eventEntity.billboard.scale = imageScale;
        });
}

export function resetView(defaultStations: any[], dataSource: any) {
    defaultStations.forEach(station => {
        dataSource.entities.getById(station.id).billboard.color = stationColor;
    });
}
