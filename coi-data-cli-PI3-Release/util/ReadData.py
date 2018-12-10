"""
Functions for reading json, msgpack infomation into COI objects
"""
import json
import msgpack
from dataobject.dataacquisition.ChannelSegment import ChannelSegment
from dataobject.stationreference.Channel import Channel
from dataobject.dataacquisition.AcquiredChannelSoh import AcquiredChannelSoh
from dataobject.stationprocessing.QcMask import QcMask


def read_json(json_):
    """
    Reads a json object and converts it to a python dict

    :param json_: json object
    :return: python dictionary
    """

    with open(json_, 'r') as f:
        data = json.load(f)
    return data


def parse_dict(dict_object, data_object):
    """
    Parses the json information into the respective COI

    :param dict_object: a python dict of the json information
    :param data_object: the data object that is being called (channelSoh, channel, channel-segment)
    :return: COI object
    """

    if data_object == 'channel-segment':
        if len(dict_object) != 0:
            channel_segment = ChannelSegment(dict_object['id'],
                                             dict_object['processingChannelId'],
                                             dict_object['name'],
                                             dict_object['segmentType'],
                                             dict_object['startTime'],
                                             dict_object['endTime'],
                                             dict_object['waveforms'],
                                             dict_object['creationInfo'])
            return channel_segment
        else:
            print("Error: Service Returned Null Channel Segment Data")

    elif data_object == 'channel':
        if len(dict_object) != 0:
            channel = Channel(dict_object['name'],
                              dict_object['type'],
                              dict_object['sites'],
                              dict_object['instruments'],
                              dict_object['calibration'])
            return channel
        else:
            print("Error: Service Returned Null Channel Data")

    elif data_object == 'acquired-channel-soh':
        if len(dict_object) != 0:
            channelSoh = []
            for i in range(len(dict_object)):
                soh = AcquiredChannelSoh(dict_object[i]['processingChannelId'],
                                         dict_object[i]['type'],
                                         dict_object[i]['startTime'],
                                         dict_object[i]['endTime'],
                                         dict_object[i]['status'],
                                         dict_object[i]['creationInfo'])
                channelSoh.append(soh)
            return channelSoh
        else:
            print("Error: Service Returned Null Acquired-Channel-Soh Data")


    elif data_object == 'qc-mask':
        if len(dict_object) != 0:
            qcMasks = []
            for i in range(len(dict_object)):
                mask = QcMask(dict_object[i]['id'],
                              dict_object[i]['processingChannelId'],
                              dict_object[i]['qcMaskVersions'])
                qcMasks.append(mask)
            return qcMasks
        else:
            print("Error: Service Returned Null Qc-Mask Data")

    else:
        print('Error: parse_dict(): data_object not found')


def encode_msgpack(json_):
    """
    Encodes json to msg pack for testing purposes
    :param json_: json file
    :return: encoded message pack dict
    """
    dic = read_json(json_)
    return msgpack.packb(dic)


def decode_msgpack(msg_pack):
    """
    Decodes a message pack object into utf-8 string
    :param msg_pack: msg pack byte object
    :return: decoded python dict of strings.
    """
    return msgpack.unpackb(msg_pack, encoding='utf-8')
