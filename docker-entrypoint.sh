#!/usr/bin/env bash

########################
# Set Helper functions #
########################
# Log level functions
CONTAINER_LOG_LEVEL=${CONTAINER_LOG_LEVEL:-"INFO"}
# Success message in green. Always printed
function success() {
  echo -e "\e[32m✓ $1\e[0m"
  return 0
}
# Critical message in bold red and exit. Always printed
function critical() {
  echo -e "\e[1;31m $1\e[0m"
  exit 1
}
# Error message in red.
function error() {
  echo -e "\e[31m✗ $1\e[0m"
  return 0
}
# Warning message in yellow
function warning() {
  if [ "${container_log_level_int}" -le 30 ]; then
    echo -e "\e[33m⚠ $1\e[0m"
  fi
  return 0
}
# Info message in blue
function info() {
  if [ "${container_log_level_int}" -le 20 ]; then
    echo -e "\e[34mⓘ $1\e[0m"
  fi
  return 0
}
# Debug message in cyan
function debug() {
  if [ "${container_log_level_int}" -le 10 ]; then
    echo -e "\e[36m▢ $1\e[0m"
  fi
  return 0
}
function set_log_level() {
  case ${CONTAINER_LOG_LEVEL} in
  "DEBUG")
    container_log_level_int=10
    ;;
  "INFO")
    container_log_level_int=20
    ;;
  "WARN")
    container_log_level_int=30
    ;;
  "ERROR")
    container_log_level_int=40
    ;;
  "CRITICAL")
    container_log_level_int=50
    ;;
  *)
    debug "No matching log level found: ${CONTAINER_LOG_LEVEL}."
    debug "Defaulting to INFO."
    CONTAINER_LOG_LEVEL="INFO"
    container_log_level_int=20
    ;;
  esac
  success "CONTAINER_LOG_LEVEL: ${CONTAINER_LOG_LEVEL}. Set CONTAINER_LOG_LEVEL=DEBUG for more details."
}

update_file() {
  local target_file_path="$1"
  local original_file_path="$2"
  # Default to false
  local print_migration_info_on_update="${3:-false}"

  if [ ! -f "${target_file_path}" ] || ! cmp -s "${original_file_path}" "${target_file_path}"; then
    success "Update the file ${target_file_path} with ${original_file_path}"
    cp -f "${original_file_path}" "${target_file_path}" || warning "Could not copy ${original_file_path} to ${target_file_path}"
    if [ "${print_migration_info_on_update}" = "true" ]; then
      print_migration_info="true"
    fi
  else
    success "The file ${target_file_path} is up to date"
  fi
}

extract_config_info() {
  local config_location="$1"
  local config_variable="$2"
  local config_value=""
  if [[ "${config_location}" = *.yml ]]; then
    config_value=$(yq -r "${config_variable}" "${config_location}")
  elif [[ "${config_location}" = *.json ]]; then
    config_value=$(jq -r "${config_variable}" "${config_location}")
  fi
  # Validate the config value
  if [ -z "${config_value}" ] || [ "${config_value}" = null ]; then
    config_value=""
  fi
  # Return the value
  echo "${config_value}"
}

echo "#################"
echo "# Container ENV #"
echo "#################"
# Validate the log level
set_log_level

# Set the jar file location
jar_file=/ors.jar
BUILD_GRAPHS=${BUILD_GRAPHS:-"false"}
REBUILD_GRAPHS=${REBUILD_GRAPHS:-"false"}
# If BUILD_GRAPHS is set to true, we need to set ors_rebuild_graphs to true and print an info about migration to REBUILD_GRAPHS
if [ "${BUILD_GRAPHS}" = "true" ]; then
  ors_rebuild_graphs="true"
  warning "BUILD_GRAPHS is deprecated and will be removed in the future."
  warning "Please use REBUILD_GRAPHS instead."
elif [ "${REBUILD_GRAPHS}" = "true" ]; then
  ors_rebuild_graphs="true"
else
  ors_rebuild_graphs="false"
fi

# Parse the ors.* properties
env | while read -r line; do
  debug "${line}"
done
info "Any config file settings can be overwritten by environment variables."
info "Use 'CONTAINER_LOG_LEVEL=DEBUG' to see the full list of active environment variables for this container."

echo "###########################"
echo "# Container sanity checks #"
echo "###########################"
info "Running container as user $(whoami) with id $(id -u) and group $(id -g)"
if [[ $(id -u) -eq 0 ]] || [[ $(id -g) -eq 0 ]] ; then
  debug "User and group are set to root with id 0 and group 0."
elif [[ $(id -u) -eq 1000 ]] || [[ $(id -g) -eq 1000 ]] ; then
  debug "User and group are set to 1000 and group 1000."
else
  # Test if the user tampered with the user and group settings
  warning "Running container as user '$(whoami)' with id $(id -u) and group $(id -g)"
  warning "Changing these values is only recommended if you're an advanced docker user and can handle file permission issues yourself."
  warning "Consider leaving the user and group options as root with 0:0 or 1000:1000 or avoid that setting completely."
  warning "If you need to change the user and group, make sure to rebuild the docker image with the appropriate UID,GID build args."
fi

if [[ -d /ors-core ]] || [[ -d /ors-conf ]]; then
  warning "Found remnants of old docker setup."
  warning "The ors-core and ors-conf folders are not used by default with the new docker setup."
  warning "Continuing with the new docker setup."
fi

# Fail if ORS_HOME env var is not set or if it is empty or set to /
if [ -z "${ORS_HOME}" ] || [ "${ORS_HOME}" = "/" ]; then
  critical "ORS_HOME is not set or empty or set to /. This is not allowed. Exiting."
fi

mkdir -p "${ORS_HOME}" || critical "Could not create ${ORS_HOME}"

# Make sure ORS_HOME is a directory
if [ ! -d "${ORS_HOME}" ]; then
  critical "ORS_HOME: ${ORS_HOME} doesn't exist. Exiting."
elif [ ! -w "${ORS_HOME}" ]; then
  error "ORS_HOME: ${ORS_HOME} is not writable."
  error "Make sure the file permission of ${ORS_HOME} in your volume mount are set to $(id -u):$(id -g)."
  error "Under linux the command for a volume would be: sudo chown -R $(id -u):$(id -g) /path/to/ors/volume"
  critical "Exiting."
fi

success "ORS_HOME: ${ORS_HOME} exists and is writable."

mkdir -p "${ORS_HOME}"/{files,logs,graphs,elevation_cache,config} || warning "Could not create the default folders in ${ORS_HOME}: files, logs, graphs, elevation_cache, config"
debug "Populated ORS_HOME=${ORS_HOME} with the default folders: files, logs, graphs, elevation_cache, config"

# Check if the original jar file exists
if [ ! -f "${jar_file}" ]; then
  critical "Jar file not found. This shouldn't happen. Exiting."
fi

# get ors.engine.graphs_root_path=. Dot notations in bash are not allowed, so we need to use awk to parse it.
ors_engine_graphs_root_path=$(env | grep "^ors\.engine\.graphs_root_path=" | awk -F '=' '{print $2}')
# get ors.engine.elevation.cache_path
ors_engine_elevation_cache_path=$(env | grep "^ors\.engine\.elevation\.cache_path=" | awk -F '=' '{print $2}')
# get ors.engine.source_file
ors_engine_source_file=$(env | grep "^ors\.engine\.source_file=" | awk -F '=' '{print $2}')

# Check that ors_engine_graphs_root_path is not empty and not set to /
if [ -n "${ors_engine_graphs_root_path}" ] && [ "${ors_engine_graphs_root_path}" = "/" ]; then
  critical "ors.engine.graphs_root_path is set to /. This is not allowed. Exiting."
else
  debug "ors.engine.graphs_root_path=${ors_engine_graphs_root_path} is set and not empty and not set to /"
fi

# Check that ors_engine_elevation_cache_path is not empty and not set to /
if [ -n "${ors_engine_elevation_cache_path}" ] && [ "${ors_engine_elevation_cache_path}" = "/" ]; then
  critical "ors.engine.elevation.cache_path is set to /. This is not allowed. Exiting."
else
  debug "ors.engine.elevation.cache_path=${ors_engine_elevation_cache_path} is set and not empty and not set to /"
fi

# Update the example-ors-config.env and example-ors-config.yml files if they don't exist or have changed
update_file "${ORS_HOME}/config/example-ors-config.env" "/example-ors-config.env" "true"
update_file "${ORS_HOME}/config/example-ors-config.yml" "/example-ors-config.yml" "true"

# The config situation is difficult due to the recent ors versions.
# To ensure a smooth transition, we need to check if the user is using a .json file or a .yml file.
# If neither is set, we need to print a migration info and default to the example-ors-config.env file.
# get ors_config_location
ors_config_location=${ORS_CONFIG_LOCATION:-""}
# Unset the ORS_CONFIG_LOCATION to not interfere with the spring-boot application
unset ORS_CONFIG_LOCATION
# Check if ors_config_location is a .json file and exists
if [[ "${ors_config_location}" = *.yml ]] && [[ -f "${ors_config_location}" ]]; then
  success "Using yml config from ENV: ${ors_config_location}"
elif [[ "${ors_config_location}" = *.json ]] && [[ -f "${ors_config_location}" ]]; then
  success "Using json config from ENV: ${ors_config_location}"
  # Print the above warning message in individual warning calls
  warning ".json configurations are deprecated and will be removed in the future."
  print_migration_info="true"
elif [[ -f ${ORS_HOME}/config/ors-config.yml ]]; then
    success "Using the existing ors-config.yml from: ${ORS_HOME}/config/ors-config.yml"
    ors_config_location="${ORS_HOME}/config/ors-config.yml"
else
  warning "No config file found. Copying /example-ors-config.yml to ${ORS_HOME}/config/ors-config.yml"
  warning "To adjust your config edit ors-config.yml in your 'config' docker volume or use the environment variable configuration."
  update_file "${ORS_HOME}/config/ors-config.yml" "/example-ors-config.yml"
  ors_config_location="${ORS_HOME}/config/ors-config.yml"
fi

# Get relevant configuration information from the .yml or .json file
if [[ -z "${ors_engine_graphs_root_path}" ]]; then
  if [[ "${config_location}" = *.yml ]]; then
    ors_engine_graphs_root_path=$(extract_config_info "${ors_config_location}" '.ors.engine.graphs_root_path')
  elif [[ "${config_location}" = *.json ]]; then
    ors_engine_graphs_root_path=$(extract_config_info "${ors_config_location}" '.ors.services.routing.profiles.default_params.graphs_root_path')
  fi
fi

if [[ -z "${ors_engine_source_file}" ]]; then
  if [[ "${ors_config_location}" = *.yml ]]; then
    ors_engine_source_file=$(extract_config_info "${ors_config_location}" '.ors.engine.source_file')
  elif [[ "${ors_config_location}" = *.json ]]; then
    ors_engine_source_file=$(extract_config_info "${ors_config_location}" '.ors.services.routing.sources[0]')
  fi
fi

if [ -n "${ors_engine_graphs_root_path}" ]; then
  success "Using graphs folder ${ors_engine_graphs_root_path}"
else
  info "Default to graphs folder: ${ORS_HOME}/graphs"
  ors_engine_graphs_root_path="${ORS_HOME}/graphs"
fi

if [ -n "${ors_engine_source_file}" ]; then
  debug "OSM source file set to ${ors_engine_source_file}"
  # Check if it is the example file in root or the home folder
  if [[ "${ors_engine_source_file}" = "${ORS_HOME}/files/example-heidelberg.osm.gz" ]]; then
    info "Default to example osm source file: \"${ors_engine_source_file}\""
  fi
fi

info "Any ENV variables will have precedence over configuration variables from config files."
success "All checks passed. For details set CONTAINER_LOG_LEVEL=DEBUG."

echo "#####################################"
echo "# Container file system preparation #"
echo "#####################################"
# Check if uid or gid is different from 1000
chown -R "$(whoami)" "${ORS_HOME}"; debug "Changed ownership of ${ORS_HOME} to $(whoami)" || warning "Could not change ownership of ${ORS_HOME} to $(whoami)"


update_file "${ORS_HOME}/files/example-heidelberg.osm.gz" "/heidelberg.osm.gz"

# Remove existing graphs if BUILD_GRAPHS is set to true
if [ "${ors_rebuild_graphs}" = "true" ]; then
  # Warn if ors.engine.graphs_root_path is not set or empty
  if [ -z "${ors_engine_graphs_root_path}" ]; then
    warning "graphs_root_path is not set or could not be found. Skipping cleanup."
  elif [ -d "${ors_engine_graphs_root_path}" ]; then
    # Check the ors.engine.graphs_root_path folder exists
    rm -rf "${ors_engine_graphs_root_path:?}"/* || warning "Could not remove ${ors_engine_graphs_root_path}"
    success "Removed graphs at ${ors_engine_graphs_root_path}/*."
  else
    debug "${ors_engine_graphs_root_path} does not exist (yet). Skipping cleanup."
  fi
  # Create the graphs folder again
  mkdir -p "${ors_engine_graphs_root_path}" || warning "Could not populate graph folder at ${ors_engine_graphs_root_path}"
fi

success "Container file system preparation complete. For details set CONTAINER_LOG_LEVEL=DEBUG."

echo "#######################################"
echo "# Prepare CATALINA_OPTS and JAVA_OPTS #"
echo "#######################################"
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
debug "CATALINA_OPTS: ${CATALINA_OPTS}"

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
debug "JAVA_OPTS: ${JAVA_OPTS}"
success "CATALINA_OPTS and JAVA_OPTS ready. For details set CONTAINER_LOG_LEVEL=DEBUG."

# Print the migration info if print_migration_info is set to true but not if PRINT_MIGRATION_INFO is set to False
if [ "${print_migration_info}" = "true" ]; then
  info "##########################################"
  info "# Config options and migration information #"
  info "##########################################"
  info ">>> Migration information <<<"
  warning "Configuring ors with a .json config is deprecated and will be removed in the future."
  info "You can use the ors-config-migration tool to migrate your .json config to .yml: https://github.com/GIScience/ors-config-migration#usage"
  info ">>> Config options <<<"
  info "You have the following options to configure ORS:"
  info "Method 1 yml config:"
  info "> docker cp ors-container-name:${ORS_HOME}/config/example-ors-config.yml ./ors-config.yml"
  info "> docker run --name example-ors-instance-conf-file -e ORS_CONFIG_LOCATION=${ORS_HOME}/config/ors-config.yml -v \$(pwd)/ors-config.yml:${ORS_HOME}/config/ors-config.yml openrouteservice/openrouteservice:latest"
  info "Method 2 environment variables:"
  info "> docker cp ors-container-name:${ORS_HOME}/config/example-ors-config.env ./ors-config.env"
  info "> docker run --name example-ors-instance-env-file --env-file ors-config.env openrouteservice/openrouteservice:latest"
  info ">>> End of migration information <<<"
fi

echo "#####################"
echo "# ORS startup phase #"
echo "#####################"
# Start the jar with the given arguments and add the ORS_HOME env var
success "🙭 Ready to start the ORS application 🙭"
debug "Startup command: java ${JAVA_OPTS} ${CATALINA_OPTS} -jar ${jar_file}"
# Export ORS_CONFIG_LOCATION to the environment of child processes
export ORS_CONFIG_LOCATION=${ors_config_location}
# shellcheck disable=SC2086 # we need word splitting here
exec java ${JAVA_OPTS} ${CATALINA_OPTS} -jar "${jar_file}" "$@"
