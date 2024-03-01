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

function printCliHelp() { # TODO adapt
  echo -e "\
${B}$SCRIPT${N} - run ors tests in containers

${B}Usage:${N} $SCRIPT [options] (at least one of -j|-m is required)

${B}Options:${N}
    ${B}-b      ${N} -- Build docker containers
    ${B}-c      ${N} -- Clear graphs volume
    ${B}-C      ${N} -- Clear graphs volume before each test (caution when running tests in parallel)
    ${B}-d <arg>${N} -- Base name for docker image, will be extended with '-jar'/'-mvn', default: ${dockerImageBase}
    ${B}-f      ${N} -- Fail fast
    ${B}-j      ${N} -- Run with ${B}java -jar${N}
    ${B}-m      ${N} -- Run with ${B}mvn spring-boot:run${N}
    ${B}-t <arg>${N} -- Tests to run specified by globbing pattern (quote)
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

    if (($verbose)); then
      $testscript "${runType}" "${imageName}"
    else
      $testscript "${runType}" "${imageName}" 1>/dev/null 2>&1
    fi

    if (($?)); then
      hasErrors=1
      ((failed++))
      echo -e "${FG_RED}${B}failed${N}"
      (($failFast)) && exit 1
    else
      ((passed++))
      echo -e "${FG_GRN}passed${N}"
    fi
}

function exitWithRunTypeMissing() {
    echo -e "${FG_RED}${B}Error: When -t <arg> or -b is set, at least one of -j nor -m is required!${N} Type ${B}$SCRIPT -h${N} for help. "
    exit 1
}

while getopts :bcCd:fjmt:vh FLAG; do
  case $FLAG in
    b) wantBuildContainers=1;;
    c) clearGraphs=1;;
    C) clearGraphsBeforeEachTest=1;;
    d) dockerImageBase="$OPTARG";;
    f) failFast=1;;
    j) jar=1;;
    m) mvn=1;;
    t) pattern="$OPTARG";;
    v) verbose=1;;
    h)
      printCliHelp
      exit 0;;
    \?)
      echo -e "${FG_RED}${B}Error: Unknown option -${B}$OPTARG${N}. Type ${B}$SCRIPT -h${N} for help."
      exit 1;;
  esac
done

# This tells getopts to move on to the next argument.
shift $((OPTIND-1))

! (($jar)) && ! (($mvn)) && (($wantBuildContainers)) && exitWithRunTypeMissing
! (($jar)) && ! (($mvn)) && [ -n "$pattern" ] && exitWithRunTypeMissing

if (($clearGraphs)); then
  echo -e "Clearing ${TESTROOT}/graphs_volume/"
  rm -rf ${TESTROOT}/graphs_volume/*
fi

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

hasErrors=0
passed=0
failed=0
for testscript in ${TESTROOT}/tests/${pattern}; do
  (($jar)) && runTest jar $testscript $dockerImageJar $verbose
  (($mvn)) && runTest mvn $testscript $dockerImageMvn $verbose
done

(($passed)) && passedText=", ${FG_GRN}${B}${passed} passed${N}"
(($failed)) && failedText=", ${FG_RED}${B}${failed} failed${N}"

total=$(($passed + $failed))
echo -e "${FG_BLU}$(date +%Y-%m-%dT%H:%M:%S)${N} ${B}done, ${total} test$( (($total-1)) && echo "s") executed${passedText}${failedText}"
exit $hasErrors
