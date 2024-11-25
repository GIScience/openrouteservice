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

echo "- enable ors.engine.profiles.driving-car"
yq -i '.ors.engine.profiles.driving-car.enabled = true' "$output_file" || exit 1

echo "- set ors.engine.profile_default.build.source_file to ors-api/src/test/files/heidelberg.test.pbf"
yq -i '.ors.engine.profile_default.build.source_file = "ors-api/src/test/files/heidelberg.test.pbf"' "$output_file" || exit 1

echo "- remove .ors.engine.graph_management"
yq -i 'del(.ors.engine.graph_management)' "$output_file" || exit 1

echo "- remove .ors.engine.profile_default.repo"
yq -i 'del(.ors.engine.profile_default.repo)' "$output_file" || exit 1

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

echo "- Uncomment ors.engine.profile_default.build.source_file and ors.engine.profiles.driving-car.enabled"
sed -i -e '/^#ors.engine.profile_default.build.source_file/s/^#//' -e '/^#ors.engine.profiles.driving-car.enabled/s/^#//' "$output_file" || exit 1

############################
### Validate output file ###
############################
echo ""
echo "Validate output file:"
echo "- checking for ors.engine.profile_default.source_file=ors-api/src/test/files/heidelberg.test.pbf"
return_value=$(sed -n '/^ors.engine.profile_default.build.source_file=ors-api\/src\/test\/files\/heidelberg.test.pbf/p' $output_file)|| exit 1
if [ -z "$return_value" ]; then
  echo "ors.engine.profile_default.build.source_file=ors-api/src/test/files/heidelberg.test.pbf not found"
  exit 1
fi
echo "- checking for ors.engine.profiles.driving-car.enabled=true"
return_value=$(sed -n '/^ors.engine.profiles.driving-car.enabled=true/p' $output_file) || exit 1
if [ -z "$return_value" ]; then
  echo "ors.engine.profiles.driving-car.enabled=true not found"
  exit 1
fi

echo "Parsing complete. Result saved to $output_file"

