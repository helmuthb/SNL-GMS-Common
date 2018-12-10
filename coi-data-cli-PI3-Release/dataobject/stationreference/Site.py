"""
Station data object
"""
from .Station import Station


class Site:
    """
    Python Site COI object that replicates Site.java
    """

    def __init__(self, name_, station_, channels_):
        """
        A class to represent a GMS site within a station

        :param name_: name of site
        :param station_: Station object
        :param channels_: Channels available at site
        """

        self.name = name_
        self.station = Station(station_['name'], station_['stationType'], station_['latitude'], station_['longitude'],
                               station_['elevation'])
        self.channels = channels_
