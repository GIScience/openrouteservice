#!/bin/bash

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 input-1.yaml input-2.yaml (1 with basic properties, 2 with profiles)"
  exit 1
fi

input_file1=$1
input_file2=$2

###########################
### Validate input files ###
###########################
echo "Validate input file1:"
echo "- checking if the input file is a valid yaml file"
yq 'true' $input_file1 /dev/null || exit 1
# Fail if ors.engine.profiles.car.enabled='false' can't be found access with schema .result    | select(.property_history != null)    | .property_history    | map(select(.event_name == "Sold"))[0].date'
echo "- checking if ors.engine.source_file exists in $input_file1 and has 'source_file' property"
yq  --exit-status '.ors.engine | has("source_file")' $input_file1 > /dev/null || exit 1



echo "Validate input file2:"
echo "- checking if the input file is a valid yaml file"
yq 'true' $input_file2 /dev/null || exit 1
# For profiles section for car with enabled using yq and contains
echo "- checking if ors.engine.profiles.car exists in $input_file2 and has 'enabled' property"
yq  --exit-status '.ors.engine.profiles.car | has("enabled")' $input_file2 > /dev/null || exit 1
