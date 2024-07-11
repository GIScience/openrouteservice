#!/usr/bin/env bash

OSM_FILE=${1:-"./heidelberg.osm.gz"}
MAX_ITERATIONS=${2:-"2"}
XMX=${3:-"5G"}
JAR_FILE=${4}

thread_times=()

# trap ctrl-c and call ctrl_c()
trap ctrl_c INT

function ctrl_c() {
        echo ">>>>>>>> Results <<<<<<<<"
        echo "${thread_times[@]}"
        exit 1
}

docker build -t openrouteservice:local -f ../Dockerfile ..

jar_volume=""
# Check JAR_FILE is not empty
if [ -n "$JAR_FILE" ]; then
  jar_volume="-v ${JAR_FILE}:/ors.jar:ro"
fi

echo "Starting ORS instance with $MAX_ITERATIONS iterations"
for i in $(seq 1 "$MAX_ITERATIONS"); do
  echo "Iteration $i"
  start_time=$(date +%s)
  docker rm --force ors-instance || true
  docker run -it --user $UID -d -p 8082:8082 --env-file ./config.env -e XMX="${XMX}" "${jar_volume}" -v "${OSM_FILE}":/home/ors/files/osm.pbf:ro --name ors-instance openrouteservice:local
  ../.github/utils/url_check.sh 127.0.0.1 8082 /ors/v2/health 200 1800 10 3
  end_time=$(date +%s)
  iteration_times+="$((end_time - start_time))s "
done
thread_times+=("Iteration timings: ${thread} [${iteration_times[@]}]")

docker stop ors-instance

echo ">>>>>>>> Results <<<<<<<<"
echo "${thread_times[@]}"

