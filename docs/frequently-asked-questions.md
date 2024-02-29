---
title: FAQ
---

# Frequently Asked Questions

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
  [configuration](/run-instance/configuration/ors/engine/profiles.md) for details.

3. The start and endpoint are passed with correct lon,lat-order and are within
   350m of a routable road. This should only happen with a local installation.
   Usually, this means that ors is trying to route in an area that graphs have not
   been built for.
   If routes in Heidelberg(Germany) can be found, the ors is still running on the
   default dataset.

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
resource-intensive](/run-instance/system-requirements.md). It takes roughly two
days for any one of the nine profiles. For the mentioned resource requirements,
this means that it will take roughly a week for all profiles to be re-built.

Once the graphs are built, the production instances have to load them. Since
this should happen in a low-traffic time slot, it is also scheduled to happen
once per week.

To sum up: if you change anything in the OSM, it will therefore take roughly a
week until it's included in the `planet`-file. This gets read once a week, the
build takes a week and reloading graphs happens once a week.

If everything aligns as it should, changes should be reflected in the
openrouteservice within two to three weeks.

If, however, anything goes wrong anywhere, this will usually mean a delay of at
least a week, assuming it gets noticed and fixed immediately. It is no sign of
concern, if changes are not reflected within a month.

## I get an Error `Native memory allocation (mmap) failed to map 16384 bytes for committing reserved memory`

See memory mapping section in [system requirements](/run-instance/system-requirements.md#memory-mapping-in-large-builds-with-a-containerized-openrouteservice-instance).

## Why does routing not work when there is clearly a road?

There are a lot of reasons why routing is seemingly "wrong" since
roads are there that are not being used. In many cases, this is a data issue more than a routing issue.
A very common occurrence has to do with `barrier=*`-nodes in the OSM.

This issue most commonly manifests itself in two ways:

1. Ferries are not taken, although they exist and should be faster/shorter
2. Roads in residential areas are not accessible from the outside.

In both cases, there might be a node with a `barrier=*`-tag on the roads accessing the ferry port or the residential area.
The corresponding barriers are made to disallow unauthorized access.
Thus, if no more information than `barrier=*` is given, the openrouteservice will not route over them.

This behaviour is often misinterpreted as _wrong_ since in many cases, the barrier is passable by default or a ticket can be purchased.
While often obvious to a human looking at the map, the openrouteservice can not know that.
Routing over such a barrier would be an assumption that the openrouteservice will not make.

[A lot](https://wiki.openstreetmap.org/wiki/Key:access#List_of_possible_values)
of [options](https://wiki.openstreetmap.org/wiki/Key:locked) exist to
[enable](https://wiki.openstreetmap.org/wiki/Tag:access%3Ddestination) routing,
but they have to be made in the data, not in the routing engine.

## Why is the response time for matrix requests in my own instance so much higher than in the live API
Depending on the parameters of your request, the openrouteservice will use
different routing algorithms with different preparations to calculate an
answer.

For matrix calculations, the ors uses the very fast RPHAST algorithm, which is
based on so-called _Contraction Hierarchies_ (CH for short).  While the usage
of CH speeds up matrix calculation by a lot, preparing them is rather costly.
Thus, they are not calculated by default, but have to be turned on manually.
Documentation on how to do that can be found [here](https://giscience.github.io/openrouteservice/run-instance/configuration/ors/engine/profiles#methods-ch).

If CH have not been prepared, matrix calculation will fall back to the Dijkstra
algorithm, which is way slower and responsible for the slow response.

## When and how does my quota reset?
When you check our [plans](https://openrouteservice.org/plans/), you'll see
that our endpoints have a daily and a minutely request limit.

All remaining quota is shown in your [developer dashboard](https://openrouteservice.org/dev/#/home).

The daily limit is reset after 24h, starting from the first time you request
anything. Thus, your 24h-window might shift over the days.

As an example, let's look at the `directions` endpoint and its default limit of
2000 requests per day.  If you request 1000 directions at 3pm today and try to
request 1500 tomorrow at 10am, the last 500 of those will fail, since your
quota only resets at 3pm tomorrow. After 3pm, you can issue up to 2000 requests
again, but if you do so at 6pm, you'll have to wait until 6pm the day after
tomorrow until you can request any more directions.

The minutely limit is enforced as a sliding window, meaning that any
consecutive period of 60 seconds may only contain 40 directions requests.

If you run into the daily limit, you will receive a `403 - Forbidden` HTTP error.
If you run into the minutely limit, you will receive a `429 - Too many requests` HTTP error.
The remaining daily quota can also be checked programmatically, compare the `x-ratelimit-remaining` and the `x-ratelimit-reset` header.
