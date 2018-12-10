"""
Station data object
"""


class Station:
    """
    Python Station COI object that replicates Station.java
    """

    def __init__(self, name_, station_type, latitude_, longitude_, elevation_):
        """
        Define a class to represent a GMS remote monitoring station

        :param name_: The stations assigned name
        :param station_type: The type of station
        :param latitude_: The latitude in WGS84 projection
        :param longitude_: The longitude in WGS84 projection
        :param elevation_: Elevation in meters
        """

        self.name = name_
        self.station_type = station_type
        self.latitude = latitude_
        self.longitude = longitude_
        self.elevation = elevation_
