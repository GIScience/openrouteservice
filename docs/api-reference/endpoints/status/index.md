# Status Endpoint

:::warning NOTE
This endpoint is not available in the public API, but you can use it when running an own instance of openrouteservice.
:::

The GET request http://localhost:8082/ors/v2/status (host and port are dependent on the setup) returns basic information about the running instance:

* `languages`: Available languages
* `engine`:
    * `version`: Release version of the running openrouteservice
    * `build_date`: Build date of the running openrouteservice
    * `graph_version` Version of the graph structure and build logic. This parameter is part of the software and cannot
      be configured. Graphs with the same `graph_version` are compatible, e.g. openrouteservice version Y can load
      graphs that were built with an older version X, if both versions have the same `graph_version`. The parameter is
      also used to find a graph in a graph repository,
      if [graph management](/run-instance/configuration/engine/graph-management.md) is enabled.
* `profiles`: Available (enabled) routing profiles.
  The profile names correspond to the keys in the configuration beneath `ors.engine.profiles`.   
  The profile names are used as path parameters in API requests and as directory names for the graph directories.
  Some basic information is shown for each profile:
    * `encoder_name`: The vehicle type
    * `encoded_values`: The list of available encoded values that can be used in [custom models](/api-reference/endpoints/directions/custom-models)
    * `osm_date`: Timestamp of the osm pbf file that was used for building the graph. This is usually the date of the
      latest included change.
    * `graph_build_date`: The date, when graph building was started for this routing profile.
    * `storages`: configured storages
    * `limits`: configured limits
* `services`: Activated services. The endpoints `status` and `health` are not included here.

This is an example response: 
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
            "encoded_values": [
                "road_environment",
                "car_ors_fastest_with_turn_costs_subnetwork",
                "car_ors_fastest_subnetwork",
                "car_ors_shortest_with_turn_costs_subnetwork",
                "car_ors_shortest_subnetwork",
                "car_ors_recommended_with_turn_costs_subnetwork",
                "car_ors_recommended_subnetwork",
                "roundabout",
                "road_class",
                "road_class_link",
                "max_speed",
                "road_access",
                "car_ors$access",
                "car_ors$average_speed",
                "car_ors$turn_cost"
            ],
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
            "encoded_values": [
                "road_environment",
                "pedestrian_ors_fastest_subnetwork",
                "pedestrian_ors_shortest_subnetwork",
                "pedestrian_ors_recommended_subnetwork",
                "roundabout",
                "road_class",
                "road_class_link",
                "max_speed",
                "road_access",
                "foot_network",
                "pedestrian_ors$access",
                "pedestrian_ors$average_speed",
                "pedestrian_ors$priority"
            ],
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
