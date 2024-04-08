# Openrouteservice

[![Docker Nightly Build Status](https://img.shields.io/github/actions/workflow/status/GIScience/openrouteservice/docker-nightly-image.yml?style=flat&label=Docker%20Nightly&link=https%3A%2F%2Fhub.docker.com%2Fr%2Fheigit%2Fopenrouteservice%2Ftags)](https://hub.docker.com/r/heigit/openrouteservice/tags)
[![Docker Version Status](https://img.shields.io/github/actions/workflow/status/GIScience/openrouteservice/publish-tagged-release.yml?style=flat&label=Docker%20Latest&link=https%3A%2F%2Fhub.docker.com%2Fr%2Fheigit%2Fopenrouteservice%2Ftags)](https://hub.docker.com/r/heigit/openrouteservice/tags)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GIScience_openrouteservice&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=GIScience_openrouteservice)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=GIScience_openrouteservice&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=GIScience_openrouteservice)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=GIScience_openrouteservice&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=GIScience_openrouteservice)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GIScience_openrouteservice&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=GIScience_openrouteservice)
[![Release](https://img.shields.io/github/v/release/GIScience/openrouteservice)](https://github.com/GIScience/openrouteservice/releases/latest)
[![LICENSE](https://img.shields.io/github/license/GIScience/openrouteservice)](LICENSE)

Openrouteservice is a highly customizable, performant routing service written in Java. 
It uses a [forked and edited version of graphhopper 4.0](https://github.com/GIScience/graphhopper) 
and provides global spatial services by consuming user-generated and collaboratively collected free geographic data directly from [OpenStreetMap](http://www.openstreetmap.org): 

* [Directions Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/directions/): Get directions for different modes of transport
* [Isochrones Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/isochrones/): Obtain areas of reachability from given locations
* [Matrix Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/matrix/): Obtain one-to-many, many-to-one and many-to-many matrices for time and distance
* [Snapping Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/snapping/)¹: Snap coordinates to the graph edges  
* [Export Service](https://giscience.github.io/openrouteservice/api-reference/endpoints/export/)¹: Export the base graph for different modes of transport
* [Health Endpoint](https://giscience.github.io/openrouteservice/api-reference/endpoints/health/)¹: Get information on the health of the running openrouteservice instance
* [Status Endpoint](https://giscience.github.io/openrouteservice/api-reference/endpoints/status/)¹: Get information on the status of the openrouteservice instance

¹) **Snapping, Export, Health and Status Endpoint are not available in our public openrouteservice API aka "live API"!** 
You can use them by running your own instance of openrouteservice.

And to avoid any misunderstandings, it should also be mentioned at this point that our live API provides several other endpoints 
that are **not part of the openrouteservice software/repository**:

* [openpoiservice](https://github.com/GIScience/openpoiservice): A stand-alone service from HeiGIT that returns points of interest in the area surrounding a geometry
* [openelevationservice](https://github.com/GIScience/openelevationservice): A stand-alone service from HeiGIT that returns the elevation for point or line geometries
* [Pelias](https://www.pelias.io): A (reverse) geocoder hosted by HeiGIT that resolves geographic coordinates to addresses and vice versa
* [VROOM](https://github.com/VROOM-Project/vroom): The Vehicle Routing Open-source Optimization Machine hosted by HeiGIT 

To play around with openrouteservice you may use our [demonstration server](https://maps.openrouteservice.org) which comes with both the backend and a [frontend](https://github.com/GIScience/ors-map-client). 
Or simply [sign up](https://openrouteservice.org/dev/#/signup) for an API key and fire your requests against the API directly.
You can also do this in our [API Playground](https://openrouteservice.org/dev/#/api-docs) - take a look at the [API Reference](https://giscience.github.io/openrouteservice/api-reference/) to get more information.

![map-client-isochrones](docs/public/map-client-isochrones.png)


## Installation

You can easily [run openrouteservice](https://giscience.github.io/openrouteservice/run-instance/) yourself! 

**tl;dr:** We suggest [using docker](https://giscience.github.io/openrouteservice/run-instance/running-with-docker) to install and launch openrouteservice. 
In short, a machine with a working [docker installation](https://www.digitalocean.com/community/tutorial_collections/how-to-install-and-use-docker) will get everything done for you. 

Change to the directory where you want to install your local openrouteservice and first create some directories, where openrouteservice will persist its data:
```shell
mkdir -p ors-docker/config ors-docker/elevation_cache ors-docker/graphs ors-docker/files ors-docker/logs
```

Only use nightly (main branch) if you know what you do. 
We recommend running docker compose with the latest release version. 
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

To see the container's logs, run:
```shell
docker compose logs 
```

Stop the container with:
```shell
docker compose down
```

## Usage

The above mentioned endpoints will be available on port 8080:

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
