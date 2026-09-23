# Export Endpoint

Export the base graph for different modes of transport.

In the request, the desired routing profile is specified as the penultimate path parameter, and the desired format as
the last path parameter. A bounding box for the area of interest has to be defined in the request body.

The result can be either obtained as a JSON of nodes and edges, or as
a [TopoJSON](https://github.com/topojson/topojson-specification/tree/master) of edges also capable of representing the
actual geometries of the edges derived from OSM data.

To make the differences between the two formats more clear, the example responses listed below are based on the same
network extract.

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
  ]
}'
```

The JSON response contains nodes and edges in the bounding box relevant for this routing profile.
The edge entry `weight` contains the fastest car duration in seconds.

The JSON response for the above request looks like this:

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

Note how edges traversable in both directions are described as two edges with reversed `fromId` and `toId`. The example
network consists of 10 nodes, 18 edges that represent 9 connections between the nodes in both directions, and two edges
representing connections that are only traversable in one direction (one-way street).

## TopoJSON Response

The [TopoJSON](https://github.com/topojson/topojson-specification/tree/master) response contains edges in the bounding
box with the geometry of the underlying road network geometry. When requesting a TopoJSON, you can pass an additional
optional parameter `geometry` accepting a boolean value (default is `true`) that controls if the actual geometry of the
edges is returned or a beeline representation omitting all in between nodes. The TopoJSON format can be directly loaded
and visualized with various tools including [QGIS](https://qgis.org) or [geojson.io](http://geojson.io).

![Development server usage](/topojson_qgis.png "Export result in QGIS"){ style="display: block; margin: 0 auto"}

To fully utilise this feature, your instance of openrouteservice needs to be configured so that the
`OsmId` [external storage feature](/run-instance/configuration/engine/profiles/build#ext-storages) is enabled for the
profile.

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
  "geometry": "true"
}'
```  

### with OSM IDs

In the TopoJSON response, each arc corresponds to an edge of the routing graph. If the openrouteservice profile that was
queried has stored the OSM IDs of the original map data (i.e. has the `OsmId` storage enabled in configuration), each
object in the `GeometryCollection` represents the set of edges that share the same ID and therefore the same properties:

- `osm_id` references the OpenStreetMap id of the underlying road geometry.
- `both_directions` is a boolean specifying if the edge can be traversed in both directions.
- `speed` contains the speed the edge can be traversed at travelling in direction of the edge definition.
- `speed_reverse` contains the speed the edge can be reversed at travelling in the opposite direction (only present if
  `both_directions` is true).
- `ors_ids` is a list of the ors edge ids.
- `ors_nodes` is a list of the touched ors node ids.
- `distances` contains a list of distances for each edge.

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

This dataset is the same network extract as in the JSON example. Note that two `LineString` elements are marked as
`"both_directions": false` and represent the one-way streets with one arc each, the remaining 9 arcs correspond to the 9
pairs of edges in the JSON response.

This format reduces information redundancy for edges and their respective geometry. The array `ors_ids` contains the
edge ids within openrouteservice. Note that in openrouteservice edges are always bidirectional, so the `ors_ids` array
contains the ids of the edges in both directions.

### without OSM IDs

If requesting the TopoJSON on a profile without `OsmId` storage, the response will contain geometries each consisting of
a
single arc, and have different set of properties:

- `weight` represents travel duration in seconds.
- `node_from` is the ors node id of the edge start.
- `node_to` is the ors node id of the edge end.

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
                        "weight": "6.872",
                        "node_from": 1168,
                        "node_to": 1169
                    },
                    "arcs": [
                        0
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "6.872",
                        "node_from": 1169,
                        "node_to": 1168
                    },
                    "arcs": [
                        -1
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "15.234",
                        "node_from": 2134,
                        "node_to": 2135
                    },
                    "arcs": [
                        1
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "15.234",
                        "node_from": 2135,
                        "node_to": 2134
                    },
                    "arcs": [
                        -2
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "8.1",
                        "node_from": 1166,
                        "node_to": 1167
                    },
                    "arcs": [
                        2
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "8.1",
                        "node_from": 1167,
                        "node_to": 1166
                    },
                    "arcs": [
                        -3
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "3.15",
                        "node_from": 1168,
                        "node_to": 2135
                    },
                    "arcs": [
                        3
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "3.15",
                        "node_from": 2135,
                        "node_to": 1168
                    },
                    "arcs": [
                        -4
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "8.07",
                        "node_from": 1378,
                        "node_to": 1166
                    },
                    "arcs": [
                        4
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "8.07",
                        "node_from": 1166,
                        "node_to": 1378
                    },
                    "arcs": [
                        -5
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "4.759",
                        "node_from": 1381,
                        "node_to": 1162
                    },
                    "arcs": [
                        5
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "4.759",
                        "node_from": 1162,
                        "node_to": 1381
                    },
                    "arcs": [
                        -6
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "6.042",
                        "node_from": 1169,
                        "node_to": 1381
                    },
                    "arcs": [
                        6
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "6.042",
                        "node_from": 1381,
                        "node_to": 1169
                    },
                    "arcs": [
                        -7
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "4.191",
                        "node_from": 2134,
                        "node_to": 1166
                    },
                    "arcs": [
                        7
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "4.191",
                        "node_from": 1166,
                        "node_to": 2134
                    },
                    "arcs": [
                        -8
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "23.852",
                        "node_from": 2135,
                        "node_to": 1165
                    },
                    "arcs": [
                        8
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "22.137",
                        "node_from": 1165,
                        "node_to": 2134
                    },
                    "arcs": [
                        9
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "7.259",
                        "node_from": 1168,
                        "node_to": 1167
                    },
                    "arcs": [
                        10
                    ]
                },
                {
                    "type": "LineString",
                    "properties": {
                        "weight": "7.259",
                        "node_from": 1167,
                        "node_to": 1168
                    },
                    "arcs": [
                        -11
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

Note how the number of arcs is equal to the number of arcs in the response with OSM IDs present, so that the geometries
can be represented without redundancies, and the geometries simply contain pairs of `LineString` elements for each edge
that is traversable in both directions, similar to the JSON response.