# Endpoints

:::warning Hint
Not all endpoints are available in our live API.
But you can use them when hosting your own instance of openrouteservice.
:::

## Spatial Endpoints

Openrouteservice offers a set of endpoints for different spatial purposes:

* [Directions Service](directions/index.md): Get directions for different modes of transport
* [Isochrones Service](isochrones/index.md): Obtain areas of reachability from given locations
* [Matrix Service](matrix/index.md): Obtain one-to-many, many-to-one and many-to-many matrices for time and distance
* [Snapping Service](snapping/index.md): Snap coordinates to the graph edges _(:warning: not available in our live API)_

## Technical Endpoints

Furthermore, there are technical endpoints that are :warning: _not available_ in our live API:

* [Export Service](export/index.md): Export the base graph for different modes of transport
* [Health Service](health/index.md): Get information on the health of the running openrouteservice instance
* [Status Service](status/index.md): Get information on the status of the openrouteservice instance

## Included Services 

There are additional other services we are hosting for convenience, which are :warning: _only_ included in our public API.
They are standalone services that are :warning: _not included_ in a local openrouteservice instance and would need
a separate local installation:

* [POI Service](poi/index.md): Stand-Alone service from HeiGIT that returns points of interest in the area surrounding a geometry
* [Elevation Service](elevation/index.md): Stand-Alone service from HeiGIT that returns the elevation for point or line geometries 
* [Geocoding Service](geocoder/index.md): Third Party Service ([Pelias](https://www.pelias.io)) hosted by HeiGIT that resolves geographic coordinates to addresses and vice versa
* [Optimization Service](optimization/index.md): Third Party Service ([VROOM](https://github.com/VROOM-Project/vroom)) hosted by HeiGIT that optimizes routes for vehicle fleets
