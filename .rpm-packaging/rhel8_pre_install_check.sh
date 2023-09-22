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

# Set variables
JWS_WEBAPPS_DIRECTORY='/var/opt/rh/scls/jws5/lib/tomcat/webapps'
JWS_CONFIGURATION_DIRECTORY='/etc/opt/rh/scls/jws5/tomcat/conf.d/'

##### Check clean environment #####
echo "Checking the clean environment"
check_folder_exists "$JWS_WEBAPPS_DIRECTORY" true || SUCCESSFUL=false
check_folder_exists "$JWS_WEBAPPS_DIRECTORY/ors" false || SUCCESSFUL=false
check_file_exists "$JWS_WEBAPPS_DIRECTORY/ors.war" false || SUCCESSFUL=false

check_file_exists '/etc/yum.repos.d/ors.repo' true || SUCCESSFUL=false
check_file_exists '${ORS_HOME}/config/example-config.json' false || SUCCESSFUL=false
check_rpm_installed 'openrouteservice-jws5' false || SUCCESSFUL=false

# Check that the temp folder is not present
check_folder_exists '/tmp/openrouteservice' false || SUCCESSFUL=false

# Fail if any of the checks failed
if [[ "$SUCCESSFUL" == false ]]; then
  log_error "Pre-install check failed. Please check the log for more details."
  exit 1
fi