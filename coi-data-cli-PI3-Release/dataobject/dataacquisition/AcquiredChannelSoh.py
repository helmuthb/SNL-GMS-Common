"""
Acquired Channel SOH COI object
"""

from dataobject.provenance.CreationInformation import CreationInformation


class AcquiredChannelSoh:
    """
    AcquiredChannelSoh class which replicated AcquiredChannelSoh.java
    """

    def __init__(self, processing_channel_id, acquired_channel_soh_type, starttime, endtime, status_type,
                 creation_info):
        """
        Python COI object defined in section 2.4.5 of Data Model v2.1 to represent a piece
        of Station SOH data as received in a packet (such as from the CD-1.1 protocol)

        :param processing_channel_id: processing channel id
        :param acquired_channel_soh_type: channel SOH type
        :param starttime: channel segment start time
        :param endtime: channel segment end time
        :param status_type: status
        :param creation_info: creation information
        """
        self.processing_channel_id = processing_channel_id
        self.acquired_channel_soh_type = acquired_channel_soh_type
        self.start_time = starttime
        self.end_time = endtime
        self.status_type = status_type
        self.creation_info = CreationInformation(creation_info['creationTime'], creation_info['creatorName'],
                                                 creation_info['softwareInfo'])
