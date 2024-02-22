#!/usr/bin/env bash
TESTROOT="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/test.conf
source $TESTROOT/files/testfunctions.sh

pattern=${1:-*.sh}
failFast=$2

hasErrors=0

for testscript in ${TESTROOT}/tests/${pattern}; do
  echo -n "${FG_BLU}$(date +%Y-%m-%dT%H:%M:%S)${N} $(basename $testscript) ... "
  $testscript 1>/dev/null 2>&1
  if (($?)); then
    hasErrors=1
    echo "${FG_RED}${B}failed${N}"
    [[ -n $failFast ]] && exit 1
  else
    echo "${FG_GRN}passed${N}"
  fi
done

exit $hasErrors
