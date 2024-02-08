# Snapping Endpoint

:::warning NOTE
This endpoint is not available in the public API,
but you can use it when running an own instance of openrouteservice.
You can easily create requests with the [swagger-ui](/api-reference/index.md#swagger-ui).
:::

The snapping endpoint can be used to snap points to the edges of the street network for a specific means of transportation.

The endpoint returns a list of points snapped to the nearest edge in the graph as JSON or GeoJSON.
In case an appropriate snapping point cannot be found within the specified search radius, "null" is returned.

The routing profile has to be specified as path parameter. 
The list of points to be snapped has to be specified as parameter `locations` in the request body, 
a list or longitude/latitude tuples. 
Another required request body parameter is the `radius` in meters. 

The result contains the snapped points in the same order as their origin position in the request.

In the following example request and result, the first point cannot be snapped within the search radius
and therefore the first entry in the result `locations` is null.

Request:
```shell
curl -X 'POST' \
  'http://localhost:8082/ors/v2/snap/driving-car/json' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "locations": [
    [
      8.681495,
      49.51461
    ],
    [
      8.686507,
      49.41943
    ]
  ],
  "id": "my_request",
  "radius": 300
}'
```

Response:
```json
{
  "locations": [
    null,
    {
      "location": [
        8.686507,
        49.41943
      ],
      "name": "Werderplatz",
      "snapped_distance": 0.01
    }
  ],
  "metadata": {
    "attribution": "openrouteservice.org, OpenStreetMap contributors",
    "service": "snap",
    "timestamp": 1702565781290,
    "query": {
      "locations": [
        [
          8.681495,
          49.51461
        ],
        [
          8.686507,
          49.41943
        ]
      ],
      "profile": "driving-car",
      "id": "my_request",
      "format": "json",
      "radius": 300
    },
    "engine": {
      "version": "8.0",
      "build_date": "2023-12-07T10:16:51Z",
      "graph_date": "2023-12-07T15:04:45Z"
    }
  }
}
```
