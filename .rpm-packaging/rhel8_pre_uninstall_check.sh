#!/usr/bin/env bash

# Define the ENV SCRIPT_NAME
# shellcheck disable=SC2034
SCRIPT_NAME=rhel8_pre_uninstall_check

START_DIRECTORY="$(
  cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
  pwd -P
)"

. "$START_DIRECTORY/scripts/helper_functions.sh"

# Assume successful at first
SUCCESSFUL=true

echo "Checking the environment before uninstallation"
# Check the log file has been created in the correct location
check_file_exists '${ORS_HOME}/logs/ors.log' true || SUCCESSFUL=false
check_file_exists '${ORS_HOME}/.openrouteservice-jws5-state' true || SUCCESSFUL=false
# Check the state file is created and contains the correct variables
check_file_exists '${ORS_HOME}/.openrouteservice-jws5-state' true || SUCCESSFUL=false
check_line_in_file "jws_webapps_folder=" '${ORS_HOME}/.openrouteservice-jws5-state' true || SUCCESSFUL=false
check_line_in_file "jws_config_location=" '${ORS_HOME}/.openrouteservice-jws5-state' true || SUCCESSFUL=false
check_line_in_file "min_ram=" '${ORS_HOME}/.openrouteservice-jws5-state' true || SUCCESSFUL=false
check_line_in_file "max_ram=" '${ORS_HOME}/.openrouteservice-jws5-state' true || SUCCESSFUL=false

# Fail if any of the checks failed
if [[ "$SUCCESSFUL" == false ]]; then
  log_error "Post-install check failed. Please check the log for more details."
  exit 1
fi