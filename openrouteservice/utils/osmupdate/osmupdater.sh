#!/bin/bash
cd /home/mrylov/osmupdate/  # (insert your user name here)

MAP="planet-latest.osm.pbf"
MAP_CROPPED="planet-cropped.osm.pbf"
MAP_CROPPED_UPDATED="planet-cropped-updated.osm.pbf"
MAP_ORS="planet-ors.osm.pbf"

POLY="planet.poly"
PBFMAPURL="http://planet.osm.org/pbf/"$MAP

echo osmupdater is running...

# rotate log and write new headline
mv upd.log upd.log_temp
tail -10000 upd.log_temp>upd.log
rm upd.log_temp
echo >> upd.log
echo Starting update script >> upd.log
date >> upd.log
 
if [ ! -e $MAP_CROPPED ]; then
  echo "----> Downloading pbf map" >> upd.log
  wget $PBFMAPURL
  echo
  echo "-----> Crop pbf map to a new map" >> upd.log
  ./osmconvert --verbose $MAP -B=$POLY -o=$MAP_CROPPED
  echo "-----> Remove planet file" >> upd.log
  rm $MAP
  echo
fi

if [ -e $MAP_CROPPED ]; then
  echo "-----> Update pbf map" >> upd.log
  ./osmupdate --verbose $MAP_CROPPED $MAP_CROPPED_UPDATED -B=$POLY 
  if [ -e $MAP_CROPPED_UPDATED ]; then
    cp $MAP_CROPPED_UPDATED $MAP_CROPPED
    mv $MAP_CROPPED_UPDATED $MAP_ORS
    md5sum $MAP_ORS > $MAP_ORS.md5 
  else
    echo "-----> OSM file is up to date" >> upd.log
  fi
  echo
else
  echo "-----> Cropped file does not exist" >> upd.log
fi

