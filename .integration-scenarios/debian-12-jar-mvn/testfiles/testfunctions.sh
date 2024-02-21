#!/usr/bin/env bash

# Textdekoration
B=`tput bold`    # bold
U=`tput sgr 0 1` # underline
R=`tput smso`    # reverse (invertiert)
N=`tput sgr0`    # normal / reset

US=`tput smul`   # underline start
UE=`tput rmul`   # underline reset

# Foreground
FG_BLA=`tput setaf 0` #  black
FG_RED=`tput setaf 1` #  red
FG_GRN=`tput setaf 2` #  green
FG_ORA=`tput setaf 3` #  orange
FG_BLU=`tput setaf 4` #  blue
FG_PUR=`tput setaf 5` #  purple
FG_CYA=`tput setaf 6` #  cyan
FG_WHT=`tput setaf 7` #  white
FG_DEF=`tput setaf 9` #  default

# Background
BG_BLA=`tput setab 0` #  black
BG_RED=`tput setab 1` #  red
BG_GRN=`tput setab 2` #  green
BG_ORA=`tput setab 3` #  orange
BG_BLU=`tput setab 4` #  blue
BG_PUR=`tput setab 5` #  purple
BG_CYA=`tput setab 6` #  cyan
BG_WHT=`tput setab 7` #  white
BG_DEF=`tput setab 9` #  default

orsUrl=http://localhost:8082/ors/v2

function stopOrs() {
#  echo "killing java processes"
  killall -w java
#  echo "pidof java : "
#  pidof java
}

function clearEnvironment() {
  stopOrs
  unset ORS_CONFIG_LOCATION
  unset ORS_ENGINE_PROFILES_HGV_ENABLED
  unset ORS_ENGINE_PROFILES_CAR_ENABLED
  unset ORS_ENGINE_PROFILES_WHEELCHAIR_ENABLED
  rm -f $WORK_DIR/ors-config.yml
  rm -f $CONF_DIR_USER/ors-config.yml
  rm -f $CONF_DIR_ETC/ors-config.yml
}

function awaitOrsReady() {
  maxWaitSeconds=$1
  elapsedSeconds=0
  while sleep 1; do
    ((elapsedSeconds++))
    if [ $elapsedSeconds -ge $maxWaitSeconds ]; then break; fi

    health=$(curl --silent $orsUrl/health | jq -r '.status')
    if [ "$health" = "ready" ]; then break; fi
  done
}

function expectOrsStartupFails() {
  maxWaitSeconds=$1
  elapsedSeconds=0
  while sleep 1; do
    ((elapsedSeconds++))
    if [ $elapsedSeconds -ge $maxWaitSeconds ]; then break ; fi

    javaProcesses=$(pidof java | wc -w)
    if [[ $javaProcesses -eq 0 ]]; then break; fi
  done

  if [ $elapsedSeconds -ge $maxWaitSeconds ]; then
    echo "timeout"
  else
    echo "terminated"
  fi

}

function assertEquals() {
  expected=$1
  received=$2
  if [ "$expected" != "$received" ]; then
    echo "${FG_RED}ASSERTION ERROR:${N}"
    echo "expected: '${FG_GRN}${expected}${N}'"
    echo "received: '${FG_RED}${received}${N}'"
    exit 1
  else
    echo "${FG_GRN}received '$received' as expected${N}"
  fi
}

function requestEnabledProfiles() {
  echo $(curl --silent $orsUrl/status | jq -r '.profiles[].profiles')
}

