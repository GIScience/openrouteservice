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

# Set variables
JWS_WEBAPPS_DIRECTORY='/var/opt/rh/scls/jws5/lib/tomcat/webapps'
JWS_CONFIGURATION_DIRECTORY='/etc/opt/rh/scls/jws5/tomcat/conf.d'


echo "Checking the installation"
# Check if the RPM package is installed
check_rpm_installed 'openrouteservice-jws5' true || SUCCESSFUL=false
# Check the correct directory and file structure
check_file_exists '${ORS_HOME}/config/example-config.json' true || SUCCESSFUL=false
check_file_exists '${ORS_HOME}/.elevation-cache/srtm_38_03.gh' true || SUCCESSFUL=false
check_file_exists '${ORS_HOME}/files/osm-file.osm.gz' true || SUCCESSFUL=false
# shellcheck disable=SC2016
check_folder_exists "$JWS_WEBAPPS_DIRECTORY" true || SUCCESSFUL=false
check_folder_exists '${ORS_HOME}' true || SUCCESSFUL=false
check_folder_exists '${ORS_HOME}/config' true || SUCCESSFUL=false
check_folder_exists '${ORS_HOME}/logs' true || SUCCESSFUL=false
check_folder_exists '${ORS_HOME}/.elevation-cache' true || SUCCESSFUL=false
check_folder_exists '${ORS_HOME}/files' true || SUCCESSFUL=false
check_folder_exists '${ORS_HOME}/.graphs' true || SUCCESSFUL=false
check_file_exists '${ORS_HOME}/config/example-config.json' true || SUCCESSFUL=false

# Check the state file is created and contains the correct variables
check_file_exists '${ORS_HOME}/.openrouteservice-jws5-permanent-state' true || SUCCESSFUL=false
check_line_in_file "jws_webapps_folder=${JWS_WEBAPPS_DIRECTORY}" '${ORS_HOME}/.openrouteservice-jws5-permanent-state' true || SUCCESSFUL=false
check_line_in_file "jws_config_location=${JWS_CONFIGURATION_DIRECTORY}" '${ORS_HOME}/.openrouteservice-jws5-permanent-state' true || SUCCESSFUL=false
check_line_in_file "min_ram=" '${ORS_HOME}/.openrouteservice-jws5-permanent-state' true || SUCCESSFUL=false
check_line_in_file "max_ram=" '${ORS_HOME}/.openrouteservice-jws5-permanent-state' true || SUCCESSFUL=false

# shellcheck disable=SC2016
# Check ors.war in webapps folder
check_file_exists "$JWS_WEBAPPS_DIRECTORY/ors.war" true || SUCCESSFUL=false
# Check user and group setup
check_group_exists 'openrouteservice' true || SUCCESSFUL=false
check_user_exists 'openrouteservice' true || SUCCESSFUL=false
check_user_exists 'tomcat' true || SUCCESSFUL=false
check_user_in_group 'tomcat' 'openrouteservice' || SUCCESSFUL=false
check_user_in_group 'openrouteservice' 'openrouteservice' || SUCCESSFUL=false
# Check environment variables
# shellcheck disable=SC2016
check_line_in_file "ORS_HOME=/opt/openrouteservice" "$JWS_CONFIGURATION_DIRECTORY/openrouteservice.conf" true || SUCCESSFUL=false
check_line_in_file "CATALINA_OPTS=" "$JWS_CONFIGURATION_DIRECTORY/openrouteservice.conf" true || SUCCESSFUL=false

# Check Java version
check_java_version '17.' || SUCCESSFUL=false
# Check for owned content
find_owned_content '${ORS_HOME}/*' "openrouteservice" "" 5 || SUCCESSFUL=false
find_owned_content '${ORS_HOME}/*' "" "openrouteservice" 5 || SUCCESSFUL=false
find_owned_content '${ORS_HOME}/*' "openrouteservice" "openrouteservice" 5 || SUCCESSFUL=false
find_owned_content '${ORS_HOME}/*' "" "root" 0 || SUCCESSFUL=false
find_owned_content '${ORS_HOME}/*' "root" "" 0 || SUCCESSFUL=false
find_owned_content '${ORS_HOME}/*' "tomcat" "" 0 || SUCCESSFUL=false
find_owned_content '${ORS_HOME}/*' "" "tomcat" 0 || SUCCESSFUL=false
# Check selinux labels
check_selinux_label '${ORS_HOME}/.elevation-cache'                        "" "" "" "" 0 || SUCCESSFUL=false # change to "*" "*" "jws5_var_lib_t" "*" when container has selinux enabled
check_selinux_label '${ORS_HOME}/.graphs'                                 "" "" "" "" 0 || SUCCESSFUL=false # change to "*" "*" "jws5_var_lib_t" "*" when container has selinux enabled
check_selinux_label '${ORS_HOME}/.openrouteservice-jws5-permanent-state'  "" "" "" "" 0 || SUCCESSFUL=false # change to "*" "*" "jws5_var_lib_t" "*" when container has selinux enabled
check_selinux_label '${ORS_HOME}/config'                                  "" "" "" "" 0 || SUCCESSFUL=false # change to "*" "*" "jws5_var_lib_t" "*" when container has selinux enabled
check_selinux_label '${ORS_HOME}/files'                                   "" "" "" "" 0 || SUCCESSFUL=false # change to "*" "*" "jws5_var_lib_t" "*" when container has selinux enabled
check_selinux_label '${ORS_HOME}/logs'                                    "" "" "" "" 0 || SUCCESSFUL=false # change to "*" "*" "jws5_var_lib_t" "*" when container has selinux enabled

# Fail if any of the checks failed
if [[ "$SUCCESSFUL" == false ]]; then
  log_error "Post-install check failed. Please check the logs for more details."
  exit 1
fi