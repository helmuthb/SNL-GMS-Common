# -*- coding: utf-8 -*-
import json
import pandas as pd
from pandas.io.json import json_normalize

'''
@param response
@ param args
takes the json_objects and prints them to the screen
formatted
'''


def print_table_to_screen(response, args):

    for json_obj in response:
        d = json_normalize(json_obj)
        print('__ __ __ __ __' + args.mode + ' __ __ __ __ __ __ __ __ __ __')
        # zip keys and numpy values as a pair
        # numpy values are always doubly nested, grab them out of the 0th index
        for k, v in zip(d.keys(), d.values[0]):
            if len(str(v)) > 50:
                print(str(k) + ': ' + 'Data was too long to display')
            else:
                print(str(k) + ': ' + str(v))

'''
@ param d
@ param indent_size
Takes a dictionary and pretty prints it for you using JSON tools.
This is an interim solution until the full tabular format is implemented.
'''


def pretty_print_dict(d, indent_size):
    print(json.dumps(d, indent=indent_size))


'''
make_dict_from_args
@param args
Takes the command line args and returns a dictionary.
'''


def make_dict_from_args(args):
    return vars(args)

'''
invert_dict
@:param d
keys to vals, vals to keys
can handle lists of vals
'''
def invert_dict(d):
    return dict((v, [k]) for k in d for v in d[k])

def normalize_json_obj(jsonObj):
    return json_normalize(jsonObj)
