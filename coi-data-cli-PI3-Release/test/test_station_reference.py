"""
Tests all station processing objects
"""
from dataobject.stationreference.Calibration import Calibration
from dataobject.stationreference.Channel import Channel
from dataobject.stationreference.Instrument import Instrument
from dataobject.stationreference.Site import Site
from dataobject.stationreference.Station import Station
import unittest


class TestStationReference(unittest.TestCase):
    """
    Test class
    """

    def setUp(self):
        software_info = {'name': 'GMS', 'version': '1.0'}
        self.name = 'HZE'
        self.station_type = 'Infrasound'
        self.latitude = 1000
        self.longitude = 5000
        self.elevation = 1000
        self.instrument_model = 'TI-89'
        self.channels = 'channel'
        self.calibration_factor = 5
        self.calibration_period = 25
        self.channel_type = 'type'
        self.creation_info = {'creationTime': '10:03:02', 'creatorName': 'Jessica',
                              'softwareInfo': software_info}
        self.station = {'name': self.name, 'stationType': self.station_type, 'latitude': self.latitude,
                        'longitude': self.longitude, 'elevation': self.elevation}
        self.sites = {'name': self.name, 'station': self.station, 'channels': self.channels}
        self.instruments = {'instrumentModel': self.instrument_model, 'creationInfo': self.creation_info}
        self.calibration = {'factor': self.calibration_factor, 'period': self.calibration_period,
                            'creationInfo': self.creation_info}

    def test_station(self):
        sta = Station(self.name, self.station_type, self.latitude, self.longitude, self.elevation)

        self.assertEqual(sta.name, self.name)
        self.assertEqual(sta.station_type, self.station_type)
        self.assertEqual(sta.latitude, self.latitude)
        self.assertEqual(sta.longitude, self.longitude)
        self.assertEqual(sta.elevation, self.elevation)

    def test_instrument(self):
        ins = Instrument(self.instrument_model, self.creation_info)

        self.assertEqual(ins.instrument_model, self.instrument_model)

    def test_site(self):
        site = Site(self.name, self.station, self.channels)

        self.assertEqual(site.name, self.name)
        self.assertEqual(site.channels, self.channels)

    def test_calibration(self):
        calib = Calibration(self.calibration_factor, self.calibration_period, self.creation_info)

        self.assertEqual(calib.calibration_period, self.calibration_period)
        self.assertEqual(calib.calibration_factor, self.calibration_factor)

    def test_channel(self):
        chan = Channel(self.name, self.channel_type, self.sites, self.instruments, self.calibration)

        self.assertEqual(chan.name, self.name)
        self.assertEqual(chan.channel_type, self.channel_type)


if __name__ == '__main__':
    unittest.main()
