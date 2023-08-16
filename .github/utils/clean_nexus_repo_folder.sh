#!/bin/bash

# Define the ENV SCRIPT_NAME
# shellcheck disable=SC2034
SCRIPT_NAME=clean_nexus_repo_folder

START_DIRECTORY="$(
  cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
  pwd -P
)"

# Import helper functions
. "$START_DIRECTORY/helper/helper_functions.sh"


# Function to delete an item by ID
# Usage: delete_nexus_item <item_id> <nexus_domain> <username> <password>
delete_nexus_item() {
    local item_id="$1"
    local nexus_domain="$2"
    local username="$3"
    local password="$4"
    local delete_url="$nexus_domain/service/rest/v1/assets/$item_id"

    log_success "Deleting item with ID '$item_id' from repository '$repository_name'."
    # Perform the delete using curl
    curl -u "$username:$password" -X DELETE "$delete_url"
    exit_code=$?
    # Check if the delete was successful
    if [ $exit_code -eq 0 ]; then
        log_success "Successfully deleted item with ID '$item_id' from repository '$repository_name'."
    fi
}

# Function to retrieve and process item IDs from Nexus API
# Usage: process_nexus_folder <nexus_domain> <repository_name> <username> <password>
process_nexus_repo() {
    local nexus_domain="$1"
    local repository_name="$2"
    local username="$3"
    local password="$4"
    local repository_directory="$5"

    # Retrieve list of files (with JSON response)
    json_data=$(curl -X GET -u "$username:$password" -H "Accept: application/json" "$nexus_domain/service/rest/v1/components?repository=$repository_name")

    # Extract items in a json array
    mapfile -t item_ids < <(echo "$json_data" | jq -r -c '.items[]')

    # If the json array is empty, exit
    if [ "${#item_ids[@]}" -eq 0 ]; then
        log_success "No items found in repository '$repository_name'."
        exit 0
    fi

    # Iterate over the json array of json objects and extract and extract id and assets.path using jq
    for item in "${item_ids[@]}"; do

        # Extract .repository, .assets into variables
        repository=$(echo "$item" | jq -r '.repository')
        assets=$(echo "$item" | jq -r '.assets')
        echo "Processing repository '$repository'."
        # Iterate over the assets array and extract .path and .id into variables
        for asset in $(echo "$assets" | jq -r -c '.[]'); do
            # Extract .path and .id into variables from the asset json object stored as a string with jq
            path=$(echo "$asset" | jq -r '.path')
            id=$(echo "$asset" | jq -r '.id')
            # Check if the path contains the repository directory
            if [[ "$path" == "$repository_directory"* ]]; then
                echo "Path contains '$repository_directory'."
                echo "Deleting item with ID '$id' and path '$path' from repository '$repository_name'."
                # Delete the item
                delete_nexus_item "$id" "$nexus_domain" "$username" "$password"
            fi
        done
    done

}

# Check if the required number of arguments is provided
if [ $# -ne 5 ]; then
    echo "Usage: $0 <nexus_domain> <username> <password>  <repository_name> <repository_directory>"
    exit 1
fi

# Get arguments from the command line
NEXUS_DOMAIN="$1"
USERNAME="$2"
PASSWORD="$3"
REPOSITORY_NAME="$4"
REPOSITORY_DIRECTORY="$5"

# Call the function with the provided arguments
process_nexus_repo "$NEXUS_DOMAIN" "$REPOSITORY_NAME" "$USERNAME" "$PASSWORD" "$REPOSITORY_DIRECTORY"
