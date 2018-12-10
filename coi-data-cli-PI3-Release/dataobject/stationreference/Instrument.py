"""
Instrument data object
"""
from dataobject.provenance.CreationInformation import CreationInformation


class Instrument:
    """
    Python Instrument COI object that replicates Instrument.java
    """

    def __init__(self, instrument_model, creation_info):
        """
        Creates a new Instrument object
        :param instrument_model: Instrument model
        :param creation_info: Metadata about when this object was created and by what/whom.
        """

        self.instrument_model = instrument_model
        self.creation_info = CreationInformation(creation_info['creationTime'], creation_info['creatorName'],
                                                 creation_info['softwareInfo'])
