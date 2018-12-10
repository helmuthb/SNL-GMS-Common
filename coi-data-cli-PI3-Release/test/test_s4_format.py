"""
Tests S4format.py functions
"""
import unittest
import os
import io
import sys
import util.S4Format as s4


class TestS4Format(unittest.TestCase):
    """
    Test class
    """

    def test_read_write(self):
        values = [1, 2, 3, 4, 5]
        s4.write("test.w", values)
        results = s4.read("test.w")
        self.assertListEqual(values, list(results))
        os.remove("test.w")

    def test_null_waveforms(self):
        values = []
        output = io.StringIO()
        sys.stdout = output
        s4.write("test.w", values)
        sys.stdout = sys.__stdout__
        self.assertEqual(output.getvalue(), 'Error s4Format write(): Null waveform values\n')


if __name__ == '__main__':
    unittest.main()
