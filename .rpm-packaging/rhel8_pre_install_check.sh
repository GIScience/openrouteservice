#!/usr/bin/env bash

# Define the ENV SCRIPT_NAME
# shellcheck disable=SC2034
SCRIPT_NAME=rhel8_pre_install_check

START_DIRECTORY="$(
  cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
  pwd -P
)"

. "$START_DIRECTORY/scripts/helper_functions.sh"

# Assume successful at first and assign false if any of the checks fails
SUCCESSFUL=true

##### Check clean environment #####
echo "Checking the clean environment"
# shellcheck disable=SC2016
check_folder_exists '${JWS_HOME}/webapps/ors' false || SUCCESSFUL=false
# shellcheck disable=SC2016
check_file_exists '${JWS_HOME}/webapps/ors.war' false || SUCCESSFUL=false
check_file_exists '/etc/yum.repos.d/ors.repo' true || SUCCESSFUL=false
check_rpm_installed 'openrouteservice-jws5' false || SUCCESSFUL=false
# shellcheck disable=SC2016
check_line_in_file 'export ORS_CONFIG=' '${JWS_HOME}/bin/setenv.sh' false || SUCCESSFUL=false

# Fail if any of the checks failed
if [[ "$SUCCESSFUL" == false ]]; then
  log_error "Pre-install check failed. Please check the log for more details."
  exit 1
fi