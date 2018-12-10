from util.cssSupport import *
from util.formatPrint import normalize_json_obj
from util.formatCSSFields import *
import base64
import datetime
import random

'''
methods for writing CSS flatfiles
'''

lddate = format_lddate(datetime.datetime.now().isoformat()) #get the current date and time in ISO8601
NA_LONG_AND_LAT = '-999.99' #NA values for latitude and longitude
NA_ENDTIME = '9999999999.999' #NA value for endtime
INSTRUMENTPRIMARYKEYSTART = 601 #Instrument primary key begins at this value (arbitrary)
CHANPRIMARYKEYSTART = 401

'''
write_network
@:param networkObj
Write NETWORK CSS flatfile with fields
net, netname, nettype, auth, commid, systemChangeTime
'''


def write_network(networkObj):
    if networkObj is None:
        open('p3.network', 'w').close()
        return
    with open('p3.network', 'w') as f:
        for n in networkObj:
            norm = normalize_json_obj(n)
            net = str(norm['name'].tolist()[0])
            nn = str(norm['description'].tolist()[0])
            if not nn:
                netname = '-'
            else:
                netname = nn
            r = str(norm['region'].tolist()[0])
            nettype = ''
            if r == 'GLOBAL':
                nettype = 'ww'
            elif r == 'REGIONAL':
                nettype = 'ar'
            elif r == 'LOCAL':
                nettype = 'LO'
            else:
                nettype = '-'
            a = str(norm['source.originatingOrganization'].tolist()[0])
            if not a:
                auth = '-'
            else:
                auth = a
            commid = '-1'
            systemChangeTime = str(norm['systemChangeTime'].tolist()[0])
            row = format_net(net) + gap() + format_netname(netname) + gap() + format_nettype(
                nettype) + gap() + format_auth(auth) + gap() + format_commid(
                commid) + gap() + format_system_change_time(systemChangeTime) + '\n'
            f.write(row)


'''
write_sitechan
@:param channelObj
@:param siteObj
Write SITECHAN CSS flatfile with fields
sta, chan, ondate, chanId, offdate, ctype, edepth, hang, vang, descrip, lddate

'''


def write_sitechan(channelObj, siteObj):
    if channelObj is None or siteObj is None:
        open('p3.sitechan', 'w').close()
        return
    site = siteObj[0]
    sta = format_sta(str(site.get('name')))
    chanid = CHANPRIMARYKEYSTART -1
    chan = channelObj[0].get('name')
    with open('p3.sitechan', 'w') as f:
        for index, ch in enumerate(channelObj):
            last = chan
            chan = format_chan(str(ch.get('name')))
            if last != chan:
                chanid += 1
            ondate = format_ondate(str(ch.get('actualTime')))
            offdate = ''
            if index < len(channelObj) - 1:
                offdate = format_offdate(str(channelObj[index + 1].get('actualTime')))
            else:
                offdate = format_offdate('-1')
            ctype = format_ctype('n')
            edepth = format_edepth(str(ch.get('depth')))
            h = str(ch.get('horizontalAngle'))
            if not h:
                h = '-1.0'
            hang = format_hang(h)
            v = str(ch.get('verticalAngle'))
            if not v:
                v = '-1.0'
            vang = format_vang(v)
            d = ch.get('description')
            if not d:
                d = '-'
            descrip = format_descrip(d)
            row = sta + gap() + chan + gap() + ondate + gap() + str(chanid) + gap() + offdate + gap() + ctype + gap() + \
                  edepth + gap() + hang + gap() + vang + gap() + descrip + gap() + lddate + '\n'
            f.write(row)


'''
write_site
@:param siteObj
@:param stationObj
@:param siteToStaMemRelationship
Write SITE CSS flatfile with fields

name, ondate, offdate, lat, lon, elev, staname, statype, refsta, dnorth, deast, lddate
'''


def write_site(siteObj, stationObj, siteToStaMemRelationship):
    if siteObj is None or stationObj is None or siteToStaMemRelationship is None:
        open('p3.site', 'w').close()
        return
    stationObj = stationObj[0]
    with open('p3.site', 'w') as f:
        for index, station in enumerate(stationObj):
            name = format_sta(str(station.get('name')))
            refsta = name
            ondate = format_ondate(str(station.get('actualChangeTime')))
            offdate = ''
            if index < len(stationObj) - 1:
                offdate = format_offdate(str(stationObj[index + 1].get('actualChangeTime')))
            else:
                offdate = format_offdate('-1')
            lat = format_lat(str(station.get('latitude')))
            if not lat:
                lat = format_lat(NA_LONG_AND_LAT)
            lon = format_lon(str(station.get('longitude')))
            if not lon:
                lon = format_lon(NA_LONG_AND_LAT )
            elev = format_elev(str(station.get('elevation')))
            if not elev:
                elev = format_elev(NA_LONG_AND_LAT)
            d = station.get('description')
            if not d:
                d = '-'
            staname = format_staname(d)
            type = str(station.get('stationType'))
            statype = '    '
            if 'array' in type.lower():
                statype = format_statype('ar')
            elif 'component' in type.lower():
                statype = format_statype('ss')
            dnorth = format_dnorth(str('0'))
            deast = format_deast(str('0'))
            stationRow = name + gap() + ondate + gap() + offdate + gap() + lat + gap() + lon + gap() + elev + gap() + staname + gap() + statype + gap() + refsta + gap() + \
                         dnorth + gap() + deast + gap() + lddate + '\n'
            f.write(stationRow)
        for index, site in enumerate(siteObj):
            name = format_sta(str(site.get('name'))) #refsta stays the same as above
            ondate = format_ondate(str(site.get('actualChangeTime')))
            offdate = ''
            if index < len(siteObj) - 1:
                offdate = format_offdate(str(siteObj[index + 1].get('actualChangeTime')))
            else:
                offdate = format_offdate('-1')
            lat = format_lat(str(station.get('latitude')))
            if not lat:
                lat = format_lat(NA_LONG_AND_LAT )
            lon = format_lon(str(station.get('longitude')))
            if not lon:
                lon = format_lon(NA_LONG_AND_LAT )
            elev = format_elev(str(station.get('elevation')))
            if not elev:
                elev = format_elev(NA_LONG_AND_LAT)
            d = station.get('description')
            if not d:
                d = '-'
            staname = format_staname(d)
            statype = format_statype('ss')
            dnorth = format_dnorth(str('0'))
            deast = format_deast(str('0'))
            siteRow = name + gap() + ondate + gap() + offdate + gap() + lat + gap() + lon + gap() + elev + gap() + staname + gap() + statype + gap() + refsta + gap() + \
                         dnorth + gap() + deast + gap() + lddate + '\n'
            f.write(siteRow)

'''
write_affiliation
@:param stationObj
@:param networkObj
@:param networkMembersObj
@:param siteObj
@:param stationMembersObj
Write NETWORK CSS flatfile with fields 
net, sta, time, endtime, lddate
'''


def write_affiliation(stationObj, networkObj, networkMembersObj, siteObj, stationMembersObj):
    if stationObj is None or siteObj is None or stationMembersObj is None:
        open('p3.affiliation', 'w').close()
        return
    with open('p3.affiliation', 'w') as f:
        if networkMembersObj is not None:
            networkMembersObj = networkMembersObj[0]
            for index, n in enumerate(networkMembersObj):
                norm = normalize_json_obj(n)  # get a row
                networkId = norm['networkId'].tolist()[0]  # only one object in this row
                stationId = norm['stationId'].tolist()[0]
                time = format_time(str(norm['actualChangeTime'].tolist()[0]))
                endtime = format_endtime(NA_ENDTIME)
                if index < len(networkMembersObj) - 1:
                    normNext = normalize_json_obj(networkMembersObj[index + 1])  # get the next row
                    if normNext['networkId'].tolist()[0] == networkId:
                        endtime = format_endtime(str(normNext['actualChangeTime'].tolist()[0]))
                for netObj in networkObj:
                    netObj = netObj[0]
                    if netObj.get('entityId') == networkId:
                        net = format_net(str(netObj.get('name')))
                for staObj in stationObj:
                    staObj = staObj[0]
                    if staObj.get('entityId') == stationId:
                        sta = format_sta(str(staObj.get('name')))
                row = net + gap() + sta + gap() + time + gap() + endtime + gap() + lddate + '\n'
                f.write(row)

        for index, n in enumerate(stationMembersObj):
            norm = normalize_json_obj(n)
            stationId = norm['stationId'].tolist()[0]
            siteId = norm['siteId'].tolist()[0]
            time = format_time(str(norm['actualChangeTime'].tolist()[0]))
            endtime = ''
            if index < len(stationMembersObj) -1:
                normNext = normalize_json_obj(stationMembersObj[index + 1])
                endtime = format_endtime(str(normNext['actualChangeTime'].tolist()[0]))
            else:
                endtime = format_endtime(NA_ENDTIME)
            for staObj in stationObj:
                staObj = staObj[0]
                if staObj.get('entityId') == stationId:
                    net = format_net(str(staObj.get('name')))
            for siObj in siteObj:
                if siObj.get('entityId') == siteId:
                    sta = format_sta(str(siObj.get('name')))
            row = net + gap() + sta + gap() + time + gap() + endtime + gap() + lddate + '\n'
            f.write(row)






'''
write_sensor
@:param chanObj
@:param siteObj
@:param calibrationObj
@:param sensorObj
Write the SENSOR CSS flatfile with fields:
sta ,chan, time ,endtime, inid, chanid, jdate, calratio, calper, tshift, instant, lddate
'''

def write_sensor(chanObj, siteObj, calibrationObj, sensorObj):
    if chanObj is None or siteObj is None or calibrationObj is None or sensorObj is None:
        open('p3.sensor', 'w').close()
        return
    with open('p3.sensor', 'w') as f:
        chanIdIndex = -1
        jdate = format_offdate('-1')
        calratio = format_calratio('1')
        instant = 'y'
        chan = format_chan(str(chanObj[0].get('name')))
        inid = INSTRUMENTPRIMARYKEYSTART - 1  # will have to increment once before writing
        chanid = CHANPRIMARYKEYSTART
        channel_id = chanObj[0].get('entityId')
        for index, s in enumerate(siteObj):
            chanIdIndex += 1
            if index < len(siteObj) - 1:
                if s.get('entityId') == siteObj[index + 1].get('entityId'):
                    continue
            sta = format_sta(str(s.get('name')))
            for index, n in enumerate(sensorObj):
                time = format_time(str(n.get('actualTime')))
                endtime = ''
                if index < len(sensorObj) - 1:
                    endtime = format_endtime(str(sensorObj[index + 1].get('actualTime')))
                    if n.get('id') == sensorObj[index + 1].get('id'):
                        break
                else:
                    endtime = format_endtime(NA_ENDTIME)
                inid += 1

                i = str(inid)
                for index, x in enumerate(calibrationObj):
                    if index > 0 and x.get('entityId') == calibrationObj[index - 1].get('entityId'):
                        continue
                    else:
                        calper = format_calper(str(x.get('calibrationPeriod')))
                        tshift = format_tshift(str(x.get('timeShift')))
                    row = sta + gap() + chan + gap() + time + gap() + endtime + gap() + i + gap() + \
                          str(chanid) + gap() + jdate + gap() + calratio + gap() + calper + gap() + tshift + gap() + \
                          instant + gap() + lddate + '\n'
                    f.write(row)


'''
write_instrument
@:param outputDir
@:param chanObj
@:param calibrationObj
@:param sensorObj
@:param responseObj
Write the INSTRUMENT CSS flatfile with fields:
inid, insname, instype, band, digital, samprate, ncalib, ncalper, dir, dfile, rsptype. lddate
'''


def write_instrument(outputDir, chanObj, calibrationObj, sensorObj, responseObj):
    if chanObj is None or calibrationObj is None or sensorObj is None or responseObj is None:
        open('p3.instrument', 'w').close()
        return
    if outputDir is None:
        baseDir = os.getcwd() + '/responses'
    else:
        baseDir = outputDir + '/responses'
    with open('p3.instrument', 'w') as f:
        channel_id = chanObj[0].get('entityId')
        inid = INSTRUMENTPRIMARYKEYSTART
        band = chanObj[0].get('name')[0].lower()
        digital = 'd'
        samprate = format_samprate(str(chanObj[0].get('nominalSampleRate')))
        for s in sensorObj:
            insname = format_insname(str(s.get('instrumentManufacturer')))
            instype = format_instype(str(s.get('instrumentModel')))
            for r in responseObj:
                dir = baseDir + str(inid) + '/'
                if not os.path.exists(dir):
                    os.mkdir(dir)
                dfile = base64.b64decode(r.get('responseData')).decode('utf8')
                n = 'data' + str(inid)
                filename = dir + n
                with open(filename, 'w') as d:
                    d.write(dfile)
                rsptype = format_rsptype(str(r.get('responseType')))
            for c in calibrationObj:
                ncalib = format_ncalib(str(c.get('calibrationFactor')))
                ncalper = format_ncalper(str(c.get('calibrationPeriod')))
                row = str(inid) + gap() + insname + gap() + instype + gap() + band + gap() + digital + gap() + \
                      samprate + gap() + ncalib + gap() + ncalper + gap() + dir + gap() + n + gap() + \
                      rsptype + gap() + lddate + '\n'
                f.write(row)
            inid += 1
