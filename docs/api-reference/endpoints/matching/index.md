# Matching Endpoint

:::warning NOTE
This endpoint is not available in the public API, but you can use it when running an own instance of openrouteservice.
:::

The matching endpoint can be used to match point, linear and polygonal spatial features to the edges of the routing graph representing the street network for a specific means of transportation.

The matching of point geometries is performed by snapping to the nearest edge in the graph, similar to the [snapping endpoint](../snapping/index.md).
Linear geometries are matched using a Hidden Markov Model (HMM) based map matching algorithm.
Polygon geometries are matched by intersecting them with the edges of the routing graph.

The routing profile has to be specified as path parameter.
The geometric `features` for matching are provided formatted as a GeoJSON `FeatureCollection` object.

The endpoint returns a JSON containing an array `edge_ids` consisting of separate arrays of matched edge ids for each of the features in the order they were provided.
In case no matching edges could be found for a given feature, an empty array is returned for that feature.

:::warning NOTE
The returned edge ids are the internal ids of the routing graph and can be used for further processing, e.g. with the [export endpoint](../export/index.md). They shall not be confused with OpenStreetMap way ids.
:::


## Examples

### Simple point query

A basic request with single point coordinates.

```shell
curl -X 'POST' \
  'http://localhost:8082/ors/v2/match/driving-car' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "features": {
    "type": "FeatureCollection",
    "features": [
      {
        "type": "Feature",
        "geometry": {
          "type": "Point",
          "coordinates": [
            8.685990,
            49.40267
          ]
        }
      }
    ]
  }
}'
```

The response consists of an array containing the matched edge id for the provided point feature and the graph timestamp.

```json
{
  "edge_ids":[
    [
      4683
    ]
  ],
  "graph_timestamp":"2025-11-21T12:35:46Z"
}
```

:::warning NOTE
Internal edge ids are subject to change between graph rebuilds.
:::

### Query with multiple different features 

A request with three valid features: a point, a linestring and a polygon.
```shell
curl -X 'POST' \
  'http://localhost:8082/ors/v2/match/driving-car' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "features": {
    "type": "FeatureCollection",
    "features": [
      {
        "type": "Feature",
        "geometry": {
          "type": "Point",
          "coordinates": [
            8.685990,
            49.40267
          ]
        }
      },
      {
        "type": "Feature",
        "geometry": {
          "type": "LineString",
          "coordinates": [
            [
              8.684963,
              49.40252
            ],
            [
              8.684932,
              49.40199
            ],
            [
              8.685466,
              49.40126
            ],
            [
              8.686026,
              49.40092
            ]
          ]
        }
      },
      {
        "type": "Feature",
        "geometry": {
          "type": "Polygon",
          "coordinates": [
            [
              [
                8.684143,
                49.40336
              ],
              [
                8.684779,
                49.40302
              ],
              [
                8.685354,
                49.40340
              ],
              [
                8.684697,
                49.40380
              ],
              [
                8.684143,
                49.40336
              ]
            ]
          ]
        }
      }
    ]
  }
}'
```

Response:
```json
{
  "edge_ids":[
    [
        4683
    ],
    [
        11185,
        5908,
        741,
        7471
    ],
    [
        7472,
        5913,
        5914,
        11548
    ]
  ],
  "graph_timestamp":"2025-11-18T10:59:03Z"
}
```

### Filter by feature type

Simple point features are matched by snapping to the nearest edge in the routing graph.
By specifying additional properties, one can restrict the snapping to certain types of edges, such as bridges or border crossings.
In case no valid edges of the specified type are found within the search radius, an empty array is returned for that feature.

:::warning HINT
The search radius can be configured in the service configuration file, see [`maximum_search_radius`](/run-instance/configuration/endpoints/matching.md).
:::

```shell
curl -X 'POST' \
  'http://localhost:8082/ors/v2/match/driving-car' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "features": {
    "type": "FeatureCollection",
    "features": [
      {
        "type": "Feature",
        "geometry": {
          "type": "Point",
          "coordinates": [
            8.685990,
            49.40267
          ]
        }
      },
      {
        "type": "Feature",
        "properties": {
          "type": "bridge"
        },
        "geometry": {
          "type": "Point",
          "coordinates": [
            8.685990,
            49.40267
          ]
        }
      },
      {
        "type": "Feature",
        "properties": {
          "type": "border"
        },
        "geometry": {
          "type": "Point",
          "coordinates": [
            8.685990,
            49.40267
          ]
        }
      }
    ]
  }
}'
```

Response:
```json
{
  "edge_ids": [
    [
      4683
    ],
    [
      2242
    ],
    [
    ]
  ],
  "graph_timestamp": "2025-11-21T12:35:46Z"
}
```
