#!/usr/bin/env bash

# Define the ENV SCRIPT_NAME
# shellcheck disable=SC2034
SCRIPT_NAME=rhel8_post_install_check

START_DIRECTORY="$(
  cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
  pwd -P
)"

. "$START_DIRECTORY/scripts/helper_functions.sh"

echo "Checking the installation"
# Check if the RPM package is installed
check_rpm_installed 'openrouteservice' true || exit 1
# Check the correct directory and file structure
check_file_exists '/opt/openrouteservice/config/ors-config.json' true || exit 1
check_file_exists '/opt/openrouteservice/.elevation-cache/srtm_38_03.gh' true || exit 1
check_file_exists '/opt/openrouteservice/files/osm-file.osm.gz' true || exit 1
# shellcheck disable=SC2016
check_folder_exists '${JWS_HOME}/webapps' true || exit 1
check_folder_exists '/opt/openrouteservice' true || exit 1
check_folder_exists '/opt/openrouteservice/config' true || exit 1
check_folder_exists '/opt/openrouteservice/logs' true || exit 1
check_folder_exists '/opt/openrouteservice/.war-files' true || exit 1
check_folder_exists '/opt/openrouteservice/.elevation-cache' true || exit 1
check_folder_exists '/opt/openrouteservice/files' true || exit 1
check_folder_exists '/opt/openrouteservice/graphs' true || exit 1
check_file_exists "/opt/openrouteservice/.war-files/${ORS_VERSION}_ors.war" true || exit 1
# shellcheck disable=SC2016
check_folder_exists '${JWS_HOME}/webapps/ors' false || exit 1
# shellcheck disable=SC2016
# Check symlink ors.war to webapps folder
check_file_is_symlink '${JWS_HOME}/webapps/ors.war' true || exit 1
# Check user and group setup
check_group_exists 'openrouteservice' true || exit 1
check_user_exists 'openrouteservice' true || exit 1
check_user_exists 'jboss' true || exit 1
check_user_in_group 'jboss' 'openrouteservice' || exit 1
check_user_in_group 'openrouteservice' 'openrouteservice' || exit 1
# Check environment variables
# shellcheck disable=SC2016
check_line_in_file 'export ORS_CONFIG=/opt/openrouteservice/config/ors-config.json' '${JWS_HOME}/bin/setenv.sh' true || exit 1
# Check Java version
check_java_version '17.' || exit 1
# Check for owned content
find_owned_content "/opt/openrouteservice/*" "openrouteservice" "" 6 || exit 1
find_owned_content "/opt/openrouteservice/*" "" "openrouteservice" 6 || exit 1
find_owned_content "/opt/openrouteservice/*" "openrouteservice" "openrouteservice" 6 || exit 1
find_owned_content "/opt/openrouteservice/*" "" "root" 0 || exit 1
find_owned_content "/opt/openrouteservice/*" "root" "" 0 || exit 1
find_owned_content "/opt/openrouteservice/*" "jboss" "" 0 || exit 1
find_owned_content "/opt/openrouteservice/*" "" "jboss" 0 || exit 1
