# Install and run openrouteservice with docker

Installing the openrouteservice backend service with **Docker** is quite straightforward.

Please clone the repository and run the following command within the `docker/` directory:

```bash
cd docker
docker-compose up -d
```

This will:

1. Build the openrouteservice [war file](https://www.wikiwand.com/en/WAR_(file_format)) from the local codebase and on container startup it sets `docker/conf/app.config.sample` as the config file and the OpenStreetMap dataset for Heidelberg under `docker/data/heidelberg.osm.gz` as sample data.
2. Launch the openrouteservice service on port `8080` within a tomcat container at the address `http://localhost:8080/ors`.

## Environment variables

- `BUILD_GRAPHS`: Forces ORS to rebuild the routings graph(s) when set to `True`. Useful when another PBF is specified in the Docker volume mapping to `/ors-core/data/osm_file.pbf`
- `JAVA_OPTS`: Custom Java runtime options, such as `-Xms` or `-Xmx`
- `CATALINA_OPTS`: Custom Catalina options

Specify either during container startup or in `docker-compose.yml`.

## Build arguments

When building the image, the following arguments are customizable:

- `APP_CONFIG`: Can be changed to specify the location of a custom `app.config` file. Default: `./docker/conf/app.config.sample`.
- `OSM_FILE`: Can be changed to point to a local custom OSM file.

These settings are also configurable during container startup in various ways.

## Customization

Once you have a built image you can decide to start a container with differently configured openrouteservice, e.g. changing the OSM file or other settings in the `app.config.sample`.

### Different OSM file

Either you point the build argument `OSM_FILE` to your desired OSM file during building the image.

Or to change the PBF file when restarting a container:
- change the volume that `/ors-core/data/osm_file.pbf` is mapping to your file location, e.g. `docker run -d -p 8080:8080 -v ./data/andorra-latest.osm.pbf:/ors-core/data/osm_file.pbf docker_ors-app` or in `docker-compose.yml`
- set the `BUILD_GRAPHS` variable to `True`, e.g. `docker run -d -p 8080:8080 -e BUILD_GRAPHS=True ./data/andorra-latest.osm.pbf:/ors-core/data/osm_file.pbf docker_ors-app` or in `docker-compose.yml`

It should be mentioned that if your dataset is very large, please adjust the `-Xmx` parameter of `JAVA_OPTS` environment variable. A good rule of thumb is to give Java 2 x file size of the PBF **per profile**.

Note, `.osm`, `.osm.gz`, `.osm.zip` and `.pbf` file format are supported for OSM files.
### Customize `app.config.sample`

Either you point the build argument `APP_CONFIG` to your custom `app.config` file.

Or to change the configuration for ORS when restarting a container:
- change (or even rename) the `/conf/app.config.sample` file
- set the config file as volume mapping to `/ors-core/openrouteservice/target/classes/resources/app.config`, e.g. `docker run -d -p 8080:8080 -v ./conf/app.config.sample:/ors-core/openrouteservice/target/classes/resources/app.config docker_ors-app` or in `docker-compose.yml`

## Checking

By default the service status is queryable via the `http://localhost:8080/ors/health` endpoint. When the service is ready, you will be able to request `http://localhost:8080/ors/status` for further information on the running services.

If you use the default dataset you will be able to request `http://localhost:8080/ors/routes?profile=foot-walking&coordinates=8.676581,49.418204|8.692803,49.409465` for test purposes.
