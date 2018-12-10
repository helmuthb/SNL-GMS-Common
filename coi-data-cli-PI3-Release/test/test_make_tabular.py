"""
Tests MakeTabular.py functions
"""
from util.MakeTabular import table_qcmask, table_waveform, table_soh
from util.ReadData import read_json, parse_dict
import unittest
import io
import os
import sys


class TestMakeTabular(unittest.TestCase):
    """
    Test class
    """

    def setUp(self):
        self.soh_json = read_json(os.path.dirname(__file__) + '/acquired_channel_soh_mock.json')
        self.channel = read_json(os.path.dirname(__file__) + '/channel_mock.json')
        self.channel_segment = read_json(os.path.dirname(__file__) + '/channel_segment_mock.json')
        self.qc_mask_json = read_json(os.path.dirname(__file__) + '/qc_mask_mock.json')
        self.chan_seg = parse_dict(self.channel_segment, 'channel-segment')
        self.chan = parse_dict(self.channel, 'channel')
        self.soh = parse_dict(self.soh_json, 'acquired-channel-soh')
        self.qcmask = parse_dict(self.qc_mask_json, 'qc-mask')

    def test_table_waveform(self):
        output = io.StringIO()
        sys.stdout = output
        table_waveform(self.chan_seg, self.chan)
        sys.stdout = sys.__stdout__
        self.assertEqual(output.getvalue(),
                         """----------------Channel---------------
id:  57015315-f7b2-4487-b3e7-8780fbcfb413
channelName:  CHAN01
processing_channel_id:  46947cc2-8c86-4fa1-a764-c9b9944614b7
channelSegmentName:  segmentName
channelSegmentType:  RAW
channelStartTime:  1970-01-02T03:04:05.123Z
channelEndTime:  1970-01-02T03:04:07.123Z
channelType:  BROADBAND_HIGH_GAIN_VERTICAL
------------Station/Site--------------
siteName:  testName
siteChannel:  CHAN
stationName:  stationName
stationLatitude:  500
stationLongitude:  600
stationElevation:  0
-------------Instrument---------------
instrumentModel:  testModel
calibrationFactor:  1.4
calibrationPeriod:  1.0
------------CreationInfo--------------
creationTime:  2017-11-15T22:18:20.633Z
creatorName:  Default creator name
------------WaveformTable-------------
                  startTime                   endTime  sampleRate  sampleCount
0  1970-01-02T03:04:05.123Z  1970-01-02T03:04:07.123Z         2.0            5
1  1998-01-02T03:04:05.123Z  1998-01-02T03:04:07.123Z         5.0            4
2  2000-01-02T03:04:05.123Z  2000-01-02T03:04:07.123Z         6.0            1\n"""
                         )

    def test_table_soh(self):
        output = io.StringIO()
        sys.stdout = output
        table_soh(self.soh)
        sys.stdout = sys.__stdout__
        self.assertEqual(output.getvalue(),
                         """---------------SohInfo----------------
                    processingChannelId acquiredChannelSohType             startTime               endTime  status                 creationTime            creatorName
0  7b23a6fb-d001-4354-9bb5-7fcb49a530b8    DEAD_SENSOR_CHANNEL  1969-12-31T17:00:00Z  1969-12-31T17:00:10Z   False  2018-01-05T15:58:16.722527Z  OsdDemoDatabaseLoader
1  7b23a6fb-d001-4354-9bb5-7fcb49a530b8      VAULT_DOOR_OPENED  1969-12-31T17:00:00Z  1969-12-31T17:00:10Z   False  2018-01-05T15:58:16.722527Z  OsdDemoDatabaseLoader
2  7b23a6fb-d001-4354-9bb5-7fcb49a530b8  GPS_RECEIVER_UNLOCKED  1969-12-31T17:00:00Z  1969-12-31T17:00:10Z   False  2018-01-05T15:58:16.722527Z  OsdDemoDatabaseLoader
3  7b23a6fb-d001-4354-9bb5-7fcb49a530b8    DEAD_SENSOR_CHANNEL  1969-12-31T17:00:10Z  1969-12-31T17:00:20Z   False  2018-01-05T15:58:16.722527Z  OsdDemoDatabaseLoader
4  7b23a6fb-d001-4354-9bb5-7fcb49a530b8      VAULT_DOOR_OPENED  1969-12-31T17:00:10Z  1969-12-31T17:00:20Z   False  2018-01-05T15:58:16.722527Z  OsdDemoDatabaseLoader
5  7b23a6fb-d001-4354-9bb5-7fcb49a530b8  GPS_RECEIVER_UNLOCKED  1969-12-31T17:00:10Z  1969-12-31T17:00:20Z   False  2018-01-05T15:58:16.722527Z  OsdDemoDatabaseLoader
6  7b23a6fb-d001-4354-9bb5-7fcb49a530b8    DEAD_SENSOR_CHANNEL  1969-12-31T17:00:20Z  1969-12-31T17:00:30Z   False  2018-01-05T15:58:16.722527Z  OsdDemoDatabaseLoader\n"""
                         )

    def test_table_qc_mask(self):
        output = io.StringIO()
        sys.stdout = output
        table_qcmask(self.qcmask)
        sys.stdout = sys.__stdout__
        self.assertEqual(output.getvalue(), """-------------QcMaskTable--------------
   id qcMaskCategory        qcMaskType                            rationale  startTime  endTime
0   0    STATION_SOH    SENSOR_PROBLEM  System created: dead sensor channel     -25080   -24960
1   0    STATION_SOH  STATION_SECURITY    System created: vault door opened     -25020   -24960
2   0    STATION_SOH  STATION_SECURITY    System created: vault door opened     -24840   -24780\n""")


if __name__ == '__main__':
    unittest.main()
