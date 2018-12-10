'''
This module supports creation of GMS COI-formatted JSON channel segment data from input
CSS3.0 wfdisc file data.

This module requires python 3.4 or greater to run.

Running the script:

python CreateChannelSegmentDataFromCss.py --channelPath <channelFilePath> --wfdsicPath <wfdiscFilePath> --segmentPath <segmentFilePath>

Parameters: 
--channelPath <channelFilePath> The path to the GMS COI-format processing channel JSON file')
--wfdsicPath <wfdiscFilePath> The path to the CSS 3.0-like tab-delimited WFDISC file')
--segmentPath <segmentFilePath> The path to the output channel segment JSON file')

Example:
(from the interactive-analysis-api-gateway/resources directory):

python CreateChannelSegmentDataFromCss.py --channelPath ./test_data/ueb_2010140/coi/processingChannels.json --wfdsicPath ./test_data/ueb_2010140/css/wfdisc.txt --segmentPath ./test_data/ueb_2010140/coi/channelSegments.json

'''

import os
import json
from pprint import pprint
import csv
import argparse
import uuid

def parseChannelSegments(wfdiscPath, channels):
    '''
    Parse & return GMS COI-format channel segment data from the CSS wfdisc file at the provided path,
    associating the data with the provided channels.

    :param wfdiscPath: The file path of the CSS wfdisc file to parse channel segment data from
    :param channels: The list of GMS COI-format channels to associate the late channel segment
                  data with (by UUID)
    '''
    # Read the wfdisc file as a tab-delimited file
    with open(wfdiscPath, newline='') as csvfile:
        wfdiscFileReader = csv.DictReader(csvfile, dialect='excel-tab')

        segments = []
        # Parse late channel segment data from the wfdisc file
        for row in wfdiscFileReader:

            # Only create the channel entry if a site can be found for the channel
            # (If no site can be found for the channel, skip this entry to avoid
            #  adding orphan siteless channels to the data set)
            matches = list(filter(lambda c: (c['name'] == row['CHAN'] and c['siteName'] == row['STA']), channels))
            if(len(matches) > 0):
                channelId = matches[0]['id']

                # Create a channel segment from the wfdisc entry
                channelSegment = {
                    'id': str(uuid.uuid4()),
                    'segmentType': 'Raw',
                    'startTime': float(row['TIME']),
                    'endTime': float(row['ENDTIME']),
                    'channelId': channelId,
                    'timeseriesList': [
                        {
                            'id': str(uuid.uuid4()),
                            'startTime': float(row['TIME']),
                            'endTime': float(row['ENDTIME']),
                            'waveformSamples': [],
                            'sampleRate': float(row['SAMPRATE']),
                            'sampleCount': int(row['NSAMP']),
                            'calibration': {
                                'factor': float(row['CALIB']),
                                'factorError': 0,
                                'period': 0,
                                'timeShift': 0
                            },
                            'fileOffset': int(row['FOFF']),
                            'fileName': row['DFILE']
                        }
                    ],
                    'featureMeasurementIds': [],
                    'creationInfo': {
                            'id': str(uuid.uuid4()),
                            'creationTime': float(row['ENDTIME']),
                            'creatorId': 'Auto',
                            'creatorType': 'System'
                    }
                }

                segments.append(channelSegment)
    csvfile.close()
    return segments

# Parse command-line arguments
parser = argparse.ArgumentParser()
parser.add_argument('--channelPath', dest='channelFilePath', help='Path to the GMS COI-format processing channel JSON file')
parser.add_argument('--wfdsicPath', dest='wfdiscFilePath', help='Path to the CSS 3.0-like tab-delimited WFDISC file')
parser.add_argument('--segmentPath', dest='segmentFilePath', help='Path to the output channel segment JSON file')
args = parser.parse_args()

channelFilePath = args.channelFilePath
wfdiscFilePath = args.wfdiscFilePath
segmentFilePath = args.segmentFilePath

# Handle invalid input
if(not channelFilePath or not wfdiscFilePath or not segmentFilePath):
    parser.print_help()
    exit(0)

# Read channels from the input file
channels = []
with open(channelFilePath) as channelFile:
    channels = json.loads(channelFile.read())
channelFile.close()

if(len(channels) == 0):
    print('Unable to read channel data from input file')
    exit(0)

# Parse channel segments from the late wfdisc file
channelSegments = parseChannelSegments(wfdiscFilePath, channels)

# Write the channel segments to file as JSON
with open(segmentFilePath, 'w') as segmentFile:
    segmentFile.write(json.dumps(channelSegments, indent=4))
segmentFile.close()
