#!/usr/bin/env bash

echo "Running container as user $(whoami) with id $(id -u)"

echo "###### ORS pre-start checks ######"
# Check for old .json configs
JSON_FILES=$(ls -d -- "${ORS_HOME}/config/"*.json 2>/dev/null)
if [ -n "$JSON_FILES" ]; then
    echo "Old .json config found. They're deprecated and will be replaced in ORS version 8."
    echo "Please migrate to the new .yml example."
fi

if [[ -d /ors-core ]] || [[ -d /ors-conf ]]; then
  echo "You're mounting old paths. Remove them and migrate to the new docker setup: https://giscience.github.io/openrouteservice/installation/Running-with-Docker.html"
  echo "Exit setup due to old folders /ors-core or /ors-conf being mounted"
  sleep 5
  exit 1
fi

# Fail if BASE_FOLDER is not set
if [ -z "${BASE_FOLDER}" ]; then
  echo "BASE_FOLDER not set. This shouldn't happening. Exiting."
  exit 1
fi

echo "###### Set ORS environment variables ######"
jar_file=${BASE_FOLDER}/lib/ors.jar
graphs=${BASE_FOLDER}/graphs

# Users can define their own ORS_HOME. If not, use the default.
if [ -z "${ORS_HOME}" ]; then
  echo "ORS_HOME not found. Using default: ${BASE_FOLDER}"
  export ORS_HOME=${BASE_FOLDER}
fi

ors_config_location=${ORS_CONFIG_LOCATION:-"${ORS_HOME}/config/ors-config.yml"}
ors_build_graphs=${BUILD_GRAPHS:-"False"}

# Let the user define every parameter via env vars if not, default to the values below
management_jmxremote_port=${MANAGEMENT_JMXREMOTE_PORT:-9001}
management_jmxremote_rmi_port=${MANAGEMENT_JMXREMOTE_RMI_PORT:-9001}
management_jmxremote_authenticate=${MANAGEMENT_JMXREMOTE_AUTHENTICATE:-false}
management_jmxremote_ssl=${MANAGEMENT_JMXREMOTE_SSL:-false}
java_rmi_server_hostname=${JAVA_RMI_SERVER_HOSTNAME:-localhost}
additional_catalina_opts=${ADDITIONAL_CATALINA_OPTS:-""}
# Let the user define every parameter via env vars if not, default to the values below
target_survivor_ratio=${TARGET_SURVIVOR_RATIO:-75}
survivor_ratio=${SURVIVOR_RATIO:-64}
max_tenuring_threshold=${MAX_TENURING_THRESHOLD:-3}
parallel_gc_threads=${PARALLEL_GC_THREADS:-4}
xms=${XMS:-1g}
xmx=${XMX:-2g}
additional_java_opts=${ADDITIONAL_JAVA_OPTS:-""}

echo "###### Construct CATALINA_OPTS and JAVA_OPTS ######"
CATALINA_OPTS="-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=${management_jmxremote_port} \
-Dcom.sun.management.jmxremote.rmi.port=${management_jmxremote_rmi_port} \
-Dcom.sun.management.jmxremote.authenticate=${management_jmxremote_authenticate} \
-Dcom.sun.management.jmxremote.ssl=${management_jmxremote_ssl} \
-Djava.rmi.server.hostname=${java_rmi_server_hostname} \
${additional_catalina_opts}"
echo "CATALINA_OPTS: ${CATALINA_OPTS}"

JAVA_OPTS="-Djava.awt.headless=true \
-server -XX:TargetSurvivorRatio=${target_survivor_ratio} \
-XX:SurvivorRatio=${survivor_ratio} \
-XX:MaxTenuringThreshold=${max_tenuring_threshold} \
-XX:+UseG1GC \
-XX:+ScavengeBeforeFullGC \
-XX:ParallelGCThreads=${parallel_gc_threads} \
-Xms${xms} \
-Xmx${xmx} \
${additional_java_opts}"
echo "JAVA_OPTS: ${JAVA_OPTS}"



echo "###### ORS data preparation ######"
echo "Populating ORS_HOME with default files and folders"
mkdir -p "${ORS_HOME}"/{data,logs,graphs,conf} || echo "Could not create ${ORS_HOME} and folders"
chown -R "$(whoami)" "${ORS_HOME}" "${BASE_FOLDER}" || echo "Could not chown ${ORS_HOME} and ${BASE_FOLDER}"

# Remove existing graphs if BUILD_GRAPHS is set to True
if [ "${ors_build_graphs}" = "True" ]; then
  echo "BUILD_GRAPHS set to True. Removing existing graphs"
  rm -rf "${graphs:?}"/*
fi

# If BASE_FOLDER is not the same as ORS_HOME, copy the default config to ORS_HOME
if [ "${BASE_FOLDER}" != "${ORS_HOME}" ]; then
  echo "Updating the example config in ${ORS_HOME}/conf"
  cp -f "${BASE_FOLDER}/config/example-ors-config.yml" "${ORS_HOME}/config/example-ors-config.yml"
fi

echo "###### ORS configuration ######"
# No config found. Use the base config
if [ ! -f "${ors_config_location}" ]; then
  echo "No ors-config.yml found. Using default: ${ORS_HOME}/config/example-ors-config.yml"
  cp -f "${ORS_HOME}/config/example-ors-config.yml" "${ORS_HOME}/config/ors-config.yml"
else
  echo "Using custom ors-config.yml: ${ors_config_location}"
  ORS_CONFIG_LOCATION=${ors_config_location}
fi

# No osm file found. Use the base osm file
if [ ! -f "${ORS_HOME}/files/osm-file.osm.gz" ]; then
  echo "No osm-file.pbf found. Using default: ${ORS_HOME}/files/example_osm_file.osm.gz"
  cp -f "${ORS_HOME}/files/example_osm_file.pbf" "${ORS_HOME}/files/osm-file.osm.gz"
else
  echo "Using custom osm-file.osm.gz: ${ORS_HOME}/files/osm-file.osm.gz"
fi

# Start the jar with the given arguments and add the ORS_HOME env var
echo "Starting ORS with ${jar_file} and ORS_HOME=${ORS_HOME}"
# shellcheck disable=SC2086 # we need word splitting here
exec java ${JAVA_OPTS} ${CATALINA_OPTS}  -DORS_HOME="${ORS_HOME}" -DORS_CONFIG_LOCATION="${ORS_CONFIG_LOCATION}" -jar "${jar_file}" "$@"