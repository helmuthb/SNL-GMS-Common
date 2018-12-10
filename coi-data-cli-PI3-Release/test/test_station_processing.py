"""
Tests all Station Processing Objects
"""
from dataobject.stationprocessing.QcMask import QcMask
from dataobject.stationprocessing.QcMaskVersionReference import QcMaskVersionReference
from dataobject.stationprocessing.QcMaskVersion import QcMaskVersion
import unittest


class TestStationProcessing(unittest.TestCase):
    """
    Test class
    """

    def setUp(self):
        software_info = {'name': 'GMS', 'version': '1.0'}

        self.processing_channel_id = 'processingChannelId'
        self.qc_mask_id = '000'
        self.qc_mask_version_id = '1'
        self.qcMaskVersionReference = {'qcMaskId': self.qc_mask_id, 'qcMaskVersionId': self.qc_mask_version_id}
        self.id_ = '1.0'
        self.parent_qc_masks = [self.qcMaskVersionReference]
        self.channel_segment_ids = ['A', 200]
        self.qc_mask_category = 'SOH'
        self.qc_mask_type = 'STATION_SECURITY'
        self.rationale = 'Test Rationale'
        self.start_time = '10:02:01.000'
        self.end_time = '10:02:01.000'
        self.creation_info = {'creationTime': '10:03:02', 'creatorName': 'Jessica',
                              'softwareInfo': software_info}
        self.qcMaskVersion = [
            {'version': self.id_, 'parentQcMasks': self.parent_qc_masks, 'channelSegmentIds': self.channel_segment_ids,
             'category': self.qc_mask_category, 'type': self.qc_mask_type, 'rationale': self.rationale,
             'startTime': self.start_time, 'endTime': self.end_time, 'creationInfoId': 'test'}]

    def test_qc_mask_version_reference(self):
        version_ref = QcMaskVersionReference(self.qc_mask_id, self.qc_mask_version_id)

        self.assertEqual(version_ref.qc_mask_id, self.qc_mask_id)
        self.assertEqual(version_ref.qc_mask_version_id, self.qc_mask_version_id)

    def test_qc_mask_version(self):
        version = QcMaskVersion(self.id_, self.parent_qc_masks, self.channel_segment_ids, self.qc_mask_category,
                                self.qc_mask_type, self.rationale, self.start_time, self.end_time, self.creation_info)

        self.assertEqual(version.version, self.id_)
        self.assertEqual(version.channel_segment_ids, self.channel_segment_ids)
        self.assertEqual(version.qc_mask_category, self.qc_mask_category)
        self.assertEqual(version.qc_mask_type, self.qc_mask_type)
        self.assertEqual(version.rationale, self.rationale)
        self.assertEqual(version.start_time, self.start_time)
        self.assertEqual(version.end_time, self.end_time)

    def test_qc_mask(self):
        qc_mask = QcMask(self.id_, self.processing_channel_id, self.qcMaskVersion)

        self.assertEqual(qc_mask.id, self.id_)
        self.assertEqual(qc_mask.processing_channel_id, self.processing_channel_id)


if __name__ == '__main__':
    unittest.main()
