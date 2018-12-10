"""
Tests CoiServiceCall.py
"""
import unittest
import os
import yaml

import requests_mock

from util.CoiServiceCall import set_params, http_request, format_time
from util.ReadData import read_json

with open(os.path.dirname(__file__) + "/../util/config.yaml", 'r') as ymlfile:
    cfg = yaml.load(ymlfile)

url_signal = cfg['url_signal_repo']
url_waveforms = cfg['url_waveform_repo']


class TestCoiService(unittest.TestCase):
    """
    Test class
    """

    @requests_mock.Mocker()
    def test_channel_segment_url(self, m):
        channelid = 100
        starttime = 200
        endtime = 300
        waveform = 'true'
        sohtype = 'boolean'

        params = set_params(channelid, starttime, endtime, waveform, sohtype)

        input_url = url_waveforms + "channel-segment?channel-id=100&start-time=200&end-time=300&with-waveforms=true"

        m.get(input_url)
        output_url = http_request(None, 'channel-segment', params).url
        self.assertEqual(input_url, output_url)

    @requests_mock.Mocker()
    def test_channel_segment_service_response(self, m):
        mock_json = str(read_json(os.path.dirname(__file__) + '/channel_segment_mock.json'))

        channelid = 100
        starttime = 200
        endtime = 300
        waveform = 'true'
        sohtype = 'boolean'

        params = set_params(channelid, starttime, endtime, waveform, sohtype)

        input_url = url_waveforms + "channel-segment?channel-id=100&start-time=200&end-time=300&with-waveforms=true"

        m.get(input_url, text=mock_json)
        resp = http_request(None, 'channel-segment', params).text
        self.assertEqual(resp, mock_json)

    @requests_mock.Mocker()
    def test_channel_soh_url(self, m):
        channelid = 100
        starttime = 200
        endtime = 300
        waveform = 'true'
        sohtype = 'boolean'

        params = set_params(channelid, starttime, endtime, waveform, sohtype)

        input_url = url_waveforms + "acquired-channel-soh/boolean?channel-id=100&start-time=200&end-time=300"

        m.get(input_url)
        output_url = http_request(None, 'acquired-channel-soh', params).url
        self.assertEqual(input_url, output_url)

    @requests_mock.Mocker()
    def test_channel_soh_service_response(self, m):
        mock_json = str(read_json(os.path.dirname(__file__) + '/channel_segment_mock.json'))

        channelid = 100
        starttime = 200
        endtime = 300
        waveform = 'true'
        sohtype = 'boolean'

        params = set_params(channelid, starttime, endtime, waveform, sohtype)

        input_url = url_waveforms + "acquired-channel-soh/boolean?channel-id=100&start-time=200&end-time=300"

        m.get(input_url, text=mock_json)
        resp = http_request(None, 'acquired-channel-soh', params).text
        self.assertEqual(resp, mock_json)

    @requests_mock.Mocker()
    def test_qc_mask_url(self, m):
        channelid = 100
        starttime = 200
        endtime = 300

        params = set_params(channelid, starttime, endtime)

        input_url = url_signal + 'qc-mask?channel-id=100&start-time=200&end-time=300'

        m.get(input_url)
        output_url = http_request(None, 'qc-mask', params).url
        self.assertEqual(input_url, output_url)

    @requests_mock.Mocker()
    def test_qc_mask_response(self, m):
        mock_json = str(read_json(os.path.dirname(__file__) + '/qc_mask_mock.json'))

        channelid = 100
        starttime = 200
        endtime = 300

        params = set_params(channelid, starttime, endtime)

        input_url = url_signal + 'qc-mask?channel-id=100&start-time=200&end-time=300'

        m.get(input_url, text=mock_json)
        resp = http_request(None, 'qc-mask', params).text
        self.assertEqual(resp, mock_json)

    @requests_mock.Mocker()
    def test_frames_response(self, m):
        mock_json = str(read_json(os.path.dirname(__file__) + '/frame_mock.json'))

        starttime = '1970-01-02T03:04:05.123Z'
        endtime = '1970-01-02T03:04:07.123Z'

        params = set_params(starttime, endtime)

        input_url = url_waveforms + 'frames'
        print("input_url = " + input_url)

        m.get(input_url, text=mock_json)
        resp = http_request(None, 'frames', params).text
        self.assertEqual(resp, mock_json)


    def test_format_time(self):
        time = '12:10:40.000'
        date = '01/29/2015'
        d = format_time(time, date)
        self.assertEqual(d, "2015-01-29T12:10:40.000Z")


def http_mock_request(data_object):
    """
    Makes a mock request call and returns a dict formatted similar to a json file.
    Used for testing only.

    :param data_object: the data object that is being called (channelSoh, channel, channel-segment)
    :return: mocked data in a dictionary
    """

    if data_object == 'channel':
        mock_chan = read_json(os.path.dirname(__file__) + '/channel_mock.json')

        return mock_chan

    elif data_object == 'channel-segment':
        mock_chan_seg = read_json(os.path.dirname(__file__) + '/channel_segment_mock.json')

        return mock_chan_seg

    elif data_object == 'acquired-channel-soh':
        mock_chan_soh = read_json(os.path.dirname(__file__) + '/acquired_channel_soh_mock.json')

        return mock_chan_soh

    elif data_object == 'qc-mask':
        mock_qcmask = read_json(os.path.dirname(__file__) + '/qc_mask_mock.json')

        return mock_qcmask
    elif data_object == 'frame':
        mock_frame = read_json(os.path.dirname(__file__) + '/frame_mock.json')
        return mock_frame

    else:
        print('Error: http_mock_request(): data_object is not found')


if __name__ == '__main__':
    unittest.main()
