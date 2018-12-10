"""
QcMask data object
"""
from dataobject.stationprocessing.QcMaskVersion import QcMaskVersion


class QcMask:
    """
    Python QcMask COI that replicates QcMask.java
    """

    def __init__(self, id_, processing_channel_id, qc_mask_versions):
        """
        Creates a QcMask object
        :param id_: qc mask id
        :param processing_channel_id: processing channel id
        :param qc_mask_versions: qc mask versions
        """
        self.id = id_
        self.processing_channel_id = processing_channel_id
        self.qc_mask_versions = [_dict2qcmask(q) for q in qc_mask_versions]


def _dict2qcmask(qc_mask_versions):
    """
    Helper function to handle multiple qc mask versions
    :param qc_mask_versions: qc mask versions
    :return: a dictionary of qc mask version information
    """
    return QcMaskVersion(qc_mask_versions['version'],
                         qc_mask_versions['parentQcMasks'],
                         qc_mask_versions['channelSegmentIds'],
                         qc_mask_versions['category'],
                         qc_mask_versions['type'],
                         qc_mask_versions['rationale'],
                         qc_mask_versions['startTime'],
                         qc_mask_versions['endTime'],
                         qc_mask_versions['creationInfoId'])
