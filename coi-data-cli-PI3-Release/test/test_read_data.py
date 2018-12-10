"""
Tests all ReadData.py functions
"""
import unittest
import io
import sys
import os
from util.ReadData import read_json, parse_dict, encode_msgpack, decode_msgpack


class TestReadData(unittest.TestCase):
    """
    Test class
    """

    def setUp(self):
        self.soh_json = read_json(os.path.dirname(__file__) + '/acquired_channel_soh_mock.json')
        self.channel = read_json(os.path.dirname(__file__) + '/channel_mock.json')
        self.channel_segment = read_json(os.path.dirname(__file__) + '/channel_segment_mock.json')
        self.qc_mask_json = read_json(os.path.dirname(__file__) + '/qc_mask_mock.json')
        self.null = []

    def test_parse_dict_channel_segment(self):
        data_object = 'channel-segment'
        chan_seg = parse_dict(self.channel_segment, data_object)

        self.assertEqual(chan_seg.name, self.channel_segment['name'])
        self.assertEqual(chan_seg.processing_channel_id, self.channel_segment['processingChannelId'])
        self.assertEqual(chan_seg.id, self.channel_segment['id'])
        self.assertEqual(chan_seg.channel_segment_type, self.channel_segment['segmentType'])
        self.assertEqual(chan_seg.start_time, self.channel_segment['startTime'])
        self.assertEqual(chan_seg.end_time, self.channel_segment['endTime'])
        self.assertEqual(chan_seg.waveform[0].end_time, self.channel_segment['waveforms'][0]['endTime'])
        self.assertEqual(chan_seg.waveform[0].start_time, self.channel_segment['waveforms'][0]['startTime'])
        self.assertEqual(chan_seg.waveform[0].sample_count, self.channel_segment['waveforms'][0]['sampleCount'])
        self.assertEqual(chan_seg.waveform[0].sample_rate, self.channel_segment['waveforms'][0]['sampleRate'])
        self.assertEqual(chan_seg.waveform[0].values, self.channel_segment['waveforms'][0]['values'])

    def test_parse_dict_channel(self):
        data_object = 'channel'
        chan = parse_dict(self.channel, data_object)

        self.assertEqual(chan.name, self.channel['name'])
        self.assertEqual(chan.channel_type, self.channel['type'])
        self.assertEqual(chan.sites.name, self.channel['sites']['name'])
        self.assertEqual(chan.sites.channels, self.channel['sites']['channels'])
        self.assertEqual(chan.sites.station.name, self.channel['sites']['station']['name'])
        self.assertEqual(chan.sites.station.elevation, self.channel['sites']['station']['elevation'])
        self.assertEqual(chan.sites.station.longitude, self.channel['sites']['station']['longitude'])
        self.assertEqual(chan.sites.station.latitude, self.channel['sites']['station']['latitude'])
        self.assertEqual(chan.sites.station.station_type, self.channel['sites']['station']['stationType'])
        self.assertEqual(chan.instruments.instrument_model, self.channel['instruments']['instrumentModel'])
        self.assertEqual(chan.calibration.calibration_period, self.channel['calibration']['period'])
        self.assertEqual(chan.calibration.calibration_factor, self.channel['calibration']['factor'])

    def test_parse_dict_soh(self):
        data_object = 'acquired-channel-soh'
        soh = parse_dict(self.soh_json, data_object)

        self.assertEqual(soh[0].processing_channel_id, self.soh_json[0]['processingChannelId'])
        self.assertEqual(soh[0].acquired_channel_soh_type, self.soh_json[0]['type'])
        self.assertEqual(soh[0].start_time, self.soh_json[0]['startTime'])
        self.assertEqual(soh[0].end_time, self.soh_json[0]['endTime'])
        self.assertEqual(soh[0].status_type, self.soh_json[0]['status'])

    def test_parse_dict_qc_mask(self):
        data_object = 'qc-mask'
        qc = parse_dict(self.qc_mask_json, data_object)

        self.assertEqual(qc[0].id, self.qc_mask_json[0]['id'])
        self.assertEqual(qc[0].processing_channel_id, self.qc_mask_json[0]['processingChannelId'])

    def test_parse_dict_null_channel_segment(self):
        output = io.StringIO()
        sys.stdout = output
        data_object = 'channel-segment'
        parse_dict(self.null, data_object)
        sys.stdout = sys.__stdout__
        self.assertEqual(output.getvalue(), "Error: Service Returned Null Channel Segment Data\n")

    def test_parse_dict_null_channel(self):
        output = io.StringIO()
        sys.stdout = output
        data_object = 'channel'
        parse_dict(self.null, data_object)
        sys.stdout = sys.__stdout__
        self.assertEqual(output.getvalue(), "Error: Service Returned Null Channel Data\n")

    def test_parse_dict_null_soh(self):
        output = io.StringIO()
        sys.stdout = output
        data_object = 'acquired-channel-soh'
        parse_dict(self.null, data_object)
        sys.stdout = sys.__stdout__
        self.assertEqual(output.getvalue(), "Error: Service Returned Null Acquired-Channel-Soh Data\n")

    def test_parse_dict_null_qc_mask(self):
        output = io.StringIO()
        sys.stdout = output
        data_object = 'qc-mask'
        parse_dict(self.null, data_object)
        sys.stdout = sys.__stdout__
        self.assertEqual(output.getvalue(), "Error: Service Returned Null Qc-Mask Data\n")

    def test_parse_dict_wrong_object(self):
        output = io.StringIO()
        sys.stdout = output
        data_object = 'wrong-object'
        parse_dict(self.null, data_object)
        sys.stdout = sys.__stdout__
        self.assertEqual(output.getvalue(), "Error: parse_dict(): data_object not found\n")

    def test_msgpack(self):
        msg = encode_msgpack(os.path.dirname(__file__) + '/channel_mock.json')
        decode = decode_msgpack(msg)
        self.assertEqual(self.channel, decode)


if __name__ == '__main__':
    unittest.main()
