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

The **JSON** output format is the most flexible and best suited one for further processing.
Its top level structure consists of three main fields:

| Field          | Description                                                                                                                           |
|----------------|---------------------------------------------------------------------------------------------------------------------------------------|
| **`bbox`**     | Bounding box `[minLon, minLat, maxLon, maxLat]` covering all of the routes in the response.                                           |
| **`routes`**   | List of routes.                                                                                                                       |
| **`metadata`** | Additional details about the request and routing engine, including `attribution`, `service`, `timestamp`, `query`, and `engine` info. |

Each of the `routes` entries can contain the following fields.

| Field            | Description                                                                                                                                                           |
|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`summary`**    | Properties of the route such as total `distance` (m) and `duration` (s).                                                                                              |
| **`segments`**   | List of route sections between waypoints, each with `distance`, `duration`, and `steps` containing turn-by-turn navigation instructions if these have been requested. |
| **`bbox`**       | Bounding box of the route.                                                                                                                                            |

Furthermore, if route geometry has been requested (which is the default), the following fields are present.

| Field            | Description                                                                                                                                                                                    |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`geometry`**   | The path of the route. In JSON it is [encoded](https://developers.google.com/maps/documentation/utilities/polylinealgorithm), while in GeoJSON it is a `LineString` with explicit coordinates. |
| **`way_points`** | Indices of way points corresponding to the `geometry`.                                                                                                                                         |

### GeoJSON

**GeoJSON** is a standardized format for encoding a variety of geographic data structures, see [geojson.org](https://datatracker.ietf.org/doc/html/rfc7946).
It is widely used and can therefore be easily processed or displayed in many applications, e.g. in [QGIS](https://qgis.org/) or on [geojson.io](http://geojson.io/).

The **GeoJSON** output contains the same routing information as its **JSON** counterpart, but organized in slightly different way.

In the **GeoJSON** format, the route data is structured as a standard **FeatureCollection** where individual routes are represented as `features`.
Each **Feature** includes a `geometry` of type `LineString` and `properties` that contain the same fields as the `routes` elements in the JSON format, i.e. `summary`, `segments`, `way_points`, etc.

### GPX

The **GPX** return type is an XML dialect from openrouteservice based on the [GPS Exchange Format](https://www.topografix.com/gpx.asp) with its own [XML Schema](https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd).
It is a very old standard for lightweight interchange of GPS data and thus being used by a wide range of software applications and Web services.

More details on the structure of the different return types can be found in the [API Playground](https://openrouteservice.org/dev/#/api-docs/directions_service).
