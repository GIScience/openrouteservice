---
parent: Documentation
nav_order: 2
has_children: true
has_toc: false
title: Travel Speeds
---

# Travel speeds
A main component that determines things such as fastest routes and the travel
time for isochrones and route instructions is travel speed. This value is
determined differently depending on the profile and can be affected by a number
of aspects.

## Travel Time Calculation

The travel time is calculated for each segment by using speed-limits for
different [waytypes](https://wiki.openstreetmap.org/wiki/Key:highway) and
adjusting them for different
[grades](https://wiki.openstreetmap.org/wiki/Key:tracktype) or
[surfaces](https://wiki.openstreetmap.org/wiki/Key:surface) of the road.
If multiple values apply at any point, the lowest value is used.

The speed limits can be reduced by setting the `maximum_speed` parameter in a [request][apidocs].
The final [average speed](../Route-Attributes#avgspeed) can be requested by adding `avgspeed` to the `attributes` parameter.

### Driving profiles (car and HGV)
The base travel speed for any road is based on a cascading assessment:
1. When an tag explicitly stating the speed limit for a road is present on the
   way in OSM (`maxspeed` or `maxspeed:forward / maxspeed:backward`), this is
   used as the base speed.
2. If the way has a `zone:maxspeed` or `zone:traffic` tag, this is compared to
   country-specific values in the `max_speeds` array of the
   [speed value files][svf].
   An overview can be found in the [country speed table](Country-Speeds). 
   This value is then used as the base speed.
3. If neither of the above set a base speed, then the type of way (`highway=`
   tag) determines the base speed. Values are defined in the
   [speed value files][svf].
   The [waytype speed table](Waytype-Speeds) gives an overview.
4. If it is a track (`highway=track`) then the base speed is set based on the
   `tracktype` tag compared to values in the [speed value files][svf].
   The [tracktype speed table](Tracktype-Speeds) gives an overview.

If the determined base speed is one resulting from a `maxspeed`-tag or from a
[country speed table](Country-Speeds) (i.e. Steps 1 or 2), it is reduced to 90%
of its original value, since it is more common that you would be travelling
below the maximum speed value. The [waytype](Waytype-Speeds) and
[tracktype](Tracktype-Speeds) defaults have already been chosen rather
conservatively, so they are not adjusted.

Following that, it is further modified based on a number of other factors:
* If a surface is defined (`surface=*`) then the surface value is set to be the
  corresponding surface type value defined in the [speed value files][svf].
  An overview can be found in the [surface speed table](Surface-Speeds).
* Attempt to take into account reduced speeds in residential areas using
  acceleration modifier or a residential penalty
* cap speed if it is entering a roundabout (based on number of lanes and
  roundabout type - mini roundabout = 25km/h, 1 lane = 35km/h, 2 or more lanes
= 40km/h)

### Bike profiles
Bike profiles determine their speeds based on a number of defaults set in the
code based on surface, highway type, and track type. _Though there is currently
a speed_limits file present in the resources for bike profiles, these values
are not used in the calculation_ 

The speeds used can be found in the [CommonBikeFlagEncoder][cbfe] for the
default values and regular bike profile, [MountainBikeFlagEncoder][mbfe] for
the mountain bike profile, [RoadBikeFlagEncoder][ebfe] for the road bike, and
[ElectroBikeFlagEncoder][ebfe] for the electric bike.

Note that each bike profile has a different value set for the maximum
downhill speed which is calculated when `consider_elevation=true` is set in the
ors-config.json. Currently, this option is turned off for openrouteservice live
servers as it can lead to undesirable routes.

### Walking profiles
The travel speeds for `foot-*` profiles (walking and hiking) are set to
5 km/h on all allowed waytypes.
For ways with a [`sac_scale`](../extra-info/Trail-Difficulty) higher than
`hiking`, they are reduced to 2 km/h.

Allowed waytypes consist of ways that are safe for use, ways that are better
avoided (but still allowed) and other allowed ways in between:

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

### Wheelchair profile
The wheelchair profile has a base speed of 4km/h which is then modified based
on a number of parameters. As such, based on the presence of sidewalks and type
of way, the actual speed can range from 3 to 10km/h

[svf]: https://github.com/GIScience/openrouteservice/tree/master/openrouteservice/src/main/resources/resources/services/routing/speed_limits
[cbfe]: https://github.com/GIScience/openrouteservice/blob/master/openrouteservice/src/main/java/org/heigit/ors/routing/graphhopper/extensions/flagencoders/bike/CommonBikeFlagEncoder.java#L179
[mbfe]: https://github.com/GIScience/openrouteservice/blob/master/openrouteservice/src/main/java/org/heigit/ors/routing/graphhopper/extensions/flagencoders/bike/MountainBikeFlagEncoder.java#L52
[rbfe]: https://github.com/GIScience/openrouteservice/blob/master/openrouteservice/src/main/java/org/heigit/ors/routing/graphhopper/extensions/flagencoders/bike/RoadBikeFlagEncoder.java#L91
[ebfe]: https://github.com/GIScience/openrouteservice/blob/master/openrouteservice/src/main/java/org/heigit/ors/routing/graphhopper/extensions/flagencoders/bike/ElectroBikeFlagEncoder.java#L41
[apidocs]: https://openrouteservice.org/dev/#/api-docs/v2/directions/{profile}/post
