#!/usr/bin/env bash
#################################
# Description: Permission related functions
# Author: Julian Psotta
# Date: 2024-02-24
# #################################

# Fail if CONTAINER_NAME is empty or not one of podman or docker
if [ -z "$CONTAINER_ENGINE" ] || [ "$CONTAINER_ENGINE" != "podman" ] && [ "$CONTAINER_ENGINE" != "docker" ]; then
    log_error "Please set the CONTAINER_ENGINE variable to either podman or docker."
    return 1
fi


# Function to check user and group permissions on a file or folder
# Usage: check_user_group_permissions <path_to_file> <user> <group> <permissions>
check_user_group_permissions() {
    local path_to_file="$1"
    local user="$2"
    local group="$3"
    local permissions="$4"

    # Fail if any of the variables are empty
    if [ -z "$path_to_file" ] || [ -z "$permissions" ]; then
        log_error "Please provide all variables to the check_user_group_permissions function."
        return 1
    fi

    # Fail if neither user nor group are set
    if [ -z "$user" ] && [ -z "$group" ]; then
        log_error "Please provide at least one of user or group to the check_user_group_permissions function."
        return 1
    fi

    if [ -n "$user" ] && [ -z "$group" ]; then
      # Set group to empty string
      group=""
      # If only user is set, get stats without group
      local result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "stat -c '%a %U' $path_to_file")
      if [[ "$result" != "$permissions $user" ]]; then
          log_error "Permissions for $path_to_file should be $permissions $user but are $result"
          return 1
      fi
      log_success "Permissions for $path_to_file are $permissions $user as expected."
      return 0
    elif [ -z "$user" ] && [ -n "$group" ]; then
      # Check for group only
      # Set user to empty string
      user=""
      local result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "stat -c '%a %G' $path_to_file")
      if [[ "$result" != "$permissions $group" ]]; then
          log_error "Permissions for $path_to_file should be $permissions $group but are $result"
          return 1
      fi
      log_success "Permissions for $path_to_file are $permissions $group as expected."
      return 0
    else
      # Check for user and group
      local result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "stat -c '%a %U %G' $path_to_file")
      if [[ "$result" != "$permissions $user $group" ]]; then
          log_error "Permissions for $path_to_file should be $permissions $user $group but are $result"
          return 1
      fi
    fi
    log_success "Permissions for $path_to_file are $permissions $user $group as expected."
}

# Function to check if owned content count matches expected count
# Usage: find_owned_content <folder> <user> <group> <count>
find_owned_content() {
    local folder="$1"
    local user="$2"
    local group="$3"
    local count="$4"

    local user_option=""
    local group_option=""

    if [ -n "$user" ]; then
        user_option="-user $user"
    fi

    if [ -n "$group" ]; then
        group_option="-group $group"
    fi

    local result
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "find $folder $user_option $group_option | wc -l")
    if [ "$result" != "$count" ]; then
        log_error "Expected $count owned items in $folder but found $result"
        return 1
    fi
    log_success "Found $count owned items in $folder for user $user | group $group as expected."
}

