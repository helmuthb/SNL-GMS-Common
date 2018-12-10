#!/bin/sh

scripts_dir="${NIFI_HOME}/scripts"

[ -f "${scripts_dir}/common.sh" ] && . "${scripts_dir}/common.sh"

nifi_host="${NIFI_WEB_HTTP_HOST:-$HOSTNAME}"
nifi_port="${NIFI_WEB_HTTP_PORT:-8080}"

export no_proxy="${no_proxy},${nifi_host}"

# Establish baseline properties
prop_replace 'nifi.web.http.port'               "${nifi_port}"
prop_replace 'nifi.web.http.host'               "${nifi_host}"
prop_replace 'nifi.remote.input.host'           "${NIFI_REMOTE_INPUT_HOST:-$HOSTNAME}"
prop_replace 'nifi.remote.input.socket.port'    "${NIFI_REMOTE_INPUT_SOCKET_PORT:-10000}"
prop_replace 'nifi.remote.input.secure'         'false'

# Add our processors directory
prop_add 'nifi.nar.library.directory.gms'   "${NIFI_HOME}/gms_processors"

tail -F "${NIFI_HOME}/logs/nifi-app.log" &
"${NIFI_HOME}/bin/nifi.sh" run &
nifi_pid="$!"

echo "curling ${nifi_host}:${nifi_port} for availability..."
curl -sSfv "${nifi_host}:${nifi_port}/nifi-api/system-diagnostics" > /dev/null
while [ $? -ne 0 ]; do
	sleep 5
    echo "curling ${nifi_host}:${nifi_port} for availability..."
	curl -sSfv "${nifi_host}:${nifi_port}/nifi-api/system-diagnostics" > /dev/null
done

echo "running python template loading script..."
python3 ${scripts_dir}/load-templates.py

trap "echo Received trapped signal, beginning shutdown...;" KILL TERM HUP INT EXIT;

echo NiFi running with PID ${nifi_pid}.
wait ${nifi_pid}