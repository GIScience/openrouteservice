#!/usr/bin/env bash

OSM_FILE=${1:-"./heidelberg.osm.gz"}
MAX_ITERATIONS=${2:-"2"}
XMX=${3:-"5G"}

thread_times=()

# trap ctrl-c and call ctrl_c()
trap ctrl_c INT

function ctrl_c() {
        echo ">>>>>>>> Results <<<<<<<<"
        echo "${thread_times[@]}"
        exit 1
}

docker build -t openrouteservice:local -f ../Dockerfile ..


echo "Starting ORS instance with $MAX_ITERATIONS iterations"
for i in $(seq 1 "$MAX_ITERATIONS"); do
  echo "Iteration $i"
  start_time=$(date +%s)
  docker rm --force ors-instance || true
#  docker run -it --user $UID -d -p 8082:8082 -p 9001:9001 -e XMX="${XMX}" -v ./config.yml:/home/ors/config/ors-config.yml -v ./ors.jar:/ors.jar -v "${OSM_FILE}":/home/ors/files/osm.pbf:ro --name ors-instance openrouteservice:local
  docker run -it --user $UID -d -p 8082:8082 -p 9001:9001 -e XMX="${XMX}" -v ./config.yml:/home/ors/config/ors-config.yml -v "${OSM_FILE}":/home/ors/files/osm.pbf:ro --name ors-instance openrouteservice:local
  ../.github/utils/url_check.sh 127.0.0.1 8082 /ors/v2/health 200 1800 10 3
  end_time=$(date +%s)
  echo "ORS Graph build in $((end_time - start_time))s"
  iteration_times+="$((end_time - start_time))s "
done
thread_times+=("Iteration timings: ${thread} [${iteration_times[@]}]")

docker stop ors-instance

echo ">>>>>>>> Results <<<<<<<<"
echo "${thread_times[@]}"

