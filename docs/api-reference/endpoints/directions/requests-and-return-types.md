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

### GeoJSON

**GeoJSON** is a format for encoding a variety of geographic data structures, see [geojson.org](https://datatracker.ietf.org/doc/html/rfc7946).
It is widely used and can therefore be easily processed or displayed in many applications, e.g. in [QGIS](https://qgis.org/) or on [geojson.io](http://geojson.io/)

More information about the result types can be found in the [API Playground](https://openrouteservice.org/dev/#/api-docs/directions_service).

### Shared Structure (JSON and GeoJSON)

Both the **JSON** and **GeoJSON** outputs contain the same routing information, organized in slightly different formats.

| Field            | Description                                                                                                                                                                                    |
| ---------------- |------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`summary`**    | Total `distance` (m) and `duration` (s) of the route.                                                                                                                                          |
| **`segments`**   | List of route sections between waypoints, each with `distance`, `duration`, and `steps`.                                                                                                       |
| **`steps`**      | Turn-by-turn navigation instructions with `instruction`, `name`, `distance` (m), `duration` (s), and `way_points`.                                                                             |
| **`way_points`** | Indices marking the start and end positions of each segment along the geometry.                                                                                                                |
| **`bbox`**       | Bounding box `[minLon, minLat, maxLon, maxLat]` of the route.                                                                                                                                  |
| **`geometry`**   | The path of the route. In JSON it is [encoded](https://developers.google.com/maps/documentation/utilities/polylinealgorithm), while in GeoJSON it is a `LineString` with explicit coordinates. |
| **`metadata`**   | Contains additional details about the request and routing engine, including `attribution`, `service`, `timestamp`, `query`, and `engine` info.                                                 |

In the **GeoJSON** format, the route data is organized as a standard **FeatureCollection** consisting of an array of route `features`.
Each **Feature** includes a `geometry` as a `LineString` and `properties` that contain the same information as in the JSON format (`summary`, `segments`, `steps`, etc.).

### GPX

The **GPX** return type is an XML dialect from openrouteservice based on the [GPS Exchange Format](https://www.topografix.com/gpx.asp) with its own [XML Schema](https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd).
It is a very old standard for lightweight interchange of GPS data and thus being used by a wide range of software applications and Web services.
