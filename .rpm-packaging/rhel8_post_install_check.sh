#!/usr/bin/env bash

# Define the ENV SCRIPT_NAME
# shellcheck disable=SC2034
SCRIPT_NAME=rhel8_post_install_check

START_DIRECTORY="$(
  cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
  pwd -P
)"

. "$START_DIRECTORY/scripts/helper_functions.sh"

# Assume successful at first and assign false if any of the checks fails
SUCCESSFUL=true


echo "Checking the installation"
# Check if the RPM package is installed
check_rpm_installed 'openrouteservice-jws5' true || SUCCESSFUL=false
# Check the correct directory and file structure
check_file_exists '/opt/openrouteservice/config/example-config.json' true || SUCCESSFUL=false
check_file_exists '/opt/openrouteservice/.elevation-cache/srtm_38_03.gh' true || SUCCESSFUL=false
check_file_exists '/opt/openrouteservice/files/osm-file.osm.gz' true || SUCCESSFUL=false
# shellcheck disable=SC2016
check_folder_exists '${JWS_HOME}/webapps' true || SUCCESSFUL=false
check_folder_exists '/opt/openrouteservice' true || SUCCESSFUL=false
check_folder_exists '/opt/openrouteservice/config' true || SUCCESSFUL=false
check_folder_exists '/opt/openrouteservice/logs' true || SUCCESSFUL=false
check_folder_exists '/opt/openrouteservice/.war-files' true || SUCCESSFUL=false
check_folder_exists '/opt/openrouteservice/.elevation-cache' true || SUCCESSFUL=false
check_folder_exists '/opt/openrouteservice/files' true || SUCCESSFUL=false
check_folder_exists '/opt/openrouteservice/graphs' true || SUCCESSFUL=false
check_file_exists "/opt/openrouteservice/.war-files/${ORS_VERSION}_ors.war" true || SUCCESSFUL=false
check_file_exists '/opt/openrouteservice/config/example-config.json' true || SUCCESSFUL=false

# shellcheck disable=SC2016
check_folder_exists '${JWS_HOME}/webapps/ors' false || SUCCESSFUL=false
# shellcheck disable=SC2016
# Check symlink ors.war to webapps folder
check_file_is_symlink '${JWS_HOME}/webapps/ors.war' true || SUCCESSFUL=false
# Check user and group setup
check_group_exists 'openrouteservice' true || SUCCESSFUL=false
check_user_exists 'openrouteservice' true || SUCCESSFUL=false
check_user_exists 'jboss' true || SUCCESSFUL=false
check_user_in_group 'jboss' 'openrouteservice' || SUCCESSFUL=false
check_user_in_group 'openrouteservice' 'openrouteservice' || SUCCESSFUL=false
# Check environment variables
# shellcheck disable=SC2016
check_line_in_file 'export ORS_CONFIG=/opt/openrouteservice/config/ors-config.json' '${JWS_HOME}/bin/setenv.sh' true || SUCCESSFUL=false
# shellcheck disable=SC2016
check_line_in_file 'export ORS_LOG_LOCATION=/opt/openrouteservice/logs/' '${JWS_HOME}/bin/setenv.sh' true || SUCCESSFUL=false
# Check Java version
check_java_version '17.' || SUCCESSFUL=false
# Check for owned content
find_owned_content "/opt/openrouteservice/*" "openrouteservice" "" 6 || SUCCESSFUL=false
find_owned_content "/opt/openrouteservice/*" "" "openrouteservice" 6 || SUCCESSFUL=false
find_owned_content "/opt/openrouteservice/*" "openrouteservice" "openrouteservice" 6 || SUCCESSFUL=false
find_owned_content "/opt/openrouteservice/*" "" "root" 0 || SUCCESSFUL=false
find_owned_content "/opt/openrouteservice/*" "root" "" 0 || SUCCESSFUL=false
find_owned_content "/opt/openrouteservice/*" "jboss" "" 0 || SUCCESSFUL=false
find_owned_content "/opt/openrouteservice/*" "" "jboss" 0 || SUCCESSFUL=false


# Fail if any of the checks failed
if [[ "$SUCCESSFUL" == false ]]; then
  log_error "Post-install check failed. Please check the logs for more details."
  exit 1
fi