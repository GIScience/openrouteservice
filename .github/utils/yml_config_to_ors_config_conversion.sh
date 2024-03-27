#!/bin/bash

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 input.yaml output.yaml"
  exit 1
fi

input_file=$1
output_file=$2

echo ""
echo "Copy $input_file to $output_file"
cp $input_file $output_file

###########################
### Replace parameters ####
###########################
echo ""
echo "Replace parameters:"

echo "- set ors.engine.source_file to ors-api/src/test/files/heidelberg.osm.gz"
yq -i '.ors.engine.source_file = "ors-api/src/test/files/heidelberg.osm.gz"' "$output_file" || exit 1

###########################
### Convert input file ####
###########################
echo ""
echo "Converting input file:"
## Add # to the beginning of each line that is not empty or a comment
echo "- Comment everything"
sed -i '/^\s*[^#]/ s/^/#/' "$output_file" || exit 1

echo "- Uncomment ors, engine and source_file"
sed -i -e '/^#ors:/s/^#//' -e '/^#.*engine:/s/^#//' -e '/^#.*source_file:/s/^#//' "$output_file"

echo "- Uncomment subsequent lines for profiles.car.enabled in ors.engine"
sed -i -e 's/^#    profiles:/    profiles:/' "$output_file"
sed -i -e 's/^#      car:/      car:\n        enabled: true/' "$output_file"

echo "Parsing complete. Result saved to $output_file"
