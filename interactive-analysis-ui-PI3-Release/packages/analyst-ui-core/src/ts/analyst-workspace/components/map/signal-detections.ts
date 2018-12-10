import { analystUiConfig, userPreferences } from '../../config';
import { MapDataSignalDetection } from './graphql/query';

(window as any).CESIUM_BASE_URL = './build/cesium';
import 'cesium/Build/Cesium/Widgets/widgets.css';
import 'cesium/Build/CesiumUnminified/Cesium.js';
declare var Cesium;

/**
 * construct a Cesium DataSource with signal detections
 * @returns a Cesium.DataSource
 */
export function draw(dataSource: any,
                     signalDetections: MapDataSignalDetection[],
                     nextOpenEventHypId: string) {

    addSignalDetections(dataSource, signalDetections, nextOpenEventHypId);
}

/**
 * Highlights the selected detection from either map/waveform viewer/list click
 * @param dataSource signal detection datasource
 * @param nextSelectedSignalDetection detections being selected
 */
export function highlightSelectedSignalDetections(
    dataSource: any,
    nextSelectedSignalDetection: string[],
) {
    const unSelectedWidth = analystUiConfig.userPreferences.map.widths.unselectedSignalDetection;
    const selectedWidth = analystUiConfig.userPreferences.map.widths.selectedSignalDetection;

    let signalDetectionEntities = dataSource.entities.values;
    signalDetectionEntities = dataSource.entities.values.map(entity => entity.id);
    const l = signalDetectionEntities.length;

    for (let i = 0; i < l; i++) {
        const sdID = signalDetectionEntities[i];
        const usd = dataSource.entities.getById(sdID);
        usd.polyline.width = unSelectedWidth;
    }

    nextSelectedSignalDetection.forEach(function (nsd) {
        const nextSignalDetection = dataSource.entities.
            getById(nsd);
        if (nextSignalDetection) {
            nextSignalDetection.polyline.width = selectedWidth;
        }
    });
}

/**
 * Resets by removing all signal detections when new interval is opened
 * @param dataSource signal detection datasoure
 */
export function resetView(dataSource: any) {
    dataSource.entities.removeAll();
}

/**
 * create new map entities for a list of signal detections
 * @param dataSource signal detection datasource
 * @param signalDetections detections to add
 * @param nextOpenEventHypId event that is being opened
 */
function addSignalDetections(dataSource: any, signalDetections: MapDataSignalDetection[], nextOpenEventHypId: string) {

    resetView(dataSource);

    signalDetections
        .filter(sd => {
            const associatedEvent = sd.signalDetectionAssociations.find(sda =>
                                                                        !sda.isRejected &&
                                                                        sda.eventHypothesis != undefined);
            return associatedEvent ?
                associatedEvent.eventHypothesis.id === nextOpenEventHypId
                : false;
        })
        .forEach(signalDetection => {
            try {
                const sigDet = signalDetection.signalDetection;
                const sigDetAssoc = signalDetection.signalDetectionAssociations.find(sda => !sda.isRejected);
                const sigDetId = sigDet.id;
                const sigDetLat = sigDet.station.location.latDegrees;
                const sigDetLon = sigDet.station.location.lonDegrees;
                const sigDetAssocLat = sigDetAssoc.eventHypothesis.
                    preferredLocationSolution.locationSolution.latDegrees;
                const sigDetAssocLon = sigDetAssoc.eventHypothesis.
                    preferredLocationSolution.locationSolution.lonDegrees;

                dataSource.entities.add({
                    name: 'id: ' + sigDetId,
                    polyline: {
                        positions: Cesium.Cartesian3.fromDegreesArray([sigDetLon, sigDetLat,
                            sigDetAssocLon, sigDetAssocLat]),
                        width: analystUiConfig.userPreferences.map.widths.unselectedSignalDetection,
                        material: Cesium.Color.fromCssColorString(userPreferences.map.colors.openEvent)
                    },
                    id: sigDetId,
                    entityType: 'sd'
                });
            } catch (e) {
                // tslint:disable-next-line:no-console
                console.error('error: ', e.message);
            }
        });
}
