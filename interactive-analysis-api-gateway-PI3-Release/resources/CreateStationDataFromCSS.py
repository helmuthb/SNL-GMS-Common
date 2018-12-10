'''
This module supports conversion of CSS3.0 data files into GMS COI data model
JSON files. Specifically, this module parses station, site, channel, channel segment,
signal detection, signal detection hypothesis and feature measurement data from the
CSS site, wfdisc and arrival files. Parsed data are then written out to processing
network, station, site, channel, channel segment and signal detection JSON files,
adhering to the GMS COI data model.

This module requires python 3.4 or greater to run.

Running the converter:

python StationDataConverter.py <input directory path> <output directory path> <network name>
Parameters: 
<input directory path>: The path to the directory containing site and wfdisc files to read. Note: the
                        software expects the site file to be named sites.txt, and the wfdisc file to
                        be named wfdisc.txt
<input directory path>: The path to the directory where this converter will write the resulting network,
                        station, site and channel JSON files. Note: the output files will be named as follows:
                        network data: processingNetworks.json
                        station data: processingStations.json
                        site data: processingSites.json
                        channel data: processingChannels.json
                        channel segment data: channelSegments.json 
                        late channel segment data: channelSegmentsLate.json
                        signal detection data: signalDetections.json
                        late signal detection data: signalDetectionsLate.json
<network name>: The name of the network to associate with the stations loaded from the site file
'''

import os
import json
import time
from pprint import pprint
from random import *
import csv
import argparse
import uuid
import statistics

def convertChannelType(channelTypeCode):
    '''
    Convert the provided channel type code (e.g. 'BHZ') to the corresponding
    ChannelType string value per the GMS data model. 'ShortPeriodHighGainVertical'
    is returned by default (e.g. for non-matching or empty code parameter values)

    :param channelTypeCode: The channel code to convert to an enum value
    '''
    if(channelTypeCode.startswith('BHZ') or
       channelTypeCode.startswith('bz')):
        return 'BroadbandHighGainVertical'
    elif(channelTypeCode.startswith('BHN') or
       channelTypeCode.startswith('BH1') or
       channelTypeCode.startswith('bn')):
        return 'BroadbandHighGainNorthSouth'
    elif(channelTypeCode.startswith('BHE') or
       channelTypeCode.startswith('BH2') or
       channelTypeCode.startswith('be')):
        return 'BroadbandHighGainEastWest'
    elif(channelTypeCode.startswith('SHZ') or
       channelTypeCode.startswith('sz')):
        return 'ShortPeriodHighGainVertical'
    elif(channelTypeCode.startswith('SHN') or
       channelTypeCode.startswith('sn')):
        return 'ShortPeriodHighGainNorthSouth'
    elif(channelTypeCode.startswith('SHE') or
       channelTypeCode.startswith('se')):        
        return 'ShortPeriodHighGainEastWest'
    elif(channelTypeCode.startswith('EDH')):
        return 'ExtremelyShortPeriodHydrophone'
    elif(channelTypeCode.startswith('EHE')):
        return 'ExtremelyShortPeriodHighGainEastWest'
    elif(channelTypeCode.startswith('EHN')):
        return 'ExtremelyShortPeriodHighGainNorthSouth'
    elif(channelTypeCode.startswith('EHZ')):
        return 'ExtremelyShortPeriodHighGainVertical'
    elif(channelTypeCode.startswith('HHE')):
        return 'HighBroadbandHighGainEastWest'
    elif(channelTypeCode.startswith('HHN')):
        return 'HighBroadbandHighGainNorthSouth'
    elif(channelTypeCode.startswith('HHV')):
        return 'HighBroadbandHighGainVertical'
    elif(channelTypeCode.startswith('MHE') or
         channelTypeCode.startswith('MH2')):
        return 'MidPeriodHighGainEastWest'
    elif(channelTypeCode.startswith('MHN') or
       channelTypeCode.startswith('MH1')):
        return 'MidPeriodHighGainNorthSouth'
    elif(channelTypeCode.startswith('MHV') or
         channelTypeCode.startswith('MHZ')):
        return 'MidPeriodHighGainVertical'
    else:
        return 'ShortPeriodHighGainVertical'


def parseDefaultStationNames(defaultStationPath):
    '''
    Parse & return the default station info (used in the UI) from the file at
    the provided path.

    :param defaultStationPath: The file path of the default station information to parse
    '''
    defaultStationInfo = []
    with open(defaultStationPath) as defaultStationFile:
        defaultStationInfo = json.loads(defaultStationFile.read())
    defaultStationFile.close()
    return list(map(lambda d: d['stationName'], defaultStationInfo['ui']['defaultStations']))

def parseStationData(sitePath, defaultStationNames, networkId):
    '''
    Parse & return GMS COI-format station and site data from the CSS site file at
    the provided path, associating the stations with the provided network ID. Filter the
    stations down to those matching the input list of default stations

    :param sitePath: The file path of the CSS site file to parse station and site data from
    :param defaultStationNames: The list of default station names to filter the station data with
    :param networkID: The ID of the processing network to associate the station data with
    '''

    # Initialize the output station and site lists
    stations = []
    sites = []

    # Read the sites file as a tab-delimited file
    with open(sitePath, newline='') as csvfile:
        siteFileReader = csv.DictReader(csvfile, dialect='excel-tab')

        # Parse station and site data from the site file
        for row in siteFileReader:

            # If the station doesn't already exist in the output list,
            # create the station and add it ot the output list
            # The check on existence is necessary because the site file includes
            # multiple stations (one for each site entry in an array station)
            # In any case, create a new site entry and add it to the site output list
            # as well as the station's site ID list
            matches = list(filter(lambda s: s['name'] == row['REFSTA'], stations))
            if(len(matches) > 0):
                station = matches[0]
            else:
                # Create the station entry if it doesn't already exist
                station = {
                    'id': str(uuid.uuid4()),
                    'name': row['REFSTA'],
                    'latitude': float(row['LAT']),
                    'longitude': float(row['LON']),
                    'elevation': float(row['ELEV']),
                    'networkIds': [networkId],
                    'siteIds': []
                }
                # Add the station to the station output list, as well as the 
                # network's station ID list
                stations.append(station)
                networks[0]['stationIds'].append(station['id'])

            # Create the site entry
            siteId = str(uuid.uuid4())
            site = {
                'id': siteId,
                'name': row['STA'],
                'latitude': float(row['LAT']),
                'longitude': float(row['LON']),
                'elevation': float(row['ELEV']),
                'stationId': station['id'],
                'channelIds': []
            }

            # Add the site to the site output list, as well as the 
            # station's site ID list
            sites.append(site)
            station['siteIds'].append(siteId)
    csvfile.close()

    # Filter the stations to those matching a name in the default station name list
    stations = list(filter(lambda st: st['name'] in defaultStationNames, stations))

    # Set the station location components (lat, lon, elevation)
    # to the mean of the associated site location components
    for station in stations:
        stationSites = list(filter(lambda si: si['id'] in station['siteIds'], sites))
        if(len(stationSites) > 0):
            station['latitude'] = statistics.mean(list(map(lambda ss: ss['latitude'], stationSites)))
            station['longitude'] = statistics.mean(list(map(lambda ss: ss['longitude'], stationSites)))
            station['elevation'] = statistics.mean(list(map(lambda ss: ss['elevation'], stationSites)))

    return (stations, sites)

def parseChannelData(wfdiscPath, sites):
    '''
    Parse & return GMS COI-format channel data from the CSC wfdisc file at the provided path,
    associating the data with the provided site data.

    :param wfdiscPath: The file path of the CSS wfdisc file to parse channel and channel segment data from
    :param sites: The list of GMS COI-format sites to associate the channel and channel segment data with (by UUID)
    '''
    # Read the wfdisc file as a tab-delimited file
    with open(wfdiscPath, newline='') as csvfile:
        wfdiscFileReader = csv.DictReader(csvfile, dialect='excel-tab')

        channels = []

        # Parse channel data from the wfdisc file
        for row in wfdiscFileReader:

            # Initialize a channel ID
            channelId = str(uuid.uuid4())

            # If the channel doesn't already exist in the output list,
            # create the channel and add it ot the output list
            # The check on existence is necessary because the wfdisc file may
            # include multiple channel segments for each channel
            matches = list(filter(lambda c: (c['name'] == row['CHAN'] and c['siteName'] == row['STA']), channels))
            if(len(matches) < 1):

                # Only create the channel entry if a site can be found for the channel
                # (If no site can be found for the channel, skip this entry to avoid
                #  adding orphan siteless channels to the data set)
                chanSites = list(filter(lambda si: si['name'] == row['STA'], sites))
                if(len(chanSites) > 0):
                    chanSite = chanSites[0]

                    # Add the channel ID to the list in the associated site
                    chanSite['channelIds'].append(channelId)

                    # Build the channel object
                    channel = {
                        'id': channelId,
                        'name': row['CHAN'],
                        'channelType': convertChannelType(row['CHAN']),
                        'dataType': 'UNKNOWN',
                        'siteId': chanSite['id'],
                        'siteName': row['STA'],
                        'latitude': float(chanSite['latitude']),
                        'longitude': float(chanSite['longitude']),
                        'elevation': float(chanSite['elevation']),
                        'depth': 0,
                        'verticalAngle': 0,
                        'horizontalAngle': 0,
                        'sampleRate': float(row['SAMPRATE']),
                        'response': {
                            'id': str(uuid.uuid4()),
                            'responseData': ''
                        }
                    }
                    # Add the new channel to the output list
                    channels.append(channel)

    csvfile.close()

    return channels

# Parse command-line arguments. The user must provider the path to the test data directory,
# as well as the name and monitoring organization of the network to associate with the stations
# in the data set
parser = argparse.ArgumentParser()
parser.add_argument('--defaultStationPath', dest='defaultStationFilePath', help='Path to the GMS COI-format default station info JSON file')
parser.add_argument('--sitePath', dest='siteFilePath', help='Path to the CSS 3.0-like tab-delimited site file')
parser.add_argument('--wfdsicPath', dest='wfdiscFilePath', help='Path to the CSS 3.0-like tab-delimited WFDISC file')
parser.add_argument('--outDir', dest='outDir', help='Path to the output directory where the converted JSON files will be written')
parser.add_argument('--network', dest='networkName', help='The name of the network to associate the stations with')
args = parser.parse_args()

# Extract command-line arguments
defaultStationPath = args.defaultStationFilePath
siteFilePath = args.siteFilePath
wfdiscFilePath = args.wfdiscFilePath
outDir = args.outDir
networkName = args.networkName

# Handle invalid input
if (not (defaultStationPath and siteFilePath and wfdiscFilePath and outDir and networkName)):
    parser.print_help()
    exit(0)

# Create a network based on the user input
networkId = str(uuid.uuid4())
networks = [{
    'id': networkId,
    'name': networkName,
    'organization':'TEST',
    'region': 'TEST',
    'stationIds': []
}]

sites = []
stations = []

# Read and parse the default station config from file
defaultStationNames = parseDefaultStationNames(defaultStationPath)

# Parse stations and sites from the site CSS file
(stations, sites) = parseStationData(siteFilePath, defaultStationNames, networkId)

channels = []
channelSegments = []

# Parse channels from the wfdisc file
channels = parseChannelData(wfdiscFilePath, sites)

# Write the network to file as JSON
with open(os.path.join(outDir, 'processingNetworks.json'), 'w') as netFile:
    netFile.write(json.dumps(networks, indent=4))
netFile.close() 

# Write the stations to file as JSON
with open(os.path.join(outDir, 'processingStations.json'), 'w') as staFile:
    staFile.write(json.dumps(stations, indent=4))
staFile.close() 
    
# Write the sites to file as JSON
siteJson = json.dumps(sites, indent=4)
with open(os.path.join(outDir, 'processingSites.json'), 'w') as siteFile:
    siteFile.write(siteJson)
siteFile.close()

# Write the channels to file as JSON
with open(os.path.join(outDir, 'processingChannels.json'), 'w') as chanFile:
    chanFile.write(json.dumps(channels, indent=4))
chanFile.close()
