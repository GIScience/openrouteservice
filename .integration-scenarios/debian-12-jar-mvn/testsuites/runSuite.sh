source $TESTFILES_DIR/testfunctions.sh

suite=$1
failFast=$2

hasErrors=0

for testscript in ${TESTSUITES_DIR}/${suite}/*.sh; do
  echo -n "${suite}/$(basename $testscript)... "
  $testscript 1>/dev/null 2>&1
  if (($?)); then
    hasErrors=1
    echo "${FG_RED}failed${N}"
    [[ -n $failFast ]] && exit 1
  else
    echo "${FG_GRN}passed${N}"
  fi
done

exit $hasErrors
