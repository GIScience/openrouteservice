#!/usr/bin/env bash

# Define the ENV SCRIPT_NAME
# shellcheck disable=SC2034
SCRIPT_NAME=rhel8_post_uninstall_check

START_DIRECTORY="$(
  cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
  pwd -P
)"

. "$START_DIRECTORY/scripts/helper_functions.sh"

# Assume successful at first
SUCCESSFUL=true

# Set variables
JWS_WEBAPPS_DIRECTORY='/var/opt/rh/scls/jws5/lib/tomcat/webapps'
JWS_CONFIGURATION_DIRECTORY='/etc/opt/rh/scls/jws5/tomcat/conf.d'

echo "Checking the environment after uninstallation"
# Check if the RPM package is installed
check_rpm_installed 'openrouteservice-jws5' false || SUCCESSFUL=false
# Check the correct directory and file structure
check_file_exists '${ORS_HOME}/config/example-config.json' true || SUCCESSFUL=false
check_file_exists '${ORS_HOME}/config/ors-config.json' true || SUCCESSFUL=false
check_file_exists '${ORS_HOME}/.elevation-cache/srtm_38_03.gh' true || SUCCESSFUL=false
check_file_exists '${ORS_HOME}/files/osm-file.osm.gz' true || SUCCESSFUL=false
# shellcheck disable=SC2016
# The webapps folder belongs to JWS and shouldn't be removed
check_folder_exists "$JWS_WEBAPPS_DIRECTORY" true || SUCCESSFUL=false
# We leave the folder structure in place. No personal data will be removed
check_folder_exists '${ORS_HOME}/config' true || SUCCESSFUL=false
check_folder_exists '${ORS_HOME}/logs' true || SUCCESSFUL=false
check_folder_exists '${ORS_HOME}/files' true || SUCCESSFUL=false
check_folder_exists '${ORS_HOME}/.elevation-cache' true || SUCCESSFUL=false
check_folder_exists '${ORS_HOME}/.graphs' true || SUCCESSFUL=false
# The webapps/ors folder and the ors.war should not exist. Else ors would still be deployed
# shellcheck disable=SC2016
check_folder_exists "$JWS_WEBAPPS_DIRECTORY/ors" false || SUCCESSFUL=false
# shellcheck disable=SC2016
# Check symlink ors.war to webapps folder
check_file_exists "$JWS_WEBAPPS_DIRECTORY/ors.war" false || SUCCESSFUL=false
# openrouteservice user and group should be removed
check_group_exists 'openrouteservice' false || SUCCESSFUL=false
check_user_exists 'openrouteservice' false || SUCCESSFUL=false
# The tomcat user should still exist
check_user_exists 'tomcat' true || SUCCESSFUL=false
# Check for owned content
find_owned_content '${ORS_HOME}/*' "openrouteservice" "" 0 || SUCCESSFUL=false
find_owned_content '${ORS_HOME}/*' "" "openrouteservice" 0 || SUCCESSFUL=false

# Check environment variables are removed
# shellcheck disable=SC2016
check_file_exists "$JWS_CONFIGURATION_DIRECTORY/openrouteservice.conf" false || SUCCESSFUL=false
check_file_exists '${ORS_HOME}/.openrouteservice-jws5-permanent-state' false || SUCCESSFUL=false

# Fail if any of the checks failed
if [[ "$SUCCESSFUL" == false ]]; then
  log_error "Post-install check failed. Please check the log for more details."
  exit 1
fi