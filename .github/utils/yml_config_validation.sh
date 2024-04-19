#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 input.yaml"
  exit 1
fi

input_file=$1

###########################
### Validate input file ###
###########################
echo "Validate input file:"
echo "- checking if the input file is a valid yaml file"
yq 'true' $input_file /dev/null || exit 1
# Fail if ors.engine.profiles.car.enabled='false' can't be found access with schema .result    | select(.property_history != null)    | .property_history    | map(select(.event_name == "Sold"))[0].date'
echo "- checking if ors.engine.source_file exists and has 'source_file' property"
yq  --exit-status '.ors.engine | has("source_file")' $input_file > /dev/null || exit 1
# For profiles section for car with enabled using yq and contains
#echo "- checking if ors.engine.profiles.car exists and has 'enabled' property"
#yq  --exit-status '.ors.engine.profiles.car | has("enabled")' $input_file > /dev/null || exit 1