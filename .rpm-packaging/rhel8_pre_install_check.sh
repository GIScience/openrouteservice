#!/usr/bin/env bash

# Define the ENV SCRIPT_NAME
# shellcheck disable=SC2034
SCRIPT_NAME=rhel8_pre_install_check

START_DIRECTORY="$(
  cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
  pwd -P
)"

. "$START_DIRECTORY/scripts/helper_functions.sh"

##### Check clean environment #####
echo "Checking the clean environment"
# shellcheck disable=SC2016
check_folder_exists '${JWS_HOME}/webapps/ors' false || exit 1
# shellcheck disable=SC2016
check_file_exists '${JWS_HOME}/webapps/ors.war' false || exit 1
check_file_exists '/tmp/ors.rpm' true || exit 1
check_rpm_installed 'openrouteservice' false || exit 1
check_line_in_file 'export ORS_CONFIG=' '${JWS_HOME}/bin/setenv.sh' false || exit 1
