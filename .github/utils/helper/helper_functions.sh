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


# Function to print a info message in blue
# Usage: log_info <message>
log_info() {
    local message="$1"
    echo -e "\e[34m($(get_date_time) | $SCRIPT_NAME | INFO ): ${message}\e[0m"
}