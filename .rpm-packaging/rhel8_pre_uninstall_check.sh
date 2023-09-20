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

# Fail if any of the checks failed
if [[ "$SUCCESSFUL" == false ]]; then
  log_error "Post-install check failed. Please check the log for more details."
  exit 1
fi