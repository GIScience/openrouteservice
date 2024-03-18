# Openrouteservice

[![Docker Nightly Build Status](https://img.shields.io/github/actions/workflow/status/GIScience/openrouteservice/docker-nightly-image.yml)](https://github.com/GIScience/openrouteservice/actions/workflows/docker-nightly-image.yml)
[![Docker](https://img.shields.io/docker/cloud/build/heigit/openrouteservice?label=Docker&style=flat)](https://hub.docker.com/r/heigit/openrouteservice/builds)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GIScience_openrouteservice&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=GIScience_openrouteservice)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=GIScience_openrouteservice&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=GIScience_openrouteservice)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=GIScience_openrouteservice&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=GIScience_openrouteservice)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GIScience_openrouteservice&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=GIScience_openrouteservice)
[![Release](https://img.shields.io/github/v/release/GIScience/openrouteservice)](https://github.com/GIScience/openrouteservice/releases/latest)
[![LICENSE](https://img.shields.io/github/license/GIScience/openrouteservice)](LICENSE)

Openrouteservice is a highly customizable, performant routing service written in Java. 
It provides global spatial services by consuming user-generated and collaboratively collected free geographic data directly from [OpenStreetMap](http://www.openstreetmap.org): 

* [Directions Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/directions/): Get directions for different modes of transport
* [Isochrones Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/isochrones/): Obtain areas of reachability from given locations
* [Matrix Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/matrix/): Obtain one-to-many, many-to-one and many-to-many matrices for time and distance
* [Snapping Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/snapping/): Snap coordinates to the graph edges _(/!\ not available in our live API)_

Furthermore, there are technical endpoints that are **not available in our live API**:

* [Export Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/export/): Export the base graph for different modes of transport
* [Health Endpoint](https://giscience.github.io/openrouteservice/api-reference/endpoints/health/): Get information on the health of the running openrouteservice instance
* [Status Endpoint](https://giscience.github.io/openrouteservice/api-reference/endpoints/status/): Get information on the status of the openrouteservice instance

And there are services that are not part of openrouteservice itself, that we have **included in our public API** for convenience:

* [POI Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/poi/): Stand-Alone service from HeiGIT that returns points of interest in the area surrounding a geometry
* [Elevation Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/elevation/): Stand-Alone service from HeiGIT that returns the elevation for point or line geometries
* [Geocoding Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/geocoder/): Third Party Service ([Pelias](https://www.pelias.io)) hosted by HeiGIT that resolves geographic coordinates to addresses and vice versa
* [Optimization Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/optimization/): Third Party Service ([VROOM](https://github.com/VROOM-Project/vroom)) hosted by HeiGIT that optimizes routes for vehicle fleets

To play around with openrouteservice you may use our [demonstration server](https://maps.openrouteservice.org) which comes with both the backend and a [frontend](https://github.com/GIScience/ors-map-client). Or simply [sign up](https://openrouteservice.org/dev/#/signup) for an API key and fire your requests against the API directly.
![map-client-isochrones](docs/public/map-client-isochrones.png)

Please note that openrouteservice uses a forked and edited version of [graphhopper 4.0](https://github.com/GIScience/graphhopper) which can be found [here](https://github.com/GIScience/graphhopper).


## API Documentation

For an easy and interactive way to test the api, visit our [API Playground](https://openrouteservice.org/dev/#/api-docs).
After obtaining your key you can try out the different endpoints instantly and start firing requests.
Go to the [API Reference](https://giscience.github.io/openrouteservice/api-reference/) for more information.


## Installation

You can easily run openrouteservice yourself! We suggest using docker to install and launch openrouteservice. In short, a machine with a working [docker installation](https://www.digitalocean.com/community/tutorial_collections/how-to-install-and-use-docker) will get everything done for you. 

Only use nightly (main branch) if you know what you do. We recommend running docker compose with the latest release version:

Change to the directory where you want to install your local openrouteservice and first create some directories, where openrouteservice will persist its data:
```shell
mkdir -p ors-docker/config ors-docker/elevation_cache ors-docker/graphs ors-docker/files ors-docker/logs
```

Get the docker compose file for a release, e.g. v8.0.0: 
```shell
wget https://github.com/GIScience/openrouteservice/releases/download/v8.0.0/docker-compose.yml
```

Start openrouteservice in the background:
```shell
docker compose up -d
```

This will pull the openrouteservice docker image of the selected version and start it up using an example setup
and the provided test OSM file for Heidelberg/Germany and surrounding area.

To see the container's logs, run
```shell
docker compose logs 
```
or
```shell
docker compose logs -tf  
```
to follow the log. You can stop tailing the output with `CTRL+C`, the docker container will continue running.

To stop the container, execute
```shell
docker compose stop 
```
or
```shell
docker compose down
```
to stop and remove the container. Removing the container is necessary, when you want to take changes on the `docker-compose.yml` to take effect.

For instructions on how to configure openrouteservice, how to build from source and for other ways to operate openrouteservice, 
take a look at [Running your own openrouteservice instance](https://giscience.github.io/openrouteservice/run-instance/).

## Usage

Openrouteservice offers a set of endpoints for different spatial purposes. By default, they will be available on port 8080:

- `http://localhost:8080/ors/v2/directions`
- `http://localhost:8080/ors/v2/isochrones`
- `http://localhost:8080/ors/v2/matrix`
- `http://localhost:8080/ors/v2/snap`
- `http://localhost:8080/ors/v2/export`
- `http://localhost:8080/ors/v2/health`
- `http://localhost:8080/ors/v2/status`

You can find more information in the endpoint documentation pages linked above.

On the [API Reference](https://giscience.github.io/openrouteservice/api-reference/) there is also a description
how you can use the Swagger-UI and the API Playground for local instances of openrouteservice.


## Changelog/latest changes

[Openrouteservice CHANGELOG](https://github.com/GIScience/openrouteservice/blob/main/CHANGELOG.md)


## Contribute

We appreciate any kind of contribution - bug reports, new feature suggestion or improving our translations are greatly appreciated. Feel free to create an [issue](https://github.com/GIScience/openrouteservice/issues) and label it accordingly. If your issue regards the openrouteservice web-app please use the [corresponding repository](https://github.com/GIScience/ors-map-client/issues).

If you want to contribute your improvements, please follow the steps outlined in [our CONTRIBUTION guidelines](./CONTRIBUTE.md)

The [sourcespy dashboard](https://sourcespy.com/github/giscienceopenrouteservice/) provides a high level overview of the repository including technology summary, module dependencies and other components of the system.


## Questions

For questions please use our [community forum](https://ask.openrouteservice.org).


## Translations

If you notice anything wrong with translations, or you want to add a new language to the ORS instructions, we have some instructions in our [backend documentation](https://GIScience.github.io/openrouteservice/contributing/contributing-translations) about how you can submit an update. You can also look over at our [maps client GitHub](https://github.com/GIScience/ors-map-client/#add-language) if you want to contribute the language to there as well (adding or editing the language in the openrouteservice GitHub repo only affects the instructions - any new language also needs adding to the client).
