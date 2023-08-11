#!/usr/bin/env bash

# Define the ENV SCRIPT_NAME
# shellcheck disable=SC2034
SCRIPT_NAME=rhel8_post_install_check

START_DIRECTORY="$(
  cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
  pwd -P
)"

. "$START_DIRECTORY/scripts/helper_functions.sh"

echo "Checking the environment after uninstallation"
# Check if the RPM package is installed
check_rpm_installed 'openrouteservice' false || exit 1
# Check the correct directory and file structure
check_file_exists '/opt/openrouteservice/config/ors-config.json' true || exit 1
check_file_exists '/opt/openrouteservice/.elevation-cache/srtm_38_03.gh' true || exit 1
check_file_exists '/opt/openrouteservice/files/osm-file.osm.gz' true || exit 1
# shellcheck disable=SC2016
# The webapps folder belongs to JWS and shouldn't be removed
check_folder_exists '${JWS_HOME}/webapps' true || exit 1
# We leave the folder structure in place. No personal data will be removed
check_folder_exists '/opt/openrouteservice' true || exit 1
check_folder_exists '/opt/openrouteservice/config' true || exit 1
check_folder_exists '/opt/openrouteservice/logs' true || exit 1
check_folder_exists '/opt/openrouteservice/.war-files' true || exit 1
check_folder_exists '/opt/openrouteservice/.elevation-cache' true || exit 1
check_folder_exists '/opt/openrouteservice/files' true || exit 1
check_folder_exists '/opt/openrouteservice/graphs' true || exit 1
# Check that the war file is deleted
check_file_exists "/opt/openrouteservice/.war-files/${ORS_VERSION}_ors.war" false || exit 1
# The webapps/ors folder and the ors.war should not exist. Else ors would still be deployed
# shellcheck disable=SC2016
check_folder_exists '${JWS_HOME}/webapps/ors' false || exit 1
# shellcheck disable=SC2016
# Check symlink ors.war to webapps folder
check_file_is_symlink '${JWS_HOME}/webapps/ors.war' false || exit 1
# openrouteservice user and group should be removed
check_group_exists 'openrouteservice' false || exit 1
check_user_exists 'openrouteservice' false || exit 1
# The jboss user should still exist
check_user_exists 'jboss' true || exit 1
# shellcheck disable=SC2016
check_line_in_file 'export ORS_CONFIG=' '${JWS_HOME}/bin/setenv.sh' false || exit 1
# Check for owned content
find_owned_content "/opt/openrouteservice/*" "openrouteservice" "" 0 || exit 1
find_owned_content "/opt/openrouteservice/*" "" "openrouteservice" 0 || exit 1
find_owned_content "/opt/openrouteservice/*" "openrouteservice" "openrouteservice" 0 || exit 1
