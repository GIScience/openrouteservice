#!/usr/bin/env bash

##### Helper functions #####
# Function to return the current date and time
# Usage: get_date_time
get_date_time() {
    date +"%Y-%m-%d %H:%M:%S"
}


# Function to print an error message in red
# Usage: log_error <message>
log_error() {
    local message="$1"
    echo -e "\e[31m($(get_date_time) | $SCRIPT_NAME | ERROR ): ${message}\e[0m"
}

# Function to print a success message in green
# Usage: log_success <message>
log_success() {
    local message="$1"
    echo -e "\e[32m($(get_date_time) | $SCRIPT_NAME | SUCCESS ): ${message}\e[0m"
}

##### Variables #####
# Define the container engine to use with default to docker
CONTAINER_ENGINE=${CONTAINER_ENGINE:-docker}

# Fail if CONTAINER_NAME is empty or not one of podman or docker
if [ -z "$CONTAINER_ENGINE" ] || [ "$CONTAINER_ENGINE" != "podman" ] && [ "$CONTAINER_ENGINE" != "docker" ]; then
    log_error "Please set the CONTAINER_ENGINE variable to either podman or docker."
    return 1
fi

##### Test functions #####
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

# Function to check for a line in a file
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
        log_error "Line '$line' should exist in file $path_to_file but does not."
        return 1
    elif [[ "$should_exist" = false && $exit_code -eq 0 ]]; then
        log_error "Line '$line' should not exist in file $path_to_file but does."
        return 1
    fi
    # If should exist is true, the logging should be different
    if [[ "$should_exist" = true ]]; then
        log_success "Line '$line' exists in file $path_to_file as expected."
    else
        log_success "Line '$line' does not exist in file $path_to_file as expected."
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

# Function to check if a file exists
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

# Function to check if a symlink exists
# Usage: check_file_symlink <path_to_file> <should_exist>
check_file_is_symlink() {
    local path_to_file="$1"
    local should_exist="$2"

    # Check if file exists. Else the symlink will fail if it doesn't exist
    local _
    local exit_code
    _=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "test -f $path_to_file")
    exit_code=$?
    if [ "$should_exist" = true ] && [ $exit_code -ne 0 ]; then
        log_error "Symlink at $path_to_file does not exist but should. Exit code was: $exit_code."
        return 1
    fi

    # Check if file is a symlink
    local result
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "find $path_to_file -type l -xtype f | wc -l")
    if [[ "$should_exist" = true && $result -ne 1 ]]; then
        log_error "A file exists at $path_to_file but it is no symlink."
        return 1
    elif [[ "$should_exist" = false && $result -ne 0 ]]; then
        log_error "The Symlink at $path_to_file shouldn't exist but does."
        return 1
    fi
    log_success "Symlink at $path_to_file exists as expected."
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

# Function to check if owned content count matches expected count
# Usage: find_owned_content <folder> <count> [<user>] [<group>]
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

# Function to check user and group permissions on a file or folder
# Usage: check_user_group_permissions <path_to_file> <user> <group> <permissions>
check_user_group_permissions() {
    local path_to_file="$1"
    local user="$2"
    local group="$3"
    local permissions="$4"

    # Fail if any of the variables are empty
    if [ -z "$path_to_file" ] || [ -z "$permissions" ] || [ -z "$user" ] || [ -z "$group" ]; then
        log_error "Please provide all variables to the check_user_group_permissions function."
        return 1
    fi

    local result
    result=$(${CONTAINER_ENGINE} exec -u root "$CONTAINER_NAME" bash -c "stat -c '%a %U %G' $path_to_file")
    if [[ "$result" != "$permissions $user $group" ]]; then
        log_error "Permissions for $path_to_file should be $permissions $user $group but are $result"
        return 1
    fi
    log_success "Permissions for $path_to_file are $permissions $user $group as expected."
}
# Check that the CONTAINER_NAME variables are set
if [ -z "$CONTAINER_NAME" ]; then
    log_error "Please set the CONTAINER_NAME variable."
    return 1
fi

# Check that the script name is set
if [ -z "$SCRIPT_NAME" ]; then
    log_error "Please set the SCRIPT_NAME variable."
    return 1
fi






