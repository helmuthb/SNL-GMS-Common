"""
Script uses Nifi REST API to submit 'upload', 'import', 'load', and 'run' commands for 
starting NiFi process groups
"""
import json
import os
import sys

import requests
import yaml


def read_config_file(config_file):
    """
    Reads Nifi template configuration files
    :param config_file: Name of the configuration file
    :return: Key-Value Pair
    """
    try:
        with open(os.path.join(os.path.dirname(__file__), config_file), 'r') as cfg_yml_file:
            config = yaml.load(cfg_yml_file)

    except Exception as e:
        sys.exit("...failed to open file: %s" % (str(e)))

    return config


def retrieve_template_paths(directory):
    """
    Collects a list of *.xml template file paths from the directory.
    :param directory: Template directory path
    :return: List of template file paths
    """
    try:
        return [os.path.join(directory, file)
                for file in os.listdir(directory) if file.endswith(".xml")]
    except Exception as e:
        sys.exit("...failed to list directory contents: %s" % (str(e)))


def get_template_metadata(api_url, template_names):
    """
    Retrieves all templates group id and template id
    :param api_url: process-group in python-config.yaml
    :param template_names: list of template names to filter metadata for
    :return: List of dictionaries with template info
    """

    url = "{0}/flow/templates".format(api_url)
    resp = requests.get(url).json()

    resp_templates = [template['template'] for template in resp['templates']]
    template_metadata = [{key: template[key] for key in ['name', 'id', 'groupId']}
                         for template in resp_templates
                         if template['name'] in template_names]

    return template_metadata


def get_root_id(api_url):
    """
    Retrieve Nifi Root Process Group Id
    :param api_url: process-group in python-config.yaml
    :return: root id
    """
    url = "{0}/flow/process-groups/root".format(api_url)
    resp = requests.get(url).json()
    process_root_id = resp["processGroupFlow"]["id"]
    return process_root_id


def upload_templates(api_url, root_id, template_paths):
    """
    Upload NiFi Templates
    :param api_url: process-group in python-config.yaml
    :param root_id: Nifi's root process group id
    :param template_paths: A list of template names
    :return: None
    """
    url = "{0}/process-groups/{1}/templates/upload".format(api_url, root_id)

    for template_path in template_paths:
        resp = requests.post(url, files={'template': open(template_path, 'rb')})

        if resp.status_code == 201:
            print("{0} has been uploaded; return status {1}"
                  .format(template_path, str(resp.status_code)))
        else:
            print("error occurred uploading {0}; return status {1}"
                  .format(template_path, str(resp.status_code)))


def import_templates(api_url, template_names):
    """
    Imports templates into Nifi.
    :param api_url: process-group in python-config.yaml
    :param template_names: Templates to import into Nifi
    :return: None
    """
    header = {'Content-Type': 'application/json'}
    template_metadata = get_template_metadata(api_url, template_names)

    i = 0
    for template in template_metadata:
        url = "{0}/process-groups/{1}/template-instance".format(api_url, template['groupId'])
        data = {"originX": 600.0, "originY": (200.0 * i), "templateId": template['id']}
        resp = requests.post(url, data=json.dumps(data), headers=header)

        if resp.status_code == 201:
            print("{0} has been imported; return status {1}"
                  .format(template['name'], str(resp.status_code)))
            i = i + 1
        else:
            print("error occurred importing {0}; return status {1}"
                  .format(template['name'], str(resp.status_code)))


def get_imported_template_metadata(api_url, template_names):
    """
    Retrieves process group ids from imported templates
    :param api_url: process-group in python-config.yaml
    :param template_names: A list of template names used to retrieve their process group ids
    :return: List of dictionaries with template info
    """
    url = "{0}/flow/process-groups/root".format(api_url)
    resp = requests.get(url).json()
    process_groups = resp['processGroupFlow']['flow']['processGroups']
    template_metadata = [{key: process_group['component'][key] for key in ['name', 'id']}
                         for process_group in process_groups
                         if process_group['component']['name'] in template_names]

    return template_metadata


def run_templates(api_url, template_names):
    """
    Runs Nifi's process group(s). Each processing sequence is assigned a Nifi Process Group Id. The REST interface 
    uses the Process Group Id to start each processing flow sequence. 
    :param api_url: base url for invoking nifi rest calls
    :param template_names: names of imported templates to start processing
    :return: None
    """

    template_metadata = get_imported_template_metadata(api_url, template_names)

    header = {'Content-Type': 'application/json'}
    for template in template_metadata:
        url = "{0}/flow/process-groups/{1}".format(api_url, template['id'])
        data = {'id': template['id'], 'state': 'RUNNING'}
        resp = requests.put(url, data=json.dumps(data), headers=header)

        if resp.status_code == 200:
            print("{0} successfully started; return status {1}"
                  .format(template['name'], str(resp.status_code)))
        else:
            print("error occurred starting {0}; return status {1}"
                  .format(template['name'], str(resp.status_code)))
