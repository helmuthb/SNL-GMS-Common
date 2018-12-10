# -*- coding: utf-8 -*-
"""
Command Line Interface for GMS services in Python
"""
import argparse
from os import getcwd
from util.CoiServiceCall import *
from util.MakeTabular import table_waveform, table_soh, table_qcmask, table_frames
from util.ReadData import parse_dict
from util.WriteWfdisc import write_wfdisc
from util import S4Format
from termcolor import colored
import ast
import sys
import matplotlib.pyplot as plt
import matplotlib as mpl
import datetime
import pandas as pd
import numpy as np
from test.test_coi_service_call import http_mock_request


global newwaveurl

print(colored(
    """
                              .
   _____ __  __  _____      . ¦
  / ____|  \/  |/ ____|     ¦.¦
 | |  __| \  / | (___       ¦|¦
 | | |_ | |\/| |\___ \      ¦|¦ .
 | |__| | |  | |____) |     ¦|¦.¦
  \_____|_|  |_|_____/   .  ¦|¦|¦. .    .
                   . . |.| |¦|¦|¦|.|. ..¦. .  Geophysical
----------~~~~~~¦|¦|¦|¦|¦|¦|¦|¦|¦|¦|¦|¦|¦|¦|  Monitoring
                   ` ` |`| |¦|¦|¦|`|' `'¦' `  System
                         '  ¦|¦|¦' |    `
                            ¦|¦|'  `
                            ¦|'|
                            ¦| `
                            `|
                             '
   """, 'green'))


parser = argparse.ArgumentParser(description='A Python CLI for obtaining GMS objects')

parser.add_argument('data_type',
                    help='The data returned. Choices: metadata, waveforms (metadata+waveforms), soh, qcmasks, frames',
                    type=str)
parser.add_argument('-channel_id', help='Channel Id', type=str, default=None)
parser.add_argument('start_date', help='Date (MM/DD/YYYY)', type=str)
parser.add_argument('end_date', help='Date (MM/DD/YYYY)', type=str)
parser.add_argument('start_time', help='Start time (HH:MM:SS.SSS)', type=str)
parser.add_argument('end_time', help='End time (HH:MM:SS.SSS)', type=str)
parser.add_argument('-wfdisc', help='Save table to wfdisc (filename)', type=str, default=None)
parser.add_argument('-table', help='Print out table in command line (boolean). Default = True', type=bool, default=True)
parser.add_argument('-soh_type', help='Type of soh data to return ("boolean" or "analog"). Default = None', type=str,
                    default=None)
parser.add_argument('-station', help='Name of the station to query by for raw station data frames. Default = None',
                    type=str, default=None)
parser.add_argument('-plot', help='Include to produce a plot of samples over time. Default = None',
                    type=bool, default=None)
parser.add_argument('-url', help='Override the default url used by the config.yaml file.', type=str, default=None)

args = parser.parse_args()

data_type = args.data_type
channelid = args.channel_id
startdate = args.start_date
enddate = args.end_date
starttime = args.start_time
endtime = args.end_time
wfdisc = args.wfdisc
table = args.table
soh_type = args.soh_type
station_name = args.station
plot = args.plot
newwaveurl = args.url

if data_type == 'metadata':
    if channelid is None:
        print('Parameter channel_id is required for metadata')
        raise TypeError
    starttime = format_time(starttime, startdate)
    endtime = format_time(endtime, enddate)
    params = set_params(channel_id=channelid, start_time=starttime, end_time=endtime, with_waveforms=False)
    channel_segment = http_request(None,'channel-segment', params, 'json')
    # channel = http_request('channel', params, 'json')
    # mocked
    channel = http_mock_request('channel')
    # channel_segment = http_mock_request('channel-segment')

    chan_seg = parse_dict(channel_segment, 'channel-segment')
    chan = parse_dict(channel, 'channel')

    if table is True:
        table_waveform(chan_seg, chan)


elif data_type == 'waveforms':
    if channelid is None:
        print('Parameter channel_id is required for waveforms')
        raise TypeError
    starttime = format_time(starttime, startdate)
    endtime = format_time(endtime, enddate)
    params = set_params(channel_id=channelid, start_time=starttime, end_time=endtime, with_waveforms=True)
    channel_segment = http_request(newwaveurl,'channel-segment', params, 'json')
    # channel = http_request('channel', params, 'json')
    # mocked
    channel = http_mock_request('channel')
    # channel_segment = http_mock_request('channel-segment')

    chan_seg = parse_dict(channel_segment, 'channel-segment')
    chan = parse_dict(channel, 'channel')
    print(str(len(chan_seg.waveform)))
    print(str(chan_seg.waveform[0].sample_rate))

    if wfdisc is not None:
        directory = getcwd()
        dfile_base = 'waveform'
        write_wfdisc(chan, chan_seg, dfile_base, directory, wfdisc)
        for w in range(len(chan_seg.waveform)):
            filepath = directory + '/' + dfile_base + str(w) + '.w'
            S4Format.write(filepath, chan_seg.waveform[w].values)

    if table is True:
        table_waveform(chan_seg, chan)

    if plot is True:
        mpl.style.use('seaborn')
        fig, (ax1, ax2) = plt.subplots(2, 1)
        ax1.set_ylabel('Samples', fontsize=12)
        ax1.set_xlabel('Time', fontsize=12)
        ax1.set_title(chan_seg.name, fontsize=16)
        series = pd.Series([])

        print(str(len(chan_seg.waveform)))
        for w in range(len(chan_seg.waveform)):
            st = chan_seg.waveform[w].start_time
            et = chan_seg.waveform[w].end_time
            sc = chan_seg.waveform[w].sample_count
            sr = chan_seg.waveform[w].sample_rate

            # Get frequency in ms
            frequency = 1000 / sr
            frequencyStr = str(frequency) + "ms"
            ts = pd.Series(chan_seg.waveform[w].values,
                           index=pd.date_range(start=st, periods=sc, freq=frequencyStr))
            ax1.plot(ts, 'tab:blue')
            demeaned = ts - np.mean(ts)
            series = series.append(demeaned)
            print("st:" + str(st) + " et:" + str(et) + " freq:" + frequencyStr)
        ax2.psd(series, detrend = 'mean')
        plt.setp(ax1.yaxis.get_majorticklabels(), rotation=45)
        plt.setp(ax2.yaxis.get_majorticklabels(), rotation=45)
        ax2.yaxis.label.set_size(12)
        ax2.xaxis.label.set_size(12)
        plt.tight_layout()
        plt.savefig('waves.png')
        plt.show()



elif data_type == 'soh':
    if channelid is None:
        print('Parameter channel_id is required for soh')
        raise TypeError
    starttime = format_time(starttime, startdate)
    endtime = format_time(endtime, enddate)
    params = set_params(channel_id=channelid, start_time=starttime, end_time=endtime, soh_type=soh_type)

    channel_soh = http_request(None,'acquired-channel-soh', params, 'json')
    # mocked
    # channel_soh = http_mock_request('acquired-channel-soh')
    chan_soh = parse_dict(channel_soh, 'acquired-channel-soh')

    if table is True:
        table_soh(chan_soh)


elif data_type == 'qcmasks':
    if channelid is None:
        print('Parameter channel_id is required for qcmasks')
        raise TypeError
    starttime = format_time(starttime, startdate)
    endtime = format_time(endtime, enddate)
    params = set_params(channel_id=channelid, start_time=starttime, end_time=endtime)

    # mocked
    # qcmask = http_mock_request('qc-mask')
    qcmask = http_request(None,'qc-mask', params, 'json')
    qc_mask = parse_dict(qcmask, 'qc-mask')

    if table is True:
        table_qcmask(qc_mask)


elif data_type == 'frames':
    starttime = format_time(starttime, startdate)
    endtime = format_time(endtime, enddate)
    params = set_params(station_name=station_name, start_time=starttime, end_time=endtime)
    frames = http_request(None,'frames', params, 'msgpack')

    if table is True:
        table_frames(frames)

else:
    print('Error please choose from the following data calls: metadata, waveforms, soh, qcmasks, frames')

if __name__ == '__main__':
    pass
