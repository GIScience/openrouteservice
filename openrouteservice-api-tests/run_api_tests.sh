#!/bin/sh
while [ ! -f  $1/ors.run ]
do
  sleep 1
done
mvn -B test --file $1/openrouteservice-api-tests/pom.xml
rm $1/ors.run