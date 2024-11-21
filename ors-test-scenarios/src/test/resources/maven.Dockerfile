# Look into the Readme.md in the ors-test-scenarios folder on how to build certain targets for maven, java or war scenarios.
ARG CONTAINER_BUILD_DIR=/build
ARG CONTAINER_WORK_DIR=/home/ors/openrouteservice

FROM ors-test-scenarios-maven-builder:latest AS ors-test-scenarios-maven-bare
# Build: docker build --target ors-test-scenarios-maven-bare --tag ors-test-scenarios-maven-bare:latest -f ors-test-scenarios/src/test/resources/Dockerfile .

# Define the healthcheck
HEALTHCHECK --interval=3s --timeout=2s --retries=4 CMD wget --quiet --tries=1 --spider http://localhost:8080/ors/v2/health || exit 1

EXPOSE 8080


FROM ors-test-scenarios-maven-bare AS ors-test-scenarios-maven
# Build: docker build --target ors-test-scenarios-maven --tag ors-test-scenarios-maven:latest -f ors-test-scenarios/src/test/resources/Dockerfile .
# Usage: docker run -d -it -e 'logging.level.org.heigit=DEBUG' --name ors-test-scenarios-maven -p 8082:8080 ors-test-scenarios-maven:latest

ARG CONTAINER_WORK_DIR

# Activate the config file
RUN mv "$CONTAINER_WORK_DIR"/ors-config.yml.deactivated "$CONTAINER_WORK_DIR"/ors-config.yml

# Set max heap size
ENV JAVA_OPTS="-Xmx350M"

# Command to run Maven
CMD ["sh", "maven-entrypoint.sh"]
