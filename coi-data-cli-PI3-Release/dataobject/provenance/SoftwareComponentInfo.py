"""
Software Component Info Object
"""


class SoftwareComponentInfo:
    """
    Python Software Component Info COI that replicates SoftwareComponentInfo.java
    """

    def __init__(self, name, version):
        """
        Defines a class to represent the software component which was used to create the associated information

        :param name: software name
        :param version: software version
        """

        self.name = name
        self.version = version
