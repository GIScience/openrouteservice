#!/usr/bin/env bash

echo "Running container as user $(whoami) with id $(id -u) and group $(id -g)"
###### Helper functions ######
# Success message in green
function success() {
  echo -e "\e[32m✓ $1\e[0m"
  return 0
}
# Error message in red
function error() {
  echo -e "\e[31m✗ $1\e[0m"
  exit 1
}
# Warning message in yellow
function warning() {
  echo -e "\e[33m⚠ $1\e[0m"
  return 0
}
# Info message in blue
function info() {
  echo -e "\e[34mⓘ $1\e[0m"
  return 0
}

echo "###### ORS pre-start checks ######"
if [[ -d /ors-core ]] || [[ -d /ors-conf ]]; then
  # In red with a cross echo "Old docker setup found. Please use the new docker setup. Exiting."
  info "Old docker setup found. See for details: https://giscience.github.io/openrouteservice/installation/Running-with-Docker.html"
  error "Please use the new docker setup. Exiting."
else
  success "Using new docker setup"
fi

# Fail if BASE_FOLDER is not set
if [ -z "${BASE_FOLDER}" ]; then
  error "BASE_FOLDER not found. This shouldn't happen. Exiting."
else
  success "BASE_FOLDER found: ${BASE_FOLDER}"
fi

# Users should define ORS_HOME. If not, use /opt/openrouteservice
if [ -z "${ORS_HOME}" ]; then
  info "ORS_HOME not found. Using default: /opt/openrouteservice"
  ORS_HOME="/opt/openrouteservice"
else
  success "ORS_HOME found: ${ORS_HOME}"
fi

# Check that ORS_HOME is not the same as BASE_FOLDER
if [ "${BASE_FOLDER}" = "${ORS_HOME}" ]; then
  error "BASE_FOLDER and ORS_HOME are the same. This shouldn't happen. Exiting."
fi

mkdir -p "${ORS_HOME}"

if ! touch "${ORS_HOME}"/test 2>/dev/null; then
  # Echo error with a red x instead of a check
  error "ORS_HOME is not writable"
else
  success "ORS_HOME is writable"
  rm -rf "${ORS_HOME:?}"/test
fi

# Check for old .json configs
JSON_FILES=$(ls -d -- "${ORS_HOME}/config/"*.json 2>/dev/null)
if [ -n "$JSON_FILES" ]; then
    # Echo in red with a cross
    warning "Old .json config found. They're deprecated and will be replaced in ORS version 8."
    info "Please migrate to the new .yml example."
else
  success "Using new .yml config"
fi

success "All relevant checks passed"
sleep 1

echo "###### Set ORS environment variables ######"
jar_file=${BASE_FOLDER}/lib/ors.jar
graphs=${ORS_HOME}/.graphs



ors_config_location=${ORS_CONFIG_LOCATION:-"${ORS_HOME}/config/ors-config.yml"}
ors_build_graphs=${BUILD_GRAPHS:-"False"}
info "ORS Environment variables:"
info "ORS_CONFIG_LOCATION: ${ors_config_location}"
info "BUILD_GRAPHS: ${ors_build_graphs}"

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

CATALINA_OPTS="-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=${management_jmxremote_port} \
-Dcom.sun.management.jmxremote.rmi.port=${management_jmxremote_rmi_port} \
-Dcom.sun.management.jmxremote.authenticate=${management_jmxremote_authenticate} \
-Dcom.sun.management.jmxremote.ssl=${management_jmxremote_ssl} \
-Djava.rmi.server.hostname=${java_rmi_server_hostname} \
${additional_catalina_opts}"
info "CATALINA_OPTS: ${CATALINA_OPTS}"

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
info "JAVA_OPTS: ${JAVA_OPTS}"



echo "###### ORS folder preparations ######"
mkdir -p "${ORS_HOME}"/{files,logs,config,.graphs,.elevation_cache} || warning "Could not create ${ORS_HOME} and folders"
chown -R "$(whoami)" "${ORS_HOME}" || warning "Could not chown ${ORS_HOME}"
success "Populated ${ORS_HOME} with the folders: files, logs, config, .graphs, .elevation_cache"

# Remove existing graphs if BUILD_GRAPHS is set to True
if [ "${ors_build_graphs}" = "True" ]; then
  rm -rf "${graphs:?}"/*
  warning "Removed existing graphs"
fi

# If BASE_FOLDER is not the same as ORS_HOME, copy the default config and example pbf to ORS_HOME
cp -f "${BASE_FOLDER}/config/example-ors-config.yml" "${ORS_HOME}/config/example-ors-config.yml" || error "Could not copy example-ors-config.yml"
success "Updated the example config in ${ORS_HOME}/conf"
cp -f "${BASE_FOLDER}/files/example_osm_file.pbf" "${ORS_HOME}/files/example_osm_file.gz" || error "Could not copy example_osm_file.pbf"
success "Updated the example osm file in ${ORS_HOME}/data"


echo "###### ORS configuration ######"
# No config found. Use the base config
if [ ! -f "${ors_config_location}" ]; then
  info "No custom ors-config.yml found. Using default: ${ORS_HOME}/config/example-ors-config.yml"
  cp -f "${BASE_FOLDER}/config/example-ors-config.yml" "${ORS_HOME}/config/ors-config.yml" || error "Could not copy example-ors-config.yml"
else
  info "Using custom ors-config.yml: ${ors_config_location}"
  ORS_CONFIG_LOCATION=${ors_config_location}
fi

# No osm file found. Use the base osm file
if [ ! -f "${ORS_HOME}/files/osm-file.osm.gz" ]; then
  info "No custom osm-file.pbf found. Using default: ${ORS_HOME}/files/example_osm_file.osm.gz"
  cp -f "${BASE_FOLDER}/files/example_osm_file.pbf" "${ORS_HOME}/files/osm-file.osm.gz" || error "Could not copy example_osm_file.pbf"

else
  info "Using custom osm-file.osm.gz: ${ORS_HOME}/files/osm-file.osm.gz"
fi

# Start the jar with the given arguments and add the ORS_HOME env var
success "Starting ORS with the following command:"
# Print the full command
info "java ${JAVA_OPTS} ${CATALINA_OPTS}  -DORS_HOME=${ORS_HOME} -DORS_CONFIG_LOCATION=${ORS_CONFIG_LOCATION} -jar ${jar_file}"
# shellcheck disable=SC2086 # we need word splitting here
export ORS_HOME=${ORS_HOME}
exec java ${JAVA_OPTS} ${CATALINA_OPTS}  -DORS_HOME=${ORS_HOME} -jar "${jar_file}" "$@"