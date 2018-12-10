"""
Tests all provenance objects
"""
from dataobject.provenance.SoftwareComponentInfo import SoftwareComponentInfo
from dataobject.provenance.CreationInformation import CreationInformation
import unittest


class TestProvenance(unittest.TestCase):
    """
    Test class
    """

    def setUp(self):
        software_name = 'GMS'
        software_version = '1.0'

        self.software_info = {'name': software_name,
                              'version': software_version}
        self.creation_time = '10:01:05.000'
        self.creation_name = 'Jessica'

    def test_software_component_information(self):
        obj = SoftwareComponentInfo(self.software_info['name'], self.software_info['version'])
        self.assertEqual(obj.name, self.software_info['name'])
        self.assertEqual(obj.version, self.software_info['version'])

    def test_creation_information(self):
        obj = CreationInformation(self.creation_time, self.creation_name, self.software_info)
        self.assertEqual(obj.creation_name, self.creation_name)
        self.assertEqual(obj.creation_time, self.creation_time)


if __name__ == '__main__':
    unittest.main()
