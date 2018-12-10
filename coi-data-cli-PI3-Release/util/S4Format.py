"""
Functions for reading and writing s4 waveforms
"""
import struct


def write(filepath, values):
    """
    Write out an array of integers in S4 binary format to a file

    :param filepath: the string file path, can be absolute or relative
    :param values: the array of integer values to write out
    """
    if len(values) != 0:
        v = [int(v) for v in values]
        s = struct.pack('>' + 'l' * len(v), *v)
        with open(filepath, 'wb') as file:
            file.write(s)
    else:
        print("Error s4Format write(): Null waveform values")


def read(filepath):
    """
    Read in a file in S4 binary format and return the resulting tuple of longs

    :param filepath: the string file path for the file to read in
    """

    with open(filepath, 'rb') as file:
        bs = file.read()
        num_values = len(bs) // struct.calcsize('>l')
        return struct.unpack('>' + 'l' * num_values, bs)
