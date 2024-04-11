#!/usr/bin/env bash
ts="-$(date +%Y%m%d%H%M)"
err=0
.github/utils/yml_config_to_ors_config_conversion.sh ors-api/src/main/resources/application.yml ors-api/src/main/resources/application-profiles.yml ors-config${ts}.yml

((err=$err+$?))

.github/utils/yml_config_to_properties_conversion.sh ors-api/src/main/resources/application.yml ors-api/src/main/resources/application-profiles.yml ors-config${ts}.env

((err=$err+$?))

(($err)) && exit 1

if [ "$1" = "replace" ]; then
  replace=y
else
  read -p "Replace ors-config.yml and ors-config.env? " -n 1 replace
fi

if [[ "$replace" =~ [yYjJ] ]]; then
  echo "Replacing ors-config.yml and ors-config.env"
  set -o xtrace
  mv ors-config${ts}.yml ors-config.yml
  mv ors-config${ts}.env ors-config.env
  set +o xtrace
fi