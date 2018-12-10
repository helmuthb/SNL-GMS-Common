import * as lodash from 'lodash';
import * as moment from 'moment';
import { userPreferences } from '../../config';
import { TimeInterval } from '../../state';
import { MapProps } from '../map';
import { MapEventHypothesis } from './graphql/query';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const eventPng = require('./img/' + userPreferences.map.icons.event);
const imageScale = userPreferences.map.icons.eventScale;

(window as any).CESIUM_BASE_URL = './build/cesium';
import 'cesium/Build/Cesium/Widgets/widgets.css';
import 'cesium/Build/CesiumUnminified/Cesium.js';
declare var Cesium;

const scaleFactor = userPreferences.map.icons.scaleFactor;
const displayDistance = userPreferences.map.icons.displayDistance;
const pixelOffset = userPreferences.map.icons.pixelOffset;

/**
 * construct a Cesium DataSource with the events
 * @returns a Cesium.DataSource
 */
export function draw(dataSource: any, currentProps: MapProps, nextProps: MapProps) {
    const nextEventHypotheses = nextProps.eventData.eventHypothesesInTimeRange;
    const currentEventHypotheses = currentProps.eventData.eventHypothesesInTimeRange;
    const currentTimeInterval = nextProps.currentTimeInterval;
    const nextOpenEventId = nextProps.openEventHypId;
    const selectedEventIds = nextProps.selectedEventHypIds;
    const prevSelectedEventIds = currentProps.selectedEventHypIds;

    const newEvents = lodash.differenceBy(nextEventHypotheses, currentEventHypotheses, 'id');
    addEvents(dataSource, newEvents, currentTimeInterval, nextOpenEventId, selectedEventIds);

    const modifiedEvents =
        (lodash.intersectionBy(nextEventHypotheses, currentEventHypotheses, 'id'));
    updateEvents(dataSource, modifiedEvents, currentTimeInterval, nextOpenEventId, selectedEventIds);

    const selectedEvents = lodash.intersectionBy(nextEventHypotheses, currentEventHypotheses, 'id')
        .filter(selectionEvent => selectedEventIds.
            find(eid => eid === selectionEvent.id) || prevSelectedEventIds.
                find(eid => eid === selectionEvent.id));
    updateEvents(dataSource, selectedEvents, currentTimeInterval, nextOpenEventId, selectedEventIds);

    const removedEvents = lodash.differenceBy(
        currentEventHypotheses,
        nextEventHypotheses, 'id');
    removeEvents(dataSource, removedEvents);
}

/**
 * Highlight Open Event when event is opened on the map or list
 * @param dataSource event datasource
 * @param currentTimeInterval current time interval opened by the analyst
 * @param currentOpenEvent open event that has been open
 * @param nextOpenEvent event being opened
 * @param selectedEventIds other selected event ids
 */
export function highlightOpenEvent(dataSource: any,
    currentTimeInterval: TimeInterval,
    currentOpenEvent: MapEventHypothesis,
    nextOpenEvent: MapEventHypothesis,
    selectedEventIds: string[]) {
    // attempt to re-color the currently selected event entity to its default state.
    if (currentOpenEvent) {
        const currentEventEntity = dataSource.entities.getById(currentOpenEvent.id);
        // If a next open event exists pass in the ID of that event,
        // in the case of selecting 'mark complete' on the current
        // open event nextopenevent is null/undefined.
        if (nextOpenEvent) {
            currentEventEntity.billboard.color = computeColorForEvent(
                currentOpenEvent, currentTimeInterval, nextOpenEvent.id, selectedEventIds);
        } else {
            currentEventEntity.billboard.color = computeColorForEvent(
                currentOpenEvent, currentTimeInterval, undefined, selectedEventIds);
        }
    }
    if (nextOpenEvent) {
        const nextEventEntity = dataSource.entities.getById(nextOpenEvent.id);
        nextEventEntity.billboard.color = computeColorForEvent(
            nextOpenEvent, currentTimeInterval, nextOpenEvent.id, selectedEventIds);
    }
}

/**
 * create new map entities for a list of events
 * @param dataSource - Cesium.DataSource
 * @param eventHypotheses - MapEventHypothesis[]
 * @param currentTimeInterval - TimeInterval
 */
function addEvents(dataSource: any, eventHypotheses: MapEventHypothesis[],
    currentTimeInterval: TimeInterval,
    nextOpenEventId: string, selectedEventIds: string[]) {

    eventHypotheses.forEach(eventHypothesis => {
        const eventLon = eventHypothesis.preferredLocationSolution.locationSolution.lonDegrees;
        const eventLat = eventHypothesis.preferredLocationSolution.locationSolution.latDegrees;
        const eventElev = eventHypothesis.preferredLocationSolution.locationSolution.depthKm;

        const eventTime = eventHypothesis.preferredLocationSolution.locationSolution.timeSec;
        const eventTimeFormatted = moment.unix(eventTime)
            .utc()
            .format('HH:mm:ss');
        const eventDateTimeFormatted = moment.unix(eventTime)
            .utc()
            .format('MM/DD/YYYY HH:mm:ss');

        const magSolutions = eventHypothesis.preferredLocationSolution.locationSolution.networkMagnitudeSolutions;
        const eventMag = magSolutions.length > 0
            ? magSolutions
                .map(mag => `${mag.magnitudeType}=${mag.magnitude}`)
                .join(', ')
            : 'none';

        const eventDescription = `<ul>
                                  <li>ID: ${eventHypothesis.id}</li>
                                  <li>Time: ${eventDateTimeFormatted}</li>
                                  <li>Latitude: ${eventLat.toFixed(3)}</li>
                                  <li>Lognitude: ${eventLon.toFixed(3)}</li>
                                  <li>Elevation: ${eventElev.toFixed(3)}</li>
                                  <li>Magnitude: ${eventMag}</li></ul>`;

        dataSource.entities.add({
            name: 'Event: ' + eventHypothesis.id,
            position: Cesium.Cartesian3.fromDegrees(eventLon, eventLat),
            id: eventHypothesis.id,
            billboard: {
                image: eventPng,
                color: computeColorForEvent(eventHypothesis, currentTimeInterval, nextOpenEventId, selectedEventIds),
                scale: imageScale
            },
            label: {
                text: eventTimeFormatted,
                font: '12px sans-serif',
                outlineColor: Cesium.Color.BLACK,
                pixelOffset: new Cesium.Cartesian2(0, pixelOffset),
                distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, displayDistance),
            },
            description: eventDescription,
            entityType: 'event'
        });
    });
}

/**
 * Update the map entities for a list of events
 * @param dataSource - Cesium.DataSource
 * @param eventHypotheses - MapEventHypothesis[]
 * @param currentTimeInterval - TimeInterval
 */
function updateEvents(dataSource: any, eventHypotheses: MapEventHypothesis[],
    currentTimeInterval: TimeInterval,
    nextOpenEventId: string, selectedEventIds: string[]) {

    eventHypotheses.forEach(eventHypothesis => {
        const eventEntity = dataSource.entities.getById(eventHypothesis.id);
        const isSelected = selectedEventIds.find(eid => eid === eventHypothesis.id);
        // eventEntity.billboard.image = isSelected ? selectedEventPng : eventPng;
        eventEntity.billboard.color = computeColorForEvent(
            eventHypothesis, currentTimeInterval, nextOpenEventId, selectedEventIds);
        eventEntity.billboard.scale = isSelected ? imageScale * scaleFactor : imageScale;
    });
}

/**
 * Remove events from datasource
 * @param dataSource event datasource 
 * @param eventHypotheses events to be removed
 */
function removeEvents(dataSource: any, eventHypotheses: MapEventHypothesis[]) {
    eventHypotheses.forEach(eventHypothesis => {
        dataSource.entities.removeById(eventHypothesis.id);
    });
}

/**
 * Compute the proper png to use for an event star
 * @param eventHypothesis - MapEventHypothesis
 * @param currentTimeInterval - TimeInterval
 */
function computeColorForEvent(eventHypothesis: MapEventHypothesis,
    currentTimeInterval: TimeInterval,
    openEventId: string,
    selectedEventIds: string[]) {
    const eventTime = eventHypothesis.preferredLocationSolution.locationSolution.timeSec;
    const isInTimeRange = (eventTime > currentTimeInterval.startTimeSecs)
        && (eventTime < currentTimeInterval.endTimeSecs);

    return isInTimeRange ?
        eventHypothesis.event.status === 'Complete' ?
            Cesium.Color.fromCssColorString(userPreferences.map.colors.completeEvent)
            : eventHypothesis.id === openEventId ?
                Cesium.Color.fromCssColorString(userPreferences.map.colors.openEvent)
                : selectedEventIds.find(eid => eid === eventHypothesis.id) ?
                    Cesium.Color.fromCssColorString(userPreferences.map.colors.selectedEvent)
                    : Cesium.Color.fromCssColorString(userPreferences.map.colors.toWorkEvent)
        : Cesium.Color.fromCssColorString(userPreferences.map.colors.outOfIntervalEvent);
}
