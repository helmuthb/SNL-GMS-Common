"""
Creation Information object
"""
from dataobject.provenance.SoftwareComponentInfo import SoftwareComponentInfo


class CreationInformation:
    """
    Python Creation Information COI that replicates CreationInfo.java
    """

    def __init__(self, creation_time, creator_name, software_component_info):
        """
        Represents information about how an object was created, such as when and by who/what. See section 2.2.2
        'Creation Info' of the GMS Conceptual Data Model document

        :param creation_time: The name of the entity creating the associated object.
        :param creator_name: The time the associated object was created
        :param software_component_info: The SoftwareComponentInfo object.
        """

        self.creation_time = creation_time
        self.creation_name = creator_name
        self.software_component_info = SoftwareComponentInfo(software_component_info['name'],
                                                             software_component_info['version'])
