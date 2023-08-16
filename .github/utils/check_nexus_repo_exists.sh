#!/bin/bash

# Function to test if a repository name exists in the JSON data
# Usage: test_repository_name_exists <repository_name> <nexus_domain> <username> <password> <should_exist>
test_repository_name_exists() {
    local repository_name="$1"
    local nexus_domain="$2"
    local username="$3"
    local password="$4"
    local should_exist="$5"

    local json_url="$nexus_domain/service/rest/v1/repositories"

    # Retrieve JSON data from the constructed URL using curl and parse with jq
    json_data=$(curl -u "$username:$password" -X GET "$json_url" | jq '.[] | select(.name == "'"${repository_name}"'")')

    # Check if the repository name exists in the parsed JSON data
    if [ "$should_exist" = "true" ]; then
        if [ -n "$json_data" ]; then
            echo "Repository '$repository_name' exists as expected."
            exit 0
        else
            echo "Repository '$repository_name' does not exist, but should."
            exit 1
        fi
    else
        if [ -n "$json_data" ]; then
            echo "Repository '$repository_name' exists, but should not."
            exit 1
        else
            echo "Repository '$repository_name' does not exist as expected."
            exit 0
        fi
    fi
}

# Check if the required number of arguments is provided
if [ $# -ne 5 ]; then
    echo "Usage: $0 <repository_name> <nexus_domain> <username> <password> <should_exist>"
    exit 1
fi

# Get arguments from the command line
NEXUS_DOMAIN="$1"
REPOSITORY_NAME="$2"
USERNAME="$3"
PASSWORD="$4"
SHOULD_EXIST="$5"

# Call the function with the provided arguments
test_repository_name_exists "$REPOSITORY_NAME" "$NEXUS_DOMAIN" "$USERNAME" "$PASSWORD" "$SHOULD_EXIST"
