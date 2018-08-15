# Install and run openrouteservice with docker

Installing the openrouteservice backend service with **Docker** is quite straightforward. Please note that the [Dockerfile](../Dockerfile) located in the repository root directory is merely for building the [WAR file](https://www.wikiwand.com/en/WAR_(file_format)).

## Short version

Please clone the repository (downloading the archive and running docker is currently not supported) and run the following command within this `docker/` directory:

```bash
sudo docker-compose up -d
```

This will:

1. Build and test the openrouteservice core from the local codebase with the `docker/conf/app.config.sample` as the config file and the OpenStreetMap dataset for Heidelberg under `docker/data/` as sample data.
2. Generate the built `ors.war` file and expose it to `docker/build/` directory.
3. Launch the openrouteservice service on port `8080` within a tomcat container.

By default the service status is queryable via the `http://localhost:8080/ors/health` endpoint. When the service is ready, you will be able to request `http://localhost:8080/ors/status` for further information on the running services. If you use the default dataset you will be able to request `http://localhost:8080/ors/routes?profile=foot-walking&coordinates=8.676581,49.418204|8.692803,49.409465` for test purposes. 

## Long version

### WAR file building

For building the WAR file only, either run

```bash
docker run -v /Users/user/build:/ors-core/build giscience/openrouteservice
```

or

```bash
docker-compose up ors-build
```

If everything goes fine, the built `ors.war` archive can be found under the shared host directory, e.g. `/Users/user/build` for the above `docker run` command or `./build/` for the `docker-compose` command.

### Run openrouteservice

No matter whether the WAR file has been built or not, simply run:

```bash
sudo docker-compose up
```

will take care of all steps with the sample Heidelberg dataset.

### Run with your own OpenStreetMap dataset

Prepare the OSM dataset (formats supported are `.osm`, `.osm.gz`, `.osm.zip` and `.pbf`) in the `docker/data/` directory. Adapt your own `app.config` (check the sample with detailed comments [here](../openrouteservice/WebContent/WEB-INF/app.config.sample) for reference) and change the `APP_CONFIG` variable in `docker-compose.yml` to let it point to your customized `app.config`. Then, run `docker-compose up`.

It should be mentioned that if your dataset is very large, please adjust the `-Xmx` parameter of `JAVA_OPTS` in `docker-compose.yml`. According to our experience, this should be at least `180g` for the whole globe if you are planning to use 3 or more modes of transport.
