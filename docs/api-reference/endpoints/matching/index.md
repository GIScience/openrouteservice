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

The endpoint returns a JSON containing a list of matched edge ids.

:::warning HINT
The returned edge ids are the internal ids of the routing graph and can be used for further processing, e.g. with the [export endpoint](../export/index.md). They shall not be confused with OpenStreetMap way ids.
:::


## Sample query and result

Request:
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
    7472,
    11185,
    2242,
    5908,
    741,
    5913,
    5914,
    11548,
    7471
  ],
  "graph_timestamp":"2025-11-17T14:18:55Z"
}
```

:::warning NOTE
Internal edge ids are subject to change between graph rebuilds.
:::
