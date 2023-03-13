#!/bin/sh
while [ ! -f "$1"/ors.run ]; do
  sleep 5
done
port=${2}
echo "Running API-Tests on port: $port"
mvn -Dserver.port="$port" -B test --file "$1"/../openrouteservice-api-tests/pom.xml
res=$?

if [ "$res" -ne 0 ]; then
  echo 'API tests failed!'
  exit $res
fi
