"""
Waveform COI data object
"""


class Waveform:
    """
    Python Waveform object that replicates Waveform.java
    """

    def __init__(self, starttime, endtime, sample_rate, sample_count, values):
        """
        Creates a waveform

        :param starttime: The start time of the waveform
        :param endtime: The endtime of the waveform
        :param sample_rate: The sample rate (a measurement of how many data points there are per unittime)
        :param sample_count: How many total samples there are in this Waveform.
        :param values: The data points of this Waveform.
        """

        self.start_time = starttime
        self.end_time = endtime
        self.sample_rate = sample_rate
        self.sample_count = sample_count
        self.values = values
