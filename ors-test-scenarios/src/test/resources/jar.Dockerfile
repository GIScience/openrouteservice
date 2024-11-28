# Look into the documentation under docs/technical-details/integration-tests.md for more information.
ARG CONTAINER_BUILD_DIR=/build
ARG CONTAINER_WORK_DIR=/home/ors/openrouteservice

FROM ors-test-scenarios-jar-builder:latest AS ors-test-scenarios-jar-bare

HEALTHCHECK --interval=3s --timeout=2s --retries=4 CMD wget --quiet --tries=1 --spider http://localhost:8080/ors/v2/health || exit 1

EXPOSE 8080

FROM ors-test-scenarios-jar-bare AS ors-test-scenarios-jar

ARG CONTAINER_WORK_DIR

RUN mv "$CONTAINER_WORK_DIR"/ors-config.yml.deactivated "$CONTAINER_WORK_DIR"/ors-config.yml

ENV JAVA_OPTS="-Xmx350M"


CMD ["java", "-jar", "ors.jar"]

