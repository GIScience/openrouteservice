# Openrouteservice

The **openrouteservice API** provides global spatial services by consuming user-generated and collaboratively collected free geographic data directly from [OpenStreetMap](http://www.openstreetmap.org). It is highly customizable, performant and written in Java.

The following services are available via a RESTful interface served by Tomcat.
- **Directions** - Returns a route between two or more locations for a selected profile with customizable additional settings and instructions.
- **Isochrones** - Obtains areas of reachability from given locations.
- **Matrix** - Computes one-to-many, many-to-one or many-to-many routes for any mode of transport provided by openrouteservice.
- **Geocoding** - Resolves input coordinates to addresses and vice versa (**NOTE:** openrouteservice acts as a wrapper and connects to either [Photon](https://github.com/komoot/photon), [Nominatim](https://github.com/openstreetmap/Nominatim) or [Pelias](https://github.com/pelias/pelias). One of these services must be installed in addition to the openrouteservice).
- **Places** - Search for points of interest around points or in geometries (**NOTE:** you will have to set up a locations database, for this please refer to [openrouteservice-tools](https://github.com/GIScience/openrouteservice-tools)).

To play around with openrouteservice you may use our [demonstration server](https://www.openrouteservice.org) which comes with both the backend and a [frontend](https://github.com/GIScience/openrouteservice-app). Or simply sign up for an API key and fire your requests against the API directly via [apiary](https://openrouteservice.docs.apiary.io/).

Please note that openrouteservice uses a forked and edited version of [graphhopper 0.9](https://github.com/GIScience/graphhopper) which can be found [here](https://github.com/GIScience/graphhopper).

[![ors client accessibility](https://user-images.githubusercontent.com/23240110/30385487-9eac96b8-98a7-11e7-9357-afd4df8fccdf.png)](https://openrouteservice.org/reach)

<!--  TODO 
## Changelog/latest changes http://blog.clojurewerkz.org/blog/2013/09/07/how-to-write-a-useful-change-log/
 -->

## Contribute

<!-- TODO write contribution guidelines Licence agreement and other important stuff like code formatting in extra file -->

We appreciate any kind of contribution - bug reports, new feature suggestion or improving our translations are greatly appreciated. Feel free to create an [issue](https://github.com/GIScience/openrouteservice/issues) and label it accordingly. If your issue regards the openrouteservice web-app please use the [corresponding repository](https://github.com/GIScience/openrouteservice-app/issues).

If you want to do contribute your improvements, please follow these steps:

  1. [Fork the openrouteservice project](https://help.github.com/articles/fork-a-repo)
  
  2. Create a branch for the improvement from the development branch on your fork and add your contributions there.
  
  3. Create a [pull request](https://help.github.com/articles/using-pull-requests) to our development branch, so we can review your changes before applying them. Please write your pull request description similar to [this](http://api.coala.io/en/latest/Developers/Writing_Good_Commits.html) standard. Also please make sure to reference your pull request to the corresponding issue, for changes regarding multiple issues please create different pullrequests using different branches in your fork.


## Installation

We suggest using docker to install and launch openrouteservice backend. In short, run the following command under the source code tree will get everything done.

```bash
cd docker && docker-compose up
```

For more details, check the [docker installation guide](docker/README.md).

## Usage

Openrouteservice offers a set of endpoints for different spatial purposes. They are served with the help of [Tomcat in a java servlet container](https://github.com/GIScience/openrouteservice/blob/master/openrouteservice/WebContent/WEB-INF/web.xml). By default you will be able to query the services with these addresses:

- `http://localhost:8080/name_of_war_archive/routes`
- `http://localhost:8080/name_of_war_archive/isochrones`
- `http://localhost:8080/name_of_war_archive/matrix`

Both `/locations` and `/geocoding` need additional setup steps for usage.
- `http://localhost:8080/name_of_war_archive/locations` | You will have to set up a locations database, for this please refer to [openrouteservice-tools](https://github.com/GIScience/openrouteservice-tools).
- `http://localhost:8080/name_of_war_archive/geocoding` | You can either use [Photon](https://github.com/komoot/photon), [Nominatim](https://github.com/openstreetmap/Nominatim) or [Pelias](https://github.com/pelias/pelias). One of these services must be installed in addition to the openrouteservice and configured in `app.config`.

Please find a detailed description of the api architecture on https://openrouteservice.docs.apiary.io/.
	
## API Documentation

For an easy and interactive way to test the api, visit our [documentation](https://openrouteservice.docs.apiary.io/) at apiary. After obtaining your key you can try out the different endpoints instantly and start firing requests.

## Questions

For questions please use our [Google Groups Forum](https://groups.google.com/forum/#!forum/openrouteservice) and we will respond to you shortly. 


<!-- 
# Technical Summary

...

 --> 
