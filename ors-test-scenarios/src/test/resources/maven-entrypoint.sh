#!/usr/bin/env bash
# Entrypoint for Maven since the spring-boot.run.jvmArguments cannot be appended with ENV variables in the Dockerfile.
JVM_ARGS=${JAVA_OPTS:-"-Xmx400m"}

echo "Running in entrypoint.sh with JVM_ARGS: ${JVM_ARGS}"

exec mvn spring-boot:run -DskipTests -Dmaven.test.skip=true -Dspring-boot.run.jvmArguments="${JVM_ARGS}" "$@"

