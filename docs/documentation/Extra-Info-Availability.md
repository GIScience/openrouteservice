---
parent: Documentation
nav_order: 4
title: Extra Info Availability
---
[:arrow_backward:  Documentation](Documentation)

# Extra Info Availability

When requesting routes, there are a number of "extra info" items that can be requested to give you more information about the route. This info could be things like the road surface, track type, or OpenStreetMap way ID. The list below details which extra info items are available for each profile **in the routing provided by https://api.openrouteservice.org**.

| Parameter | Description | Code Information |
|:---------:|:------------|:-----------------|
| steepness | Provides information about how steep parts of the route are | [Steepness IDs](https://github.com/GIScience/openrouteservice-docs#steepness)
| suitability | How suitable the way is based on characteristics of the route and the profile | 1 (unsuitable) - 10 (very suitable)
| surface | The surface covering along the route | [Surface IDs](https://github.com/GIScience/openrouteservice-docs#surface)
| waycategory | Specific categories of parts of the route (tollways, highways, fords etc.) | [Category IDs](https://github.com/GIScience/openrouteservice-docs#waycategory)
| waytype | Types of roads and paths that are used in the route | [Type IDs](https://github.com/GIScience/openrouteservice-docs#waytype)
| tollways  | Any tollways that the route crosses | 0 (no tollway) or 1 (tollway) |
| traildifficulty | The difficulty of parts of the way based on sac or mountainbike scales | [Difficulty IDs](https://github.com/GIScience/openrouteservice-docs#trail-difficulty)  |
| osmid | The OpenStreetMap way IDs of the ways the route uses |   |
| roadaccessrestrictions | Information about ways that may have access restrictions (e.g. private roads, destination only) | [Restrictions IDs](https://github.com/GIScience/openrouteservice-docs#road-access-restrictions) |
| countryinfo | Which country parts of the way lies in | [Country IDs](https://github.com/GIScience/openrouteservice-docs#country-list)
| green | How "green" the parts of the route are (influenced by things like number of trees, parks, rivers etc.) | 0 (minimal greenspace) - 10 (a lot of green space) |
| noise | How noisy the parts of the route are (influenced by things like proximity to highways) | 0 (quiet) - 10 (noisy) |



|      |    steepness  | suitability  | surface  | waycategory  | waytype | tollways | traildifficulty | osmid | roadaccessrestrictions | countryinfo | green | noise |
|:-----------:|:----------------:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
| driving-car | x | x | x | x | x | x | x |   | x | x |   |   |
| driving-hgv | x | x | x | x | x | x | x |   | x | x |   |   |
| cycling-regular | x | x | x | x | x |   | x |   |   |   |   |   |
| cycling-mountain | x | x | x | x | x |   | x |   |   |   |   |   |
| cycling-road | x | x | x | x | x |   | x |   |   |   |   |   |
| foot-walking | x | x | x | x | x |   | x |   |   |   | x | x |
| foot-hiking | x | x | x | x | x |   | x |   |   |   | x | x |
| wheelchair | x | x | x | x | x |   | x | x |   |   |   |   |

## Suitability

The suitability values for the selected profile range from ``10`` for best suitability to ``1`` for worst suitability.



## AvgSpeed

This value is in _km/h_ and equals the average speed for this way segment after grading and applying factors.


## Tollways

Tollway specific information for the [selected mode of
transport](https://en.wikipedia.org/wiki/Vehicle_category). Thus it depends on
the `profile` if a ways is marked as
[tollway](https://wiki.openstreetmap.org/wiki/Key:toll). 

| Value |  Encoding  |
|:-----:|:----------:|
|   0   | no tollway |
|   1   | is tollway |




