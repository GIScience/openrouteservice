#!/bin/bash

function checkPropertyEq() {
  local file=$1
  local path=$2
  local expected=$3
  echo -n "- checking for ${path} = ${expected}:"
  if [ "$(yq ".${path}" < ${file})" = "${expected}" ]; then
    echo -e "\e[1;32m ok \e[0m"
  else
    echo -e "\e[1;31m error \e[0m"
    hasError=1
  fi
}

if [ "$#" -ne 3 ]; then
  echo "Usage: $0 input-1.yaml input-2.yaml output.yaml"
  exit 1
fi

input_file1=$1
input_file2=$2
output_file=$3

echo ""
echo "Merge $input_file1 and $input_file2 to $output_file"
cp $input_file1 $output_file
yq eval-all '. as $item ireduce ({}; . *+ $item)' "$input_file1" "$input_file2" > "$output_file"

###########################
### Replace parameters ####
###########################
#echo ""
#echo "Replace parameters:"
#
#echo "- set ors.engine.source_file to ors-api/src/test/files/heidelberg.osm.gz"
#yq -i '.ors.engine.source_file = "ors-api/src/test/files/heidelberg.osm.gz"' "$output_file" || exit 1

############################
### Validate output file ###
############################
echo ""
echo "Validate output file:"
hasError=0
checkPropertyEq $output_file "ors.engine.source_file" "ors-api/src/test/files/heidelberg.osm.gz"
checkPropertyEq $output_file "ors.engine.profiles.car.enabled" "true"
(($hasError)) && exit 1

echo ""
echo "Parsing complete. Result saved to $output_file"
