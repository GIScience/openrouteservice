#!/usr/bin/env bash
#################################
# Description: Package related functions
# Author: Julian Psotta
# Date: 2024-02-24
# #################################

# Fail if CONTAINER_NAME is empty or not one of podman or docker
if [ -z "$CONTAINER_ENGINE" ] || [ "$CONTAINER_ENGINE" != "podman" ] && [ "$CONTAINER_ENGINE" != "docker" ]; then
    log_error "Please set the CONTAINER_ENGINE variable to either podman or docker."
    return 1
fi

# Function to check the version of the installed and activated java package.
# Usage: check_java_version <java_version>
check_java_version() {
    local java_version="$1"

    local result
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "java -version 2>&1 | grep \"openjdk version \\\"$java_version.\"")
    if [[ -z "$result" ]]; then
        log_error "Java version should be $java_version but is not."
        return 1
    fi
    log_success "Java version is $java_version as expected"
}

# Function to check if an RPM package is installed
# Usage: check_rpm_installed <package>
check_rpm_installed() {
    local package="$1"
    local should_be_installed="$2"

    local result
    local exit_code
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "rpm -q $package")
    exit_code=$?
    if [[ "$should_be_installed" = "true" && $exit_code -ne 0 ]]; then
        log_error "Package $package should be installed but is not. Message was: $result"
        return 1
    elif [[ "$should_be_installed" = "false" && $exit_code -eq 0 ]]; then
        log_error "Package $package should not be installed but is. Message was: $result"
        return 1
    fi
    # If should exist is true, the logging should be different
    if [[ "$should_be_installed" = "true" ]]; then
        log_success "Package $package is installed as expected."
    else
        log_success "Package $package is not installed as expected."
    fi
}

# Function to check if a systemd service is enabled
# Usage: check_systemd_service_enabled <service>
check_systemd_service_enabled() {
    local service="$1"
    local should_be_enabled="$2"

    local result
    local exit_code
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "systemctl is-enabled --quiet $service")
    exit_code=$?
    if [[ "$should_be_enabled" = "true" && $exit_code -ne 0 ]]; then
        log_error "Service $service should be enabled but is not. Message was: $result"
        return 1
    elif [[ "$should_be_enabled" = "false" && $exit_code -eq 0 ]]; then
        log_error "Service $service should not be enabled but is. Message was: $result"
        return 1
    fi
    # If should exist is true, the logging should be different
    if [[ "$should_be_enabled" = "true" ]]; then
        log_success "Service $service is enabled as expected."
    else
        log_success "Service $service is not enabled as expected."
    fi
}

# Function to have a turning pipe. This function returns the next "pipe" position based on a given.
# $1 Start pipe position. Defaults to "|"
piper() {

  if [ "$1" == "|" ] || [ -z "$1" ]; then
    echo "/"
  elif [ "$1" == "/" ]; then
    echo "_"
  elif [ "$1" == "_" ]; then
    echo "\\"
  elif [ "$1" == "\\" ]; then
    echo "|"
  fi
}

timer() {
  # Return a well formatted time string in the format xx Days xx Hours xx Minutes xx Seconds from a start time.
  # Use a start time like this: "local start=$(date +%s)"
  # $1 Start time
  local start=${1}
  local end
  local dt
  local dd
  local dt2
  local dh
  local dt3
  local dm
  local ds
  end=$(date +%s)
  dt=$(echo "$end - $start" | bc)
  dd=$(echo "$dt/86400" | bc)
  dt2=$(echo "$dt-86400*$dd" | bc)
  dh=$(echo "$dt2/3600" | bc)
  dt3=$(echo "$dt2-3600*$dh" | bc)
  dm=$(echo "$dt3/60" | bc)
  ds=$(echo "$dt3-60*$dm" | bc)
  echo "${dd} Days ${dh} Hours ${dm} Minutes ${ds} Seconds"
}

# Function to wait for a PID to finish
# Usage: wait_for_pid <pid> <message>
wait_for_pid() {
  # $1 PID
  # $2 Waiting message. If empty none will be shown.
  local program_pid=$1
  local message=$2
  local start
  local pipe
  start=$(date +%s)
  sleep 3
  while [ -e /proc/"$program_pid" ]; do
    if [ -n "${message}" ]; then
      pipe=$(piper "${pipe}")
      echo -ne "\r${message} | Runtime: $(timer "$start") ${pipe}"
    fi
    sleep 1
  done
  echo -ne "\n"
}

# Function to check if a program is installed
# Usage: check_program_installed <program>
check_program_installed() {
  if type -p $1 >/dev/null; then
    log_success "Program $1 is installed."
    return 0
  else
    log_error "Program $1 is not installed."
    return 1
  fi
}
