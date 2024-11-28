# Look into the documentation under docs/technical-details/integration-tests.md for more information.
ARG CONTAINER_BUILD_DIR=/build
ARG CONTAINER_WORK_DIR=/home/ors/openrouteservice

FROM ors-test-scenarios-war-builder:latest AS ors-test-scenarios-war-bare

HEALTHCHECK --interval=3s --timeout=2s --retries=4 CMD wget --quiet --tries=1 --spider http://localhost:8080/ors/v2/health || exit 1

EXPOSE 8080

FROM ors-test-scenarios-war-bare AS ors-test-scenarios-war

ARG CONTAINER_WORK_DIR

RUN mv "$CONTAINER_WORK_DIR"/ors-config.yml.deactivated "$CONTAINER_WORK_DIR"/ors-config.yml && \
    echo 'export JAVA_OPTS="$JAVA_OPTS -Dors.config.location=/home/ors/openrouteservice/ors-config.yml"' >> /usr/local/tomcat/bin/setenv.sh

ENV JAVA_OPTS="-Xmx350M"

CMD ["catalina.sh", "run"]
