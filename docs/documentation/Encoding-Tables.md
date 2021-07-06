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

# Travel Time Calculation

The travel time is calculated for each segment by using speed-limits for different [waytypes](https://wiki.openstreetmap.org/wiki/Key:highway) and adjusting them for different [grades](https://wiki.openstreetmap.org/wiki/Key:tracktype) or [surfaces](https://wiki.openstreetmap.org/wiki/Key:surface) of the road.
If multiple values apply for this segment, the lowest value is used. For `cycling` profiles also the steepness is considered.
These limits can be reduced by setting the `maxSpeed` parameter in the [options](#routing-options).
The final [average speed-limits](#avgspeed) can be requested by adding `AvgSpeed` to the `extra_info` parameter.
The following table shows the initial speed-limits used for the main profiles:

_(all Values in km/h)_

## Waytype Speeds
Corresponds to the OSM [highway](https://wiki.openstreetmap.org/wiki/Key:highway) tag value.

  | Waytype \ Profile -> | driving-car | driving-hgv | cycling-regular |
  |:--------------------:|:-----------:|:-----------:|:---------------:|
  |       motorway       |     100     |      85     |        -        |
  |     motorway_link    |      60     |      50     |        -        |
  |       motorroad      |      90     |      80     |        -        |
  |         trunk        |      85     |      60     |        18       |
  |      trunk_link      |      60     |      50     |        18       |
  |        primary       |      65     |      60     |        18       |
  |     primary_link     |      50     |      50     |        18       |
  |       secondary      |      60     |      60     |        18       |
  |    secondary_link    |      50     |      50     |        18       |
  |       tertiary       |      50     |      50     |        18       |
  |     tertiary_link    |      40     |      40     |        18       |
  |     unclassified     |      30     |      30     |        16       |
  |      residential     |      30     |      30     |        18       |
  |     living_street    |      10     |      10     |        6        |
  |        service       |      20     |      20     |        14       |
  |         road         |      20     |      20     |        12       |
  |         track        |      15     |      15     |        12       |
  |         path         |      -      |      -      |        12       |
  |        footway       |      -      |      -      |        6        |
  |      pedestrian      |      -      |      -      |        6        |
  |       cycleway       |      -      |      -      |        18       |

## Surface Speeds
Corresponds to the OSM [surface](https://wiki.openstreetmap.org/wiki/Key:surface) tag value.

  | Surface \ Profile -> | driving-car | driving-hgv | cycling-regular |
  |:--------------------:|:-----------:|:-----------:|:---------------:|
  |        asphalt       |      -1     |      -1     |        18       |
  |       concrete       |      -1     |      -1     |        18       |
  |    concrete:plates   |      -1     |      -1     |        16       |
  |    concrete:lanes    |      -1     |      -1     |        16       |
  |         paved        |      -1     |      -1     |        18       |
  |        cement        |      80     |      60     |        -1       |
  |       compacted      |      80     |      60     |        18       |
  |      fine_gravel     |      60     |      50     |        16       |
  |     paving_stones    |      40     |      40     |        12       |
  |         metal        |      40     |      40     |        10       |
  |        bricks        |      40     |      40     |        -1       |
  |         grass        |      30     |      30     |         8       |
  |         wood         |      30     |      30     |         6       |
  |         sett         |      30     |      30     |        10       |
  |      grass_paver     |      30     |      30     |         8       |
  |        gravel        |      30     |      30     |        12       |
  |        unpaved       |      30     |      30     |        14       |
  |        ground        |      30     |      30     |        12       |
  |         dirt         |      30     |      30     |        10       |
  |      pebblestone     |      30     |      30     |        16       |
  |        tartan        |      30     |      30     |        -1       |
  |      cobblestone     |      20     |      20     |         8       |
  |         clay         |      20     |      20     |        -1       |
  |         earth        |      15     |      15     |        12       |
  |         stone        |      15     |      15     |        -1       |
  |         rocky        |      15     |      15     |        -1       |
  |         sand         |      15     |      15     |         6       |
  |          mud         |      10     |      10     |        10       |
  |       unknown:       |      30     |      30     |        -1       |

## Tracktype Speeds
Corresponds to the OSM [tracktype](https://wiki.openstreetmap.org/wiki/Key:tracktype) tag value.

  | Tracktype \ Profile -> | driving-car | driving-hgv | cycling-regular|
  |:----------------------:|:-----------:|:-----------:|:--------------:|
  |         grade1         |      40     |      40     |       18       |
  |         grade2         |      30     |      30     |       12       |
  |         grade3         |      20     |      20     |        8       |
  |         grade4         |      15     |      15     |        6       |
  |         grade5         |      10     |      10     |        4       |

## Pedestrian Speeds
The `foot-*` profiles generally use 5 km/h on all allowed waytypes.
Allowed waytypes consist of ways that are safe for use, ways that are better avoided (but still allowed) and other allowed ways in between:

  |        safe tags       |  avoid tags      |  other highway tags |
  |:----------------------:|:----------------:|:-------------------:|
  |         footway        |   trunk          |       cycleway      |
  |         path           | trunk_link       |    unclassified     |
  |         steps          |  primary         |       road        |
  |         pedestrian     |  primary_link    |                   |
  |         living_street  |  secondary       |                   |
  |         track          |  secondary_link  |                   |
  |         residential    |  tertiary        |                   |
  |         service        |  tertiary_link   |                   |

  
## Country Speed Sets
As there are various traffic regulations in different countries. If [maximum speed](http://wiki.openstreetmap.org/wiki/Key:maxspeed) tag is not given in openstreetmap, we adjust the maximum speed according to the following key values taken from [country specific speed limits](http://wiki.openstreetmap.org/wiki/Speed_limits).

  |    Country     |         Tags         | driving-car | driving-hgv |
  |:--------------:|:--------------------:|:-----------:|:-----------:|
  |   Austria      |       AT:urban       |      50     |      50     |
  |                |       AT:rural       |     100     |      80     |
  |                |       AT:trunk       |     100     |      80     |
  |                |      AT:motorway     |     130     |      80     |
  | Switzerland    |       CH:urban       |      50     |      50     |
  |                |       CH:rural       |      80     |      80     |
  |                |       CH:trunk       |     100     |      80     |
  |                |      CH:motorway     |     120     |      80     |
  | Czech Republic |       CZ:urban       |      50     |      50     |
  |                |       CZ:rural       |      90     |      90     |
  |                |       CZ:trunk       |      80     |      80     |
  |                |      CZ:motorway     |      80     |      80     |
  |   Denmark      |       DK:urban       |      50     |      50     |
  |                |       DK:rural       |      80     |      80     |
  |                |      DK:motorway     |     130     |      80     |
  |   Germany      |   DE:living_street   |       7     |       7     |
  |                |       DE:urban       |      50     |      50     |
  |                |       DE:rural       |     100     |      80     |
  |                |      DE:motorway     |     130     |      80     |
  |   Finland      |       FI:urban       |      50     |      50     |
  |                |       FI:rural       |      80     |      80     |
  |                |       FI:trunk       |     100     |      80     |
  |                |      FI:motorway     |     120     |      80     |
  |   France       |       FR:urban       |      50     |      50     |
  |                |       FR:rural       |      80     |      80     |
  |                |       FR:trunk       |     110     |      80     |
  |                |      FR:motorway     |     130     |      80     |
  |   Greece       |       GR:urban       |      50     |      50     |
  |                |       GR:rural       |      90     |      80     |
  |                |       GR:trunk       |     110     |      80     |
  |                |      GR:motorway     |     130     |      80     |
  |   Hungary      |       HU:urban       |      50     |      50     |
  |                |       HU:rural       |      90     |      80     |
  |                |       HU:trunk       |     110     |      80     |
  |                |      HU:motorway     |     130     |      80     |
  |    Italy       |       IT:urban       |      50     |      50     |
  |                |       IT:rural       |      90     |      80     |
  |                |       IT:trunk       |     110     |      80     |
  |                |      IT:motorway     |     130     |      80     |
  |    Japan       |      JP:national     |      60     |      60     |
  |                |      JP:motorway     |     100     |      80     |
  |   Poland       |   PL:living_street   |      20     |      20     |
  |                |       PL:urban       |      50     |      50     |
  |                |       PL:rural       |      90     |      80     |
  |                |      PL:motorway     |     140     |      80     |
  |   Romania      |       RO:urban       |      50     |      50     |
  |                |       RO:rural       |      90     |      80     |
  |                |       RO:trunk       |     100     |      80     |
  |                |      RO:motorway     |     130     |      80     |
  | Russia         |   RU:living_street   |      20     |      20     |
  |                |       RU:rural       |      90     |      80     |
  |                |       RU:urban       |      60     |      60     |
  |                |      RU:motorway     |     110     |      80     |
  |  Slovakia      |       SK:urban       |      50     |      50     |
  |                |       SK:rural       |      90     |      80     |
  |                |       SK:trunk       |      90     |      80     |
  |                |      SK:motorway     |      90     |      80     |
  |  Slovenia      |       SI:urban       |      50     |      50     |
  |                |       SI:rural       |      90     |      80     |
  |                |       SI:trunk       |     110     |      80     |
  |                |      SI:motorway     |     130     |      80     |
  |    Spain       |       ES:urban       |      50     |      50     |
  |                |       ES:rural       |      90     |      80     |
  |                |       ES:trunk       |     100     |      80     |
  |                |      ES:motorway     |     120     |      80     |
  |   Sweden       |       SE:urban       |      50     |      50     |
  |                |       SE:rural       |      70     |      70     |
  |                |       SE:trunk       |      90     |      80     |
  |                |      SE:motorway     |     110     |      80     |
  | United Kingdom |     GB:nsl_single    |      95     |      90     |
  |                |      GB:nsl_dual     |     112     |      90     |
  |                |      GB:motorway     |     112     |      90     |
  | Ukraine        |       UA:urban       |      60     |      60     |
  |                |       UA:rural       |      90     |      80     |
  |                |       UA:trunk       |     110     |      80     |
  |                |      UA:motorway     |     130     |      80     |
  | Uzbekistan     |   UZ:living_street   |      30     |      30     |
  |                |       UZ:urban       |      70     |      70     |
  |                |       UZ:rural       |     100     |      90     |
  |                |      UZ:motorway     |     110     |      90     |


# Geocoding structured query

A structured geocoding request is more precise than a normal one. It is also very useful for querying locations from tables.
For a structured request insert a JSON Object with at least on of the following parameters into the query parameter of the geocoding request:

  |   Parameter   | Description                                                                                                                                                                                              |
  |:-------------:|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
  |    address    | Can contain a full address with house number or only a street name                                                                                                                                       |
  | neighbourhood | Vernacular geographic entities that may not necessarily be official administrative divisions but are important nonetheless                                                                               |
  |    borough    | Mostly known in the context of New York City, even though they may exist in other cities, such as Mexico City                                                                                            |
  |    locality   | Name of a City                                                                                                                                                                                           |
  |     county    | Administrative division between localities and regions                                                                                                                                                   |
  |     region    | Normally the first-level administrative divisions within countries, analogous to states and provinces in the United States and Canada, respectively, though most other countries contain regions as well |
  |   postalcode  | A postalcode                                                                                                                                                                                             |
  |    country    | Name of a country. Supports two- and three-letter abbreviations                                                                                                                                          |

example:

```json
{
  "address": "Berliner Straße 45",
  "locality": "Heidelberg",
  "country": "Germany",
  "postalcode": "69120"
}
```
uglyfied and encoded:

`%7B"address": "Berliner Straße 45","locality": "Heidelberg","country": "Germany","postalcode": "69120"%7D`


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


