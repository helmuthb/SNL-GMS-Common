# -*- coding: utf-8 -*-
import requests
from requests.compat import urljoin
import sys

'''
json_response
get the JSON response from the endpoint
@ param url: the endpoint to query for response content
@ param pload: the payload dictionary with start and end params
requests library constructs urls based on payload
returns the json file
'''


def json_response(url, pload):

    if pload is not None:
        try:
            r = requests.get(url, params=pload)
            print('Requesting ' + r.url)
        except requests.ConnectionError as e:
            print("Unable to connect to the URL.")
            sys.exit(0)  # exit
    else:
        try:
            r = requests.get(url)
            print('Requesting ' + r.url)
        except requests.ConnectionError as e:
            print("Unable to connect to the URL.")
            sys.exit(0)
    try:
        obj = r.json()
    except ValueError:
        print("Invalid JSON file.")
        raise
    return obj


'''
@ param hostname
@ param mode
construct_url
'''


def construct_base_url(hostname, mode):
    url = hostname + '/mechanisms/object-storage-distribution/station-reference/' + mode
    if not url.startswith('http://'):
        return 'http://' + url
    else:
        return url


'''
@ param start
@ param end
@ param hostname
retrieve_networks
'''


def retrieve_networks(start, end, hostname):
    mode = 'networks'
    url = construct_base_url(hostname, mode)
    p = {'start-time': start, 'end-time': end}
    return json_response(url, p)


'''
retrieve_stations
'''


def retrieve_stations(network, start, end, hostname):
    mode = 'stations'
    url = construct_base_url(hostname, mode)
    p = {'network-name': network, 'start-time': start, 'end-time': end}
    return json_response(url, p)


'''
retrieve_sites
'''


def retrieve_sites(station, start, end, hostname):
    mode = 'sites'
    url = construct_base_url(hostname, mode)
    p = {'station-name': station, 'start-time': start, 'end-time': end}
    return json_response(url, p)


'''
retrieve_digitizers
'''


def retrieve_digitizers(channel_id, start, end, hostname):
    mode = 'digitizers'
    url = construct_base_url(hostname, mode)
    p = {'channel-id': channel_id, 'start-time': start, 'end-time': end}
    return json_response(url, p)


'''
retrieve_channels
'''


def retrieve_channels(site, start, end, hostname):
    mode = 'channels'
    url = construct_base_url(hostname, mode)
    p = {'site-name': site, 'start-time': start, 'end-time': end}
    return json_response(url, p)


'''
retrieve_calibrations
'''


def retrieve_calibrations(channel_id, start, end, hostname):
    mode = 'calibrations'
    url = construct_base_url(hostname, mode)
    p = {'channel-id': channel_id, 'start-time': start, 'end-time': end}
    return json_response(url, p)


'''
retrieve_sensors
'''


def retrieve_sensors(channel_id, start, end, hostname):
    mode = 'sensors'
    url = construct_base_url(hostname, mode)
    p = {'channel-id': channel_id, 'start-time': start, 'end-time': end}
    return json_response(url, p)


'''
retrieve_responses
'''


def retrieve_responses(channel_id, start, end, hostname):
    mode = 'responses'
    url = construct_base_url(hostname, mode)
    p = {'channel-id': channel_id, 'start-time': start, 'end-time': end}
    return json_response(url, p)



'''
retrieve_networks_by_id
'''


def retrieve_networks_by_id(network_id, start, end, hostname):
    mode = 'networks'
    url = construct_base_url(hostname, mode) + '/id/' + str(network_id)
    p = {'start-time': start, 'end-time': end}  # default to no extra params and change below if necessary
    return json_response(url, p)


'''
retrieve_channels_by_id
'''


def retrieve_channels_by_id(channel_id, start, end, hostname):
    mode = 'channels'
    url = construct_base_url(hostname, mode) + '/id/' + str(channel_id)
    p = {'start-time': start, 'end-time': end}  # default to no extra params and change below if necessary
    return json_response(url, p)

'''
retrieve_stations_by_id
'''


def retrieve_stations_by_id(station_id, start, end, hostname):
    mode = 'stations'
    url = construct_base_url(hostname, mode) + '/id/' + str(station_id)
    p = {'start-time': start, 'end-time': end}  # default to no extra params and change below if necessary
    return json_response(url, p)

'''
retrieve_sites_by_channel
'''


def retrieve_sites_by_channel(channel_id, start, end, hostname):
    mode = 'sites'
    url = construct_base_url(hostname, mode)
    p = {'channel-id': channel_id, 'start-time': start, 'end-time': end}
    return json_response(url, p)


'''
retrieve_station_members
'''


def retrieve_station_members(siteEntityId, start, end, hostname):
    mode = 'station-memberships'
    url = construct_base_url(hostname, mode)
    p = {'site-id': siteEntityId, 'start-time': start, 'end-time': end}
    return json_response(url, p)


'''
retrieve_network_members
'''


def retrieve_network_members(stationEntityId, start, end, hostname):
    mode = 'network-memberships'
    url = construct_base_url(hostname, mode)
    p = {'station-id': stationEntityId, 'start-time': start, 'end-time': end}
    return json_response(url, p)



