#!/usr/bin/env bash
# Entrypoint for Maven since the spring-boot.run.jvmArguments cannot be appended with ENV variables in the Dockerfile.
JVM_ARGS=${JAVA_OPTS:-"-Xmx400m"}

echo "Running in entrypoint.sh with JVM_ARGS: ${JVM_ARGS}"

exec ./mvnw spring-boot:run -o -pl '!:ors-test-scenarios,!:ors-report-aggregation,!:ors-engine' -DskipTests -Dmaven.test.skip=true -Dspring-boot.run.jvmArguments="${JVM_ARGS}" "$@"

