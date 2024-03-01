# To execute this script locally, see the run.sh in the same folder
FROM docker.io/debian:bookworm-slim as builder
RUN apt-get update -y && \
    apt-get install -y openjdk-17-jdk-headless maven curl init systemd vim yq jq

# Create work directory init
WORKDIR /build
# Copy the project files into the container
COPY ors-api ors-api
COPY ors-engine ors-engine
COPY ors-report-aggregation ors-report-aggregation
COPY pom.xml pom.xml
COPY ors-config.yml ors-config.yml

ARG UNIT_TESTS=false
# Build the project to cache the layers for faster builds
RUN if [ "$UNIT_TESTS" = "true" ]; then mvn -B clean package -PbuildWar,apitests -DCI=true -T $(nproc); else mvn clean package -DskipTests -PbuildWar -T $(nproc); fi

# Copy the integration scenarios folder into the container. This changes often so keep it after the mvn build
COPY .integration-scenarios .integration-scenarios

ARG TOMCAT_MAJOR=10
ARG USER=ors

# Create user and group and execute the script
RUN useradd -m -U -s /bin/bash "$USER" && \
    mkdir -p /home/$USER/ && \
    .integration-scenarios/debian-12-tomcat-war-systemd/setup/install.sh -u $USER -w /build/ors-api/target/ors.war && \
    systemctl enable openrouteservice.service

WORKDIR /home/$USER

# Expose ports
EXPOSE 8080
# Start systemd with the following command
CMD [ "/usr/sbin/init" ]