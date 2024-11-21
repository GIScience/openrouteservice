# Run stages for the integration tests.
# Build the Builder.Dockefile first to get the necessary stages:
# docker build --target ors-test-scenarios-maven-builder --tag ors-test-scenarios-maven-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .
# docker build --target ors-test-scenarios-jar-builder --tag ors-test-scenarios-jar-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .
# docker build --target ors-test-scenarios-war-builder --tag ors-test-scenarios-war-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .
# Look into the Readme.md in the ors-test-scenarios folder on how to build certain targets for maven, java or war scenarios.
ARG CONTAINER_BUILD_DIR=/build
ARG CONTAINER_WORK_DIR=/home/ors/openrouteservice

FROM ors-test-scenarios-jar-builder:latest AS ors-test-scenarios-jar-bare
# Build: docker build --target ors-test-scenarios-jar-bare --tag ors-test-scenarios-jar-bare:latest -f ors-test-scenarios/src/test/resources/Dockerfile .

HEALTHCHECK --interval=3s --timeout=2s --retries=4 CMD wget --quiet --tries=1 --spider http://localhost:8080/ors/v2/health || exit 1

EXPOSE 8080

FROM ors-test-scenarios-jar-bare AS ors-test-scenarios-jar
# Build: docker build --target ors-test-scenarios-jar --tag ors-test-scenarios-jar:latest -f ors-test-scenarios/src/test/resources/Dockerfile .
# Usage: docker run -d -it -e 'logging.level.org.heigit=DEBUG' --name ors-test-scenarios-jar -p 8082:8080 ors-test-scenarios-jar:latest

ARG CONTAINER_WORK_DIR

RUN mv "$CONTAINER_WORK_DIR"/ors-config.yml.deactivated "$CONTAINER_WORK_DIR"/ors-config.yml

ENV JAVA_OPTS="-Xmx350M"


CMD ["java", "-jar", "ors.jar"]

