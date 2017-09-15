# OpenRouteService

The **openrouteservice API** provides global spatial services by consuming user-generated and collaboratively collected free geographic data directly from [OpenStreetMap](http://www.openstreetmap.org). It is highly customizable, performant and written in Java.

The following services are available via a RESTful Tomcat interface.
- **Directions** - Returns a route between two or a set of locations for a selected profile with customizable additional settings and instructions.
- **Isochrones** - Obtains areas of reachability from given locations.
- **Matrix** - Computes one-to-many, many-to-one or many-to-many routes for any mode of transport provided by openrouteservice.
- **Geocoding** - Resolves input coordinates to addresses and vice versa (**NOTE:** openrouteservice acts as a wrapper and connects to either [Photon](https://github.com/komoot/photon), [Nominatim](https://github.com/openstreetmap/Nominatim) or [Pelias](https://github.com/pelias/pelias). One of these services must be installed additionally).
- **Places** - Search for points of interest around points or in geometries (**NOTE:** you will have to set up a locations database, for this please refer to [openrouteservice-tools](https://github.com/GIScience/openrouteservice-tools).

To play around with OpenRouteService you may use our [demonstration server](https://www.openrouteservice.org) which comes with both the backend and a [frontend](https://github.com/GIScience/openrouteservice-app). Or simply sign up for an API key and fire your requests against the API directly via [swagger hub](https://app.swaggerhub.com/apis/OpenRouteService/ors-api/).

Please note that OpenRouteService uses a forked and edited version of [graphhopper 0.9](https://github.com/GIScience/graphhopper) which can be found [here](https://github.com/GIScience/graphhopper).

[![ors client accessibility](https://user-images.githubusercontent.com/23240110/30385487-9eac96b8-98a7-11e7-9357-afd4df8fccdf.png)](https://openrouteservice.org/reach)

<!--  TODO 
## Changelog/latest changes http://blog.clojurewerkz.org/blog/2013/09/07/how-to-write-a-useful-change-log/
 -->

## Contribute

<!-- TODO write contribution guidelines Licence agreement and other important stuff like code formatting in extra file -->

We appreciate any kind of contribution - bug reports, new feature suggestion or improving our translations are greatly appreciated. Feel free to create an [issue](https://github.com/GIScience/openrouteservice/issues) and label it accordingly. If your issue regards the OpenRouteService web-app please use the [corresponding repository](https://github.com/GIScience/openrouteservice-app/issues).

If you want to do contribute your improvements, please follow these steps:

  1. [Fork the openrouteservice project](https://help.github.com/articles/fork-a-repo)

  2. Add your contribution in the development branch of your fork.
  
  3. Create a [pull request](https://help.github.com/articles/using-pull-requests) to our development branch, so we can review your changes before applying them. Please write your pull request description similar to [this](http://api.coala.io/en/latest/Developers/Writing_Good_Commits.html) standard. Also please make sure to reference your pull request to the corresponding issue, for changes regarding multiple issues please create different pullrequests.


## Installation

Instructions on how to install openrouteservice will follow soon.


## API Documentation

For an easy and interactive way to test the api, visit our [documentation](https://app.swaggerhub.com/apis/OpenRouteService/ors-api/) at swaggerhub. After obtaining your key you can try out the different endpoints instantly and start firing requests.

## Questions

For questions please use our [Google Groups Forum](https://groups.google.com/forum/#!forum/openrouteservice) and we will respond to you shortly. 


<!-- 
# Technical Summary

...

 --> 
