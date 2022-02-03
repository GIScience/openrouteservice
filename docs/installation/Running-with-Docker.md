---
parent: Installation and Usage
nav_order: 1
title: Running with Docker
---

# Running with Docker

## Install and run openrouteservice with docker

Installing the openrouteservice backend service with Docker is quite straightforward. All you need is an OSM extract, e.g. from [Geofabrik](http://download.geofabrik.de), and a working [docker installation](https://www.digitalocean.com/community/tutorial_collections/how-to-install-and-use-docker).

Use [Dockerhub's hosted Openrouteservice image](https://hub.docker.com/r/openrouteservice/openrouteservice) or build your own image

- either with `docker run`

```bash
# create directories for volumes to mount as local user
mkdir -p conf elevation_cache graphs logs/ors logs/tomcat
docker run -dt -u "${UID}:${GID}" \
  --name ors-app \
  -p 8080:8080 \
  -v $PWD/graphs:/ors-core/data/graphs \
  -v $PWD/elevation_cache:/ors-core/data/elevation_cache \
  -v $PWD/conf:/ors-conf \  # will copy the container's ors-config.json to the host
  #-v $PWD/your_osm.pbf:/ors-core/data/osm_file.pbf \  # your local PBF file
  -e "JAVA_OPTS=-Djava.awt.headless=true -server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:ParallelGCThreads=4 -Xms1g -Xmx2g" \
  -e "CATALINA_OPTS=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.rmi.port=9001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost" \
  openrouteservice/openrouteservice:latest
```

- or with `docker-compose`

```bash
cd docker
# create directories for volumes to mount as local user
mkdir -p conf elevation_cache graphs logs/ors logs/tomcat
docker-compose up -d
```

If you need to change the UID the ors is running with, you can use these variables:
```bash
# set it explicitly to 1001
ORS_UID=1001 ORS_GID=1001 docker-compose up -d

# or set it to the current user
ORS_UID=${UID} ORS_GID=${GID} docker-compose up -d
```

This will:

1. Build the openrouteservice [war file](https://www.wikiwand.com/en/WAR_(file_format)) from the local codebase and on container startup a local `./conf` folder is created which contains the `ors-config.json` controlling ORS behaviour. If you map a local `your_osm.pbf` to the container's `/ors-core/data/osm_file.pbf`, it will build a graph from that OSM file.
2. Launch the openrouteservice service on port `8080` within a tomcat container at the address `http://localhost:8080/ors`.

After container launch, modify the `./conf/ors-config.json` to your needs, and restart the container. Alternatively you can map an existing `ors-config.json` which you have locally to the container's `/ors-conf/ors-config.json` to initialize ORS immediately. If you changed the OSM file after the first container start, don't forget to use `BUILD_GRAPHS=True` to force a rebuild of the graph(s) (or delete the `./graphs` folder, which is the same thing).

## Volumes

There are some important directories one might want to preserve on the host machine, to survive container regeneration. These directories should be mapped as volumes.

- `/ors-core/data/graphs`: Contains the built graphs after ORS intialized.
- `/ors-core/data/elevation_cache`: Contains the CGIAR elevation tiles if elevation was specified.
- `/var/log/ors/`: Contains the ORS logs.
- `/usr/local/tomcat/logs`: Contains the Tomcat logs.
- `/ors-conf`: Contains the `ors-config.json` which is used to control ORS.
- `/ors-core/data/osm_file.pbf`: The OSM file being used to generate graphs.

Look at the [`docker-compose.yml`](https://github.com/GIScience/openrouteservice/blob/master/docker/docker-compose.yml) for examples.

## Environment variables

- `BUILD_GRAPHS`: Forces ORS to rebuild the routings graph(s) when set to `True`. Useful when another PBF is specified in the Docker volume mapping to `/ors-core/data/osm_file.pbf`
- `JAVA_OPTS`: Custom Java runtime options, such as `-Xms` or `-Xmx`
- `CATALINA_OPTS`: Custom Catalina options

Specify either during container startup or in `docker-compose.yml`.

## Build arguments

When building the image, the following arguments are customizable:

- `ORS_CONFIG`: Can be changed to specify the location of a custom `ors-config.json` file. Default `./openrouteservice/src/main/resources/ors-config-sample.json`.
- `OSM_FILE`: Can be changed to point to a local custom OSM file. Default `./openrouteservice/src/main/files/heidelberg.osm.gz`.

## Customization

Once you have a built image you can decide to start a container with different settings, e.g. changing the OSM file or other settings in the `ors-config-sample.json`.

### Different OSM file

Either you point the build argument `OSM_FILE` to your desired OSM file during building the image.

Or to change the PBF file when restarting a container:

1. change the path `/ors-core/data/osm_file.pbf` is pointing to to your new PBF
2. set the `BUILD_GRAPHS` variable to `True`

E.g.
`docker run -d -p 8080:8080 -e BUILD_GRAPHS=True ./data/andorra-latest.osm.pbf:/ors-core/data/osm_file.pbf docker_ors-app`

It should be mentioned that if your dataset is very large, please adjust the `-Xmx` parameter of `JAVA_OPTS` environment variable. A good rule of thumb is to give Java 2 x file size of the PBF **per profile**.

Note, `.osm`, `.osm.gz`, `.osm.zip` and `.pbf` file format are supported as OSM files.

### Customize ors configuration

Either you point the build argument `ORS_CONFIG` to your custom `ors-config.json` file.

The `ors-config.json` which is used is also copied to the container's `/share` directory. By mapping a directory to that path, you will get access to the `ors-config.json`. After changing values in the `ors-config.json` just restart the container. Example:

`docker run -d -p 8080:8080 -v ./conf:/ors-conf ors-app`

## Checking

By default the service status is queriable via the `http://localhost:8080/ors/v2/health` endpoint. When the service is ready, you will be able to request `http://localhost:8080/ors/v2/status` for further information on the running services.

If you use the default dataset you will be able to request `http://localhost:8080/ors/v2/directions/foot-walking?start=8.676581,49.418204&end=8.692803,49.409465` for test purposes.
