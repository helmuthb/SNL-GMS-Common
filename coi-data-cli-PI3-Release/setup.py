from setuptools import setup, find_packages
import os

'To rebuild, run python setup.py sdist'

VERSION = '1.3.11'


def resolve_version():
    """
    Finds version number
    :return:
    """

    global VERSION
    if "BUILD_NUMBER" in os.environ.keys():
        print(os.environ["BUILD_NUMBER"])
        VERSION += ".{}".format(os.environ["BUILD_NUMBER"])

    return VERSION


setup(name='GMSpy',
      version=resolve_version(),
      description='A python interface for GMS',
      packages=find_packages(),
      scripts=['coi_data_service_cli.py'],
      install_requires=['obspy',
                        'requests',
                        'requests_mock',
                        'pandas',
                        'msgpack-python',
                        'pyyaml',
                        'termcolor',
                        'pandas',
                        'numpy'],
      python_requires='>=3')
