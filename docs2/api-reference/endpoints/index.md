# Endpoints



## Spatial Endpoints

Openrouteservice offers a set of endpoints for different spatial purposes:

* [Directions Service](directions/index.md): Get directions for different modes of transport
* [Isochrones Service](isochrones/index.md): Obtain areas of reachability from given locations
* [Matrix Service](matrix/index.md): Obtain one-to-many, many-to-one and many-to-many matrices for time and distance
* [Export Service](export/index.md): Export the base graph for different modes of transport
* [Snapping Service](snapping/index.md): Snap coordinates to the graph edges

## Technical Endpoints

Furthermore, there are technical endpoints:

* [Health Service](health/index.md): Get information on the health of the running openrouteservice instance
* [Status Service](status/index.md): Get information on the status of the openrouteservice instance

## Included Services 

In addition, there are other services accessible via our public API that are not served by the openrouteservice itself:

* [POI Service](https://openrouteservice.org/dev/#/api-docs/pois): Stand-Alone service from HeiGIT that returns points of interest in the area surrounding a geometry
* [Elevation Service](https://openrouteservice.org/dev/#/api-docs/elevation): Stand-Alone service from HeiGIT that returns the elevation for point or line geometries 
* [Geocoder](https://openrouteservice.org/dev/#/api-docs/geocode): A Third Party Service ([Pelias](https://www.pelias.io)) that resolves geo coordinates to addresses and vice versa 
