"""
Functions for making information into tables to print out to command console
"""
import pandas as pd

pd.set_option('display.width', 999)


def table_waveform(chan_seg, chan):
    """
    Creates a tabular output of the information

    :param chan_seg: ChannelSegment COI
    :param chan: Channel COI
    :return: Prints out tabular table of metadata
    """

    print('----------------Channel---------------')
    print('id: ', chan_seg.id)
    print('channelName: ', chan.name)
    print('processing_channel_id: ', chan_seg.processing_channel_id)
    print('channelSegmentName: ', chan_seg.name)
    print('channelSegmentType: ', chan_seg.channel_segment_type)
    print('channelStartTime: ', chan_seg.start_time)
    print('channelEndTime: ', chan_seg.end_time)
    print('channelType: ', chan.channel_type)
    print('------------Station/Site--------------')
    print('siteName: ', chan.sites.name)
    print('siteChannel: ', chan.sites.channels)
    print('stationName: ', chan.sites.station.name)
    print('stationLatitude: ', chan.sites.station.latitude)
    print('stationLongitude: ', chan.sites.station.longitude)
    print('stationElevation: ', chan.sites.station.elevation)
    print('-------------Instrument---------------')
    print('instrumentModel: ', chan.instruments.instrument_model)
    print('calibrationFactor: ', chan.calibration.calibration_factor)
    print('calibrationPeriod: ', chan.calibration.calibration_period)
    print('------------CreationInfo--------------')
    print('creationTime: ', chan_seg.creation_info.creation_time)
    print('creatorName: ', chan_seg.creation_info.creation_name)
    print('------------WaveformTable-------------')
    labels = ['startTime', 'endTime', 'sampleRate', 'sampleCount']
    if len(chan_seg.waveform) > 0:
        df = pd.DataFrame([[
            chan_seg.waveform[0].start_time,
            chan_seg.waveform[0].end_time,
            chan_seg.waveform[0].sample_rate,
            chan_seg.waveform[0].sample_count,
        ]], columns=labels)

        for i in range(1, len(chan_seg.waveform)):
            df2 = pd.DataFrame([[
                chan_seg.waveform[i].start_time,
                chan_seg.waveform[i].end_time,
                chan_seg.waveform[i].sample_rate,
                chan_seg.waveform[i].sample_count,
            ]], columns=labels)
            df = df.append(df2, ignore_index=True)

        print(df)


def table_soh(chan_soh):
    """
    Creates a tabular table for soh information

    :param chan_soh: AcquiredChannelSoh COI object
    :param qc_mask: QcMask COI object
    :return: table with soh information
    """
    print('---------------SohInfo----------------')
    labels = ['processingChannelId', 'acquiredChannelSohType', 'startTime', 'endTime', 'status', 'creationTime',
              'creatorName']
    if len(chan_soh) > 0:
        df = pd.DataFrame(
            [[chan_soh[0].processing_channel_id,
              chan_soh[0].acquired_channel_soh_type,
              chan_soh[0].start_time,
              chan_soh[0].end_time,
              chan_soh[0].status_type,
              chan_soh[0].creation_info.creation_time,
              chan_soh[0].creation_info.creation_name]], columns=labels)

        for i in range(1, len(chan_soh)):
            df2 = pd.DataFrame(
                [[chan_soh[i].processing_channel_id,
                  chan_soh[i].acquired_channel_soh_type,
                  chan_soh[i].start_time,
                  chan_soh[i].end_time,
                  chan_soh[i].status_type,
                  chan_soh[i].creation_info.creation_time,
                  chan_soh[i].creation_info.creation_name]], columns=labels)
            df = df.append(df2, ignore_index=True)
        print(df)


def table_qcmask(qc_mask):
    print('-------------QcMaskTable--------------')
    if len(qc_mask) > 0:
        labels = ['id', 'qcMaskCategory', 'qcMaskType', 'rationale', 'startTime', 'endTime']
        for i in range(len(qc_mask[0].qc_mask_versions)):
            df = pd.DataFrame([[
                qc_mask[0].qc_mask_versions[i].version,
                qc_mask[0].qc_mask_versions[i].qc_mask_category,
                qc_mask[0].qc_mask_versions[i].qc_mask_type,
                qc_mask[0].qc_mask_versions[i].rationale,
                qc_mask[0].qc_mask_versions[i].start_time,
                qc_mask[0].qc_mask_versions[i].end_time]], columns=labels)
            for j in range(1, len(qc_mask)):
                df2 = pd.DataFrame([[
                    qc_mask[j].qc_mask_versions[i].version,
                    qc_mask[j].qc_mask_versions[i].qc_mask_category,
                    qc_mask[j].qc_mask_versions[i].qc_mask_type,
                    qc_mask[j].qc_mask_versions[i].rationale,
                    qc_mask[j].qc_mask_versions[i].start_time,
                    qc_mask[j].qc_mask_versions[i].end_time]], columns=labels)
                df = df.append(df2, ignore_index=True)

        print(df)


def table_frames(frames):
    print("Number of frames: " + str(len(frames)))
    if len(frames) > 0:
        labels = ['stationName', 'acquisitionProtocol', 'payloadDataStartTime', 'payloadDataEndTime', 'receptionTime']
        for f in frames:
            print('-------------RawStationDataFrame--------------')
            for l in labels:
                print(l + ": " + f.get(l))
            print('bytes in raw frame payload: ' + str(len(f.get('rawPayload'))))
        print('----------------------------------------------')
