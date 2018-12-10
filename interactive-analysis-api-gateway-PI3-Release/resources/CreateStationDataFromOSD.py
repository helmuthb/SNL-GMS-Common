"""
This module supports creation of GMS COI-formatted JSON files containing test data
for processng networks, stations, sites and channels based on network data from the OSD.
The network data is provided either as an input file or input URL to the OSD network endpoint
This module parses processing network, station, site, and channel data from the OSD data.
Parsed data are then written out to separate processing network, station, site, and channel
JSON files, adhering to the GMS COI data model.

This module requires python 3.4 or greater to run.

Running the converter:

python CreateStationDataFromOSD.py <input directory path> <output directory path> <network name>
Parameters:
--osdUrl <osdUrl> The OSD network URL to fetch station data from. If not provided, the --osdFile argument must be provided.
                  If both are provided, the osdURL argument will be used')
--osdFile <osdNetworkFilePath> The network data JSON file as received from the OSD HTTP interface. If not provided,
                               the --osdUrl argument must be provided. If both are provided, the osdURL argument will be used')
--coiDir' <coiDir> Path to the COI-format test data directory where output JSON files will be written.

Example
(from the interactive-analysis-api-gateway/resources directory):

python CreateStationDataFromOSD.py --osdFile ./tmp.txt --coiDir test_data/ueb_2010140/coi/

"""

import os
import argparse
import urllib.parse
import urllib.request
import json

def parseDefaultStationNames(defaultStationPath):
    """
    Parse & return the default station info (used in the UI) from the file at
    the provided path.

    :param defaultStationPath: The file path of the default station information to parse
    """
    defaultStationInfo = []
    with open(defaultStationPath) as defaultStationFile:
        defaultStationInfo = json.loads(defaultStationFile.read())
    defaultStationFile.close()
    return list(map(lambda d: d["stationName"], defaultStationInfo["ui"]["defaultStations"]))


# Parse command-line arguments. The user must provider the OSD URL to retrieve station data from, as well 
# as the network name and effectivity time to provide to the HTTP request
parser = argparse.ArgumentParser()
parser.add_argument('--osdUrl', dest='osdUrl', help='OSD network URL to fetch station data from.' +
    ' If not provided, the --osdFile argument must be provided. If both are provided, the osdURL argument will be used')
parser.add_argument('--osdFile', dest='osdNetworkFilePath', help='network data JSON file as received from the OSD HTTP interface' +
    ' If not provided, the --osdUrl argument must be provided. If both are provided, the osdURL argument will be used')
parser.add_argument('--coiDir', dest='coiDir', help='Path to the COI-format test data directory where output JSON files will be written')
args = parser.parse_args()

if ((not args.osdUrl and not args.osdNetworkFilePath) or not args.coiDir):
    parser.print_help()
    exit(0)

#if (not args.url or not args.network or not args.time):
#    parser.print_help()
#    exit(0)

# url = args.url

#values = {'name' : args.network,
#          'time' : args.time
#}

#data = urllib.parse.urlencode(values)
#data = data.encode('ascii')

# headers={'Accept': 'application/json'}

# request = urllib.request.Request(url=url, headers=headers)
# response = urllib.request.urlopen(request, timeout=60)

# print(response.status)
# print(response.reason)

osdStationData = {}
with open(args.osdNetworkFilePath) as osdNetworkFile:
    osdStationData = json.loads(osdNetworkFile.read())
osdNetworkFile.close()

if (not osdStationData):
    print('Unable to read OSD network/station/site/channel data from file')
    exit(0)

# Read default station names from the file in the COI test data directory
# These will be used to filter down the stations built from the OSD input data
defaultStationNames = parseDefaultStationNames(os.path.join(args.coiDir, "uiConfig.json"))

# Create a network based on the user input
networkId = str(osdStationData['id'])
networks = [{
    'id': networkId,
    'name': osdStationData['name'],
    'organization':osdStationData['organization'],
    'region': osdStationData['region'],
    'stationIds': []
}]

stations = []
sites = []
channels = []

# Parse station data from the OSD data file
for stationIn in osdStationData['stations']:

    # Create the station entry
    stationId = stationIn['id']
    station = {
        'id': stationId,
        'name': stationIn['name'],
        'latitude': float(stationIn['latitude']),
        'longitude': float(stationIn['longitude']),
        'elevation': float(stationIn['elevation']),
        'networkIds': [networkId],
        'siteIds': []
    }
    # Add the station to the station output list, as well as the 
    # network's station ID list
    stations.append(station)
    networks[0]['stationIds'].append(stationId)

    # Parse site data from the OSD data file
    for siteIn in stationIn['sites']:

        # Create the site entry
        siteId = str(siteIn['id'])
        site = {
            'id': siteId,
            'name': siteIn['name'],
            'latitude': float(siteIn['latitude']),
            'longitude': float(siteIn['longitude']),
            'elevation': float(siteIn['elevation']),
            'stationId': stationId,
            'channelIds': []
        }
        sites.append(site)
        station['siteIds'].append(siteId)

        # Parse channel data from the OSD data file
        for channelIn in siteIn['channels']:
            # Build the channel object
            channelId = channelIn['id']
            channel = {
                'id': channelId,
                'name': channelIn['name'],
                'channelType': channelIn['channelType'],
                'dataType': channelIn['dataType'],
                'siteId': siteId,
                'siteName': siteIn['name'],
                'latitude': float(channelIn['latitude']),
                'longitude': float(channelIn['longitude']),
                'elevation': float(channelIn['elevation']),
                'depth': float(channelIn['depth']),
                'verticalAngle': float(channelIn['verticalAngle']),
                'horizontalAngle': float(channelIn['horizontalAngle']),
                'sampleRate': float(channelIn['sampleRate']),
                'response': {
                    'id': channelIn['response']['id'],
                    'responseData': ''
                }
            }
            channels.append(channel)
            site['channelIds'].append(channelId)

# Filter the stations to those matching a name in the default station name list
stations = list(filter(lambda st: st["name"] in defaultStationNames, stations))

# Write the network to file as JSON
with open(os.path.join(args.coiDir, 'processingNetworks.json'), 'w') as netFile:
    netFile.write(json.dumps(networks, indent=4))
netFile.close() 

# Write the stations to file as JSON
with open(os.path.join(args.coiDir, 'processingStations.json'), 'w') as staFile:
    staFile.write(json.dumps(stations, indent=4))
staFile.close() 
    
# Write the sites to file as JSON
siteJson = json.dumps(sites, indent=4)
with open(os.path.join(args.coiDir, 'processingSites.json'), 'w') as siteFile:
    siteFile.write(siteJson)
siteFile.close()

# Write the channels to file as JSON
with open(os.path.join(args.coiDir, 'processingChannels.json'), 'w') as chanFile:
    chanFile.write(json.dumps(channels, indent=4))
chanFile.close()

