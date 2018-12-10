# -*- coding: utf-8 -*-
import argparse
from util.validate import *
from util.access import *
from util.formatPrint import make_dict_from_args, pretty_print_dict, print_table_to_screen
from util.cssSupport import *

'''
# Retrieve station reference information from the OSD.
# for help: 
python stationreferenceCLI.py -h 

# REQUIRED:
In the first position, enter one of the following modes after the -mode argument:
Example:
python stationreferenceCLI.py -mode networks
retrieves all networks

networks
stations
sites
digitizers
channels
calibrations
sensors

# OPTIONAL FOR ALL MODES: 
1) Time Interval
Invoke with:
-start_time x -end_time y
or
-start_time x
or
-end_time y

Please enter the times in UTC ISO 8601 format or Epoch time format.

UTC ISO 8601 format is yyyy-mm-ddThh:mm:ss
example: 1987-09-22T12:08:44
Milliseconds are not supported. 
Referencing the above examples, entries of 1987-09-22T12:08:44 or 1987-09-22T00:00:00 would both be valid.
if just the date is desired with no specific time, please enter just the date in the form:
1987-09-22

Epoch format is the number of seconds that have elapsed since January 1st, 1970.
example: 1514764800 is January 1st, 2018. 

The two formats can be mixed and matched, i.e. -start_time in epoch and -end_time in UTC ISO 8601 or vice versa.
Additionally, just a single argument can be given:

-start_time 1987-09-22 -end_time 1514764800
-start_time 31536000 -end_time 1514764800
-start_time 31536000 -end_time 2108-03-01T12:15:01
-start_time 0
-end_time 1999-01-01T12:00:00

are all valid inputs to the system.

To export the flatfiles, run the client in css mode with the -css_export argument and specify a channel id, and, optionally
a time range:
python stationReferenceCLI.py -mode css_export -channel_id 1fac571c-b0f7-466e-a0a6-306d3ba97252
python stationReferenceCLI.py -mode css_export -channel_id 1fac571c-b0f7-466e-a0a6-306d3ba97252 -start_time 1987-09-22 -end_time 1514764800

are both valid inputs to the system, and time formats can be mixed and matched as above.

'''


# get the command line arguments from the client
def get_command_line_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-mode', type=str, action='store', required=True)
    parser.add_argument('-start_time', type=str, action='store', default=None)
    parser.add_argument('-end_time', type=str, action='store', default=None)
    parser.add_argument('-network', type=str, action='store', default=None)
    parser.add_argument('-station', type=str, action='store', default=None)
    parser.add_argument('-site', type=str, action='store', default=None)
    parser.add_argument('-digitizer_id', type=str, action='store', default=None)
    parser.add_argument('-channel_id', type=str, action='store', default=None)
    parser.add_argument('-hostname', type=str, action='store', default='localhost:8080')
    parser.add_argument('-output_directory', type=str, action= 'store', default = None)
    # toss unknown args or extra args stored in [1]
    return parser.parse_known_args()[0]


args = get_command_line_args()

# validate command line args
args = validate_args(args)

# set path for output files--when we write flatfiles
# output_path = os.getcwd()

mode = args.mode

modeList = ['networks', 'stations', 'sites', 'channels', 'digitizers', 'calibrations', 'sensors', 'responses', 'css_export']
channelRequired = modeList[4:]

if mode not in modeList:
    print('Please enter a valid mode from the following: ' + str(modeList))
    raise ValueError
    sys.exit[1]

if mode in channelRequired and (not args.channel_id or not validUuid(args.channel_id)):
        print('Please enter a valid channel_id when using one of these modes: '+ str(channelRequired))
        raise ValueError
        sys.exit[1]

# query for information based on client's parameters
if mode == 'networks':
    data = retrieve_networks(args.start_time, args.end_time, args.hostname)
elif mode == 'stations':
    data = retrieve_stations(args.network, args.start_time, args.end_time, args.hostname)
elif mode == 'sites':
    data = retrieve_sites(args.station, args.start_time, args.end_time, args.hostname)
elif mode == 'digitizers':
    data = retrieve_digitizers(args.channel_id, args.start_time, args.end_time, args.hostname)
elif mode == 'channels':
    data = retrieve_channels(args.site, args.start_time, args.end_time, args.hostname)
elif mode == 'calibrations':
    data = retrieve_calibrations(args.channel_id, args.start_time, args.end_time, args.hostname)
elif mode == 'sensors':
    data = retrieve_sensors(args.channel_id, args.start_time, args.end_time, args.hostname)
elif mode == 'responses':
    data = retrieve_responses(args.channel_id, args.start_time, args.end_time, args.hostname)
elif mode == 'css_export':
    css_writer(args.channel_id, args.start_time, args.end_time, args.hostname, args.output_directory)
else:
    print("Error: unknown mode " + mode)
    raise TypeError

'''
# print args to screen
pretty_print_dict(make_dict_from_args(args), 10)
'''
# count objects, de-serialize and normalize nested json data and print to screen
if mode != 'css_export':
    print_table_to_screen(data, args)
