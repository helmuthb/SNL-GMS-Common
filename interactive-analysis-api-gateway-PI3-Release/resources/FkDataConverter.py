import json
from pprint import pprint
from random import *
#from json import encoder

# Hack the JSON encoder to limit floating point precision to 5 digits
json.encoder.FLOAT_REPR = lambda f: ("%.17g" % f)

#this file was sent to us from Chris 
readFile = 'FK_test_data_new.json'

initialJson = json.loads(open(readFile).read())

filedata = json.dumps(initialJson['fkArray'])


jsondata = json.loads(filedata)

newJsonData = []

writeFile = 'test_data/ueb_2010140/fkData.json'

# Just assume 111 km per degree (based on 6378.1 Earth radius ignoring Ellipsoidal variation)
DEGREES_TO_KM = 111.318845

for fk in jsondata:
    # Get contrib channels and reformat to use / instead of : to separate
    contrib = fk['contribChannels']
    newChannels = []
    for channel in contrib:
        channel = channel.replace(' ','')
        newChannels.append(channel.replace(':', '/'))
    fk['contribChannels'] = newChannels

    # Convert slowness values from degrees to KM
    # fk['slow'] = [slow / DEGREES_TO_KM for slow in fk['slow']]
    # fk['maxSlow'] /= DEGREES_TO_KM
    # fk['maxFk']['slow'] /= DEGREES_TO_KM
    # fk['maxFk']['xSlow'] /= DEGREES_TO_KM
    # fk['maxFk']['ySlow'] /= DEGREES_TO_KM
    # fk['maxFk']['theoSlow'] /= DEGREES_TO_KM
    # fk['maxFk']['theoXSlow'] /= DEGREES_TO_KM
    # fk['maxFk']['theoYSlow'] /= DEGREES_TO_KM
    # fk['fstatStruct']['slow'] = [slow / DEGREES_TO_KM for slow in fk['fstatStruct']['slow']]


#Write the file out again
with open(writeFile, 'w') as file:
    file.write(json.dumps(newJsonData))
 
file.close() 
