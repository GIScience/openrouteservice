#!/bin/bash

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 input.yaml output.yaml"
  exit 1
fi

input_file=$1
output_file=$2

echo "Generate minimal sample configuration..."

echo "\
################################################################################
### Configuration file for openrouteservice. For a description please visit: ###
### https://giscience.github.io/openrouteservice/run-instance/configuration/ ###
################################################################################" > ${output_file}

yq --null-input '
(.ors.engine.profile_default.build.source_file = "ors-api/src/test/files/heidelberg.test.pbf" )|
(.ors.engine.profiles.driving-car.enabled = true )
' >> ${output_file}


echo "Prepare all properties to be added as commented text..."
tmpYml=$(mktemp "prepare.XXXXXXXXX.yml")
cp ${input_file} ${tmpYml}
echo "Copied ${input_file} to ${tmpYml}"
echo "Remove config properties that should not be added.."
yq -i 'del(.ors.engine.graph_management)' ${tmpYml} || exit 1
yq -i 'del(.ors.engine.profile_default.repo)' ${tmpYml} || exit 1

echo "Append commented application.yml..."
echo "" >> ${output_file}
sed '/^\s*[^#]/ s/^/#/' ${tmpYml} >> ${output_file}

echo "Remove temporary file ${tmpYml}..."
rm ${tmpYml}
