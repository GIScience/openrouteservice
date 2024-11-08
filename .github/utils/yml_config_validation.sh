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
echo "- checking if ors.engine.profile_default.build exists and has 'source_file' property"
yq  --exit-status '.ors.engine.profile_default.build | has("source_file")' $input_file > /dev/null || exit 1
