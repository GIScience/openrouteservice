#!/usr/bin/env bash

max_iteration=2
max_threads=(10 15)
thread_times=()

# trap ctrl-c and call ctrl_c()
trap ctrl_c INT

function ctrl_c() {
        echo ">>>>>>>> Results <<<<<<<<"
        echo "${thread_times[@]}"
        exit 1
}

for thread in "${max_threads[@]}"
do
  iteration_times=()
  for i in $(seq 1 $max_iteration); do
    start_time=$(date +%s)
    docker rm --force ors-instance
    echo "Running iteration $i with ${thread} threads"
    ch_threads="ors.engine.profile_default.preparation.methods.ch.threads=${thread}"
    fastisochrones_threads="ors.engine.profile_default.preparation.fastisochrones.threads=${thread}"
    docker run -it --user $UID -d -p 8082:8082 --env-file ./config.env -e ${ch_threads} -e {fastisochrones_threads} -v ./karlsruhe-regbez-latest.osm.pbf:/home/ors/files/osm.pbf:ro --name ors-instance openrouteservice/openrouteservice:v8.1.0
    ../.github/utils/url_check.sh 127.0.0.1 8082 /ors/v2/health 200 1800 10 3
    end_time=$(date +%s)
    iteration_times+="$((end_time - start_time))s "
  done
  thread_times+=("Threads: ${thread} [${iteration_times[@]}]")
done

docker rm --force ors-instance

echo ">>>>>>>> Results <<<<<<<<"
echo "${thread_times[@]}"

