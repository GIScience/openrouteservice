---
parent: Documentation
nav_order: 4
has_children: true
has_toc: false
title: Routing Options
---

# Routing `options`

For advanced options formatted as json object. For structure refer to the [examples](Examples).
The available parameters are:


- `avoid_features` : array of features to avoid.
  The available features are :

  |     Feature         | Available for                               |
  |:-------------------:|---------------------------------------------|
  | `highways`          | driving-\*                                  |
  | `tollways`          | driving-\*                                  |
  | `ferries`           | driving-\*, cycling-\*, foot-\*, wheelchair |
  | `fords`             | driving-\*, cycling-\*, foot-\*             |
  | `steps`             | cycling-\*, foot-\*, wheelchair             |


- `avoid_borders` : `"all"` for no border crossing. `"controlled"` to cross open borders but avoid controlled ones. Only for `driving-*` profiles.

- `avoid_countries` : array of country ids to exclude from routing with `driving-*` profiles. Can be used together with `"avoid_borders": "controlled"`. `"[11,193]"` would exclude Austria and Switzerland. List of countries and application examples can be found in the [country list](Country-List).

- `vehicle_type` (for `profile=driving-hgv` only): `hgv`,`bus`,`agricultural`,`delivery`,`forestry` and `goods`. It is needed for **vehicle restrictions** to work.

- `profile_params` : Specifies additional routing parameters.

  - `weightings`: Weightings will prioritize specified factors over the shortest path.

    - `steepness_difficulty`: Specifies the fitness level for `cycling-*` profiles.
      - `level`: `0` = Novice, `1` = Moderate, `2` = Amateur, `3` = Pro. The prefered gradient increases with level

    - `green`: Specifies the Green factor for `foot-*` profiles.
      - `factor`: Values range from `0` to `1`. `0` equals normal routing. `1` will prefer ways through green areas over a shorter route.

    - `quiet`: Specifies the Quiet factor for `foot-*` profiles.
      - `factor`: Values range from `0` to `1`. `0` equals normal routing. `1` will prefer quiet ways over a shorter route.

  - `restrictions` : Specifies restrictions for `driving-hgv`, `wheelchair` or `cycling-*` profiles.

    - for `cycling-*`:

      |  Parameter | Description                                                                                                                                                                               |
      |:----------:|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
      | `gradient` | Only for avoided `hills` or specified `steepness_difficulty`. Specifies the maximum route steepness in percent. Values range from `1` to `15`. Routes with a higher gradient are avoided. |

    - for `driving-hgv`:
      *(you have to specify the `vehicle_type` in the options for these parameters)*

      |  Parameter | Description                                                                                                                       |
      |:----------:|-----------------------------------------------------------------------------------------------------------------------------------|
      | `length`   | Length restriction in meters.                                                                                                     |
      | `width`    | Width restriction in meters.                                                                                                      |
      | `height`   | Height restriction in meters.                                                                                                     |
      | `axleload` | Axleload restriction in tons.                                                                                                     |
      | `weight`   | Weight restriction in tons.                                                                                                       |
      | `hazmat`   | Specifies whether to use appropriate routing for delivering hazardous goods and avoiding water protected areas. Default is false. |

    - for `wheelchair`:

      |       Parameter       | Description                                                                                                                 |
      |:---------------------:|-----------------------------------------------------------------------------------------------------------------------------|
      |     `surface_type`    | Specifies the minimum [surface type](http://wiki.openstreetmap.org/wiki/Key:surface). Default is `"cobblestone:flattened"`. |
      |      `track_type`     | Specifies the minimum [grade](http://wiki.openstreetmap.org/wiki/Key:tracktype) of the route. Default is `"grade1"`.        |
      |   `smoothness_type`   | Specifies the minimum [smoothness](http://wiki.openstreetmap.org/wiki/Key:smoothness) of the route. Default is `"good"`.    |
      | `maximum_sloped_kerb` | Specifies the maximum height of the sloped kerb in meters. Values are `0.03`, `0.06`(default), `0.1` or `any`.              |
      |   `maximum_incline`   | Specifies the maximum incline as a percentage. `3`, `6`(default), `10`, `15` or `any`.                                      |
      |   `minimum_width`     | Specifies the minimum width of a road in meters.                                                                            |


- `avoid_polygons` : Comprises areas to be avoided for the route. Formatted as [geojson polygon](http://geojson.org/geojson-spec.html#id4) or [geojson multipolygon](http://geojson.org/geojson-spec.html#id7).

## URL Encoding

To use the curl command string you have to encode special characters.
Values you need are shown in this table:

  | Character |  {  |  \| |  }  |  "  |  [  |  ]  |
  |:---------:|:---:|:---:|:---:|:---:|:---:|:---:|
  |  Encoding | %7B | %7C | %7D | %22 | %5B | %5D |

Sometimes needs to be used for the [options object](#examples).
