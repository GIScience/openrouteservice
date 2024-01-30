# Status Endpoint

:::warning NOTE
This endpoint is not available in the public API, but you can use it when running an own instance of openrouteservice.
:::

The GET request http://localhost:8082/ors/v2/status (host and port are dependent on the setup) returns basic information about the running instance:

* `languages`: available languages
* `engine`: the build date and version of the openrouteservice
* `profiles`: available routing profiles, info about storages and configured limits
* `services`: activated services

[//]: # (TODO: engine git die ORS version aus, nicht die eigentliche engine version, die wir dann auch im graph management verwenden, oder?)

 
:::details This is an example response: 
```json
{
    "languages": [
        "cs",
        "cs-cz",
        "de",
        "de-de",
        "en",
        "en-us",
        "eo",
        "eo-eo",
        "es",
        "es-es",
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
        "zh",
        "zh-cn"
    ],
    "engine": {
        "build_date": "2023-12-15T14:31:27Z",
        "version": "8.0"
    },
    "profiles": {
        "profile 1": {
            "storages": {
                "WayCategory": {
                    "gh_profile": "car_ors_fastest_with_turn_costs"
                },
                "HeavyVehicle": {
                    "gh_profile": "car_ors_fastest_with_turn_costs"
                },
                "WaySurfaceType": {
                    "gh_profile": "car_ors_fastest_with_turn_costs"
                },
                "RoadAccessRestrictions": {
                    "gh_profile": "car_ors_fastest_with_turn_costs",
                    "use_for_warnings": "true"
                }
            },
            "profiles": "driving-car",
            "creation_date": "",
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

