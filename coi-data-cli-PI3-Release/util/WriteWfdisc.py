"""
Functions for writing wfdisc files
"""
import pandas as pd
from obspy.core.utcdatetime import UTCDateTime
import datetime


def date2epoch(date):
    """
    Converts a GMT data to Epoch time (seconds)

    :param date: date string
    :return: date in Epoch seconds
    """
    d = UTCDateTime(date)
    return d.timestamp


def date2jul(date):
    """
    Converts date to julian data

    :param date: date string
    :return: julian date
    """
    d = UTCDateTime(date)
    return d.julday


def current_time():
    """
    Gets current time
    :return: Current time formatted %y-%m-%d %H:%M:%S
    """
    t = datetime.datetime.now()
    return t.strftime('%y-%m-%d %H:%M:%S')


def format_wfdisc(chan_, chan_seg, dfile_base, dir_):
    """
    Formats COI objects into wfdisc format

    :param chan_: Channel COI
    :param chan_seg: ChannelSegment COI
    :param dfile_base: base name of waveform file
    :param dir_: directory location of waveform file
    :return: Formatted python dictionary
    """
    columns = ['sta', 'chan', 'time', 'wfid', 'chanid', 'jdate', 'endtime', 'nsamp', 'samprate', 'calib', 'calper',
               'instype', 'segtype', 'datatype', 'clip', 'dir', 'dfile', 'foff', 'commid', 'lddate']

    if len(chan_seg.waveform) == 1:
        sta = chan_.sites.station.name
        chan = chan_.name
        time = date2epoch(chan_seg.waveform[0].start_time)
        wfid = 1
        chanid = 1
        jdate = date2jul(chan_seg.waveform[0].start_time)
        endtime = date2epoch(chan_seg.waveform[0].end_time)
        nsamp = int(chan_seg.waveform[0].sample_count)
        samprate = float(chan_seg.waveform[0].sample_rate)
        calib = float(chan_.calibration.calibration_factor)
        calper = float(chan_.calibration.calibration_period)
        instype = chan_.instruments.instrument_model
        segtype = chan_seg.channel_segment_type
        datatype = 's4'
        clip = 'c or n'
        directory = dir_
        dfile = dfile_base + '1.w'
        foff = nsamp * 4
        commid = -1
        lddate = current_time()

        df = pd.DataFrame(columns=columns)
        df['sta'] = pd.Series(sta.__format__('40.40s'))
        df['chan'] = pd.Series(chan.__format__('8.8s'))
        df['time'] = pd.Series(time.__format__('17.5f'))
        df['wfid'] = pd.Series(wfid.__format__('8d'))
        df['chanid'] = pd.Series(chanid.__format__('8d'))
        df['jdate'] = pd.Series(jdate.__format__('8d'))
        df['endtime'] = pd.Series(endtime.__format__('17.5f'))
        df['nsamp'] = pd.Series(nsamp.__format__('8d'))
        df['samprate'] = pd.Series(samprate.__format__('11.7f'))
        df['calib'] = pd.Series(calib.__format__('16.6f'))
        df['calper'] = pd.Series(calper.__format__('16.6f'))
        df['instype'] = pd.Series(instype.__format__('6.6s'))
        df['segtype'] = pd.Series(segtype.__format__('1.1s'))
        df['datatype'] = pd.Series(datatype.__format__('2.2s'))
        df['clip'] = pd.Series(clip.__format__('1.1s'))
        df['dir'] = pd.Series(directory.__format__('64.64s'))
        df['dfile'] = pd.Series(dfile.__format__('32.32s'))
        df['foff'] = pd.Series(foff.__format__('10d'))
        df['commid'] = pd.Series(commid.__format__('8d'))
        df['lddate'] = pd.Series(lddate)
        return df

    else:

        sta = chan_.sites.station.name
        chan = chan_.name
        time = date2epoch(chan_seg.waveform[0].start_time)
        wfid = 1
        chanid = 1
        jdate = date2jul(chan_seg.waveform[0].start_time)
        endtime = date2epoch(chan_seg.waveform[0].end_time)
        nsamp = int(chan_seg.waveform[0].sample_count)
        samprate = float(chan_seg.waveform[0].sample_rate)
        calib = float(chan_.calibration.calibration_factor)
        calper = float(chan_.calibration.calibration_period)
        instype = chan_.instruments.instrument_model
        segtype = chan_seg.channel_segment_type
        datatype = 's4'
        clip = 'c or n'
        directory = dir_
        dfile = dfile_base + '1.w'
        foff = nsamp * 4
        commid = -1
        lddate = current_time()

        df = pd.DataFrame(columns=columns)
        df['sta'] = pd.Series(sta.__format__('40.40s'))
        df['chan'] = pd.Series(chan.__format__('8.8s'))
        df['time'] = pd.Series(time.__format__('17.5f'))
        df['wfid'] = pd.Series(wfid.__format__('8d'))
        df['chanid'] = pd.Series(chanid.__format__('8d'))
        df['jdate'] = pd.Series(jdate.__format__('8d'))
        df['endtime'] = pd.Series(endtime.__format__('17.5f'))
        df['nsamp'] = pd.Series(nsamp.__format__('8d'))
        df['samprate'] = pd.Series(samprate.__format__('11.7f'))
        df['calib'] = pd.Series(calib.__format__('16.6f'))
        df['calper'] = pd.Series(calper.__format__('16.6f'))
        df['instype'] = pd.Series(instype.__format__('6.6s'))
        df['segtype'] = pd.Series(segtype.__format__('1.1s'))
        df['datatype'] = pd.Series(datatype.__format__('2.2s'))
        df['clip'] = pd.Series(clip.__format__('1.1s'))
        df['dir'] = pd.Series(directory.__format__('64.64s'))
        df['dfile'] = pd.Series(dfile.__format__('32.32s'))
        df['foff'] = pd.Series(foff.__format__('10d'))
        df['commid'] = pd.Series(commid.__format__('8d'))
        df['lddate'] = pd.Series(lddate)

        for w in range(len(chan_seg.waveform)):
            if w != 0:
                sta = chan_.sites.station.name
                chan = chan_.name
                time = date2epoch(chan_seg.waveform[w].start_time)
                wfid = w + 1
                chanid = w + 1
                jdate = date2jul(chan_seg.waveform[w].start_time)
                endtime = date2epoch(chan_seg.waveform[w].end_time)
                nsamp = int(chan_seg.waveform[w].sample_count)
                samprate = float(chan_seg.waveform[w].sample_rate)
                calib = float(chan_.calibration.calibration_factor)
                calper = float(chan_.calibration.calibration_period)
                instype = chan_.instruments.instrument_model
                segtype = chan_seg.channel_segment_type
                datatype = 's4'
                clip = 'c or n'
                directory = dir_
                dfile = dfile_base + str(w + 1) + '.w'
                foff = nsamp * 4
                commid = -1
                lddate = current_time()

                df2 = pd.DataFrame(columns=columns)
                df2['sta'] = pd.Series(sta.__format__('40.40s'))
                df2['chan'] = pd.Series(chan.__format__('8.8s'))
                df2['time'] = pd.Series(time.__format__('17.5f'))
                df2['wfid'] = pd.Series(wfid.__format__('8d'))
                df2['chanid'] = pd.Series(chanid.__format__('8d'))
                df2['jdate'] = pd.Series(jdate.__format__('8d'))
                df2['endtime'] = pd.Series(endtime.__format__('17.5f'))
                df2['nsamp'] = pd.Series(nsamp.__format__('8d'))
                df2['samprate'] = pd.Series(samprate.__format__('11.7f'))
                df2['calib'] = pd.Series(calib.__format__('16.6f'))
                df2['calper'] = pd.Series(calper.__format__('16.6f'))
                df2['instype'] = pd.Series(instype.__format__('6.6s'))
                df2['segtype'] = pd.Series(segtype.__format__('1.1s'))
                df2['datatype'] = pd.Series(datatype.__format__('2.2s'))
                df2['clip'] = pd.Series(clip.__format__('1.1s'))
                df2['dir'] = pd.Series(directory.__format__('64.64s'))
                df2['dfile'] = pd.Series(dfile.__format__('32.32s'))
                df2['foff'] = pd.Series(foff.__format__('10d'))
                df2['commid'] = pd.Series(commid.__format__('8d'))
                df2['lddate'] = pd.Series(lddate)

                df = df.append(df2, ignore_index=True)
        return df


def write_wfdisc(chan_, chan_seg, dfile_base, dir_, filename):
    """
    Writes a wfdisc file

    :param chan_: Channel COI
    :param chan_seg: ChannelSegment COI
    :param dfile_base: base name of waveform file
    :param dir_: directory location of waveform file
    :param filename: filename of wfdisc
    """
    wfdisc_df = format_wfdisc(chan_, chan_seg, dfile_base, dir_)
    wfdisc_df.to_csv(filename, sep='\t', index=False, header=None, encoding='ascii')


def read_wfdisc(file):
    """
    Reads in wfdisc file into pandas dataframe
    :param file:
    :return: pandas dataframe
    """
    columns = ['sta', 'chan', 'time', 'wfid', 'chanid', 'jdate', 'endtime', 'nsamp', 'samprate', 'calib', 'calper',
               'instype', 'segtype', 'datatype', 'clip', 'dir', 'dfile', 'foff', 'commid', 'lddate']
    return pd.read_csv(file, sep='\t', names=columns, encoding='ascii')
