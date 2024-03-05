#!/usr/bin/env bash
#################################
# Description: User related functions
# Author: Julian Psotta
# Date: 2024-02-24
#################################

# Fail if CONTAINER_NAME is empty or not one of podman or docker
if [ -z "$CONTAINER_ENGINE" ] || [ "$CONTAINER_ENGINE" != "podman" ] && [ "$CONTAINER_ENGINE" != "docker" ]; then
    log_error "Please set the CONTAINER_ENGINE variable to either podman or docker."
    return 1
fi


# Function to check if a user is in a group
# Usage: check_user_in_group <group_name> <user_name>
check_user_in_group() {
    local user_name="$1"
    local group_name="$2"

    local result
    local exit_code
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "getent group $group_name | grep $user_name")
    exit_code=$?
    if [[ $exit_code -ne 0 ]]; then
        log_error "User $user_name should be in group $group_name but is not."
        return 1
    fi
    log_success "User $user_name is in group $group_name as expected."
}

# Function to check if a user exists
# Usage: check_user_exists <user_name> <should_exist>
check_user_exists() {
    local user_name="$1"
    local should_exist="$2"

    local result
    local exit_code
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "getent passwd $user_name")
    exit_code=$?
    if [[ "$should_exist" = true && $exit_code -ne 0 ]]; then
      log_error "User $user_name should exist but does not."
      return 1
    elif [[ "$should_exist" = false && $exit_code -eq 0 ]]; then
        log_error "User $user_name shouldn't exist but does."
        return 1
    fi
    # If should exist is true, the logging should be different
    if [[ "$should_exist" = true ]]; then
        log_success "User $user_name exists as expected."
    else
        log_success "User $user_name does not exist as expected."
    fi
}

# Function to check if a group exists
# Usage: check_group_exists <group_name> <should_exist>
check_group_exists() {
    local group_name="$1"
    local should_exist="$2"

    local result
    local exit_code
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "getent group $group_name")
    exit_code=$?
    if [[ "$should_exist" = true && $exit_code -ne 0 ]]; then
        log_error "Group $group_name should exist but does not."
        return 1
    elif [[ "$should_exist" = false && $exit_code -eq 0 ]]; then
        log_error "Group $group_name shouldn't exist but does."
        return 1
    fi
    # If should exist is true, the logging should be different
    if [[ "$should_exist" = true ]]; then
        log_success "Group $group_name exists as expected."
    else
        log_success "Group $group_name does not exist as expected."
    fi
}
