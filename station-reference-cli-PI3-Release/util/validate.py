# -*- coding: utf-8 -*-
import datetime
import os
import errno
import uuid
'''
epoch_time
@param time
checks if time given is in epoch time format
returns true if it is and false otherwise
'''


def epoch_time(time):
    try:
        cast = int(time)
        return True
    except ValueError:
        return False


'''
iso_time
@param time
checks if time given is in iso time format
returns true if it is and false otherwise
'''


def iso_time(time):
    # throw out anything that doesn't have at least possibly yyyy-mm-dd
    if len(time) < 10:
        return False
    try:
        datetime.datetime.strptime(time[0:10], "%Y-%m-%d")
        return True
    except ValueError:
        return False

'''
uuid
@:param hexstring
checks if the hex input string is in uuid format
returns true if it is and false otherwise
'''

def validUuid(hexstring):
    if hexstring is None:
        return False
    h = '{' + hexstring + '}'
    try:
        cast = uuid.UUID(h)
        return True
    except ValueError:
        return False




'''
reformatted_epoch_string
@ param epoch_string
Returns a datetime object from the given epoch string.
Casts the string to an int to perform the conversion.
'''


def reformatted_epoch_string(epoch_string):
    return datetime.datetime.utcfromtimestamp(int(epoch_string)).isoformat()+'Z'


'''
reformatted_iso_string
@ param epoch_string
Returns a datetime object from the given epoch string.
If the string doesn't contain the suffix of Thh:mm:ss, adds it so the object parses correctly in the general case.
'''


def reformatted_iso_string(iso_string):
    # this might have just yyyy-mm-dd, try to append Thh:mm:ss and then parse
    if len(iso_string) < 19:
        iso_string = iso_string[0:10] + 'T00:00:00'
    return iso_string + 'Z'
    #return datetime.datetime.strptime(iso_string, "%Y-%m-%dT%H:%M:%SZ")


'''
@ param mode
Check that a mode was entered.
'''


def validate_mode(mode):
    # validate that mode was entered
    if mode is None:
        print("Missing mode. Required: one of [ networks | stations | sites | digitizers | channels | "
              "calibrations | sensors | responses ] Optional: [-start_time yyyy-mm-ddThh.mm.ss.sss OR -start_time ssssssssss ][ -end_time"
              "yyyy-mm-ddThh.mm.ss.sss OR -end_time ssssssssss] (replace with desired start time, end time; can specify one, both, or neither)")
        raise TypeError


'''
@ param time
Check that the time is in the expected format.
'''


def validate_time(time):
    if not epoch_time(time) and not iso_time(time):
        print('Please ensure all times are valid and in either epoch or ISO 8601 format. Consult the help '
              'for more information on these formats.')
        raise TypeError
    if epoch_time(time):
        return reformatted_epoch_string(time)
    if iso_time(time):
        return reformatted_iso_string(time)

'''
validate_time_range
@param start (datetime obj)
@param end (datetime obj)
compares 2 datetime objects to each other and the current time to check for common sense values
'''


def validate_time_range(start, end):
    now = datetime.datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S")
    if start > end or start > now:
        print('Please ensure your start and end times are valid and in either epoch or ISO 8601 format. '
              'Consult the help for more information on these formats.')
        return False  # return true if there is a date error
    return True


'''
@ param args (command line args)
Check that a mode was entered.
Check that times were entered in valid formats and that if a time range exists, that it makes sense.
Reformat args where necessary.
Raise an error otherwise.
'''


def validate_args(args):
    validate_mode(args.mode)
    if args.start_time is not None:
        validate_time(args.start_time)
        if epoch_time(args.start_time):
            args.start_time = reformatted_epoch_string(args.start_time)
        else:
            args.start_time = reformatted_iso_string(args.start_time)

    if args.end_time is not None:
        validate_time(args.end_time)
        if epoch_time(args.end_time):
            args.end_time = reformatted_epoch_string(args.end_time)
        else:
            args.end_time = reformatted_iso_string(args.end_time)

    if args.start_time is not None and args.end_time is not None:
        if not validate_time_range(args.start_time, args.end_time):
            raise TypeError
    if args.output_directory is not None:
        find_or_create_path(args.output_directory)
    return args

'''
find_or_create_path
@param path
Takes the path give by the user and validates it.
If no path exists, tries to create it.
If cannot create, throws an exception.
'''


def find_or_create_path(path):
    if not os.path.exists(path):
        try:
            os.mkdir(path)
        # avoid race condition between this process and another one
        except OSError as exception:
            if exception.errno != errno.EEXIST:
                print(
                    'Please try to close other programs and try again. Or, refrain from creating the directory yourself while the program is creating it.')
                raise

    return True
