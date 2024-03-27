#!/usr/bin/env bash

# Textdekoration
B='\e[1m' #`tput bold`    # bold
U='\e[4m' #`tput sgr 0 1` # underline
R='\e[7m' #`tput smso`    # reverse (invertiert)
N='\e[0m' #`tput sgr0`    # normal / reset

BE='\e[22m' #`tput smul`   # bold reset
UE='\e[24m' #`tput rmul`   # underline reset
RE='\e[27m'

# Foreground
FG_BLA='\e[30m' #`tput setaf 0` #  black
FG_RED='\e[31m' #`tput setaf 1` #  red
FG_GRN='\e[32m' #`tput setaf 2` #  green
FG_ORA='\e[33m' #`tput setaf 3` #  orange
FG_BLU='\e[34m' #`tput setaf 4` #  blue
FG_PUR='\e[35m' #`tput setaf 5` #  purple
FG_CYA='\e[36m' #`tput setaf 6` #  cyan
FG_WHT='\e[37m' #`tput setaf 7` #  white
FG_DEF='\e[39m' #`tput setaf 9` #  default

# Background
BG_BLA='\e[40m' #`tput setab 0` #  black
BG_RED='\e[41m' #`tput setab 1` #  red
BG_GRN='\e[42m' #`tput setab 2` #  green
BG_ORA='\e[43m' #`tput setab 3` #  orange
BG_BLU='\e[44m' #`tput setab 4` #  blue
BG_PUR='\e[45m' #`tput setab 5` #  purple
BG_CYA='\e[46m' #`tput setab 6` #  cyan
BG_WHT='\e[47m' #`tput setab 7` #  white
BG_DEF='\e[49m' #`tput setab 9` #  default


function getOrsUrl() {
  port=$1
  echo "http://localhost:${port}/ors/v2"
}

function stopOrs() {
  killall -w java
}

function clearEnvironment() {
  stopOrs
  unset ORS_CONFIG_LOCATION
  unset ORS_ENGINE_PROFILES_HGV_ENABLED
  unset ORS_ENGINE_PROFILES_CAR_ENABLED
  unset ORS_ENGINE_PROFILES_WHEELCHAIR_ENABLED
  rm -f $CONTAINER_WORK_DIR/ors-config.yml
  rm -f $CONTAINER_CONF_DIR_USER/ors-config.yml
  rm -f $CONTAINER_CONF_DIR_ETC/ors-config.yml
}

function awaitOrsReady() {
  maxWaitSeconds=${1}
  port=${2}
  elapsedSeconds=0
  while sleep 1; do
    ((elapsedSeconds++))
    if [ $elapsedSeconds -ge $maxWaitSeconds ]; then break; fi

    health=$(curl --silent $(getOrsUrl $port)/health | jq -r '.status')
    if [ "$health" = "ready" ]; then break; fi
  done
}

function expectOrsStartupFails() {
  maxWaitSeconds=$1
  container=$2
  elapsedSeconds=0
  while sleep 1; do
    ((elapsedSeconds++))
    if [ $elapsedSeconds -ge $maxWaitSeconds ]; then break ; fi

    running=$(podman ps | grep -e " $CONTAINER$" | wc -l)
    if [[ $running -eq 0 ]]; then break; fi
  done

  if [ $elapsedSeconds -ge $maxWaitSeconds ]; then
    echo "timeout"
  else
    echo "terminated"
  fi
}

function assertSortedWordsEquals() {
  expected=$1
  received=$2
  sorted_expected=$(echo "$expected" | tr ' ' '\n' | sort | tr '\n' ' ')
  sorted_received=$(echo "$received" | tr ' ' '\n' | sort | tr '\n' ' ')
  assertEquals "$sorted_expected" "$sorted_received"
}

function assertEquals() {
  expected=$1
  received=$2
  check=$3
  if [ -n "$check" ]; then checkMsg="Checking '$check': "; fi
  if [ "$expected" != "$received" ]; then
    echo -e "${FG_RED}ASSERTION ERROR:${N} ${checkMsg}"
    echo -e "expected: '${FG_GRN}${expected}${N}'"
    echo -e "received: '${FG_RED}${received}${N}'"
    exit 1
  else
    echo -e "${FG_GRN}${checkMsg}Received '${received}' as expected${N}"
  fi
}

function assertContains() {
  expected=$1
  received=$2
  num=$(echo "${received}" | grep -c "${expected}")
  if [[ $num -eq 0 ]]; then
    echo -e "${FG_RED}ASSERTION ERROR:${N}: '${expected}' not contained as expected${N} $num"
    exit 1
  else
    echo -e "${FG_GRN}'${expected}' is contained as expected${N}"
  fi
}

function requestStatusString() {
  port=$1
  echo $(curl --silent $(getOrsUrl $port)/status | jq . )
}

function requestEnabledProfiles() {
  port=$1
  echo $(curl --silent $(getOrsUrl $port)/status | jq -r '.profiles[].profiles')
}

function removeExtension() {
  echo "${1%.*}"
}

function isPortInUse() {
  port=$1
  if [ -z "$port" ]; then echo ""; exit 1; fi
  echo "$(ss -tulpn | grep ":${port}" | wc -l)"
}

function findFreePort() {
    local port="${1:-8080}"
    local max_port=${2:-$((port + 100))}
#    echo "$port .. $max_port"
    while [[ "$port" -lt "$max_port" ]]; do
        if ! (($(isPortInUse $port))); then
            echo "$port"
            return 0  # Port is available, return success
        fi
        ((port++))  # Try the next port
    done

    # If no available port found, return failure
    return 1
}

function getProgramArguments() {
    runType=$1
    shift
    case "$runType" in
      jar) echo "$*";;
      mvn) echo "-Dspring-boot.run.arguments='$*'";;
    esac
}

function printError(){
  message="$1"
  echo -e "${FG_RED}ERROR: ${N}${B}${FG_RED}${message}"
}

function prepareTest() {
  script=$1
  runType=$2
  IMAGE=$3

  if [ -z "$runType" ]; then printError "missing param 1: runType (jar|mvn)"; exit 1; fi
  if [ -z "$IMAGE" ]; then printError "missing param 2: docker image"; exit 1; fi

  CONTAINER=${runType}-$(removeExtension "$(basename $script)")
  HOST_PORT=$(findFreePort 8082)

  mkdir -p ~/.m2
  M2_FOLDER="$(realpath ~/.m2)"
}

function cleanupTest() {
  podman stop "$CONTAINER"
  deleteTempFiles "$script"
}

function printVariables(){
  echo "CONTAINER_WORK_DIR=$CONTAINER_WORK_DIR"
  echo "CONTAINER_CONF_DIR_USER=$CONTAINER_CONF_DIR_USER"
  echo "CONTAINER_CONF_DIR_ETC=$CONTAINER_CONF_DIR_ETC"
  echo "IMAGE=$IMAGE"
  echo "M2_FOLDER=$M2_FOLDER"
}

function makeTempFile() {
  script=$1
  content=$2
  mkdir -p "$TESTROOT/tmp"
  tempFile=$(mktemp "${TESTROOT}/tmp/${script}.XXXXXXXXX")
  echo "$content" >> $tempFile
  echo "$tempFile"
}

function deleteTempFiles() {
  script=$1
  rm "${TESTROOT}/tmp/${script}".*
}