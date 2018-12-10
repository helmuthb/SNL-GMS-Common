'''
This module supports creation of GMS COI-formatted JSON signal detection data from input
CSS3.0 arrival and assoc file data.

This module requires python 3.4 or greater to run.

Running the script:

python CreateSignalDetectionDataFromCss.py --stationPath <stationFilePath> --arrivalPath <arrivalFilePath> --assocPath <assocFilePath> --detectionPath <detectionFilePath>

Parameters: 
--stationPath <stationFilePath> Path to the GMS COI-format processing station JSON file')
--arrivalPath <arrivalFilePath> Path to the CSS 3.0-like tab-delimited arrivals file')
--detectionPath <detectionFilePath> Path to the output signal detection JSON file')

Example:

(from the interactive-analysis-api-gateway/resources directory):

python CreateSignalDetectionDataFromCss.py --stationPath ./test_data/ueb_2010140/coi/processingStations.json --arrivalPath ./test_data/ueb_2010140/css/arrival.txt --detectionPath ./test_data/ueb_2010140/coi/signalDetections.json
'''

import os
import json
import time
from pprint import pprint
import csv
import argparse
import uuid


def parseDetectionData(arrivalPath, stations):
    '''
    Parse & return GMS COI-format signal detection (SD), SD hypothesis (SDH) and feature measurement (FM) data
    from the CSS arrival file at the provided path, associating the data with the provided station data.

    :param arrivalPath: The file path of the CSS arrival file to parse SD, SDH and FM data from
    :param stations: The list of GMS COI-format stations to associate the SD data with (by UUID)
    '''
    # Read the arrival file as a tab-delimited file
    with open(arrivalPath, newline='') as csvfile:
        arrivalFileReader = csv.DictReader(csvfile, dialect='excel-tab')

        detections = []

        # Parse arrival data from the arrival file
        for row in arrivalFileReader:

            # Only create the detection entry if a matching station can be found
            # (If no station can be found for the station, skip this entry to avoid
            #  adding orphan stationless detections to the data set)
            matchStations = list(filter(lambda st: st['name'] == row['STA'], stations))

            if(len(matchStations) > 0):
                station = matchStations[0]

                # Create the arrival time feature measurement
                arrivalTimeMeasurement = {
                    'id': str(uuid.uuid4()),
                    'hypothesisId': str(row['ARID']),
                    'featureType': 'ArrivalTime',
                    'definingRules': [
                        {
                            'operationType': 'Location',
                            'isDefining': bool(str(row['IPHASE']) == 'P')
                        }
                    ],
                    'timeSec': float(row['TIME']),
                    'uncertaintySec': float(row['DELTIM'])
                }

                # Create the azimuth/slowness feature measurement
                azSlownessMeasurement = {
                    'id': str(uuid.uuid4()),
                    'hypothesisId': str(row['ARID']),
                    'featureType': 'AzimuthSlowness',
                    'azimuthDefiningRules': [
                        {
                            'operationType': 'Location',
                            'isDefining': bool(str(row['IPHASE']) == 'P')
                        }
                    ],
                    'slownessDefiningRules': [
                        {
                            'operationType': 'Location',
                            'isDefining': bool(str(row['IPHASE']) == 'P')
                        }
                    ],
                    'azimuthDeg': float(row['AZIMUTH']),
                    'slownessSecPerDeg': float(row['SLOW']),
                    'azimuthUncertainty': float(row['DELAZ']),
                    'slownessUncertainty': float(row['DELSLO']),
                    'fkDataId': ''
                }

                # Create two hypotheses for each detection, changing the phase
                # from 'tx' in the first hypothesis to the test data set phase
                # in the 'current' hypothesis

                # Create the initial SD hypothesis
                initialHypothesis = {
                    'id': str(uuid.uuid4()),
                    'phase': 'tx',
                    'isRejected': False,
                    'signalDetectionId': str(row['ARID']),
                    'arrivalTimeMeasurement': arrivalTimeMeasurement,
                    'azSlownessMeasurement': azSlownessMeasurement,
                    'creationInfo': {
                        'id': str(uuid.uuid4()),
                        'creationTime': int(time.time()) - 600,
                        'creatorId': 'Auto',
                        'creatorType': 'System'
                    }
                }

                # Create the current SD hypothesis
                currentHypothesis = {
                    'id': str(row['ARID']),
                    'phase': str(row['IPHASE']),
                    'isRejected': False,
                    'signalDetectionId': str(row['ARID']),
                    'arrivalTimeMeasurement': arrivalTimeMeasurement,
                    'azSlownessMeasurement': azSlownessMeasurement,
                    'creationInfo': {
                        'id': str(uuid.uuid4()),
                        'creationTime': int(time.time()),
                        'creatorId': 'Auto',
                        'creatorType': 'System'
                    }
                }

                # Create the signal detection
                detection = {
                    'id': str(row['ARID']),
                    'monitoringOrganization': 'TEST',
                    'stationId': str(station['id']),
                    'hypotheses': [
                        initialHypothesis,
                        currentHypothesis
                    ],
                    'currentHypothesis': currentHypothesis
                }

                # Add the signal detection to the list
                detections.append(detection)

    csvfile.close()
    return detections

# Parse command-line arguments
parser = argparse.ArgumentParser()
parser.add_argument('--stationPath', dest='stationFilePath', help='Path to the GMS COI-format processing station JSON file')
parser.add_argument('--arrivalPath', dest='arrivalFilePath', help='Path to the CSS 3.0-like tab-delimited arrivals file')
parser.add_argument('--detectionPath', dest='detectionFilePath', help='Path to the output signal detection JSON file')
args = parser.parse_args()

stationFilePath = args.stationFilePath
arrivalFilePath = args.arrivalFilePath
detectionFilePath = args.detectionFilePath

# Handle invalid input
if(not stationFilePath or not arrivalFilePath or not detectionFilePath):
    parser.print_help()
    exit(0)

# Read stations from the input file
stations = []
with open(stationFilePath) as stationFile:
    stations = json.loads(stationFile.read())
stationFile.close()

if(len(stations) == 0):
    print('Unable to read station data from input file')
    exit(0)

# Parse signal detections from the late arrival file
signalDetections = parseDetectionData(arrivalFilePath, stations)

# Filter the signal detections to those matching a station by ID in the default station list
# signalDetections = list(filter(lambda st: sd["stationId"] in defaultStationIds, stations))

# Write the signal detections to file as JSON
with open(detectionFilePath, 'w') as detectionFile:
    detectionFile.write(json.dumps(signalDetections, indent=4))
detectionFile.close()