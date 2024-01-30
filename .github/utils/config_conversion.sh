#!/bin/bash

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 input.yaml output.yaml"
  exit 1
fi

input_file=$1
output_file=$2

# Add # to the beginning of each line that is not empty or a comment
awk '!/^[[:space:]]*#/ && !/^[[:space:]]*$/ {print "#" $0; next} {print}' "$input_file" > "$output_file"
# Replace '#ors:' with 'ors:'
sed -i 's/#ors:/ors:/g' "$output_file"
# Replace '#  engine:' with '  engine:'
sed -i 's/#  engine:/  engine:/g' "$output_file"
# Replace '#    source_file:' with '    source_file: "YOUR_FILE"'
sed -i 's/#    source_file:/    source_file: "YOUR_FILE"/g' "$output_file"
# Replace '#    profiles:' with '    profiles:'
sed -i 's/#    profiles:/    profiles:/g' "$output_file"

# Replace the default_profile. Ignore the value of enabled and always set to false.
awk -i inplace '/#    profile_default:/{getline; if ($0 ~ /#      enabled:/) {print "    profile_default:"; print "      enabled: false"; next}} {print}' "$output_file"

# Replace the individual profiles. Ignore the value of enabled and always set to false.
for profile in car hgv bike-regular bike-mountain bike-road bike-electric walking hiking public-transport; do
  awk -i inplace "/#      $profile:/{getline; if (\$0 ~ /#        enabled:/) {print \"      $profile:\"; print \"        enabled: false\"; next}} {print}" "$output_file"
done

echo "Parsing complete. Result saved to $output_file"
