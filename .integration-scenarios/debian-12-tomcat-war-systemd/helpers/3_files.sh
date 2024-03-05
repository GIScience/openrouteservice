#!/usr/bin/env bash
#################################
# Description: Path related functions
# Author: Julian Psotta
# Date: 2024-02-24
# #################################

# Fail if CONTAINER_NAME is empty or not one of podman or docker
if [ "$CONTAINER_ENGINE" != "podman" ] && [ "$CONTAINER_ENGINE" != "docker" ]; then
    log_error "Please set the CONTAINER_ENGINE variable to either podman or docker."
    return 1
fi

# Function to check if a symlink exists
# Usage: check_file_symlink <path_to_file> <should_exist>
check_file_is_symlink() {
    local path_to_file="$1"
    local should_exist="$2"

    # Check if directory exists. Else the symlink will fail if it doesn't exist
    local _
    local exit_code
    _=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "test -f $path_to_file")
    exit_code=$?
    if [ "$should_exist" = true ] && [ $exit_code -ne 0 ]; then
        log_error "Symlink at $path_to_file does not exist but should. Exit code was: $exit_code."
        return 1
    fi

    # Check if directory is a symlink
    local result
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "find $path_to_file -type l -xtype f | wc -l")
    if [[ "$should_exist" = true && $result -ne 1 ]]; then
        log_error "A directory exists at $path_to_file but it is no symlink."
        return 1
    elif [[ "$should_exist" = false && $result -ne 0 ]]; then
        log_error "The Symlink at $path_to_file shouldn't exist but does."
        return 1
    fi
    log_success "Symlink at $path_to_file exists as expected."
}

# Function to check if a directory exists
# Usage: check_file_exists <path_to_file> <should_exist>
check_file_exists() {
    local path_to_file="$1"
    local should_exist="$2"

    local result
    local exit_code
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "test -f $path_to_file")
    exit_code=$?
    if [[ "$should_exist" = true && $exit_code -ne 0 ]]; then
            log_error "File or folder at $path_to_file should exist but does not."
            return 1
    elif [[ "$should_exist" = false && $exit_code -eq 0 ]]; then
            log_error "File or folder at $path_to_file should not exist but does."
            return 1
    fi
    # If should exist is true, the logging should be different
    if [[ "$should_exist" = true ]]; then
        log_success "File or folder at $path_to_file exists as expected."
    else
        log_success "File or folder at $path_to_file does not exist as expected."
    fi
}

# Function to check if a folder exists
# Usage: check_folder_exists <path_to_folder> <should_exist>
check_folder_exists() {
    local path_to_folder="$1"
    local should_exist="$2"

    local result
    local exit_code
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "test -d $path_to_folder")
    exit_code=$?
    if [[ "$should_exist" = true && $exit_code -ne 0 ]]; then
        log_error "Folder at $path_to_folder should exist but does not."
        return 1
    elif [[ "$should_exist" = false && $exit_code -eq 0 ]]; then
        log_error "Folder at $path_to_folder should not exist but does."
        return 1
    fi
    # If should exist is true, the logging should be different
    if [[ "$should_exist" = true ]]; then
        log_success "Folder at $path_to_folder exists as expected."
    else
        log_success "Folder at $path_to_folder does not exist as expected."
    fi
}

# Function to check for a line in a directory
# Usage: check_line_in_file <line> <path_to_file> <should_exist>
check_line_in_file() {
    local line="$1"
    local path_to_file="$2"
    local should_exist="$3"

    local result
    local exit_code
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "grep '$line' $path_to_file")
    exit_code=$?
    if [[ "$should_exist" = true && $exit_code -ne 0 ]]; then
        log_error "Line '$line' should exist in directory $path_to_file but does not."
        return 1
    elif [[ "$should_exist" = false && $exit_code -eq 0 ]]; then
        log_error "Line '$line' should not exist in directory $path_to_file but does."
        return 1
    fi
    # If should exist is true, the logging should be different
    if [[ "$should_exist" = true ]]; then
        log_success "Line '$line' exists in directory $path_to_file as expected."
    else
        log_success "Line '$line' does not exist in directory $path_to_file as expected."
    fi
}

