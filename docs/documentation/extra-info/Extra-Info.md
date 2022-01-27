---
parent: Documentation
nav_order: 6
has_children: true
has_toc: false
title: Extra Info 
---

# Extra Info

When requesting routes, there are a number of "extra info" items that can be requested to give you more information about the route. This info could be things like the road surface, track type, or OpenStreetMap way ID. The list below details which extra info items are available for each profile **in the routing provided by https://api.openrouteservice.org**.

| Parameter | Description | Code Information |
|:---------:|:------------|:-----------------|
| steepness | Provides information about how steep parts of the route are | [Steepness IDs](Steepness)
| suitability | How suitable the way is based on characteristics of the route and the profile | 1 (unsuitable) - 10 (very suitable)
| surface | The surface covering along the route | [Surface IDs](Surface)
| waycategory | Specific categories of parts of the route (tollways, highways, fords etc.) | [Category IDs](Waycategory)
| waytype | Types of roads and paths that are used in the route | [Type IDs](Waytype)
| tollways  | Any tollways that the route crosses. Whether a way is marked as [tollway](https://wiki.openstreetmap.org/wiki/Key:toll) depends on `profile` | 0 (no tollway) or 1 (tollway) |
| traildifficulty | The difficulty of parts of the way based on sac or mountainbike scales | [Difficulty IDs](Trail-Difficulty)  |
| osmid | The OpenStreetMap way IDs of the ways the route uses |   |
| roadaccessrestrictions | Information about ways that may have access restrictions (e.g. private roads, destination only) | [Restrictions IDs](Road-Access-Restrictions) |
| countryinfo | Which country parts of the way lies in | [Country IDs](../routing-options/Country-List)
| green | How "green" the parts of the route are (influenced by things like number of trees, parks, rivers etc.) | 0 (minimal greenspace) - 10 (a lot of green space) |
| noise | How noisy the parts of the route are (influenced by things like proximity to highways) | 0 (quiet) - 10 (noisy) |

# Extra Info Availability

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
