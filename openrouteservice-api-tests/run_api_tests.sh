#!/bin/bash
host=${1}
port=${2}
base_path=${3}

wait_for_url() {
    echo "Waiting for status code 200: $1"
    # shellcheck disable=SC2016
    timeout --foreground -s TERM 240s bash -c \
        'while [[ "$(curl -s -o /dev/null -m 3 -L -w ''%{http_code}'' ${0})" != "200" ]];\
        do echo "Waiting for ${0}" && sleep 2;\
        done' "${1}"
    local TIMEOUT_RETURN="$?"
    if [[ "${TIMEOUT_RETURN}" == 0 ]]; then
        echo "OK: ${1}"
        return
    elif [[ "${TIMEOUT_RETURN}" == 124 ]]; then
        echo "TIMEOUT: ${1} -> EXIT"
        exit "${TIMEOUT_RETURN}"
    else
        echo "Other error with code ${TIMEOUT_RETURN}: ${1} -> EXIT"
        exit "${TIMEOUT_RETURN}"
    fi
}

wait_for_url "$host:$port$base_path/v2/health"

echo "Running API-Tests for $host:$port$base_path"


mvn -Dserver.host="$host" -Dserver.port="$port" -Dserver.base="$base_path" -B test --file ../openrouteservice-api-tests/pom.xml
res=$?

if [ "$res" -ne 0 ]; then
  echo 'API tests failed!'
  exit $res
fi
