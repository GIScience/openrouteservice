# Status Endpoint

:::warning NOTE
This endpoint is not available in the public API, but you can use it when running an own instance of openrouteservice.
:::

The GET request http://localhost:8082/ors/v2/status (host and port are dependent on the setup) returns basic information about the running instance:

* `languages`: available languages
* `engine`: the build date and version of the openrouteservice.
  `graph_version` is the version of the graph structure and build logic determining the graph repository address
  if [graph management](/run-instance/configuration/engine/graph-management.md) is enabled
* `profiles`: available routing profiles,
  info about the graph's build (start) date (`graph_build_date`),
  timestamp of the osm pbf file that was used for building the graph (`osm_date`),
  storages and configured limits
* `services`: activated services

[//]: # (TODO: engine git die ORS version aus, nicht die eigentliche engine version, die wir dann auch im graph management verwenden, oder?)

 
:::details This is an example response: 
```json
{
    "languages": [
        "cs",
        "cs-cz",
        "da",
        "da-dk",
        "de",
        "de-de",
        "en",
        "en-us",
        "eo",
        "eo-eo",
        "es",
        "es-es",
        "fi",
        "fi-fi",
        "fr",
        "fr-fr",
        "gr",
        "gr-gr",
        "he",
        "he-il",
        "hu",
        "hu-hu",
        "id",
        "id-id",
        "it",
        "it-it",
        "ja",
        "ja-jp",
        "nb",
        "nb-no",
        "ne",
        "ne-np",
        "nl",
        "nl-nl",
        "pl",
        "pl-pl",
        "pt",
        "pt-pt",
        "ro",
        "ro-ro",
        "ru",
        "ru-ru",
        "tr",
        "tr-tr",
        "ua",
        "ua-ua",
        "vi",
        "vi-vn",
        "zh",
        "zh-cn"
    ],
    "engine": {
        "build_date": "2024-12-11T12:44:36Z",
        "graph_version": "1",
        "version": "9.1.0"
    },
    "profiles": {
        "car": {
            "storages": {
                "WayCategory": {
                    "enabled": true
                },
                "HeavyVehicle": {
                    "restrictions": true,
                    "enabled": true
                },
                "WaySurfaceType": {
                    "enabled": true
                },
                "RoadAccessRestrictions": {
                    "useForWarnings": true,
                    "enabled": true
                }
            },
            "encoder_name": "driving-car",
            "graph_build_date": "2024-10-28T14:42:49Z",
            "osm_date": "2023-10-11T20:21:48Z",
            "limits": {
                "maximum_distance": 100000,
                "maximum_waypoints": 50,
                "maximum_distance_dynamic_weights": 100000,
                "maximum_distance_avoid_areas": 100000
            }
        },
        "pedestrian": {
            "encoder_name": "foot-walking",
            "graph_build_date": "2024-10-11T11:08:44Z",
            "osm_date": "2024-01-22T21:21:14Z",
            "limits": {
                "maximum_distance": 100000,
                "maximum_waypoints": 50,
                "maximum_distance_dynamic_weights": 100000,
                "maximum_distance_avoid_areas": 100000
            }
        }
    },
    "services": [
        "routing",
        "isochrones",
        "matrix",
        "snap"
    ]
}
```
:::

