#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $(basename $0) $*

configWithNonExistingPbf=$(makeTempFile $(basename $0) '{
  "ors": {
    "info": {
      "base_url": "https://openrouteservice.org/",
      "swagger_documentation_url": "https://api.openrouteservice.org/",
      "support_mail": "support@openrouteservice.org",
      "author_tag": "openrouteservice",
      "content_licence": "LGPL 3.0"
    },
    "api_settings": {
      "cors": {
        "allowed": {
          "origins": [
            "*"
          ],
          "headers": [
            "Content-Type",
            "X-Requested-With",
            "accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Authorization"
          ]
        },
        "exposed": {
          "headers": [
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
          ]
        },
        "preflight_max_age": 600
      }
    },
    "services": {
      "matrix": {
        "enabled": true,
        "maximum_routes": 100,
        "maximum_routes_flexible": 25,
        "maximum_search_radius": 5000,
        "maximum_visited_nodes": 100000,
        "allow_resolve_locations": true,
        "attribution": "openrouteservice.org, OpenStreetMap contributors"
      },
      "isochrones": {
        "enabled": true,
        "maximum_range_distance": [
          {
            "profiles": "any",
            "value": 50000
          },
          {
            "profiles": "driving-car, driving-hgv",
            "value": 100000
          }
        ],
        "maximum_range_time": [
          {
            "profiles": "any",
            "value": 18000
          },
          {
            "profiles": "driving-car, driving-hgv",
            "value": 3600
          }
        ],
        "fastisochrones": {
          "maximum_range_distance": [
            {
              "profiles": "any",
              "value": 50000
            },
            {
              "profiles": "driving-car, driving-hgv",
              "value": 500000
            }
          ],
          "maximum_range_time": [
            {
              "profiles": "any",
              "value": 18000
            },
            {
              "profiles": "driving-car, driving-hgv",
              "value": 10800
            }
          ],
          "profiles": {
            "default_params": {
              "enabled": false,
              "threads": 12,
              "weightings": "recommended",
              "maxcellnodes": 5000
            },
            "hgv": {
              "enabled": false,
              "threads": 12,
              "weightings": "recommended, shortest",
              "maxcellnodes": 5000
            }
          }
        },
        "maximum_intervals": 10,
        "maximum_locations": 2,
        "allow_compute_area": true
      },
      "routing": {
        "enabled": true,
        "mode": "normal",
        "routing_description": "This is a routing file from openrouteservice",
        "routing_name": "openrouteservice routing",
        "sources": [
          "ors-api/src/test/files/andorra.osm.gz"
        ],
        "init_threads": 1,
        "attribution": "openrouteservice.org, OpenStreetMap contributors",
        "elevation_preprocessed": false,
        "profiles": {
          "active": [
            "car"
          ],
          "default_params": {
            "encoder_flags_size": 8,
            "graphs_root_path": "graphs",
            "elevation_provider": "multi",
            "elevation_cache_path": "elevation_cache",
            "elevation_cache_clear": false,
            "instructions": true,
            "maximum_distance": 100000,
            "maximum_distance_dynamic_weights": 100000,
            "maximum_distance_avoid_areas": 100000,
            "maximum_waypoints": 50,
            "maximum_snapping_radius": 400,
            "maximum_avoid_polygon_area": 200000000,
            "maximum_avoid_polygon_extent": 20000,
            "maximum_distance_alternative_routes": 100000,
            "maximum_alternative_routes": 3,
            "maximum_distance_round_trip_routes": 100000,
            "maximum_speed_lower_bound": 80,
            "preparation": {
              "min_network_size": 200,
              "min_one_way_network_size": 200,
              "methods": {
                "lm": {
                  "enabled": true,
                  "threads": 1,
                  "weightings": "recommended,shortest",
                  "landmarks": 16
                }
              }
            },
            "execution": {
              "methods": {
                "lm": {
                  "disabling_allowed": true,
                  "active_landmarks": 8
                }
              }
            }
          },
          "profile-car": {
            "profiles": "driving-car",
            "parameters": {
              "encoder_flags_size": 8,
              "encoder_options": "turn_costs=true|block_fords=false|use_acceleration=true",
              "maximum_distance": 100000,
              "elevation": true,
              "maximum_snapping_radius": 350,
              "preparation": {
                "min_network_size": 200,
                "min_one_way_network_size": 200,
                "methods": {
                  "ch": {
                    "enabled": true,
                    "threads": 1,
                    "weightings": "fastest"
                  },
                  "lm": {
                    "enabled": false,
                    "threads": 1,
                    "weightings": "fastest,shortest",
                    "landmarks": 16
                  },
                  "core": {
                    "enabled": true,
                    "threads": 1,
                    "weightings": "fastest,shortest",
                    "landmarks": 64,
                    "lmsets": "highways;allow_all"
                  }
                }
              },
              "execution": {
                "methods": {
                  "ch": {
                    "disabling_allowed": true
                  },
                  "lm": {
                    "disabling_allowed": true,
                    "active_landmarks": 6
                  },
                  "core": {
                    "disabling_allowed": true,
                    "active_landmarks": 6
                  }
                }
              },
              "ext_storages": {
                "WayCategory": {},
                "HeavyVehicle": {},
                "WaySurfaceType": {},
                "RoadAccessRestrictions": {
                  "use_for_warnings": true
                }
              }
            }
          },
          "profile-hgv": {
            "profiles": "driving-hgv",
            "parameters": {
              "encoder_flags_size": 8,
              "encoder_options": "turn_costs=true|block_fords=false|use_acceleration=true",
              "maximum_distance": 100000,
              "elevation": true,
              "preparation": {
                "min_network_size": 200,
                "min_one_way_network_size": 200,
                "methods": {
                  "ch": {
                    "enabled": true,
                    "threads": 1,
                    "weightings": "recommended"
                  },
                  "core": {
                    "enabled": true,
                    "threads": 1,
                    "weightings": "recommended,shortest",
                    "landmarks": 64,
                    "lmsets": "highways;allow_all"
                  }
                }
              },
              "execution": {
                "methods": {
                  "ch": {
                    "disabling_allowed": true
                  },
                  "core": {
                    "disabling_allowed": true,
                    "active_landmarks": 6
                  }
                }
              },
              "ext_storages": {
                "WayCategory": {},
                "HeavyVehicle": {
                  "restrictions": true
                },
                "WaySurfaceType": {}
              }
            }
          },
          "profile-bike-regular": {
            "profiles": "cycling-regular",
            "parameters": {
              "encoder_options": "consider_elevation=true|turn_costs=true|block_fords=false",
              "elevation": true,
              "ext_storages": {
                "WayCategory": {},
                "WaySurfaceType": {},
                "HillIndex": {},
                "TrailDifficulty": {}
              }
            }
          },
          "profile-bike-mountain": {
            "profiles": "cycling-mountain",
            "parameters": {
              "encoder_options": "consider_elevation=true|turn_costs=true|block_fords=false",
              "elevation": true,
              "ext_storages": {
                "WayCategory": {},
                "WaySurfaceType": {},
                "HillIndex": {},
                "TrailDifficulty": {}
              }
            }
          },
          "profile-bike-road": {
            "profiles": "cycling-road",
            "parameters": {
              "encoder_options": "consider_elevation=true|turn_costs=true|block_fords=false",
              "elevation": true,
              "ext_storages": {
                "WayCategory": {},
                "WaySurfaceType": {},
                "HillIndex": {},
                "TrailDifficulty": {}
              }
            }
          },
          "profile-bike-electric": {
            "profiles": "cycling-electric",
            "parameters": {
              "encoder_options": "consider_elevation=true|turn_costs=true|block_fords=false",
              "elevation": true,
              "ext_storages": {
                "WayCategory": {},
                "WaySurfaceType": {},
                "HillIndex": {},
                "TrailDifficulty": {}
              }
            }
          },
          "profile-walking": {
            "profiles": "foot-walking",
            "parameters": {
              "encoder_options": "block_fords=false",
              "elevation": true,
              "ext_storages": {
                "WayCategory": {},
                "WaySurfaceType": {},
                "HillIndex": {},
                "TrailDifficulty": {}
              }
            }
          },
          "profile-hiking": {
            "profiles": "foot-hiking",
            "parameters": {
              "encoder_options": "block_fords=false",
              "elevation": true,
              "ext_storages": {
                "WayCategory": {},
                "WaySurfaceType": {},
                "HillIndex": {},
                "TrailDifficulty": {}
              }
            }
          },
          "profile-wheelchair": {
            "profiles": "wheelchair",
            "parameters": {
              "encoder_options": "block_fords=true",
              "elevation": true,
              "maximum_snapping_radius": 50,
              "ext_storages": {
                "WayCategory": {},
                "WaySurfaceType": {},
                "Wheelchair": {
                  "KerbsOnCrossings": "true"
                },
                "OsmId": {}
              }
            }
          },
          "profile-public-transport": {
            "profiles": "public-transport",
            "parameters": {
              "encoder_options": "block_fords=false",
              "elevation": true,
              "maximum_visited_nodes": 1000000,
              "gtfs_file": "ors-api/src/test/files/vrn_gtfs.zip"
            }
          }
        }
      }
    },
    "logging": {
      "enabled": true,
      "level_file": "DEBUG_LOGGING.json",
      "location": "./logs",
      "stdout": true
    },
    "system_message": [
      {
        "active": false,
        "text": "This message would be sent with every routing bike fastest request",
        "condition": {
          "request_service": "routing",
          "request_profile": "cycling-regular,cycling-mountain,cycling-road,cycling-electric",
          "request_preference": "fastest"
        }
      },
      {
        "active": false,
        "text": "This message would be sent with every request for geojson response",
        "condition": {
          "api_format": "geojson"
        }
      },
      {
        "active": false,
        "text": "This message would be sent with every request on API v1 from January 2020 until June 2050",
        "condition": {
          "api_version": 1,
          "time_after": "2020-01-01T00:00:00Z",
          "time_before": "2050-06-01T00:00:00Z"
        }
      },
      {
        "active": false,
        "text": "This message would be sent with every request"
      }
    ]
  }
}
')

# This test should load the json config which contains a path to a non existing OSM file.
# Without the variable PBF_FILE_PATH, which contains a path to an existing OSM file,
# openrouteservice should not start up. But if PBF_FILE_PATH is evaluated correctly,
# it should start with the expected activated profile.
podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${TESTROOT}/graphs_volume":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${configWithNonExistingPbf}":${CONTAINER_WORK_DIR}/ors-config.json \
  --env ORS_CONFIG="${CONTAINER_WORK_DIR}/ors-config.json" \
  --env PBF_FILE_PATH=ors-api/src/test/files/heidelberg.osm.gz \
  "local/${IMAGE}:latest" &

awaitOrsReady 60 "${HOST_PORT}"
profiles=$(requestEnabledProfiles ${HOST_PORT})
cleanupTest

assertEquals "driving-car" "${profiles}"
