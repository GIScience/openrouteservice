#!/bin/bash

# Function to delete an item by ID
# Usage: delete_nexus_item <item_id> <nexus_domain> <username> <password>
delete_nexus_item() {
    local item_id="$1"
    local nexus_domain="$2"
    local username="$3"
    local password="$4"
    local delete_url="$nexus_domain/service/rest/v1/components/$item_id"

    # Perform the delete using curl
    curl -u "$username:$password" -X DELETE "$delete_url"
}

# Function to retrieve and process item IDs from Nexus API
# Usage: process_nexus_folder <nexus_domain> <repository_name> <username> <password>
process_nexus_repo() {
    local nexus_domain="$1"
    local repository_name="$2"
    local username="$3"
    local password="$4"

    # Retrieve list of files (with JSON response)
    json_data=$(curl -X GET -u "$username:$password" -H "Accept: application/json" "$nexus_domain/service/rest/v1/components?repository=$repository_name")

    # Extract item IDs using jq and add them to a Bash array
    mapfile -t item_ids < <(echo "$json_data" | jq -r '.items[].id')

    # If the array is empty, echo a message and exit successfully
    if [ ${#item_ids[@]} -eq 0 ]; then
        echo "Either repository $repository_name not found or no items in the repository found."
        exit 0
    fi

    # Iterate over the array and delete files by ID
    for id in "${item_ids[@]}"; do
        echo "Deleting item with ID: $id"
        delete_nexus_item "$id" "$nexus_domain" "$username" "$password"
    done
}

# Check if the required number of arguments is provided
if [ $# -ne 4 ]; then
    echo "Usage: $0 <nexus_domain> <repository_name> <username> <password>"
    exit 1
fi

# Get arguments from the command line
NEXUS_DOMAIN="$1"
REPOSITORY_NAME="$2"
USERNAME="$3"
PASSWORD="$4"

# Call the function with the provided arguments
process_nexus_repo "$NEXUS_DOMAIN" "$REPOSITORY_NAME" "$USERNAME" "$PASSWORD"
