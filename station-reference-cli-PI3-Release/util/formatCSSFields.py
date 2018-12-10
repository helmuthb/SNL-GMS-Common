# -*- coding: utf-8 -*-
from util.conversions import *
import util.cssSupport
'''
helper methods for formatting CSS flatfiles
All methods use ljust to left justify the field. 
The integer argument to ljust specifies that a field has a width less than that integer.
The field is padded with whitespace if the contents do not subsume the entire 
width.

The slicing mechanism is added at the end of each field as a safeguard to 
ensure that a field has width no more than that integer.
The filed is truncated if necessary.
'''


def format_lat(latitude):
    return latitude.ljust(9)[:9]


def format_lon(longitude):
    return longitude.ljust(9)[:9]


def format_sta(siteUUID):
    return siteUUID.ljust(6)[:6]


def format_refsta(refsta):
    return refsta.ljust(6)[:6]


def format_statype(station_type):
    return station_type.ljust(4)[:4]


def format_elev(elevation):
    return elevation.ljust(9)[:9]


def format_dnorth(north_displacement):
    return north_displacement.ljust(9)[:9]


def format_deast(east_displacement):
    return east_displacement.ljust(9)[:9]


def format_chan(chanUUID):
    return chanUUID.ljust(8)[:8]


def format_ondate(actualChangeTime):
    return iso8601_to_jdate(actualChangeTime).ljust(8)[:8]


def format_offdate(actualChangeTime):
    if actualChangeTime == '-1':
        return actualChangeTime.ljust(8)[:8]
    else:
        return iso8601_to_jdate(actualChangeTime).ljust(8)[:8]

def format_calratio(calibrationConversionRatio):
    return calibrationConversionRatio.ljust(16)[:16]


def format_chanid(UUID):
    return UUID.ljust(8)[:8]


def format_version_id(versionId):
    return versionId.ljust(5)[:5]


def format_ctype(n):
    return n.ljust(4)[:4]


def format_edepth(depth):
    return depth.ljust(24)[:24]


def format_hang(horizontalAngle):
    return horizontalAngle.ljust(24)[:24]


def format_vang(verticalAngle):
    return verticalAngle.ljust(24)[:24]


def format_descrip(description):
    return description.ljust(50)[:50]

def format_lddate(sysdate):
    return str(iso8601_to_regular_datetime(sysdate)).ljust(17)[:17]


def format_net(net):
    return net.ljust(8)[:8]


def format_netname(netname):
    return netname.ljust(80)[:80]


def format_nettype(nettype):
    return nettype.ljust(4)[:4]


def format_auth(auth):
    return auth.ljust(15)[:15]


def format_commid(commid):
    return commid.ljust(9)[:9]


def format_system_change_time(systemChangeTime):
    return iso8601_to_regular_datetime(systemChangeTime).ljust(17)[:17]


def format_time(actualChangeTime):
    return str(iso8601_to_epoch(actualChangeTime)).ljust(17)[:17]


def format_endtime(actualChangeTime):
    if actualChangeTime == util.writeCSS.NA_ENDTIME:
        return actualChangeTime.ljust(17)[:17]
    else:
        return str(iso8601_to_epoch(actualChangeTime)).ljust(17)[:17]

def format_calper(calibrationPeriod):
    return calibrationPeriod.ljust(16)[:16]

def format_tshift(timeShift):
    return timeShift.ljust(16)[:16]

def format_staname(description):
    return description.ljust(50)[:50]

def format_insname(manufacturer):
    return manufacturer.ljust(50)[:50]

def format_instype(model):
    return model.ljust(6)[:6]

def format_samprate(nominalSampleRate):
    return nominalSampleRate.ljust(11)[:11]

def format_ncalib(nominalCalibrationFactor):
    return nominalCalibrationFactor.ljust(16)[:16]

def format_ncalper(nominalCalibrationPeriod):
    return nominalCalibrationPeriod.ljust(16)[:16]

def format_rsptype(type):
    return type.ljust(6)[:6]

def format_elev(elevation):
    return elevation.ljust(9)[:9]


'''
1 space
'''


def gap():
    return ' '
