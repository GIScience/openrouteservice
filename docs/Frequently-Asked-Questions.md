---
title: FAQ
nav_order: 6
has_toc: true
---

# Frequently Asked Questions
 {: .no_toc }

1. TOC
{:toc}

---

## Why is my ors reporting `Could not find point`?

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

2. The given start and endpoint are further than 350m away from any routable
   road. The maximum distance for snapping to road segments in our API is 350m.
 This can be customized for local installations via the
  `maximum_snapping_radius` and `location_index_resolution` config-parameter. See
  [configuration](Installation/Configuration) for details.

3. The start and enpoint are passed with correct lon,lat-order and are within
   350m of a routable road. This should only happen with a local installation.
   Usually, this means that ors is trying to route in an area that graphs have not
   been built for.
   If routes in Heidelberg(Germany) can be found, the ors is still running on the
   default dataset.

---

## When does the OSM data update in the openrouteservice?

The openrouteservice builds its data from the `planet.osm.pbf`-files. According
to [the osm-wiki](https://wiki.openstreetmap.org/wiki/Planet.osm), these files
take two days to build and are updated weekly.

Since the `planet`-files are rather large (currently over 60GB), there is a bit
of work involved to make sure the download went right and the file is not
corrupted in any way and in fact new. Parts of this process are in the hands of
the OSM, parts are done by the openrouteservice.

Once the newest `planet`-file is on the openrouteservice-servers, it needs to
be preprocessed before the openrouteservice can start building the graphs used
for routing.

The build process in itself is [rather
resource-intensive](Installation/System-Requirements). It takes roughly two
days for any one of the nine profiles. For the mentioned resource requirements,
this means that it will take roughly a week for all profiles to be re-built.

Once the graphs are built, the production instances have to load them. Since
this should happen in a low-traffic timeslot, it is also scheduled to happen
once per week.

To sum up: if you change anything in the OSM, it will therefore take roughly a
week until it's included in the `planet`-file. This gets read once a week, the
build takes a week and reloading graphs happens once a week.

If everything aligns as it should, changes should be reflected in the
openrouteservice within two to three weeks.

If, however, anything goes wrong anywhere, this will usually mean a delay of at
least a week, assuming it gets noticed and fixed immediately. It is no sign of
concern, if changes are not reflected within a month.
