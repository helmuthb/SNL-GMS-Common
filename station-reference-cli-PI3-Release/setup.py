# -*- coding: utf-8 -*-
# setup.py
# Last update: 04/09/2018

from setuptools import setup, find_packages

'''
To rebuild, run python setup.py
'''

VERSION = '1.0'

setup(name='station-reference-CLI',
      version=VERSION,
      description='A command line application to retrieve station reference information.',
      packages=find_packages(),
      scripts=['stationreferenceCLI.py'],
      install_requires=['pandas',
                        'requests'
                        'responses'],
      python_requires='>=3')
