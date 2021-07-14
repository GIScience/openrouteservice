---
parent: Documentation
nav_order: 11
title: Geocoding Response
---

# Geocoding Response

Explanation of returned parameters

## Place type

Describes the returned location type

  |      Value      |                                           Description                                           |
  |:---------------:|:------------------------------------------------------------------------------------------------|
  |     `venue`     | Points of interest, businesses, things with walls                                               |
  |    `address`    | Places with a street address                                                                    |
  |     `street`    | Streets, roads, highways                                                                        |
  | `neighbourhood` | Social communities, neighbourhoods                                                              |
  |    `borough`    | Local administrative boundary, currently only used for New York City                            |
  |   `localadmin`  | Local administrative boundaries                                                                 |
  |    `locality`   | Towns, hamlets, cities                                                                          |
  |     `county`    | Official governmental area; usually bigger than a locality, almost always smaller than a region |
  |  `macrocounty`  | Related group of counties. Mostly in Europe                                                     |
  |     `region`    | States and provinces                                                                            |
  |  `macroregion`  | Related group of regions. Mostly in Europe                                                      |
  |    `country`    | Places that issue passports, nations, nation-states                                             |
