# -*- coding: utf-8 -*-
import json
import os
import pandas as pd
from pandas.io.json import json_normalize
from util.access import *
from util.writeCSS import *
from util.formatPrint import normalize_json_obj

'''
@:param channel_id Taken on the command line from the user
@:param start
@:param end
@:param hostname
Given the css_export arg by the user, take the channel_id and retrieve sites given channel_id, 
station membership given site id, stations given station membership, network membership given station id, 
networks given network membership.
Retrieve calibrations, sensors, and responses given channel id.
Call the write functions for the flatfiles.
'''


def css_writer(channel_id, start, end, hostname, outputDir):
    chanObj = retrieve_channels_by_id(channel_id, start, end, hostname)
    siteObj, siteIds = site_query(channel_id, start, end, hostname)
    calibrationObj = retrieve_calibrations(channel_id, start, end, hostname)
    sensorObj = retrieve_sensors(channel_id, start, end, hostname)
    responseObj = retrieve_responses(channel_id, start, end, hostname)

    if not calibrationObj or not sensorObj or not responseObj:
        calibrationObj = None
        sensorObj = None
        responseObj = None

    stationMembersObj, siteToStaMemRelationship = stationMembers_query(siteIds, start, end, hostname)

    stationObj, staMemtoStaRelationship = station_query(siteToStaMemRelationship, start, end, hostname)

    networkMembersObj, stationToNetMemRelationship = networkMembers_query(staMemtoStaRelationship, start, end, hostname)

    networkObj, netMemtoNetRelationship = network_query(stationToNetMemRelationship, start, end, hostname)

    #NOTE: SITECHAN MUST BE CALLED BEFORE SENSOR
    #THE FK VALUES FOR SENSOR ARE GENERATED IN SITECHAN
    write_network(networkObj)
    write_sitechan(chanObj, siteObj)
    write_site(siteObj, stationObj, siteToStaMemRelationship)
    write_affiliation(stationObj, networkObj, networkMembersObj, siteObj, stationMembersObj)
    write_sensor(chanObj, siteObj, calibrationObj, sensorObj)
    write_instrument(outputDir, chanObj, calibrationObj, sensorObj, responseObj)


def site_query(channel_id, start, end, hostname):
    siteObj = retrieve_sites_by_channel(channel_id, start, end, hostname)  # list of dicts
    siteFlat = normalize_json_obj(siteObj)  # flat dataframe, can pull cols from here
    siteIds = siteFlat['entityId'].unique().tolist()  # grab the siteIds col from the dataframe and make a set
    return siteObj, siteIds


def stationMembers_query(siteIds, start, end, hostname):
    stationMembersObj = []
    siteToStaMemRelationship = {}
    for s in siteIds:
        resp = retrieve_station_members(s, start, end,
                                        hostname)  # query sta membership for each site and get the json response
        if not resp:
            return None, None
        stationMembersObj.append(resp)  # store all the json responses in a list
        norm = normalize_json_obj(resp)  # normalize each json response into a dataframe
        stamids = norm['stationId'].unique().tolist()  # get the unique station membership ids
        siteToStaMemRelationship.update(
            {s: stamids})  # dict of dicts of siteIds mapped to staMembershipIds they are associated with
    return stationMembersObj, siteToStaMemRelationship


def station_query(siteToStaMemRelationship, start, end, hostname):
    if not siteToStaMemRelationship:
        return None, None
    stationObj = []
    staMemtoStaRelationship = {}
    for rel in siteToStaMemRelationship:
        for g in siteToStaMemRelationship.get(rel):
            resp = retrieve_stations_by_id(g, start, end, hostname)
            if not resp:
                return None, None
            stationObj.append(resp)
            norm = normalize_json_obj(resp)
            statids = norm['entityId'].unique().tolist()
            staMemtoStaRelationship.update({g: statids})
    return stationObj, staMemtoStaRelationship


def networkMembers_query(staMemtoStaRelationship, start, end, hostname):
    if not staMemtoStaRelationship:
        return None, None
    networkMembersObj = []
    stationToNetMemRelationship = {}
    for rel in staMemtoStaRelationship:
        for g in staMemtoStaRelationship.get(rel):
            resp = retrieve_network_members(g, start, end, hostname)
            if not resp:
                return None, None
            networkMembersObj.append(resp)
            norm = normalize_json_obj(resp)
            nMemIds = norm['networkId'].unique().tolist()
            stationToNetMemRelationship.update({g: nMemIds})
    return networkMembersObj, stationToNetMemRelationship


def network_query(stationToNetMemRelationship, start, end, hostname):
    if not stationToNetMemRelationship:
        return None, None
    networkObj = []
    netMemtoNetRelationship = {}
    for rel in stationToNetMemRelationship:
        for g in stationToNetMemRelationship.get(rel):
            resp = retrieve_networks_by_id(g, start, end, hostname)
            if not resp:
                return None, None
            networkObj.append(resp)
            norm = normalize_json_obj(resp)
            netIds = norm['entityId'].unique().tolist()
            netMemtoNetRelationship.update({g: netIds})
    return networkObj, netMemtoNetRelationship
