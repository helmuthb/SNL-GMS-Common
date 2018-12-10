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
