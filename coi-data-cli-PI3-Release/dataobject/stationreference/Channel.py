"""
Channel data object
"""
from dataobject.stationreference.Site import Site
from dataobject.stationreference.Instrument import Instrument
from dataobject.stationreference.Calibration import Calibration


class Channel:
    """
    Python Channel COI object that replicates Channel.java
    """

    def __init__(self, name_, channel_type, sites_, instruments_, calibration_):
        """
        Create an instance of a channel

        :param name_: The channel name
        :param channel_type: The type of channel.
        :param sites_: Site object containing site information
        :param instruments_: Instrument object with instrument information
        :param calibration_: Calibration object with calibration information
        """

        self.name = name_
        self.channel_type = channel_type
        self.sites = Site(sites_['name'], sites_['station'], sites_['channels'])
        self.instruments = Instrument(instruments_['instrumentModel'], instruments_['creationInfo'])
        self.calibration = Calibration(calibration_['factor'], calibration_['period'],
                                       calibration_['creationInfo'])
