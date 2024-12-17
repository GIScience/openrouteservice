# Export Endpoint

:::warning NOTE
This endpoint is not available in the public API,
but you can use it when running an own instance of openrouteservice.
You can easily create requests with the [swagger-ui](/api-reference/index.md#swagger-ui).
:::

Export the base graph for different modes of transport.

In the request, the desired routing profile is specified as the penultimate path parameter.
The result can be either obtained as a JSON of nodes and edges,
or as a [TopoJSON](https://github.com/topojson/topojson-specification/tree/master) of edges.
A bounding box for the area of interest has to be defined in the request body.

## JSON Response

This is an example request for a base graph for the profile `driving-car`:
```shell
curl -X 'POST' \
  'http://localhost:8082/ors/v2/export/driving-car/json' \
  -H 'accept: application/json' \
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

The json response contains nodes and edges in the bounding box relevant for this routing profile.
The edge entry `weight` contains the fastest car duration in seconds.

The json response for the above request looks like this:
```json
{
    "nodes": [
        {
            "nodeId": 1168,
            "location": [
                8.686742,
                49.427883
            ]
        },
        {
            "nodeId": 1169,
            "location": [
                8.687107,
                49.427982
            ]
        },
        {
            "nodeId": 1378,
            "location": [
                8.685439,
                49.427668
            ]
        },
        {
            "nodeId": 1381,
            "location": [
                8.687425,
                49.428074
            ]
        },
        {
            "nodeId": 2134,
            "location": [
                8.685844,
                49.427873
            ]
        },
        {
            "nodeId": 2135,
            "location": [
                8.686699,
                49.427997
            ]
        },
        {
            "nodeId": 1162,
            "location": [
                8.68758,
                49.428213
            ]
        },
        {
            "nodeId": 1165,
            "location": [
                8.685955,
                49.428631
            ]
        },
        {
            "nodeId": 1166,
            "location": [
                8.685896,
                49.42772
            ]
        },
        {
            "nodeId": 1167,
            "location": [
                8.686352,
                49.427783
            ]
        }
    ],
    "edges": [
        {
            "fromId": 1168,
            "toId": 1169,
            "weight": "6.872"
        },
        {
            "fromId": 1169,
            "toId": 1168,
            "weight": "6.872"
        },
        {
            "fromId": 2134,
            "toId": 2135,
            "weight": "15.234"
        },
        {
            "fromId": 2135,
            "toId": 2134,
            "weight": "15.234"
        },
        {
            "fromId": 1166,
            "toId": 1167,
            "weight": "8.1"
        },
        {
            "fromId": 1167,
            "toId": 1166,
            "weight": "8.1"
        },
        {
            "fromId": 1168,
            "toId": 2135,
            "weight": "3.15"
        },
        {
            "fromId": 2135,
            "toId": 1168,
            "weight": "3.15"
        },
        {
            "fromId": 1378,
            "toId": 1166,
            "weight": "8.07"
        },
        {
            "fromId": 1166,
            "toId": 1378,
            "weight": "8.07"
        },
        {
            "fromId": 1381,
            "toId": 1162,
            "weight": "4.759"
        },
        {
            "fromId": 1162,
            "toId": 1381,
            "weight": "4.759"
        },
        {
            "fromId": 1169,
            "toId": 1381,
            "weight": "6.042"
        },
        {
            "fromId": 1381,
            "toId": 1169,
            "weight": "6.042"
        },
        {
            "fromId": 2134,
            "toId": 1166,
            "weight": "4.191"
        },
        {
            "fromId": 1166,
            "toId": 2134,
            "weight": "4.191"
        },
        {
            "fromId": 2135,
            "toId": 1165,
            "weight": "23.852"
        },
        {
            "fromId": 1165,
            "toId": 2134,
            "weight": "22.137"
        },
        {
            "fromId": 1168,
            "toId": 1167,
            "weight": "7.259"
        },
        {
            "fromId": 1167,
            "toId": 1168,
            "weight": "7.259"
        }
    ],
    "nodes_count": 10,
    "edges_count": 20
}
```

## TopoJSON Response

The [TopoJSON](https://github.com/topojson/topojson-specification/tree/master) response contains edges in the bounding box with the geometry of the underlying road network geometry. When requesting a TopoJSON, you can pass an additional optional parameter `geometry` accepting a boolean value (default is `true`) that controls if the actual geometry of the edges is returned or a beeline representation omitting all in between nodes. The TopoJSON format can be directly loaded and visualized with various tools including [QGIS](https://qgis.org) or [geojson.io](http://geojson.io).

![Development server usage](/public/topojson_qgis.png "Export result in QGIS"){ style="display: block; margin: 0 auto"}

This is an example request for a TopoJSON graph for the profile `driving-car`:

```shell
curl -X 'POST' \
  'http://localhost:8082/ors/v2/export/driving-car/topojson' \
  -H 'accept: application/json' \
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
  "geometry": "true"
}'
```

In the TopoJSON response (example listed below), each arc corresponds to an edge of the routing graph. If the openrouteservice profile that was queried has stored the OSM IDs of the original map data, each object in the `GeometryCollection` represents the set of edges that share the same OSM ID and therefore the same properties: 

- `osm_id` references the OpenStreetMap id of the underlying road geometry.
- `both_directions` is a boolean specifying if the edge can be traversed in both directions.
- `speed` contains the speed the edge can be traversed at travelling in direction of the edge definition.
- `speed_reverse` contains the speed the edge can be reversed at travelling in the opposite direction.
- `ors_ids` is a list of the ors edge ids.
- `ors_nodes` is a list of the touched ors node ids.
- `distances` contains a list of distances for each edge.

To fully utilise this feature, you need to run your own instance of openrouteservice and configure the profile so that the `OsmId` [external storage feature](/run-instance/configuration/engine/profiles/build#ext-storages) is enabled.

```json
{
    "type": "Topology",
    "objects": {
        "network": {
            "type": "GeometryCollection",
            "geometries": [
                {
                    "type": "LineString",
                    "properties": {
                        "osm_id": 24837547,
                        "both_directions": true,
                        "speed": 15.0,
                        "speed_reverse": 15.0,
                        "ors_ids": [
                            1498
                        ],
                        "ors_nodes": [
                            2135,
                            2134
                        ],
                        "distances": [
                            63.476
                        ]
                    },
                    "arcs": [
                        0
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "osm_id": 42806217,
                        "both_directions": true,
                        "speed": 15.0,
                        "speed_reverse": 15.0,
                        "ors_ids": [
                            8017,
                            8018,
                            8019
                        ],
                        "ors_nodes": [
                            1168,
                            1169,
                            1381,
                            1162
                        ],
                        "distances": [
                            28.632,
                            25.173,
                            19.828
                        ]
                    },
                    "arcs": [
                        1,
                        2,
                        3
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "osm_id": 42806216,
                        "both_directions": true,
                        "speed": 15.0,
                        "speed_reverse": 15.0,
                        "ors_ids": [
                            8016
                        ],
                        "ors_nodes": [
                            1166,
                            1167
                        ],
                        "distances": [
                            33.748
                        ]
                    },
                    "arcs": [
                        4
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "osm_id": 52956623,
                        "both_directions": true,
                        "speed": 15.0,
                        "speed_reverse": 15.0,
                        "ors_ids": [
                            8909
                        ],
                        "ors_nodes": [
                            2134,
                            1166
                        ],
                        "distances": [
                            17.461
                        ]
                    },
                    "arcs": [
                        5
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "osm_id": 24000937,
                        "both_directions": true,
                        "speed": 15.0,
                        "speed_reverse": 15.0,
                        "ors_ids": [
                            719
                        ],
                        "ors_nodes": [
                            2135,
                            1168
                        ],
                        "distances": [
                            13.127
                        ]
                    },
                    "arcs": [
                        6
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "osm_id": 42806218,
                        "both_directions": true,
                        "speed": 15.0,
                        "speed_reverse": 15.0,
                        "ors_ids": [
                            8020
                        ],
                        "ors_nodes": [
                            1168,
                            1167
                        ],
                        "distances": [
                            30.247
                        ]
                    },
                    "arcs": [
                        7
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "osm_id": 52956625,
                        "both_directions": false,
                        "speed": 15.0,
                        "ors_ids": [
                            8911
                        ],
                        "ors_nodes": [
                            2135,
                            1165
                        ],
                        "distances": [
                            99.385
                        ]
                    },
                    "arcs": [
                        8
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "osm_id": 305482793,
                        "both_directions": true,
                        "speed": 15.0,
                        "speed_reverse": 15.0,
                        "ors_ids": [
                            18296
                        ],
                        "ors_nodes": [
                            1378,
                            1166
                        ],
                        "distances": [
                            33.626
                        ]
                    },
                    "arcs": [
                        9
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "osm_id": 37831084,
                        "both_directions": false,
                        "speed": 15.0,
                        "ors_ids": [
                            7456
                        ],
                        "ors_nodes": [
                            1165,
                            2134
                        ],
                        "distances": [
                            92.239
                        ]
                    },
                    "arcs": [
                        10
                    ]
                }
            ]
        }
    },
    "arcs": [
        [
            [
                8.686699,
                49.427997
            ],
            [
                8.686342,
                49.427932
            ],
            [
                8.685844,
                49.427873
            ]
        ],
        [
            [
                8.686742,
                49.427883
            ],
            [
                8.687107,
                49.427982
            ]
        ],
        [
            [
                8.687107,
                49.427982
            ],
            [
                8.687425,
                49.428074
            ]
        ],
        [
            [
                8.687425,
                49.428074
            ],
            [
                8.687534,
                49.428133
            ],
            [
                8.68758,
                49.428213
            ]
        ],
        [
            [
                8.685896,
                49.42772
            ],
            [
                8.686352,
                49.427783
            ]
        ],
        [
            [
                8.685844,
                49.427873
            ],
            [
                8.685896,
                49.42772
            ]
        ],
        [
            [
                8.686699,
                49.427997
            ],
            [
                8.686742,
                49.427883
            ]
        ],
        [
            [
                8.686742,
                49.427883
            ],
            [
                8.686352,
                49.427783
            ]
        ],
        [
            [
                8.686699,
                49.427997
            ],
            [
                8.686558,
                49.428345
            ],
            [
                8.686455,
                49.428512
            ],
            [
                8.686352,
                49.428567
            ],
            [
                8.686085,
                49.428622
            ],
            [
                8.685955,
                49.428631
            ]
        ],
        [
            [
                8.685439,
                49.427668
            ],
            [
                8.685646,
                49.427682
            ],
            [
                8.685896,
                49.42772
            ]
        ],
        [
            [
                8.685955,
                49.428631
            ],
            [
                8.685879,
                49.428609
            ],
            [
                8.685782,
                49.42854
            ],
            [
                8.685709,
                49.428405
            ],
            [
                8.685766,
                49.428125
            ],
            [
                8.685844,
                49.427873
            ]
        ]
    ],
    "bbox": [
        8.685439,
        49.427668,
        8.68758,
        49.428631
    ]
}
```

If requesting the TopoJSON on a profile without OsmId storage, the response will contain geometries each consisting of a single arc, and only have a single property `weight` representing travel duration in seconds.

```json
{
    "type": "Topology",
    "objects": {
        "network": {
            "type": "GeometryCollection",
            "geometries": [
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "6.872"
                    },
                    "arcs": [
                        0
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "6.872"
                    },
                    "arcs": [
                        1
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "15.234"
                    },
                    "arcs": [
                        2
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "15.234"
                    },
                    "arcs": [
                        3
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "8.1"
                    },
                    "arcs": [
                        4
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "8.1"
                    },
                    "arcs": [
                        5
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "3.15"
                    },
                    "arcs": [
                        6
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "3.15"
                    },
                    "arcs": [
                        7
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "8.07"
                    },
                    "arcs": [
                        8
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "8.07"
                    },
                    "arcs": [
                        9
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "4.759"
                    },
                    "arcs": [
                        10
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "4.759"
                    },
                    "arcs": [
                        11
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "6.042"
                    },
                    "arcs": [
                        12
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "6.042"
                    },
                    "arcs": [
                        13
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "4.191"
                    },
                    "arcs": [
                        14
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "4.191"
                    },
                    "arcs": [
                        15
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "23.852"
                    },
                    "arcs": [
                        16
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "22.137"
                    },
                    "arcs": [
                        17
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "7.259"
                    },
                    "arcs": [
                        18
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "7.259"
                    },
                    "arcs": [
                        19
                    ]
                }
            ]
        }
    },
    "arcs": [
        [
            [
                8.686742,
                49.427883
            ],
            [
                8.687107,
                49.427982
            ]
        ],
        [
            [
                8.687107,
                49.427982
            ],
            [
                8.686742,
                49.427883
            ]
        ],
        [
            [
                8.685844,
                49.427873
            ],
            [
                8.686342,
                49.427932
            ],
            [
                8.686699,
                49.427997
            ]
        ],
        [
            [
                8.686699,
                49.427997
            ],
            [
                8.686342,
                49.427932
            ],
            [
                8.685844,
                49.427873
            ]
        ],
        [
            [
                8.685896,
                49.42772
            ],
            [
                8.686352,
                49.427783
            ]
        ],
        [
            [
                8.686352,
                49.427783
            ],
            [
                8.685896,
                49.42772
            ]
        ],
        [
            [
                8.686742,
                49.427883
            ],
            [
                8.686699,
                49.427997
            ]
        ],
        [
            [
                8.686699,
                49.427997
            ],
            [
                8.686742,
                49.427883
            ]
        ],
        [
            [
                8.685439,
                49.427668
            ],
            [
                8.685646,
                49.427682
            ],
            [
                8.685896,
                49.42772
            ]
        ],
        [
            [
                8.685896,
                49.42772
            ],
            [
                8.685646,
                49.427682
            ],
            [
                8.685439,
                49.427668
            ]
        ],
        [
            [
                8.687425,
                49.428074
            ],
            [
                8.687534,
                49.428133
            ],
            [
                8.68758,
                49.428213
            ]
        ],
        [
            [
                8.68758,
                49.428213
            ],
            [
                8.687534,
                49.428133
            ],
            [
                8.687425,
                49.428074
            ]
        ],
        [
            [
                8.687107,
                49.427982
            ],
            [
                8.687425,
                49.428074
            ]
        ],
        [
            [
                8.687425,
                49.428074
            ],
            [
                8.687107,
                49.427982
            ]
        ],
        [
            [
                8.685844,
                49.427873
            ],
            [
                8.685896,
                49.42772
            ]
        ],
        [
            [
                8.685896,
                49.42772
            ],
            [
                8.685844,
                49.427873
            ]
        ],
        [
            [
                8.686699,
                49.427997
            ],
            [
                8.686558,
                49.428345
            ],
            [
                8.686455,
                49.428512
            ],
            [
                8.686352,
                49.428567
            ],
            [
                8.686085,
                49.428622
            ],
            [
                8.685955,
                49.428631
            ]
        ],
        [
            [
                8.685955,
                49.428631
            ],
            [
                8.685879,
                49.428609
            ],
            [
                8.685782,
                49.42854
            ],
            [
                8.685709,
                49.428405
            ],
            [
                8.685766,
                49.428125
            ],
            [
                8.685844,
                49.427873
            ]
        ],
        [
            [
                8.686742,
                49.427883
            ],
            [
                8.686352,
                49.427783
            ]
        ],
        [
            [
                8.686352,
                49.427783
            ],
            [
                8.686742,
                49.427883
            ]
        ]
    ],
    "bbox": [
        8.685439,
        49.427668,
        8.68758,
        49.428631
    ]
}
```