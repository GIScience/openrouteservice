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

# URL Encoding

To use the curl command string you have to encode special characters.
Values you need are shown in this table:

  | Character |  {  |  \| |  }  |  "  |  [  |  ]  |
  |:---------:|:---:|:---:|:---:|:---:|:---:|:---:|
  |  Encoding | %7B | %7C | %7D | %22 | %5B | %5D |

Sometimes needs to be used for the [options object](#examples).

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


# Routing options

For advanced options formatted as json object. For structure refer to the examples below.
The available parameters are:


- `maximum_speed` : Specifies a maximum travel speed restriction in km/h.


- `avoid_features` : Pipe (|) separated list of features to avoid.
  The available features are :

  |     Feature         | Available for                               |
  |:-------------------:|---------------------------------------------|
  | `highways`          | driving-*                                   |
  | `tollways`          | driving-*                                   |
  | `ferries`           | driving-\*, cycling-\*, foot-\*, wheelchair |
  | `tunnels`           | driving-*                                   |
  | `tracks`            | driving-*                                   |
  | `fords`             | driving-\*, cycling-\*, foot-*              |
  | `steps`             | cycling-\*, foot-\*, wheelchair             |
  | `hills`             | cycling-\*, foot-\*                         |


- `avoid_borders` : `"all"` for no border crossing. `"controlled"` to cross open borders but avoid controlled ones. Only for `driving-*` profiles.

- `avoid_countries` : Pipe (|) separated list of countries to exclude from routing with `driving-*` profiles. Can be used together with `"avoid_borders": "controlled"`. `"11|193"` would exclude Austria and Switzerland. List of countries and application examples can be found in the [country list](#country-list).

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


- `avoid_polygons` : Comprises areas to be avoided for the route. Formatted as [geojson polygon](http://geojson.org/geojson-spec.html#id4) or [geojson multipolygon](http://geojson.org/geojson-spec.html#id7).


##### country list

  | country_id |                        name                        |             name:en            |   | country_id |                    name                   |             name:en            |   | country_id |                   name                  |        name:en        |   | country_id |                      name                     |                    name:en                    |   | country_id |                    name                   |          name:en         |
  |:----------:|:--------------------------------------------------:|:------------------------------:|---|:----------:|:-----------------------------------------:|:------------------------------:|---|:----------:|:---------------------------------------:|:---------------------:|---|:----------:|:---------------------------------------------:|:---------------------------------------------:|---|:----------:|:-----------------------------------------:|:------------------------:|
  |      1     | افغانستان                                          | Afghanistan                    |   |     49     | Hrvatska                                  | Croatia                        |   |     97     | Italia                                  | Italy                 |   |     145    | Nigeria                                       | Nigeria                                       |   |     193    | Schweiz - Suisse - Svizzera - Svizra      | Switzerland              |
  |      2     | Shqipëria                                          | Albania                        |   |     50     | Cuba                                      | Cuba                           |   |     98     | Jamaica                                 | Jamaica               |   |     146    | Niuē                                          | Niue                                          |   |     194    | سوريا                                     | Syria                    |
  |      3     | Algeria                                            | Algeria                        |   |     51     | Κύπρος - Kıbrıs                           | Cyprus                         |   |     99     | Jangy-ayyl                              | Jangy-ayyl            |   |     147    | 조선민주주의인민공화국                        | North Korea                                   |   |     195    | 臺灣                                      | Taiwan                   |
  |      4     | Andorra                                            | Andorra                        |   |     52     | Česko                                     | Czech Republic                 |   |     100    | 日本                                    | Japan                 |   |     148    | Norge                                         | Norway                                        |   |     196    | Тоҷикистон                                | Tajikistan               |
  |      5     | Angola                                             | Angola                         |   |     53     | Danmark                                   | Denmark                        |   |     101    | Jersey                                  | Jersey                |   |     149    | عمان                                          | Oman                                          |   |     197    | Tanzania                                  | Tanzania                 |
  |      6     | Anguilla                                           | Anguilla                       |   |     54     | Djibouti                                  | Djibouti                       |   |     102    | الأردن                                  | Jordan                |   |     150    | ‏پاکستان‎                                       | Pakistan                                      |   |     198    | ประเทศไทย                                 | Thailand                 |
  |      7     | Antigua and Barbuda                                | Antigua and Barbuda            |   |     55     | Dominica                                  | Dominica                       |   |     103    | Қазақстан                               | Kazakhstan            |   |     151    | Belau                                         | Palau                                         |   |     199    | The Bahamas                               | The Bahamas              |
  |      8     | Argentina                                          | Argentina                      |   |     56     | República Dominicana                      | Dominican Republic             |   |     104    | Kenya                                   | Kenya                 |   |     152    | الضفة الغربية وقطاع غزة                       | Palestinian Territories                       |   |     200    | Nederland                                 | The Netherlands          |
  |      9     | Հայաստան                                           | Armenia                        |   |     57     | Timór Loro Sa'e                           | East Timor                     |   |     105    | Kiribati                                | Kiribati              |   |     153    | Panamá                                        | Panama                                        |   |     201    | Togo                                      | Togo                     |
  |     10     | Australia                                          | Australia                      |   |     58     | Ecuador                                   | Ecuador                        |   |     106    | Kosovë                                  | Kosovo                |   |     154    | Papua Niugini                                 | Papua New Guinea                              |   |     202    | Tokelau                                   | Tokelau                  |
  |     11     | Österreich                                         | Austria                        |   |     59     | Egypt مصر                                 | Egypt                          |   |     107    | ‏الكويت‎                                  | Kuwait                |   |     155    | Paraguay                                      | Paraguay                                      |   |     203    | Tonga                                     | Tonga                    |
  |     12     | Azərbaycan                                         | Azerbaijan                     |   |     60     | El Salvador                               | El Salvador                    |   |     108    | Кыргызстан                              | Kyrgyzstan            |   |     156    | Perú                                          | Peru                                          |   |     204    | Trinidad and Tobago                       | Trinidad and Tobago      |
  |     13     | ‏البحرين‎                                            | Bahrain                        |   |     61     | Guinea Ecuatorial                         | Equatorial Guinea              |   |     109    | ປະເທດລາວ                                | Laos                  |   |     157    | Philippines                                   | Philippines                                   |   |     205    | Tunisie ⵜⵓⵏⵙ تونس                         | Tunisia                  |
  |     14     | বাংলাদেশ                                           | Bangladesh                     |   |     62     | ኤርትራ                                      | Eritrea                        |   |     110    | Latvija                                 | Latvia                |   |     158    | Pitcairn Islands                              | Pitcairn Islands                              |   |     206    | Türkiye                                   | Turkey                   |
  |     15     | Barbados                                           | Barbados                       |   |     63     | Eesti                                     | Estonia                        |   |     111    | لبنان                                   | Lebanon               |   |     159    | Polska                                        | Poland                                        |   |     207    | Türkmenistan                              | Turkmenistan             |
  |     16     | Беларусь                                           | Belarus                        |   |     64     | ኢትዮጵያ Ethiopia                            | Ethiopia                       |   |     112    | Lesotho                                 | Lesotho               |   |     160    | Portugal                                      | Portugal                                      |   |     208    | Turks and Caicos Islands                  | Turks and Caicos Islands |
  |     17     | België - Belgique - Belgien                        | Belgium                        |   |     65     | Falkland Islands                          | Falkland Islands               |   |     113    | Liberia                                 | Liberia               |   |     161    | ‏قطر‎                                           | Qatar                                         |   |     209    | Tuvalu                                    | Tuvalu                   |
  |     18     | Belize                                             | Belize                         |   |     66     | Føroyar                                   | Faroe Islands                  |   |     114    | Libya ⵍⵉⴱⵢⴰ ليبيا                       | Libya                 |   |     162    | România                                       | Romania                                       |   |     210    | Uganda                                    | Uganda                   |
  |     19     | Bénin                                              | Benin                          |   |     67     | Federated States of Micronesia            | Federated States of Micronesia |   |     115    | Liechtenstein                           | Liechtenstein         |   |     163    | Российская Федерация                          | Russian Federation                            |   |     211    | Україна                                   | Ukraine                  |
  |     20     | Bermuda                                            | Bermuda                        |   |     68     | Viti                                      | Fiji                           |   |     116    | Lietuva                                 | Lithuania             |   |     164    | Rwanda                                        | Rwanda                                        |   |     212    | الإمارات العربيّة المتّحدة                  | United Arab Emirates     |
  |     21     | འབྲུག་ཡུལ་                                            | Bhutan                         |   |     69     | Suomi                                     | Finland                        |   |     117    | Lëtzebuerg                              | Luxembourg            |   |     165    | Sahrawi Arab Democratic Republic              | Sahrawi Arab Democratic Republic              |   |     213    | United Kingdom                            | United Kingdom           |
  |     22     | Bolivia                                            | Bolivia                        |   |     70     | France                                    | France                         |   |     118    | Македонија                              | Macedonia             |   |     166    | Saint Helena - Ascension and Tristan da Cunha | Saint Helena - Ascension and Tristan da Cunha |   |     214    | United States of America                  | United States of America |
  |     23     | Bosna i Hercegovina                                | Bosnia and Herzegovina         |   |     71     | Gabon                                     | Gabon                          |   |     119    | Madagasikara                            | Madagascar            |   |     167    | Saint Kitts and Nevis                         | Saint Kitts and Nevis                         |   |     215    | Uruguay                                   | Uruguay                  |
  |     24     | Botswana                                           | Botswana                       |   |     72     | Gambia                                    | Gambia                         |   |     120    | Malawi                                  | Malawi                |   |     168    | Saint Lucia                                   | Saint Lucia                                   |   |     216    | Oʻzbekiston                               | Uzbekistan               |
  |     25     | Brasil                                             | Brazil                         |   |     73     | საქართველო                                | Georgia                        |   |     121    | Malaysia                                | Malaysia              |   |     169    | Saint Vincent and the Grenadines              | Saint Vincent and the Grenadines              |   |     217    | Vanuatu                                   | Vanuatu                  |
  |     26     | British Indian Ocean Territory                     | British Indian Ocean Territory |   |     74     | Deutschland                               | Germany                        |   |     122    | ދިވެހިރާއްޖެ                                  | Maldives              |   |     170    | Sāmoa                                         | Samoa                                         |   |     218    | Città del Vaticano                        | Vatican City             |
  |     27     | British Sovereign Base Areas                       | British Sovereign Base Areas   |   |     75     | Deutschland - Belgique / België / Belgien | Germany - Belgium              |   |     123    | Mali                                    | Mali                  |   |     171    | San Marino                                    | San Marino                                    |   |     219    | Venezuela                                 | Venezuela                |
  |     28     | British Virgin Islands                             | British Virgin Islands         |   |     76     | Ghana                                     | Ghana                          |   |     124    | Malta                                   | Malta                 |   |     172    | São Tomé e Príncipe                           | São Tomé and Príncipe                         |   |     220    | Việt Nam                                  | Vietnam                  |
  |     29     | Brunei Darussalam                                  | Brunei                         |   |     77     | Gibraltar                                 | Gibraltar                      |   |     125    | Aelōn̄ in M̧ajeļ                          | Marshall Islands      |   |     173    | ‏المملكة العربية السعودية‎                      | Saudi Arabia                                  |   |     221    | اليمن                                     | Yemen                    |
  |     30     | България                                           | Bulgaria                       |   |     78     | Ελλάδα                                    | Greece                         |   |     126    | Mauritanie موريتانيا                    | Mauritania            |   |     174    | Sénégal                                       | Senegal                                       |   |     222    | Zambia                                    | Zambia                   |
  |     31     | Burkina Faso                                       | Burkina Faso                   |   |     79     | Kalaallit Nunaat                          | Greenland                      |   |     127    | Mauritius                               | Mauritius             |   |     175    | Србија                                        | Serbia                                        |   |     223    | Zimbabwe                                  | Zimbabwe                 |
  |     32     | Burundi                                            | Burundi                        |   |     80     | Grenada                                   | Grenada                        |   |     128    | México                                  | Mexico                |   |     176    | Sesel                                         | Seychelles                                    |   |     224    | Border India - Bangladesh                 |                          |
  |     33     | ព្រះរាជាណាចក្រ​កម្ពុជា                                  | Cambodia                       |   |     81     | Guatemala                                 | Guatemala                      |   |     129    | Moldova                                 | Moldova               |   |     177    | Sierra Leone                                  | Sierra Leone                                  |   |     225    | Île Verte                                 |                          |
  |     34     | Cameroun                                           | Cameroon                       |   |     82     | Guernsey                                  | Guernsey                       |   |     130    | Monaco                                  | Monaco                |   |     178    | Singapore                                     | Singapore                                     |   |     226    | Border Azerbaijan - Armenia (Enclave AZE) |                          |
  |     35     | Canada                                             | Canada                         |   |     83     | Guinée                                    | Guinea                         |   |     131    | Монгол улс                              | Mongolia              |   |     179    | Slovensko                                     | Slovakia                                      |   |     227    | Freezland Rock                            |                          |
  |     36     | Cabo Verde                                         | Cape Verde                     |   |     84     | Guiné-Bissau                              | Guinea-Bissau                  |   |     132    | Црна Гора / Crna Gora                   | Montenegro            |   |     180    | Slovenija                                     | Slovenia                                      |   |     228    | Border SI-HR                              |                          |
  |     37     | Cayman Islands                                     | Cayman Islands                 |   |     85     | Guyana                                    | Guyana                         |   |     133    | Montserrat                              | Montserrat            |   |     181    | Solomon Islands                               | Solomon Islands                               |   |     229    | Willis Island                             |                          |
  |     38     | Ködörösêse tî Bêafrîka - République Centrafricaine | Central African Republic       |   |     86     | Ayiti                                     | Haiti                          |   |     134    | Maroc ⵍⵎⵖⵔⵉⴱ المغرب                     | Morocco               |   |     182    | Soomaaliya                                    | Somalia                                       |   |     230    | Chong-Kara                                |                          |
  |     39     | Tchad تشاد                                         | Chad                           |   |     87     | Honduras                                  | Honduras                       |   |     135    | Moçambique                              | Mozambique            |   |     183    | South Africa                                  | South Africa                                  |   |     231    | Ελλάδα - Παγγαίο                          |                          |
  |     40     | Chile                                              | Chile                          |   |     88     | Magyarország                              | Hungary                        |   |     136    | မြန်မာ                                  | Myanmar               |   |     184    | South Georgia and South Sandwich Islands      | South Georgia and the South Sandwich Islands  |   |     232    | Bristol Island                            |                          |
  |     41     | 中国                                               | China                          |   |     89     | Ísland                                    | Iceland                        |   |     137    | name                                    | name:en               |   |     185    | 대한민국                                      | South Korea                                   |   |     233    | Dist. Judges Court                        |                          |
  |     42     | Colombia                                           | Colombia                       |   |     90     | India                                     | India                          |   |     138    | Namibia                                 | Namibia               |   |     186    | South Sudan                                   | South Sudan                                   |   |     234    | Border Kyrgyzstan - Uzbekistan            |                          |
  |     43     | Komori                                             | Comoros                        |   |     91     | Indonesia                                 | Indonesia                      |   |     139    | Naoero                                  | Nauru                 |   |     187    | España                                        | Spain                                         |   |     235    | Border Malawi - Mozambique                |                          |
  |     44     | République du Congo                                | Congo-Brazzaville              |   |     92     | ‏ایران‎                                     | Iran                           |   |     140    | नेपाल                                    | Nepal                 |   |     188    | ශ්‍රී ලංකාව இலங்கை                                | Sri Lanka                                     |   |     236    | 中華民國                                  |                          |
  |     45     | République démocratique du Congo                   | Congo-Kinshasa                 |   |     93     | العراق                                    | Iraq                           |   |     141    | Nederland - Belgique / België / Belgien | Netherlands - Belgium |   |     189    | Sudan السودان                                 | Sudan                                         |   |            |                                           |                          |
  |     46     | Cook Islands                                       | Cook Islands                   |   |     94     | Ireland                                   | Ireland                        |   |     142    | New Zealand/Aotearoa                    | New Zealand           |   |     190    | Suriname                                      | Suriname                                      |   |            |                                           |                          |
  |     47     | Costa Rica                                         | Costa Rica                     |   |     95     | Isle of Man                               | Isle of Man                    |   |     143    | Nicaragua                               | Nicaragua             |   |     191    | Swaziland                                     | Swaziland                                     |   |            |                                           |                          |
  |     48     | Côte d’Ivoire                                      | Côte d'Ivoire                  |   |     96     | מדינת ישראל                               | Israel                         |   |     144    | Niger                                   | Niger                 |   |     192    | Sverige                                       | Sweden                                        |   |            |                                           |                          |

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

## Instruction Types

| Value |     Encoding     |
|:-----:|:----------------:|
| 0     | Left             |
| 1     | Right            |
| 2     | Sharp left       |
| 3     | Sharp right      |
| 4     | Slight left      |
| 5     | Slight right     |
| 6     | Straight         |
| 7     | Enter roundabout |
| 8     | Exit roundabout  |
| 9     | U-turn           |
| 10    | Goal             |
| 11    | Depart           |
| 12    | Keep left        |
| 13    | Keep right       |

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


