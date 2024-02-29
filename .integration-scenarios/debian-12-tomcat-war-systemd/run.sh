#!/bin/bash
####### Description #######
# This script is used to build and run the container that emulates the debian-12 with tomcat and a war file scenario.
# Tomcat itself is started using systemd, and the war file is deployed to the webapps directory.
###########################
###################################
# Initialize the helper functions #
###################################
export SCRIPT_NAME=$(basename "$0")
export CONTAINER_ENGINE="podman"
export CONTAINER_NAME="tomcat-test"
CONTAINER_IMAGE='local/tomcat-test:latest'

# Get the absolute path of this script
START_DIRECTORY="$(
  cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
  pwd -P
)"


#################################
# Parse command-line options with long commands --something
#################################
# Function to display usage information
display_usage() {
    echo "This script is used to build and run the container that emulates the debian-12 with tomcat and a war file scenario."
    echo "Usage: ./run.sh -c <true/false> -t <true/false> -u <true/false>"
    echo "-c: Run in CI mode. Default is false."
    echo "-t: Run integration tests. Default is false."
    echo "-p: Run tests in parallel. Default is false."
    echo "-u: Run unit tests. Default is false."
    echo "-i: Container image to use. Default is '-i local/tomcat-test:latest'"
    echo "-l: Log level. Default is '-l SUCCESS'"
    echo "-h: Display this help message"
}
while getopts 'ctpu:i:l:h' opt; do
    case ${opt} in
        c)
          CI=true
          ;;
        t)
          RUN_INTEGRATION_TESTS=true
          ;;
        p)
          PARALLEL_TESTS=true
          ;;
        u)
          UNIT_TESTS=true
          ;;
        i)
          CONTAINER_IMAGE=${OPTARG}
          ;;
        l)
          LOG_LEVEL=${OPTARG}
          ;;
        h)
            display_usage
            exit 0
            ;;
        \?)
            warning "Invalid option: -$OPTARG" >&2
            display_usage
            exit 1
            ;;
        :)
            warning "Option -$OPTARG requires an argument." >&2
            display_usage
            exit 1
            ;;
    esac
done

export CONTAINER_IMAGE=${CONTAINER_IMAGE}

# Import logging
. "$START_DIRECTORY/helpers/1_logging.sh"
. "$START_DIRECTORY/helpers/2_system.sh"

check_program_installed "podman" || exit 1
check_program_installed "jq" || exit 1
check_program_installed "bc" || exit 1
check_program_installed "mvn" || exit 1

# Set default values
CI=${CI:-false}
RUN_INTEGRATION_TESTS=${RUN_INTEGRATION_TESTS:-false}
UNIT_TESTS=${UNIT_TESTS:-false}
PARALLEL_TESTS=${PARALLEL_TESTS:-false}
export LOG_LEVEL=${LOG_LEVEL:-"SUCCESS"}

if [ "$CI" = "false" ]; then
  log_info "Running in local mode. Tailing catalina.out file in the end."
else
  log_info "Running in CI mode. Skipping catalina.out tailing"
fi

#############################
# Build the container image #
#############################
# If ci build without the cache mount
if [ "$CI" != "true" ]; then
  log_info "Running in local mode. Create the m2 folder and populate it."
  # Ensure that the .m2 folder exists
  mkdir -p ./.m2
  # Get absolute path to the .m2 folder
  M2_PATH="$(cd ./.m2 >/dev/null 2>&1 || exit 1
    pwd -P
  )"
  mvn clean package -DskipTests -Dmaven.repo.local=$M2_PATH
fi

podman build --ignorefile ./.integration-scenarios/debian-12-tomcat-war-systemd/Dockerfile.dockerignore -t $CONTAINER_IMAGE --build-arg TOMCAT_MAJOR=10 --build-arg UNIT_TESTS=$UNIT_TESTS -f .integration-scenarios/debian-12-tomcat-war-systemd/Dockerfile .


##############################
# Run the integration tests  #
##############################
# If parallel tests are enabled, we need to set the log level to ERROR to avoid too much output
if [ "$PARALLEL_TESTS" = "true" ]; then
  export LOG_LEVEL="ERROR"
fi

declare -A running_processes

if [ "$RUN_INTEGRATION_TESTS" = "true" ]; then
#  # Execute the tests, catch the PIDs and add them to the processes array
  $START_DIRECTORY/tests/0_test_default.sh $CONTAINER_IMAGE &
  if [ "$PARALLEL_TESTS" = "false" ]; then wait $!; else running_processes+=( ["0_test_default.sh"]=$! ) && sleep 5; fi
  $START_DIRECTORY/tests/1_test_activate_second_profile_with_config.sh $CONTAINER_IMAGE &
  if [ "$PARALLEL_TESTS" = "false" ]; then wait $!; else running_processes+=( ["1_test_activate_second_profile_with_config.sh"]=$! ) && sleep 5; fi
  $START_DIRECTORY/tests/2_test_activate_three_profiles_with_env.sh $CONTAINER_IMAGE &
  if [ "$PARALLEL_TESTS" = "false" ]; then wait $!; else running_processes+=( ["2_test_activate_three_profiles_with_env.sh"]=$! ) && sleep 5; fi
fi

# Reactivate the logging
export LOG_LEVEL="SUCCESS"

# Wait for all processes to finish
for key in "${!running_processes[@]}"; do
  pid=${running_processes[${key}]}
  wait_for_pid $pid "Waiting for $key to finish"
  log_success "$key finished."
done

##############################
# Clean up the containers    #
##############################

# Get all containers starting with tomcat-test-* and clean them
CONTAINERS=$(podman ps -a --format "{{.Names}}" | grep $CONTAINER_NAME)
# If empty string, exit
if [ -z "$CONTAINERS" ]; then
  log_info "No containers to clean up"
  exit 0
else
  # Remove dangling containers
  for CONTAINER in $CONTAINERS; do
    if [[ $CONTAINER == "tomcat-test-"* ]]; then
      log_info "Cleanup container $CONTAINER"
      podman rm -f $CONTAINER
    fi
  done
fi