#!/bin/sh
for i in 1 2 3 4 5
do
  sleep 1m
  echo "Waiting for ORS init... $i"
done
mvn test --file ../openrouteservice-api-tests/pom.xml
