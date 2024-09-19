#!/usr/bin/env bash
TESTROOT="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/test.conf
source $TESTROOT/files/testfunctions.sh
SCRIPT=$(basename $0)

dockerImageBase="ors-test"
failFast=0
jar=0
mvn=0
verbose=0
pattern=""

function printCliHelp() {
  echo -e "\
${B}$SCRIPT${N} - run ors tests in podman containers

${B}Usage:${N} $SCRIPT [options] (at least one of -j|-m is required)

${B}Options:${N}
    ${B}-b      ${N} -- Build podman containers
    ${B}-c      ${N} -- Clear graphs volume
    ${B}-C      ${N} -- Clear graphs volume before each test (caution when running tests in parallel)
    ${B}-d <arg>${N} -- Base name for podman image, will be extended with '-jar'/'-mvn', default: ${dockerImageBase}
    ${B}-f      ${N} -- Fail fast
    ${B}-i      ${N} -- Investigate: Keep temp files/directories and do not stop podman containers of failed tests
    ${B}-I      ${N} -- Investigate strong: Like -i, but also do not clear temp before test run and do not stop any containers
    ${B}-j      ${N} -- Run with ${B}java -jar${N}
    ${B}-l      ${N} -- List tests and exit
    ${B}-m      ${N} -- Run with ${B}mvn spring-boot:run${N}
    ${B}-t <arg>${N} -- Tests to run specified by globbing pattern
                Quote patterns with '*' or use '%' which will be replaced by '*'
                Multiple patterns can be defined in one argument, separated by blanks
    ${B}-v      ${N} -- Verbose: Print tests console output
    ${B}-h      ${N} -- Display this help and exit
"
}

function buildContainer() {
  dockerfile=$1
  imageName=$2
  m2Folder=$3
  CONTEXT_PATH="$(realpath "${TESTROOT}/../..")"

  echo -e "${FG_CYA}${B}Building docker image ${IMAGE_NAME_JAR}${N} with context ${CONTEXT_PATH}"
  if podman ps -a | grep -q "$imageName"; then
    echo -e "Removing existing image $imageName${N}"
    podman rm -f "$imageName";
  fi
  podman build -t local/"$imageName" -f "${TESTROOT}/${dockerfile}" --ignorefile "${TESTROOT}/${dockerfile}.dockerignore" -v ${m2Folder}:/root/.m2 --build-arg CONTAINER_WORK_DIR="$CONTAINER_WORK_DIR" --build-arg CONTAINER_CONF_DIR_USER="$CONTAINER_CONF_DIR_USER" --build-arg CONTAINER_CONF_DIR_ETC="$CONTAINER_CONF_DIR_ETC" "$CONTEXT_PATH"
}

function runTest() {
    runType=$1
    testscript=$2
    imageName=$3
    verbose=$4
    if [ ! -f "$testscript" ]; then return; fi

    case "${runType}" in
      "jar") coloredRunType="${FG_ORA}jar";;
      "mvn") coloredRunType="${FG_CYA}mvn";;
    esac

    echo -ne "${FG_BLU}$(date +%Y-%m-%dT%H:%M:%S)${N} $(basename $testscript)${N} ${coloredRunType}${N}... "

    (($clearGraphsBeforeEachTest)) && rm -rf ${TESTROOT}/graphs_volume/*

    local container=$(createContainerName $testscript $runType)

    if (($verbose)); then
      $testscript "${runType}" "${imageName}"
    else
      $testscript "${runType}" "${imageName}" 1>/dev/null 2>&1
    fi
    testStatus=$?
    stopContainerMsg=""

    if [ $testStatus -eq 1 ]; then
      hasErrors=1
      ((failed++))
      if (($investigate)) || (($investigateStrong)); then
        stopContainerMsg="investigate: ${FG_BLU}podman exec -ti ${FG_PUR}${container} ${FG_BLU}bash${N}"
      else
        podman stop "$container" 1>/dev/null
      fi
      echo -e "${FG_RED}${B}failed${N} ${stopContainerMsg}${N}"
      (($failFast)) && exit 1

    elif [ $testStatus -eq 2 ]; then
      ((skipped++))
      echo -e "${FG_ORA}${B}skipped${N}"

    else
      ((passed++))
      if (($investigateStrong)); then
        stopContainerMsg="investigate: ${FG_BLU}podman exec -ti ${FG_PUR}${container} ${FG_BLU}bash${N}"
      else
        podman stop "$container" 1>/dev/null
      fi
      echo -e "${FG_GRN}passed${N} ${stopContainerMsg}${N}"
    fi
}

function exitWithRunTypeMissing() {
    echo -e "${FG_RED}${B}Error: When -t <arg> or -b is set, at least one of -j nor -m is required!${N} Type ${B}$SCRIPT -h${N} for help. "
    exit 1
}

function listTests() {
  ls -A ${TESTROOT}/tests
}

while getopts :bcCd:fhiIjlmt:v FLAG; do
  case $FLAG in
    b) wantBuildContainers=1;;
    c) clearGraphs=1;;
    C) clearGraphsBeforeEachTest=1;;
    d) dockerImageBase="$OPTARG";;
    f) failFast=1;;
    h)
      printCliHelp
      exit 0;;
    i) investigate=1;;
    I) investigate=1; investigateStrong=1;;
    j) jar=1;;
    l)
      listTests
      exit 0;;
    m) mvn=1;;
    t) pattern="$OPTARG";;
    v) verbose=1;;
    \?)
      echo -e "${FG_RED}${B}Error: Unknown option -${B}$OPTARG${N}. Type ${B}$SCRIPT -h${N} for help."
      exit 1;;
  esac
done

# This tells getopts to move on to the next argument.
shift $((OPTIND-1))

! (($jar)) && ! (($mvn)) && (($wantBuildContainers)) && exitWithRunTypeMissing
! (($jar)) && ! (($mvn)) && [ -n "$pattern" ] && exitWithRunTypeMissing

dockerImageJar="${dockerImageBase}-jar"
dockerImageMvn="${dockerImageBase}-mvn"

if (($wantBuildContainers)); then
  mkdir -p ~/.m2
  m2Folder="$(realpath ~/.m2)"

  (($jar)) && buildContainer Dockerfile-jar "${dockerImageJar}" "${m2Folder}"
  (($mvn)) && buildContainer Dockerfile-mvn "${dockerImageMvn}" "${m2Folder}"
  (($jar)) || (($mvn)) || echo -e "${FG_RED}${B}Set -j or -m to specify which Docker image(s) to build!${N}"
fi

if [ -z "$pattern" ]; then echo -e "${B}No tests specified!${N}"; exit 0; fi

mkdir -p "${TESTROOT}/graphs_volume"
mkdir -p "${TESTROOT}/tmp"

if (($clearGraphs)); then
  echo -e "Clearing ${TESTROOT}/graphs_volume/"
  rm -rf ${TESTROOT}/graphs_volume/*
fi

if ! (($investigateStrong)); then
  echo -e "Clearing ${TESTROOT}/tmp/"
  rm -rf ${TESTROOT}/tmp/*
fi

hasErrors=0
passed=0
skipped=0
failed=0

for word in $pattern; do
  for testscript in ${TESTROOT}/tests/$(echo "$word" | sed s/%/\*/g ); do
    (($jar)) && runTest jar $testscript $dockerImageJar $verbose
    (($mvn)) && runTest mvn $testscript $dockerImageMvn $verbose
  done
  (($investigate)) || rm -rf ${TESTROOT}/tmp/*
done

(($passed)) && passedText=", ${FG_GRN}${B}${passed} passed${N}"
(($skipped)) && skippedText=", ${FG_ORA}${B}${skipped} skipped${N}"
(($failed)) && failedText=", ${FG_RED}${B}${failed} failed${N}"

total=$(($passed + $skipped + $failed))
echo -e "${FG_BLU}$(date +%Y-%m-%dT%H:%M:%S)${N} ${B}done, ${total} test$( (($total-1)) && echo "s") executed${passedText}${skippedText}${failedText}"
exit $hasErrors
