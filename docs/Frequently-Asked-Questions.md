---
title: FAQ
nav_order: 3
has_toc: true
---

# Frequently Asked Questions

## Why is my ors reporting `Could not find point`

This is a frequently encountered error message:
```
Could not find point 0: 25.3531986 51.5214311 within a radius of 350.0 meters.;
Could not find point 1: 25.3524229 51.4627229 within a radius of 350.0 meters.
```

There are three main reasons for this problem, listed in order of most to least common.

1. If both points are not found you probably just mixed up Lat and Long. Our
   API expects coordinates in [lon,lat] order as described in our documentation
   (check help button for parameter info). Output is also [lon,lat] as by the
   GeoJSON Specification.

2. The given start and endpoint are further than 350m away from any
   routable road. The maximum distance for snapping to road segments in our API
   is 350m. This can be customized for local installations via the TODO-parameter.

3. The start and enpoint are passed with correct lon,lat-order and are within
   350m of a routable road. This should only happen with a local installation.
   Usually, this means that ors is trying to route in an area that graphs have not
   been built for.
   If routes in Heidelberg(Germany) can be found, the ors is still running on the
   default dataset.

## How much RAM does building `file.pbf` need?


Here are a few examples of RAM usage for different `.pbf`-files on a machine with an Intel i7-6600U (2x2.6GHz):

| PBF-File                      | Size   | RAM used |
|:-----------------------------:|:------:|----------------------|
| berlin-latest.osm.pbf         |  67 Mb | 353 MB, max: 4.8GB used: 3.2GB
| tennessee-latest.osm.pbf      | 120 Mb |
| north-carolina-latest.osm.pbf | 293 Mb |
| spain-latest.osm.pbf          | 948 Mb |

## How long does it take to build `file.pbf`?

On a machine with an Intel i7-6600U (2x2.6GHz), it will take around 40 to 50 seconds to compile ors and start building graphs.
This time is **not** included in the following table. Note, that graphs for all nine profiles were being built.

| PBF-File                      | Size   | Build duration (min:sec) |
|:-----------------------------:|:------:|--------------------------|
| berlin-latest.osm.pbf         |  67 Mb | 09:58
| tennessee-latest.osm.pbf      | 120 Mb |
| north-carolina-latest.osm.pbf | 293 Mb |
| spain-latest.osm.pbf          | 948 Mb |


## 
