"""
Tests all DataAcquisition COI objects
"""
import unittest
from dataobject.dataacquisition.AcquiredChannelSoh import AcquiredChannelSoh
from dataobject.dataacquisition.ChannelSegment import ChannelSegment
from dataobject.dataacquisition.Waveform import Waveform


class TestDataAcquisition(unittest.TestCase):
    """
    Test class
    """

    def setUp(self):
        software_info = {'name': 'GMS', 'version': '1.0'}

        self.processing_channel_id = '000'
        self.acquired_channel_soh_type = 'STATION_SECURITY'
        self.start_time = '12:03:01.000'
        self.end_time = '12:03:05.000'
        self.status_type = True
        self.creation_info = {'creationTime': '10:03:02', 'creatorName': 'Jessica',
                              'softwareInfo': software_info}
        self.id_ = '000'
        self.name = "HZE"
        self.channel_segment_type = "RAW"
        self.sample_rate = 5
        self.sample_count = 3
        self.values = [1, 2, 3]
        self.waveform = [{'startTime': self.start_time, 'endTime': self.end_time, 'sampleRate': self.sample_rate,
                          'sampleCount': self.sample_count, 'values': self.values}]

    def test_acquired_channel_soh(self):
        soh = AcquiredChannelSoh(self.processing_channel_id, self.acquired_channel_soh_type, self.start_time,
                                 self.end_time,
                                 self.status_type, self.creation_info)
        self.assertEqual(soh.processing_channel_id, self.processing_channel_id)
        self.assertEqual(soh.acquired_channel_soh_type, self.acquired_channel_soh_type)
        self.assertEqual(soh.start_time, self.start_time)
        self.assertEqual(soh.end_time, self.end_time)
        self.assertEqual(soh.status_type, self.status_type)

    def test_waveform(self):
        waveform = Waveform(self.start_time, self.end_time, self.sample_rate, self.sample_count, self.values)
        self.assertEqual(waveform.start_time, self.start_time)
        self.assertEqual(waveform.end_time, self.end_time)
        self.assertEqual(waveform.sample_rate, self.sample_rate)
        self.assertEqual(waveform.sample_count, self.sample_count)
        self.assertEqual(waveform.values, self.values)

    def test_channel_segment(self):
        chan_seg = ChannelSegment(self.id_, self.processing_channel_id, self.name, self.channel_segment_type,
                                  self.start_time,
                                  self.end_time,
                                  self.waveform,
                                  self.creation_info)

        self.assertEqual(chan_seg.id, self.id_)
        self.assertEqual(chan_seg.processing_channel_id, self.processing_channel_id)
        self.assertEqual(chan_seg.name, self.name)
        self.assertEqual(chan_seg.channel_segment_type, self.channel_segment_type)
        self.assertEqual(chan_seg.start_time, self.start_time)
        self.assertEqual(chan_seg.end_time, self.end_time)


if __name__ == '__main__':
    unittest.main()
