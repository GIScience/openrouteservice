#!/bin/bash

# Define the ENV SCRIPT_NAME
# shellcheck disable=SC2034
SCRIPT_NAME=upload_rpm_package.sh

START_DIRECTORY="$(
  cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
  pwd -P
)"

# Import helper functions
. "$START_DIRECTORY/helper/helper_functions.sh"


# Uploads an RPM package to the testing repository.
# Usage: upload_rpm_to_repository <username> <password> <local_file_path> <upload_url_with_filename>
upload_rpm_to_repository() {
    local username="$1"
    local password="$2"
    local local_file_path="$3"
    local upload_url_with_filename="$4"

    # Log info
    log_info "Uploading RPM package to the testing repository..."

    http_code=$(curl -s -u "$username:$password" --write-out '%{http_code}' \
        --upload-file "$local_file_path" "$upload_url_with_filename")

    # Save the exit code
    exit_code=$?

    # Log info
    log_info "HTTP code: $http_code and exit code: $exit_code"

    # Validate curl's exit code
    if [ $exit_code -ne 0 ]; then
        log_error "Failed to upload the RPM package to the testing repository"
        exit 1
    fi

    # Validate that the HTTP code is >= 200 and < 300
      if [ "$http_code" -lt 200 ] || [ "$http_code" -ge 300 ]; then
        log_error "Failed to upload the RPM package to the testing repository"
        exit 1
    else
        # Echo success
        log_success "Successfully uploaded the RPM package to the testing repository"
    fi
}

# Check if the required number of arguments is provided
if [ $# -ne 4 ]; then
    log_info "Usage: $0 <USERNAME> <PASSWORD> <LOCAL_FILE_PATH> <UPLOAD_URL_WITH_FILENAME>"
    exit 1
fi

USERNAME="$1"
PASSWORD="$2"
LOCAL_FILE_PATH="$3"
UPLOAD_URL_WITH_FILENAME="$4"

# Call the function with the provided arguments
upload_rpm_to_repository "$USERNAME" "$PASSWORD" "$LOCAL_FILE_PATH" "$UPLOAD_URL_WITH_FILENAME"
