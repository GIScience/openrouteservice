# Export Endpoint

:::warning NOTE
This endpoint is not available in the public API,
but you can use it when running an own instance of openrouteservice.
You can easily create requests with the [swagger-ui](/api-reference/index.md#swagger-ui).
:::

Export the base graph for different modes of transport.

In the request, the desired routing profile is specified as the last path parameter, 
a bounding box for the area of interest has to be defined in the request body.

This is an example requests for a base graph for the profile `driving-car`:
```shell
curl -X 'POST' \
  'http://localhost:8082/ors/v2/export/driving-car' \
  -H 'accept: application/geo+json' \
  -H 'Content-Type: application/json' \
  -d '{
  "bbox": [
    [
      8.681495,
      49.41461
    ],
    [
      8.686507,
      49.41943
    ]
  ],
  "id": "export_request"
}'
```

The response contains nodes and edges in the bounding box relevant for this routing profile.
The edge entry `weight` contains the fastest car duration in seconds:

```json
{
  "nodes": [
    {
      "nodeId": 11008,
      "location": [
        8.682782,
        49.417388
      ]
    },
    {
      "nodeId": 11009,
      "location": [
        8.682461,
        49.417389
      ]
    },
    {
      "nodeId": 11010,
      "location": [
        8.681794,
        49.417637
      ]
    },
    {
      "nodeId": 1987,
      "location": [
        8.681674,
        49.416601
      ]
    },
    {
      "nodeId": 1988,
      "location": [
        8.681532,
        49.418291
      ]
    },
    {
      "nodeId": 1669,
      "location": [
        8.685746,
        49.415712
      ]
    },
    {
      "nodeId": 1221,
      "location": [
        8.685382,
        49.417368
      ]
    },
    {
      "nodeId": 15494,
      "location": [
        8.683159,
        49.419081
      ]
    },
    {
      "nodeId": 1672,
      "location": [
        8.686424,
        49.417375
      ]
    },
    {
      "nodeId": 3788,
      "location": [
        8.683666,
        49.414963
      ]
    },
    {
      "nodeId": 3789,
      "location": [
        8.685888,
        49.415001
      ]
    },
    {
      "nodeId": 3790,
      "location": [
        8.684803,
        49.414908
      ]
    },
    {
      "nodeId": 16273,
      "location": [
        8.681976,
        49.418537
      ]
    },
    {
      "nodeId": 16275,
      "location": [
        8.682777,
        49.417663
      ]
    },
    {
      "nodeId": 16276,
      "location": [
        8.682465,
        49.417623
      ]
    },
    {
      "nodeId": 16277,
      "location": [
        8.682592,
        49.417719
      ]
    },
    {
      "nodeId": 2072,
      "location": [
        8.683596,
        49.417386
      ]
    },
    {
      "nodeId": 216,
      "location": [
        8.686507,
        49.41943
      ]
    },
    {
      "nodeId": 219,
      "location": [
        8.681882,
        49.417391
      ]
    },
    {
      "nodeId": 12891,
      "location": [
        8.683295,
        49.418568
      ]
    },
    {
      "nodeId": 3360,
      "location": [
        8.68504,
        49.419273
      ]
    },
    {
      "nodeId": 3506,
      "location": [
        8.682577,
        49.415744
      ]
    },
    {
      "nodeId": 3507,
      "location": [
        8.683801,
        49.415725
      ]
    },
    {
      "nodeId": 3508,
      "location": [
        8.683767,
        49.416544
      ]
    },
    {
      "nodeId": 3510,
      "location": [
        8.68269,
        49.417389
      ]
    },
    {
      "nodeId": 3511,
      "location": [
        8.682661,
        49.416511
      ]
    }
  ],
  "edges": [
    {
      "fromId": 1669,
      "toId": 1221,
      "weight": 44.655840000000005
    },
    {
      "fromId": 3506,
      "toId": 3507,
      "weight": 21.26352
    },
    {
      "fromId": 3507,
      "toId": 3506,
      "weight": 21.26352
    },
    {
      "fromId": 3510,
      "toId": 3511,
      "weight": 23.4336
    },
    {
      "fromId": 3788,
      "toId": 3790,
      "weight": 19.99776
    },
    {
      "fromId": 3790,
      "toId": 3789,
      "weight": 19.008000000000003
    },
    {
      "fromId": 3508,
      "toId": 3511,
      "weight": 19.22184
    },
    {
      "fromId": 3511,
      "toId": 3506,
      "weight": 20.515680000000003
    },
    {
      "fromId": 11010,
      "toId": 1988,
      "weight": 18.03816
    },
    {
      "fromId": 1988,
      "toId": 11010,
      "weight": 18.03816
    },
    {
      "fromId": 3507,
      "toId": 3508,
      "weight": 21.87696
    },
    {
      "fromId": 3508,
      "toId": 3507,
      "weight": 21.87696
    },
    {
      "fromId": 3789,
      "toId": 1669,
      "weight": 19.117440000000002
    },
    {
      "fromId": 1672,
      "toId": 1221,
      "weight": 18.080640000000002
    },
    {
      "fromId": 1221,
      "toId": 1672,
      "weight": 18.080640000000002
    },
    {
      "fromId": 11008,
      "toId": 16275,
      "weight": 7.34184
    },
    {
      "fromId": 16275,
      "toId": 11008,
      "weight": 7.34184
    },
    {
      "fromId": 11009,
      "toId": 16276,
      "weight": 9.339839999999999
    },
    {
      "fromId": 16276,
      "toId": 11009,
      "weight": 9.339839999999999
    },
    {
      "fromId": 11010,
      "toId": 16277,
      "weight": 14.45232
    },
    {
      "fromId": 16277,
      "toId": 11010,
      "weight": 14.45232
    },
    {
      "fromId": 1987,
      "toId": 219,
      "weight": 21.433200000000003
    },
    {
      "fromId": 2072,
      "toId": 11008,
      "weight": 14.133600000000001
    },
    {
      "fromId": 219,
      "toId": 1987,
      "weight": 21.433200000000003
    },
    {
      "fromId": 11008,
      "toId": 2072,
      "weight": 14.133600000000001
    },
    {
      "fromId": 219,
      "toId": 11010,
      "weight": 6.74016
    },
    {
      "fromId": 11010,
      "toId": 219,
      "weight": 6.74016
    },
    {
      "fromId": 219,
      "toId": 11009,
      "weight": 10.039919999999999
    },
    {
      "fromId": 11009,
      "toId": 219,
      "weight": 10.039919999999999
    },
    {
      "fromId": 1221,
      "toId": 2072,
      "weight": 31.017120000000002
    },
    {
      "fromId": 2072,
      "toId": 1221,
      "weight": 31.017120000000002
    },
    {
      "fromId": 12891,
      "toId": 1988,
      "weight": 18.889488
    },
    {
      "fromId": 1988,
      "toId": 12891,
      "weight": 18.889488
    },
    {
      "fromId": 1221,
      "toId": 3360,
      "weight": 51.17568
    },
    {
      "fromId": 15494,
      "toId": 3360,
      "weight": 33.06288
    },
    {
      "fromId": 3508,
      "toId": 2072,
      "weight": 22.6584
    },
    {
      "fromId": 2072,
      "toId": 3508,
      "weight": 22.6584
    },
    {
      "fromId": 3511,
      "toId": 1987,
      "weight": 17.377920000000003
    },
    {
      "fromId": 3507,
      "toId": 1669,
      "weight": 33.76656
    },
    {
      "fromId": 3510,
      "toId": 11008,
      "weight": 1.59384
    },
    {
      "fromId": 11008,
      "toId": 3510,
      "weight": 1.59384
    },
    {
      "fromId": 3510,
      "toId": 11009,
      "weight": 3.9808799999999995
    },
    {
      "fromId": 11009,
      "toId": 3510,
      "weight": 3.9808799999999995
    },
    {
      "fromId": 3360,
      "toId": 216,
      "weight": 25.802400000000002
    },
    {
      "fromId": 3788,
      "toId": 3507,
      "weight": 20.477040000000002
    },
    {
      "fromId": 3507,
      "toId": 3788,
      "weight": 20.477040000000002
    }
  ],
  "nodes_count": 26,
  "edges_count": 46
}
```