# Routing Options

With the request body parameter `options`, advanced routing options can be specified for a directions request.

The `options` parameter is a JSON object, multiple of the here mentioned parameters can be set. 
For structure refer to the [examples](#examples).


## Available parameters

### `options.avoid_borders`
String value specifying which borders to avoid. Only for **`driving-*`** profiles.

| Value          | Description                                  |
|----------------|----------------------------------------------|
| `"all"`        | for no border crossing                       |
| `"controlled"` | cross open borders but avoid controlled ones | 

### `options.avoid_countries` 
An integer array of country ids to exclude from routing with **`driving-*`** profiles. Can be used together with `"avoid_borders": "controlled"`. 
The list of countries and application examples can be found in the [country list](/technical-details/country-list.md).

`"[11,193]"` would exclude Austria and Switzerland. 

### `options.avoid_features`
A string array of features to avoid. The available features are :

| Feature    | Available for                               |
|------------|---------------------------------------------|
| `highways` | driving-\*                                  |
| `tollways` | driving-\*                                  |
| `ferries`  | driving-\*, cycling-\*, foot-\*, wheelchair |
| `fords`    | driving-\*, cycling-\*, foot-\*             |
| `steps`    | cycling-\*, foot-\*, wheelchair             |

### `options.avoid_polygons` 
Comprises areas to be avoided for the route. Formatted as [geojson polygon](https://datatracker.ietf.org/doc/html/rfc7946#appendix-A.3) or [geojson multipolygon](https://datatracker.ietf.org/doc/html/rfc7946#appendix-A.6).

### `options.profile_params`
An object of additional routing parameters for all profiles except `driving-car`: 

#### `options.profile_params.weightings`
Weightings will prioritize specified factors over the shortest path. 
The value is an object that can have the following properties:

* `steepness_difficulty`: Integer specifying the fitness level for **`cycling-*`** profiles. The preferred gradient increases with the value.

    | Value | Fitness level |
    |-------|---------------|
    | `0`   | Novice        |
    | `1`   | Moderate      |
    | `2`   | Amateur       |
    | `3`   | Pro           | 

* `green`: Integer value specifying the Green factor for **`foot-*`** profiles.

    | Value | Green factor                                         |
    |-------|------------------------------------------------------|
    | `0`   | normal routing                                       |
    | `1`   | prefer ways through green areas over a shorter route |


* `quiet`: Integer value specifying the Quiet factor for **`foot-*`** profiles.
    
    | Value | Quiet factor                           |
    |-------|----------------------------------------|
    | `0`   | normal routing                         |
    | `1`   | prefer quiet ways over a shorter route |

#### `options.profile_params.restrictions` 

[//]: # (see RequestProfileParamsRestrictions)

An object specifying restrictions for `cycling-*`, `driving-hgv` or `wheelchair`profiles.

* for `driving-hgv`:

  | Parameter  | Type    | Description                                                                                                                       |
  |------------|---------|-----------------------------------------------------------------------------------------------------------------------------------|
  | `length`   | Number  | Length restriction in meters.                                                                                                     |
  | `width`    | Number  | Width restriction in meters.                                                                                                      |
  | `height`   | Number  | Height restriction in meters.                                                                                                     |
  | `axleload` | Number  | Axle load restriction in tons.                                                                                                     |
  | `weight`   | Number  | Weight restriction in tons.                                                                                                       |
  | `hazmat`   | Boolean | Specifies whether to use appropriate routing for delivering hazardous goods and avoiding water protected areas. Default is false. |

* for `wheelchair`:

  | Parameter             | Type    | Description                                                                                                                 |
  |-----------------------|---------|-----------------------------------------------------------------------------------------------------------------------------|
  | `surface_type`        | String  | Specifies the minimum [surface type](http://wiki.openstreetmap.org/wiki/Key:surface). Default is `"cobblestone:flattened"`. |
  | `track_type`          | String  | Specifies the minimum [quality](http://wiki.openstreetmap.org/wiki/Key:tracktype) of the route. Default is `"grade1"`.        |
  | `smoothness_type`     | String  | Specifies the minimum [smoothness](http://wiki.openstreetmap.org/wiki/Key:smoothness) of the route. Default is `"good"`.    |
  | `maximum_sloped_kerb` | Number  | Specifies the maximum height of the sloped kerb in meters. Values are `0.03`, `0.06`(default), `0.1` or `any`.              |
  | `maximum_incline`     | Integer | Specifies the maximum incline as a percentage. `3`, `6`(default), `10`, `15` or `any`.                                      |
  | `minimum_width`       | Number  | Specifies the minimum width of a road in meters.                                                                            |

### `options.round-trip` 

An object with specifications of a round-trip:

| Parameter | Type    | Description                                                                                              |
|-----------|---------|----------------------------------------------------------------------------------------------------------|
| `length`  | Number  | The target length of the route in m (note that this is a preferred value, but results may be different). |
| `points`  | Integer | The number of points to use on the route. Larger values create more circular routes.                     |
| `seed`    | Integer | A seed to use for adding randomisation to the overall direction of the generated route (optional).       |

### `options.vehicle_type`
For `profile=driving-hgv` only.
It is needed for **vehicle restrictions** to work (see [tag filtering](/technical-details/tag-filtering.md#driving-hgv)).
Possible values:

* `hgv`
* `bus`
* `agricultural`
* `delivery`
* `forestry` 
* `goods`



## Examples

Some `options` examples in readable and minified JSON form:

### for `profile=driving-car`:

```json
{"avoid_features":["ferries","tollways"]}
```

### for `profile=cycling-*`:

```json
{
  "avoid_features": ["steps"],
  "profile_params": {
      "weightings": {
          "steepness_difficulty": 2
      }
  },
  "avoid_polygons": {
    "coordinates": [
      [
        [
          8.683223,
          49.41971
        ],
        [
          8.68322,
          49.41635
        ],
        [
          8.68697,
          49.41635
        ],
        [
          8.68697,
          49.41971
        ],
        [
          8.683223,
          49.41971
        ]
      ]
    ],
    "type": "Polygon"
  }
}
```
```json
{"avoid_features":["steps"],"profile_params":{"weightings":{"steepness_difficulty":2}},"avoid_polygons":{"coordinates":[[[8.683223,49.41971],[8.68322,49.41635],[8.68697,49.41635],[8.68697,49.41971],[8.683223,49.41971]]],"type":"Polygon"}}
```

### for `profile=foot-*`:

```json
{
    "avoid_features": ["fords","ferries"],
    "profile_params": {
        "weightings": {
            "green": {
                "factor": 0.8
              },
            "quiet": {
                "factor": 1.0
            }
        }
    }
}
```
```json
{"avoid_features":["fords","ferries"],"profile_params":{"weightings":{"green":{"factor":0.8},"quiet":{"factor":1.0}}}}
```

### for `profile=driving-hgv`:

```json
{
    "avoid_features": ["ferries","tollways"],
    "profile_params": {
        "restrictions": {
            "length": 30,
            "width": 30,
            "height": 3,
            "axleload": 4,
            "weight": 3,
            "hazmat": true
        }
    }
}
```
```json
{"avoid_features":["ferries","tollways"],"profile_params":{"restrictions":{"length":30,"width":30,"height":3,"axleload":4,"weight":3,"hazmat":true}}}
```

### for `profile=wheelchair`:

```json
{
    "avoid_features": ["ferries","steps"],
    "profile_params": {
        "restrictions": {
            "surface_type": "cobblestone:flattened",
            "track_type": "grade1",
            "smoothness_type": "good",
            "maximum_sloped_kerb": 0.06,
            "maximum_incline": 6
        }
    }
}
```

```json
{"avoid_features":["ferries","steps"],"profile_params":{"restrictions":{"surface_type":"cobblestone:flattened","track_type":"grade1","smoothness_type":"good","maximum_sloped_kerb":0.06,"maximum_incline":6}}}
```

### Border restrictions

Examples for routing options object with border restrictions:

#### _Do not cross country borders at all_

```json
{"avoid_borders":"all"}
```

#### _Do not cross controlled borders (i.e. USA - Canada) but allow crossing of open borders (i.e. France - Germany)_

```json
{"avoid_borders":"controlled"}
```

#### _Do not route through Austria or Switzerland_

```json
{"avoid_countries": [11,193]}
```

#### _Pass open borders but do not cross into Switzerland_

```json
{"avoid_borders": "controlled","avoid_countries": [193]}
```
