#!/usr/bin/env bash
#################################
# Description: Test the default container
#################################

# Define container name
SCRIPT_NAME=$(basename "$0")
export CONTAINER_NAME="tomcat-test-$SCRIPT_NAME"
export CONTAINER_IMAGE="${1:-'local/tomcat-test:latest'}"
export CONTAINER_ENGINE="podman"

# Get the absolute path of this script
START_DIRECTORY="$(
  cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
  pwd -P
)"


# Import logging and networking
. "$START_DIRECTORY/../helpers/1_logging.sh"
. "$START_DIRECTORY/../helpers/2_system.sh"
. "$START_DIRECTORY/../helpers/3_files.sh"
. "$START_DIRECTORY/../helpers/7_networking.sh"
. "$START_DIRECTORY/../helpers/8_openrouteservice.sh"

log_info "Running test suite $SCRIPT_NAME with container $CONTAINER_NAME from image $CONTAINER_IMAGE"
# Check if the container exists and remove it
if podman ps -a | grep -q $CONTAINER_NAME; then podman rm -f $CONTAINER_NAME; fi

# Start container
PORT=$(get_free_port)
podman run -d --name $CONTAINER_NAME --systemd true -p ${PORT}:8080 $CONTAINER_IMAGE

# Check the java version
check_java_version "17" || exit 1

# Check if the systemd service is enabled
check_systemd_service_enabled "openrouteservice" true || exit 1

# Wait for the container to start
wait_for_url "127.0.0.1:${PORT}/ors/v2/health" 100 200 1 5 || exit 1

# Check that one profile is loaded
check_number_of_profiles_loaded "127.0.0.1:${PORT}/ors/v2/status" 1 || exit 1

# Check that 'driving-car' is loaded
check_profile_loaded "127.0.0.1:${PORT}/ors/v2/status" "driving-car" true|| exit 1


log_info "Set the source file to heidelberg.osm.gz through the ors-config.yml"
podman exec -it $CONTAINER_NAME /bin/bash -c "yq -y -i '.ors.engine.source_file = \"/home/ors/files/heidelberg.osm.gz\"' /home/ors/ors-config.yml" || exit 1

log_info "Activating all profiles through the setenv.sh"
podman exec -it $CONTAINER_NAME /bin/bash -c "echo 'export JAVA_OPTS=\"\$JAVA_OPTS \
-Dors.engine.profiles.car.enabled=true \
-Dors.engine.profiles.hgv.enabled=true \
-Dors.engine.profiles.bike-regular.enabled=true \
-Dors.engine.profiles.bike-mountain.enabled=true \
-Dors.engine.profiles.bike-road.enabled=true \
-Dors.engine.profiles.bike-electric.enabled=true \
-Dors.engine.profiles.walking.enabled=true \
-Dors.engine.profiles.hiking.enabled=true \
-Dors.engine.profiles.wheelchair.enabled=true \
-Dors.engine.profiles.public-transport.enabled=true \
-Dors.engine.profiles.car.ext_storages.Borders.boundaries=/home/ors/files/borders/borders\.geojson \
-Dors.engine.profiles.car.ext_storages.Borders.ids=/home/ors/files/borders/ids.csv \
-Dors.engine.profiles.car.ext_storages.Borders.openborders=/home/ors/files/borders/openborders.csv \
-Dors.engine.profiles.car.ext_storages.HereTraffic.streets=/home/ors/files/traffic_data/here/Streets.shp \
-Dors.engine.profiles.car.ext_storages.HereTraffic.ref_pattern=/home/ors/files/traffic_data/here/ref_pattern.csv \
-Dors.engine.profiles.car.ext_storages.HereTraffic.pattern_15min=/home/ors/files/traffic_data/here/pattern_kph_15min.csv \
-Dors.engine.profiles.hgv.ext_storages.Borders.boundaries=/home/ors/files/borders/borders.geojson \
-Dors.engine.profiles.hgv.ext_storages.Borders.ids=/home/ors/files/borders/ids.csv \
-Dors.engine.profiles.hgv.ext_storages.Borders.openborders=/home/ors/files/borders/openborders.csv \
-Dors.engine.profiles.hgv.ext_storages.HereTraffic.streets=/home/ors/files/traffic_data/here/Streets.shp \
-Dors.engine.profiles.hgv.ext_storages.HereTraffic.ref_pattern=/home/ors/files/traffic_data/here/ref_pattern.csv \
-Dors.engine.profiles.hgv.ext_storages.HereTraffic.pattern_15min=/home/ors/files/traffic_data/here/pattern_kph_15min.csv \
-Dors.engine.profiles.walking.ext_storages.GreenIndex.filepath=/home/ors/files/green_streets_hd.csv \
-Dors.engine.profiles.walking.ext_storages.NoiseIndex.filepath=/home/ors/files/noise_data_hd.csv \
-Dors.engine.profiles.walking.ext_storages.ShadowIndex.filepath=/home/ors/files/shadow_index_hd.csv \
-Dors.engine.profiles.walking.ext_storages.csv.filepath=/home/ors/files/csv_data_hd.csv \
-Dors.engine.profiles.hiking.ext_storages.GreenIndex.filepath=/home/ors/files/green_streets_hd.csv \
-Dors.engine.profiles.hiking.ext_storages.NoiseIndex.filepath=/home/ors/files/noise_data_hd.csv \
-Dors.engine.profiles.hiking.ext_storages.ShadowIndex.filepath=/home/ors/files/shadow_index_hd.csv \
-Dors.engine.profiles.hiking.ext_storages.csv.filepath=/home/ors/files/csv_data_hd.csv \
-Dors.engine.profiles.public-transport.gtfs_file=/home/ors/files/vrn_gtfs_cut.zip\"' > /home/ors/setenv.sh" || exit 1

log_info "Cleaning the current graphs under /home/ors/graphs"
podman exec -it $CONTAINER_NAME /bin/bash -c "rm -rf /home/ors/graphs/*" || exit 1


# Restart the container silently
log_info "Restarting the container to activate the hgv and bike-regular profiles"
podman restart $CONTAINER_NAME > /dev/null || exit 1

# Wait for the container to start
wait_for_url "127.0.0.1:${PORT}/ors/v2/health" 300 200 5 5 || exit 1

# Check that one profile is loaded
check_number_of_profiles_loaded "127.0.0.1:${PORT}/ors/v2/status" 10 || exit 1

profiles_to_be_loaded=(
  "driving-car"
  "driving-hgv"
  "cycling-regular"
  "cycling-mountain"
  "cycling-road"
  "cycling-electric"
  "foot-walking"
  "foot-hiking"
  "wheelchair"
  "public-transport"
)
for profile in "${profiles_to_be_loaded[@]}"; do
  check_profile_loaded "127.0.0.1:${PORT}/ors/v2/status" $profile true || exit 1
done

# Check that each of the following folders exist
folders_to_expect=(
  "/home/ors/graphs/car"
  "/home/ors/graphs/hgv"
  "/home/ors/graphs/bike-regular"
  "/home/ors/graphs/bike-mountain"
  "/home/ors/graphs/bike-road"
  "/home/ors/graphs/bike-electric"
  "/home/ors/graphs/walking"
  "/home/ors/graphs/hiking"
  "/home/ors/graphs/wheelchair"
  "/home/ors/graphs/public-transport"
)
for folder in "${folders_to_expect[@]}"; do
  check_folder_exists $folder true || exit 1
done

# Check that each of the following files exist
files_to_expect=(
  "/home/ors/ors-config.yml"
  "/home/ors/setenv.sh"
)
for file in "${files_to_expect[@]}"; do
  check_file_exists $file true || exit 1
done

# Check that /opt/tomcat/10/temp/GeoTools folder doesnt exist yet
check_folder_exists "/opt/tomcat/10/temp/GeoTools" false || exit 1

# Check that the server responds with a 200 status code for an avoid polygon request
check_avoid_area_request "http://127.0.0.1:${PORT}/ors/v2/directions/cycling-regular/geojson" 200 || exit 1

# Check that  /opt/tomcat/10/temp/GeoTools folder exists now
check_folder_exists "/opt/tomcat/10/temp/GeoTools" true || exit 1