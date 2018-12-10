"""
QcMaskVersion data object
"""
from dataobject.stationprocessing.QcMaskVersionReference import QcMaskVersionReference


class QcMaskVersion:
    """
    Python QcMaskVersion COI objects that replicates QcMaskVersion.java
    """

    def __init__(self, version, parent_qc_masks, uuid_identity, qc_mask_category, qc_mask_type,
                 rationale, starttime, endtime, creation_info_id):
        """
        Create a QcMaskVersion object

        :param version: mask version
        :param parent_qc_masks: parent qc mask metadata
        :param uuid_identity: identity
        :param qc_mask_category: qc mask category
        :param qc_mask_type: qc mask type
        :param rationale: rationale behind mask creation
        :param starttime: mask start time
        :param endtime: mask end time
        :param creation_info_id: creation information id
        """
        self.version = version
        self.parent_qc_masks = [_dict2parentmask(q) for q in parent_qc_masks]
        self.channel_segment_ids = [i for i in uuid_identity]
        self.qc_mask_category = qc_mask_category
        self.qc_mask_type = qc_mask_type
        self.rationale = rationale
        self.start_time = starttime
        self.end_time = endtime
        self.creation_info_id = creation_info_id


def _dict2parentmask(parent_qc_masks):
    """
    Helper function to handle multiple parent masks
    :param parent_qc_masks: parent qc mask info
    :return: a dictionary of parent qc mask metadata
    """
    return QcMaskVersionReference(parent_qc_masks['qcMaskId'], parent_qc_masks['qcMaskVersionId'])
