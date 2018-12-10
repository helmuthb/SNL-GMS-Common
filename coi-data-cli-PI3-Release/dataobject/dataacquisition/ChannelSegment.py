"""
Channel Segment COI data object
"""
from dataobject.dataacquisition.Waveform import Waveform
from dataobject.provenance.CreationInformation import CreationInformation


class ChannelSegment:
    """
    Python ChannelSegment object that replicates ChannelSegment.java
    """

    def __init__(self, id_, processing_channel_id, name, channel_segment_type, starttime, endtime, waveforms,
                 creation_info):
        """
        Creates a ChannelSegment.

        :param id_:
        :param processing_channel_id: the id of the processing channel the segment is from
        :param name: Name of the ChannelSegment
        :param channel_segment_type: The type of the ChannelSegment, e.g. ChannelSegmentType.RAW
        :param starttime: Start time for the ChannelSegment
        :param endtime: End time for the ChannelSegment
        :param waveforms: The waveform representing the data of the ChannelSegment
        :param creation_info: metadata about when this object was created and by what/whom
        """
        self.id = id_
        self.processing_channel_id = processing_channel_id
        self.name = name
        self.channel_segment_type = channel_segment_type
        self.start_time = starttime
        self.end_time = endtime
        self.waveform = [_dict2waveform(w) for w in waveforms]
        self.creation_info = CreationInformation(creation_info['creationTime'], creation_info['creatorName'],
                                                 creation_info['softwareInfo'])


def _dict2waveform(waveform):
    """
    Helper function to handle ChannelSegments with multiple waveforms.
    :param waveform: waveform information stored in the json
    :return: a dictionary of the waveform information.
    """
    return Waveform(waveform['startTime'], waveform['endTime'], waveform['sampleRate'],
                    waveform['sampleCount'], waveform['values'])
