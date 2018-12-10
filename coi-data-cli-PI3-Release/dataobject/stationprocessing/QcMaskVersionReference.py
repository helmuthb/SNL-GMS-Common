"""
QcMaskVersionReference data object
"""


class QcMaskVersionReference:
    """
    Python QcMaskVersionReference COI that replicates QcMaskVersionReference.java
    """

    def __init__(self, uuid_identity, long):
        """
        Creates a QcMaskVersionReference Object for parent qc masks

        :param uuid_identity: parent qc mask id
        :param long_identity: parent qc mask version id
        """
        self.qc_mask_id = uuid_identity
        self.qc_mask_version_id = long
