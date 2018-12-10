"""
Functions to make service calls
"""
import requests
import yaml
import os
from util.ReadData import decode_msgpack
from collections import OrderedDict
with open(os.path.dirname(__file__) + "/config.yaml", 'r') as ymlfile:
    cfg = yaml.load(ymlfile)
global url_signal
global url_waveforms
url_signal = cfg['url_signal_repo']
url_waveforms = cfg['url_waveform_repo']

def format_time(time, date):
    """
    A function that formats the time input by the researcher to the time format needed by the query

    :param time: input time (GMT)
    :param date: input date (GMT)
    :return: a formated datetime object.
    """

    m = date.split('/')[0]
    d = date.split('/')[1]
    y = date.split('/')[2]

    datetime = y + '-' + m + '-' + d + 'T' + time + 'Z'
    return datetime


def set_params(channel_id=None, start_time=None, end_time=None, with_waveforms=None, soh_type=None, station_name=None):
    """
    A function that set the parameters for http request query

    :param channel_id: channel id (str)
    :param start_time: start time (str)
    :param end_time: endtime (str)
    :param with_waveforms: waveform (bool)
    :param soh_type: type of soh (bool)
    :param station_name: name of station (str)
    :return:
    """
    params = {
        'channel-id': channel_id,
        'start-time': start_time,
        'end-time': end_time,
        'with-waveforms': str(with_waveforms).lower(),
        'soh-type': soh_type,
        'station-name': station_name
    }

    return params


def http_request(wave, data_object, params, return_type=None):
    """
    Makes http request to service
    :param data_object:
    :param params:
    :param return_type:
    :return:
    """
    paths = [data_object]
    query_params = ['start-time', 'end-time']

    if data_object == 'acquired-channel-soh':
        paths.append(params['soh-type'])
        query_params = ['channel-id'] + query_params

    if data_object in 'channel':
        paths = paths + ['channel-id', params['channel-id']]
        query_params.append('with-waveforms')

    if data_object == 'channel-segment':
        query_params = ['channel-id'] + query_params
        query_params.append('with-waveforms')

    if data_object == 'qc-mask':
        query_params = ['channel-id'] + query_params

    if data_object == 'frames':
        query_params = ['station-name'] + query_params

    url = _make_url(url_waveforms, wave, paths, data_object)
    query_params = OrderedDict(
        {query_param: params[query_param] for query_param in query_params if params[query_param] is not None})

    headers = {} if return_type is None else {'Accept': 'application/' + return_type}
    resp = requests.get(url, query_params, headers=headers)

    if return_type == 'json':
        return resp.json()
    if return_type == 'msgpack':
        return decode_msgpack(resp.content)
    else:
        return resp


def _make_url(url_waveforms, wave, paths, data_object):
    """
    Makes the url used by http_request()

    :param paths: base url
    :return: formated url
    """

    str_paths = [str(path) for path in paths]

    if data_object == 'qc-mask':
        url = url_signal + '/'.join(str_paths)
    else:
        #data object is a channel segment
        if wave is not None:
            url_waveforms = wave

        url = url_waveforms + '/'.join(str_paths)
    return url
