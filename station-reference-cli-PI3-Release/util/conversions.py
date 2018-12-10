import dateutil
from dateutil.parser import *
import calendar
import datetime

"""
iso8601_to_epoch
@:param datestring
convert iso8601 date into unix epoch time
"""


def iso8601_to_epoch(datestring):
    return calendar.timegm(dateutil.parser.parse(datestring).timetuple())


'''
iso8601_to_jdate
@:param datestring
convert iso8601 into julian (jdate)
'''


def iso8601_to_jdate(datestring):
    t = dateutil.parser.parse(datestring).timetuple()
    if t.tm_yday < 100:
        return str(t.tm_year) + '0' + str(t.tm_yday)
    return str(t.tm_year) + str(t.tm_yday)


def iso8601_to_regular_datetime(datestring):
    t = dateutil.parser.parse(datestring).timetuple()
    mon = str(t.tm_mon)
    if int(mon) < 10:
        mon = zero_padder(mon)
    mday = str(t.tm_mday)
    if int(mday) < 10:
        mday = zero_padder(mday)
    hour = str(t.tm_hour)
    if int(hour) < 10:
        hour = zero_padder(hour)
    min = str(t.tm_min)
    if int(min) < 10:
        min = zero_padder(min)
    sec = str(t.tm_sec)
    if int(sec) < 10:
        sec = zero_padder(sec)
    return str(t.tm_year)[2:] + '/' + mon + '/' + mday + ' ' + hour + ':' + min + ':' + sec

'''
zero_padder
@:param needsPadding
pad the thing that needs padding with one leading zero
'''


def zero_padder(needsPadding):
    return '0' + needsPadding
