# Openrouteservice

[![Build Status](https://travis-ci.org/GIScience/openrouteservice.svg?branch=master)](https://travis-ci.org/GIScience/openrouteservice) 
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GIScience_openrouteservice&metric=alert_status&branch=master)](https://sonarcloud.io/dashboard?id=GIScience_openrouteservice&metric)

The **openrouteservice API** provides global spatial services by consuming user-generated and collaboratively collected free geographic data directly from [OpenStreetMap](http://www.openstreetmap.org). It is highly customizable, performant and written in Java.

The following services are available via a HTTP interface served by Tomcat.
- **Directions** - Returns a route between two or more locations for a selected profile with customizable additional settings and instructions.
- **Isochrones** - Obtains areas of reachability from given locations.
- **Matrix** - Computes one-to-many, many-to-one or many-to-many routes for any mode of transport provided by openrouteservice.

To play around with openrouteservice you may use our [demonstration server](https://maps.openrouteservice.org) which comes with both the backend and a [frontend](https://github.com/GIScience/ors-map-client). Or simply [sign up](https://openrouteservice.org/dev/#/signup) for an API key and fire your requests against the API directly.

Please note that openrouteservice uses a forked and edited version of [graphhopper 0.13](https://github.com/GIScience/graphhopper) which can be found [here](https://github.com/GIScience/graphhopper).

[![ors client accessibility](https://user-images.githubusercontent.com/23240110/30385487-9eac96b8-98a7-11e7-9357-afd4df8fccdf.png)](https://openrouteservice.org/reach)

**Note**

- Our geocoding API is a separate service running the stack built around [**Pelias**](https://github.com/pelias/pelias).
- Our locations/API is another service which we have coined **openpoiservice** which can be found [here](https://github.com/GIScience/openpoiservice).


## Changelog/latest changes

[Openrouteservice CHANGELOG](https://github.com/GIScience/openrouteservice/blob/master/CHANGELOG.md)

## Contribute

We appreciate any kind of contribution - bug reports, new feature suggestion or improving our translations are greatly appreciated. Feel free to create an [issue](https://github.com/GIScience/openrouteservice/issues) and label it accordingly. If your issue regards the openrouteservice web-app please use the [corresponding repository](https://github.com/GIScience/ors-map-client/issues).

If you want to contribute your improvements, please follow the steps outlined in [our CONTRIBUTION guidelines](./CONTRIBUTE.md)

The [sourcespy dashboard](https://sourcespy.com/github/giscienceopenrouteservice/) provides a high level overview of the repository including technology summary, module dependencies and other components of the system.

## Installation

We recommend using Docker to install and launch the openrouteservice backend: 

```bash
cd docker && mkdir -p conf elevation_cache graphs logs/ors logs/tomcat && docker-compose up
```

For more details, check the [docker installation guide](https://GIScience.github.io/openrouteservice/installation/Running-with-Docker.html).

For instructions on how to [build from source](https://GIScience.github.io/openrouteservice/installation/Building-from-Source.html) or [configure](https://GIScience.github.io/openrouteservice/installation/Configuration.html), visit our [Installation and Usage Instructions](https://GIScience.github.io/openrouteservice/installation/Installation-and-Usage.html).

## Usage

Openrouteservice offers a set of endpoints for different spatial purposes. By default they will be available at

- `http://localhost:8080/ors/v2/directions`
- `http://localhost:8080/ors/v2/isochrones`
- `http://localhost:8080/ors/v2/matrix`

You can find more information in the [Installation and Usage Instructions](https://GIScience.github.io/openrouteservice/installation/Installation-and-Usage.html).

## API Documentation

For an easy and interactive way to test the api, visit our API documentation at [openrouteservice.org](https://openrouteservice.org/dev/#/api-docs).
After obtaining your key you can try out the different endpoints instantly and start firing requests.


## Questions

For questions please use our [community forum](https://ask.openrouteservice.org).

## Translations

If you notice anything wrong with translations, or you want to add a new language to the ORS instructions, we have some instructions in our [backend documentation](https://GIScience.github.io/openrouteservice/contributing/Contributing-Translations) about how you can submit an update. You can also look over at our [maps client GitHub](https://github.com/GIScience/ors-map-client/#add-language) if you want to contribute the language to there as well (adding or editing the language in the openrouteservice GitHub repo only affects the instructions - any new language also needs adding to the client).
