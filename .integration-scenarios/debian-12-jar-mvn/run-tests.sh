#!/usr/bin/env bash
TESTROOT="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/test.conf
source $TESTROOT/files/testfunctions.sh

failFast=0
jar=0
mvn=0
pattern="*.sh"

function printCliHelp() { # TODO adapt
  printInfo "\
${B}$SCRIPT${N} - run ors tests in containers

${B}Usage:${N} $SCRIPT [options]

${B}Options:${N}
    ${B}-f      ${N} -- Fail fast
    ${B}-j      ${N} -- Run with java -jar
    ${B}-m      ${N} -- Run with mvn spring-boot:run
    ${B}-p <arg>${N} -- Pattern to specify tests (quote)
    ${B}-h      ${N} -- Display this help and exit
"
}

function runTest() {
    runType=$1
    testscript=$2
    echo -n "${FG_BLU}$(date +%Y-%m-%dT%H:%M:%S)${N} ${B}$(basename $testscript)${N} with ${runType}... "
    $testscript "${runType}" 1>/dev/null 2>&1
    if (($?)); then
      hasErrors=1
      echo "${FG_RED}${B}failed${N}"
      [[ -n $failFast ]] && exit 1
    else
      echo "${FG_GRN}passed${N}"
    fi
}

while getopts :fjmp:h FLAG; do # TODO adapt
  case $FLAG in
    f) failFast=1;;
    j) jar=1;;
    m) mvn=1;;
    p) pattern="$OPTARG";;
    h)
      printCliHelp
      exit 0;;
    \?) #unrecognized option - show help
      printError "Unknown option -${B}$OPTARG${N}. Type ${B}$SCRIPT -h${N} for help."
      exit 1;;
  esac
done

# This tells getopts to move on to the next argument.
shift $((OPTIND-1))

hasErrors=0

for testscript in ${TESTROOT}/tests/${pattern}; do
  (($jar)) && runTest jar $testscript
  (($mvn)) && runTest mvn $testscript
done

exit $hasErrors
