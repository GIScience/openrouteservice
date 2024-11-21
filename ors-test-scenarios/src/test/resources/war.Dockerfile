ARG CONTAINER_BUILD_DIR=/build
ARG CONTAINER_WORK_DIR=/home/ors/openrouteservice

FROM ors-test-scenarios-war-builder:latest AS ors-test-scenarios-war-bare
# Build: docker build --target ors-test-scenarios-war-bare --tag ors-test-scenarios-war-bare:latest -f ors-test-scenarios/src/test/resources/Dockerfile .

HEALTHCHECK --interval=3s --timeout=2s --retries=4 CMD wget --quiet --tries=1 --spider http://localhost:8080/ors/v2/health || exit 1

EXPOSE 8080

FROM ors-test-scenarios-war-bare AS ors-test-scenarios-war
# Build: docker build --target ors-test-scenarios-war --tag ors-test-scenarios-war:latest -f ors-test-scenarios/src/test/resources/Dockerfile .
# Usage: docker run -d -it -e 'logging.level.org.heigit=DEBUG' --name ors-test-scenarios-war -p 8082:8080 ors-test-scenarios-war:latest

ARG CONTAINER_WORK_DIR

RUN mv "$CONTAINER_WORK_DIR"/ors-config.yml.deactivated "$CONTAINER_WORK_DIR"/ors-config.yml && \
    echo 'export JAVA_OPTS="$JAVA_OPTS -Dors.config.location=/home/ors/openrouteservice/ors-config.yml"' >> /usr/local/tomcat/bin/setenv.sh

ENV JAVA_OPTS="-Xmx350M"


CMD ["catalina.sh", "run"]
