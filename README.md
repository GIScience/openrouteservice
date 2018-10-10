# Openrouteservice

- [master](https://github.com/GIScience/openrouteservice) [![Build Status](https://travis-ci.org/GIScience/openrouteservice.svg?branch=master)](https://travis-ci.org/GIScience/openrouteservice)
- [development](https://github.com/GIScience/openrouteservice/tree/development) [![Build Status](https://travis-ci.org/GIScience/openrouteservice.svg?branch=development)](https://travis-ci.org/GIScience/openrouteservice)

The **openrouteservice API** provides global spatial services by consuming user-generated and collaboratively collected free geographic data directly from [OpenStreetMap](http://www.openstreetmap.org). It is highly customizable, performant and written in Java.

The following services are available via a RESTful interface served by Tomcat.
- **Directions** - Returns a route between two or more locations for a selected profile with customizable additional settings and instructions.
- **Isochrones** - Obtains areas of reachability from given locations.
- **Matrix** - Computes one-to-many, many-to-one or many-to-many routes for any mode of transport provided by openrouteservice.

To play around with openrouteservice you may use our [demonstration server](https://maps.openrouteservice.org) which comes with both the backend and a [frontend](https://github.com/GIScience/openrouteservice-app). Or simply sign up for an [API key](https://openrouteservice.org) and fire your requests against the API directly.

Please note that openrouteservice uses a forked and edited version of [graphhopper 0.9](https://github.com/GIScience/graphhopper) which can be found [here](https://github.com/GIScience/graphhopper).

[![ors client accessibility](https://user-images.githubusercontent.com/23240110/30385487-9eac96b8-98a7-11e7-9357-afd4df8fccdf.png)](https://openrouteservice.org/reach)

**Note**
- Our geocoding API is a separate service running the stack built around [**Pelias**](https://github.com/pelias/pelias).
- Our locations/API is another service which we have coined **openpoiservice** which can be found [here](https://github.com/GIScience/openpoiservice).


## Changelog/latest changes

[Openrouteservice CHANGELOG](https://github.com/GIScience/openrouteservice/blob/master/CHANGELOG.md)

## Contribute

We appreciate any kind of contribution - bug reports, new feature suggestion or improving our translations are greatly appreciated. Feel free to create an [issue](https://github.com/GIScience/openrouteservice/issues) and label it accordingly. If your issue regards the openrouteservice web-app please use the [corresponding repository](https://github.com/GIScience/openrouteservice-app/issues).

If you want to do contribute your improvements, please follow these steps:

  1. [Fork the openrouteservice project](https://help.github.com/articles/fork-a-repo)
  
  2. Create a branch for the improvement from the development branch on your fork and add your contributions there.
  
  3. Create a [pull request](https://help.github.com/articles/using-pull-requests) to our development branch, so we can review your changes before applying them. Please write your pull request description similar to [this](http://api.coala.io/en/latest/Developers/Writing_Good_Commits.html) standard. Also please make sure to reference your pull request to the corresponding issue, for changes regarding multiple issues please create different pullrequests using different branches in your fork.


## Installation

We suggest using docker to install and launch openrouteservice backend. In short, run the following command under the source code tree will get everything done (for this please
clone the repository, running docker via the archive is currently not supported).

```bash
cd docker && docker-compose up
```

For more details, check the [docker installation guide](docker/README.md).

## Usage

Openrouteservice offers a set of endpoints for different spatial purposes. They are served with the help of [Tomcat in a java servlet container](https://github.com/GIScience/openrouteservice/blob/master/openrouteservice/WebContent/WEB-INF/web.xml). By default you will be able to query the services with these addresses:

- `http://localhost:8080/name_of_war_archive/routes`
- `http://localhost:8080/name_of_war_archive/isochrones`
- `http://localhost:8080/name_of_war_archive/matrix`

## API Documentation

For an easy and interactive way to test the api, visit our documentation at [openrouteservice.org](https://openrouteservice.org).
After obtaining your key you can try out the different endpoints instantly and start firing requests.


## Questions

For questions please use our [Google Groups Forum](https://groups.google.com/forum/#!forum/openrouteservice) and we will respond to you shortly or add a GitHub issue if it is of technical nature.

## Translations

If you notice any thing wrong with translations, or you want to add a new language to the ORS instructions, please add/edit files in the src/main/resources/locales folder. You can use the ors_en.resources file as a template as to what information needs to be present.
As a guide, for each instruction there are two formats - one where there is a named place (i.e. 47 Berlinerstraße) and one without. It is important to keep the {way_name} tag in the text, but it should be moved to the correct location.
To show ORS what alnguage is being used, you should alter the name of the file to include the ISO identifier (i.e. for Austrian German the filename would be ors_de_AT.resources).
