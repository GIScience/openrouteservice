# Isochrones Endpoint

The Isochrone Service supports time and distance analyses for one single or multiple locations.
It is possible to specify the isochrone interval or provide multiple exact isochrone range values.
This service allows the same range of profile options as the `/directions` endpoint,
which help you to further customize your request to obtain a more detailed reachability area response.

The result is a GeoJSON where the isochrone polygons are represented as `features`:

```jsonpath
$.features
```

For an overview of all features of the isochrones endpoint please refer to the [API Playground](https://openrouteservice.org/dev/#/api-docs/isochrones_service).
