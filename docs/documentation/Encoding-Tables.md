---
parent: Documentation
nav_order: 4
title: Encoding Tables
---

This document stores encoding tables that go beyond the display options of swagger.

- [Travel Time Calculation](#travel-time-calculation)
  - [Waytype](#waytype-speeds)
  - [Surface](#surface-speeds)
  - [Tracktype](#tracktype-speeds)
  - [Pedestrian Speeds](#$pedestrian-speeds)
  - [Country Speed Sets](#country-speed-sets)
- [URL Encoding](#url-encoding)
- [Geocoding structured query](#geocoding-structured-query)
- [Geocoding Response](#geocoding-response)
  - [Place Type](#place-type)
- [Routing `options`](#routing-options)
  - [Examples](#examples)
  - [Border Restrictions](#border-restrictions)
    - [Country List](#country-list)
- [Routing Response](#routing-response)
  - [Steepness](#steepness)
  - [Suitability](#suitability)
  - [Surface](#surface)
  - [WayCategory](#waycategory)
  - [Waytype](#waytype)
  - [AvgSpeed](#avgspeed)
  - [Tollways](#tollways)
  - [TrailDifficulty](#trail-difficulty)
  - [Instruction Types](#instruction-types)
  - [Road Access Restrictions](#road-access-restrictions)
  - [Geometry Decoding](#geometry-decoding)
- [Places Response](#places-response)
  - [category_group_ids](#category_group_ids)
  - [category_ids](#category_ids)
- [Matrix Response](#matrix-response)


# Routing Response

Encoding of the Extra Information:

## Steepness

Negative values indicate decline, positive incline.

| Value | Encoding |
|:-----:|:--------:|
| -5    | >16%     |
| -4    | 12-15%   |
| -3    | 7-11%    |
| -2    | 4-6%     |
| -1    | 1-3%     |
| 0     | 0%       |
| 1     | 1-3%     |
| 2     | 4-6%     |
| 3     | 7-11%    |
| 4     | 12-15%   |
| 5     | >16%     |

## Suitability

The suitability values for the selected profile range from ``10`` for best suitability to ``1`` for worst suitability.

## Surface

| Value |       Name       |
|:-----:|:----------------:|
| 0     | Unknown          |
| 1     | Paved            |
| 2     | Unpaved          |
| 3     | Asphalt          |
| 4     | Concrete         |
| 5     | Cobblestone      |
| 6     | Metal            |
| 7     | Wood             |
| 8     | Compacted Gravel |
| 9     | Fine Gravel      |
| 10    | Gravel           |
| 11    | Dirt             |
| 12    | Ground           |
| 13    | Ice              |
| 14    | Paving Stones    |
| 15    | Sand             |
| 16    | Woodchips        |
| 17    | Grass            |
| 18    | Grass Paver      |

## WayCategory

The exponential assignment of the values is used for [bit fields](http://eddmann.com/posts/using-bit-flags-and-enumsets-in-java/). One route section may belong to different categories. Hence a value of ``97`` would indicate a belonging to ``Paved road``, ``Tunnel`` and ``Highway`` (``64`` + ``32`` + ``1`` ).

| Value |             Name             |
|:-----:|:----------------------------:|
| 0     | No category                  |
| 1     | Highway                      |
| 2     | Steps                        |
| 4     | Unpaved road                 |
| 8     | Ferry                        |
| 16    | Track                        |
| 32    | Tunnel                       |
| 64    | Paved road                   |
| 128   | Ford                         |

## Waytype

| Value |     Name     |
|:-----:|:------------:|
| 0     | Unknown      |
| 1     | State Road   |
| 2     | Road         |
| 3     | Street       |
| 4     | Path         |
| 5     | Track        |
| 6     | Cycleway     |
| 7     | Footway      |
| 8     | Steps        |
| 9     | Ferry        |
| 10    | Construction |

## AvgSpeed

This value is in _km/h_ and equals the average speed for this way segment after grading and applying factors.


## Tollways

Tollway specific information for the [selected mode of transport](https://en.wikipedia.org/wiki/Vehicle_category). Thus it depends on the `profile` if a ways is marked as [tollway](https://wiki.openstreetmap.org/wiki/Key:toll). 

| Value |  Encoding  |
|:-----:|:----------:|
|   0   | no tollway |
|   1   | is tollway |

## Trail difficulty

This extra provides information about a trails difficulty for [hiking](https://wiki.openstreetmap.org/wiki/Key:sac_scale) as well as for [mountain-biking](https://wiki.openstreetmap.org/wiki/Key:mtb:scale).

| Value | foot-*                              | cycling-*   |
|:-----:|-------------------------------------|-------------|
|   0   | no tag                              | no tag      |
|   1   | sac_scale=hiking                    | mtb:scale=0 |
|   2   | sac_scale=mountain_hiking           | mtb:scale=1 |
|   3   | sac_scale=demanding_mountain_hiking | mtb:scale=2 |
|   4   | sac_scale=alpine_hiking             | mtb:scale=3 |
|   5   | sac_scale=demanding_alpine_hiking   | mtb:scale=4 |
|   6   | sac_scale=difficult_alpine_hiking   | mtb:scale=5 |
|   7   |                 ---                 | mtb:scale=6 |

## Road access restrictions

Provides information about possible restrictions on roads.

| Value |  Encoding  |
|:-----:|:----------:|
| 0 | None (there are no restrictions) |
| 1 | No |
| 2 | Customers |
| 4 | Destination |
| 8 | Delivery |
| 16 | Private |
| 32 | Permissive |


