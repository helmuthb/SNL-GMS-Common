import nifi_rest as nifi
import os
# ---------------Auto Script Example----------------------

python_config_file = "python-config.yaml"
python_startup_config_file = "python-startup-config.yaml"

# Read in configuration file(s) and override with environment variables
cfg = nifi.read_config_file(python_config_file)

cfg["api_host"] = os.environ["NIFI_WEB_HTTP_HOST"] if "NIFI_WEB_HTTP_HOST" in os.environ else os.environ["HOSTNAME"]
cfg["api_port"] = os.environ["NIFI_WEB_HTTP_PORT"] if "NIFI_WEB_HTTP_PORT" in os.environ else 8080

cfg["api_url"] = "http://{0}:{1}/nifi-api".format(cfg["api_host"], cfg["api_port"])

startup_cfg = nifi.read_config_file(python_startup_config_file)

# Assign Key-Value configuration values to variables
api_url = cfg['api_url']
template_dir = cfg['template-dir']
templates_to_start = startup_cfg['templates-to-start']

# Collect a list of templates from template directory to upload
template_paths = nifi.retrieve_template_paths(template_dir)

# Upload template(s) - all templates found in the templates directory
root_id = nifi.get_root_id(api_url)
nifi.upload_templates(api_url, root_id, template_paths)

# Import template(s) - only templates found in 'python-startup-config.yaml'
nifi.import_templates(api_url, templates_to_start)

# Run Nifi Sequence
nifi.run_templates(api_url, templates_to_start)
