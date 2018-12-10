"""
Calibration data object
"""
from dataobject.provenance.CreationInformation import CreationInformation


class Calibration:
    """
    Python Calibration data object that replicates Calibration.java
    """

    def __init__(self, calibration_factor, calibration_period, creation_info):
        """
        Class to define calibration specifications associated with an instrument.

        :param calibration_factor: The calibration factor in nm/count.
        :param calibration_period:  The calibration period in seconds
        :param creation_info: metadata about when this object was created and by what/whom.
        """

        self.calibration_factor = calibration_factor
        self.calibration_period = calibration_period
        self.creation_info = CreationInformation(creation_info['creationTime'], creation_info['creatorName'],
                                                 creation_info['softwareInfo'])
