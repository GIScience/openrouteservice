#!/bin/bash

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 input.yaml output.yaml"
  exit 1
fi

input_file=$1
output_file=$2
ors_file="ors-api/src/test/files/heidelberg.osm.gz"

###########################
### Validate input file ###
###########################
# Fail if ors: can't be found
echo "Checking for ors section"
awk '/^ors:/{print "Found ors section"}' "$input_file" || exit 1

# For engine and source file section, make sure they are in place
echo "Checking for engine and source_file section"
awk '/  engine:/{getline; if ($0 ~ /source_file:/) {print "Found engine and source_file section"} else { exit 1 }}' "$input_file" || exit 1

# For profiles section for car wit enabled
echo "Checking for profiles section"
awk '/    profiles:/{getline; if ($0 ~ /      car:/) {getline; if ($0 ~ /        enabled:/) {print "Found profiles section"} else { exit 1 }}}' "$input_file" || exit 1

###########################
### Convert input file ####
###########################
# Add # to the beginning of each line that is not empty or a comment
awk '!/^[[:space:]]*#/ && !/^[[:space:]]*$/ {print "#" $0; next} {print}' "$input_file" > "$output_file"

# Replace '#ors:' with 'ors:'
sed -i 's/#ors:/ors:/g' "$output_file"


# Remove the comments for the engine and source_file section
awk -i inplace "/engine:/{getline; if (\$0 ~ /source_file:/) {print \"  engine:\"; print \"    source_file: '$ors_file'\"; next} else { exit1 }} {print}" "$output_file"

# Remove the comments for the profiles section for car
awk -i inplace "/#    profiles:/{getline; if (\$0 ~ /#      car:/) {getline; if (\$0 ~ /#        enabled:/) {print \"    profiles:\"; print \"      car:\"; print \"        enabled: true\"; next}}}{print}" "$output_file"

echo "Parsing complete. Result saved to $output_file"
