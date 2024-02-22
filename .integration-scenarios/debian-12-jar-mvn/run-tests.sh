#!/usr/bin/env bash
TESTROOT="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/test.conf
source $TESTROOT/files/testfunctions.sh
SCRIPT=$(basename $0)
failFast=0
jar=0
mvn=0
pattern="*.sh"

function printCliHelp() { # TODO adapt
  echo "\
${B}$SCRIPT${N} - run ors tests in containers

${B}Usage:${N} $SCRIPT [options] (at least one of -j|-m is required)

${B}Options:${N}
    ${B}-f      ${N} -- Fail fast
    ${B}-j      ${N} -- Run with ${B}java -jar${N}
    ${B}-m      ${N} -- Run with ${B}mvn spring-boot:run${N}
    ${B}-p <arg>${N} -- Pattern to specify tests (quote)
    ${B}-h      ${N} -- Display this help and exit
"
}

function runTest() {
    runType=$1
    testscript=$2
    echo -n "${FG_BLU}$(date +%Y-%m-%dT%H:%M:%S)${N} ${B}$(basename $testscript)${N} ${runType}... "
    $testscript "${runType}" 1>/dev/null 2>&1
    if (($?)); then
      hasErrors=1
      echo "${FG_RED}${B}failed${N}"
      (($failFast)) && exit 1
    else
      echo "${FG_GRN}passed${N}"
    fi
}

while getopts :fjmp:h FLAG; do
  case $FLAG in
    f) failFast=1;;
    j) jar=1;;
    m) mvn=1;;
    p) pattern="$OPTARG";;
    h)
      printCliHelp
      exit 0;;
    \?) #unrecognized option - show help
      echo "${FG_RED}${B}Error: Unknown option -${B}$OPTARG${N}. Type ${B}$SCRIPT -h${N} for help."
      exit 1;;
  esac
done

# This tells getopts to move on to the next argument.
shift $((OPTIND-1))

if ! (($jar)) && ! (($mvn)); then echo "${FG_RED}${B}Error: Neither option -j nor -m is set!${N} Type ${B}$SCRIPT -h${N} for help. "; exit 1; fi

hasErrors=0

for testscript in ${TESTROOT}/tests/${pattern}; do
  (($jar)) && runTest jar $testscript
  (($mvn)) && runTest mvn $testscript
done

exit $hasErrors
