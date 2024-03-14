#!/bin/bash

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 input.yaml output.properties"
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

echo "- enable ors.engine.profiles.car"
yq -i '.ors.engine.profiles.car.enabled = true' "$output_file" || exit 1

echo "- set ors.engine.source_file to ors-api/src/test/files/heidelberg.osm.gz"
yq -i '.ors.engine.source_file = "ors-api/src/test/files/heidelberg.osm.gz"' "$output_file" || exit 1


###########################
### Convert input file ####
###########################
echo ""
echo "Convert .yaml to .env/properties file:"
echo "- unwrap yaml structure to flat properties"
yq -i -o=props --unwrapScalar=false '..  | select(tag != "!!map" and tag != "!!seq") | ( (path | join(".")) + "=" + .)' "$output_file" || exit 1

## Add # to the beginning of each line that is not empty or a comment
echo "- Comment everything"
sed -i '/^\s*[^#]/ s/^/#/' "$output_file" || exit 1

echo "- Uncomment ors.engine.source_file and ors.engine.profiles.car.enabled"
sed -i -e '/^#ors.engine.source_file/s/^#//' -e '/^#ors.engine.profiles.car.enabled/s/^#//' "$output_file" || exit 1

############################
### Validate output file ###
############################
echo ""
echo "Validate output file:"
echo "- checking for ors.engine.source_file=ors-api/src/test/files/heidelberg.osm.gz"
return_value=$(sed -n '/^ors.engine.source_file=ors-api\/src\/test\/files\/heidelberg.osm.gz/p' $output_file)|| exit 1
if [ -z "$return_value" ]; then
  echo "ors.engine.source_file=ors-api/src/test/files/heidelberg.osm.gz not found"
  exit 1
fi
echo "- checking for ors.engine.profiles.car.enabled=true"
return_value=$(sed -n '/^ors.engine.profiles.car.enabled=true/p' $output_file) || exit 1
if [ -z "$return_value" ]; then
  echo "ors.engine.profiles.car.enabled=true not found"
  exit 1
fi

echo "Parsing complete. Result saved to $output_file"

