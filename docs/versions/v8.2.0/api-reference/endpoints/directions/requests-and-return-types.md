# Requests and Return Types

## Direction Requests

There are four POST directions requests that all accept the same request body and differ only in the result types, specified by the last path parameter:

    POST /v2/directions/{profile}            <- returns JSON
    POST /v2/directions/{profile}/json
    POST /v2/directions/{profile}/geojson
    POST /v2/directions/{profile}/gpx

Additionally, there is one simple GET request that does not allow advanced request options. It returns GeoJSON:

    GET  /v2/directions/{profile}            <- returns GeoJSON


## Return Types

### JSON 

The **JSON** return type is best suited for further processing.

### GPX

The **GPX** return type is an XML dialect from openrouteservice based on the [GPS Exchange Format](https://www.topografix.com/gpx.asp) with an own [XML Schema](https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd).
It is a very old standard for lightweight interchange of GPS data and thus being used by hundreds of software programs and Web services.

### GeoJSON

**GeoJSON** is a format for encoding a variety of geographic data structures, see [geojson.org](https://datatracker.ietf.org/doc/html/rfc7946).
It is widely used and can therefore be easily processed or displayed in many applications, e.g. in [QGIS](https://qgis.org/) or on [geojson.io](http://geojson.io/)

More information about the result types can be found in the [API Playground](https://openrouteservice.org/dev/#/api-docs/directions_service).
