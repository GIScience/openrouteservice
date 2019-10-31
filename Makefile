download:
	curl -L https://download.bbbike.org/osm/bbbike/SanFrancisco/SanFrancisco.osm.pbf > docker/data/SanFrancisco.osm.pbf

setup: download
	make -C docker volume
	make -C route-web build-image

start:
	make -C docker up
	make -C route-web start

stop:
	make -C docker down
