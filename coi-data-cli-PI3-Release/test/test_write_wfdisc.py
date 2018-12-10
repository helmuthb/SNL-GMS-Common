"""
Tests all WriteWfdisc.py functions
"""
import unittest
import os
from util.ReadData import read_json, parse_dict
from util.WriteWfdisc import date2jul, date2epoch, format_wfdisc, read_wfdisc, write_wfdisc


class TestWriteWfdisc(unittest.TestCase):
    """
    Test class
    """

    def setUp(self):
        self.date = '1969-12-31T17:00:00Z'
        self.channel = read_json(os.path.dirname(__file__) + '/channel_mock.json')
        self.channel_segment = read_json(os.path.dirname(__file__) + '/channel_segment_mock.json')
        self.chan_seg = parse_dict(self.channel_segment, 'channel-segment')
        self.chan = parse_dict(self.channel, 'channel')
        self.dfile_base = 'waveform'
        self.dir = os.path.dirname(__file__)
        self.wfdisc = 'test.wfdisc'
        self.wfdisc_wav = 'test_one_wave.wfdisc'
        self.chan_seg_one_wv = parse_dict(self.channel_segment, 'channel-segment')
        self.chan_seg_one_wv.waveform = [self.chan_seg_one_wv.waveform[0]]

    def test_date2epoch(self):
        d = date2epoch(self.date)
        self.assertEqual(d, -25200)

    def test_date2jul(self):
        j = date2jul(self.date)
        self.assertEqual(j, 365)

    def test_wfdisc_with_one_wav(self):
        df = format_wfdisc(self.chan, self.chan_seg_one_wv, self.dfile_base, self.dir)
        write_wfdisc(self.chan, self.chan_seg_one_wv, self.dfile_base, self.dir, self.wfdisc_wav)
        df_read = read_wfdisc(os.path.dirname(self.dir) + '/' + self.wfdisc_wav)

        self.assertEqual(df['sta'][0], df_read['sta'][0])
        self.assertEqual(df['chan'][0], df_read['chan'][0])
        self.assertAlmostEqual(float(df['time'][0]), df_read['time'][0])
        self.assertEqual(float(df['wfid'][0]), df_read['wfid'][0])
        self.assertEqual(float(df['chanid'][0]), df_read['chanid'][0])
        self.assertEqual(float(df['jdate'][0]), df_read['jdate'][0])
        self.assertAlmostEqual(float(df['endtime'][0]), df_read['endtime'][0])
        self.assertEqual(float(df['nsamp'][0]), df_read['nsamp'][0])
        self.assertAlmostEqual(float(df['samprate'][0]), df_read['samprate'][0])
        self.assertAlmostEqual(float(df['calib'][0]), df_read['calib'][0])
        self.assertAlmostEqual(float(df['calper'][0]), df_read['calper'][0])
        self.assertEqual(df['instype'][0], df_read['instype'][0])
        self.assertEqual(df['segtype'][0], df_read['segtype'][0])
        self.assertEqual(df['datatype'][0], df_read['datatype'][0])
        self.assertEqual(df['clip'][0], df_read['clip'][0])
        self.assertEqual(df['dfile'][0], df_read['dfile'][0])
        self.assertEqual(df['dir'][0], df_read['dir'][0])
        self.assertEqual(float(df['foff'][0]), df_read['foff'][0])
        self.assertEqual(float(df['commid'][0]), df_read['commid'][0])
        os.remove(os.path.dirname(self.dir) + '/' + self.wfdisc_wav)

    def test_wfdisc(self):
        df = format_wfdisc(self.chan, self.chan_seg, self.dfile_base, self.dir)
        write_wfdisc(self.chan, self.chan_seg, self.dfile_base, self.dir, self.wfdisc)
        df_read = read_wfdisc(os.path.dirname(self.dir) + '/' + self.wfdisc)
        for i in range(len(df)):
            self.assertEqual(df['sta'][i], df_read['sta'][i])
            self.assertEqual(df['chan'][i], df_read['chan'][i])
            self.assertAlmostEqual(float(df['time'][i]), df_read['time'][i])
            self.assertEqual(float(df['wfid'][i]), df_read['wfid'][i])
            self.assertEqual(float(df['chanid'][i]), df_read['chanid'][i])
            self.assertEqual(float(df['jdate'][i]), df_read['jdate'][i])
            self.assertAlmostEqual(float(df['endtime'][i]), df_read['endtime'][i])
            self.assertEqual(float(df['nsamp'][i]), df_read['nsamp'][i])
            self.assertAlmostEqual(float(df['samprate'][i]), df_read['samprate'][i])
            self.assertAlmostEqual(float(df['calib'][i]), df_read['calib'][i])
            self.assertAlmostEqual(float(df['calper'][i]), df_read['calper'][i])
            self.assertEqual(df['instype'][i], df_read['instype'][i])
            self.assertEqual(df['segtype'][i], df_read['segtype'][i])
            self.assertEqual(df['datatype'][i], df_read['datatype'][i])
            self.assertEqual(df['clip'][i], df_read['clip'][i])
            self.assertEqual(df['dfile'][i], df_read['dfile'][i])
            self.assertEqual(df['dir'][i], df_read['dir'][i])
            self.assertEqual(float(df['foff'][i]), df_read['foff'][i])
            self.assertEqual(float(df['commid'][i]), df_read['commid'][i])
        os.remove(os.path.dirname(self.dir) + '/' + self.wfdisc)


if __name__ == '__main__':
    unittest.main()
