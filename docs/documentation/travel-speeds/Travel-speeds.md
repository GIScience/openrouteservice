---
parent: Documentation
nav_order: 2
has_children: true
title: Travel Speeds
---
[:arrow_backward:  Documentation](Documentation)

# Travel speeds
A main component that determines things such as fastest routes and the travel time for isochrones and route instructions is travel speed. This value is determined differently depending on the profile and can be affected by a number of aspects.

More information about the individual components and assigned speeds can be found in the more detailed [documentation](https://github.com/GIScience/openrouteservice-docs/blob/master/README.md#travel-time-calculation).

# Travel Time Calculation

The travel time is calculated for each segment by using speed-limits for different [waytypes](https://wiki.openstreetmap.org/wiki/Key:highway) and adjusting them for different [grades](https://wiki.openstreetmap.org/wiki/Key:tracktype) or [surfaces](https://wiki.openstreetmap.org/wiki/Key:surface) of the road.
If multiple values apply for this segment, the lowest value is used. For `cycling` profiles also the steepness is considered.
These limits can be reduced by setting the `maxSpeed` parameter in the [options](#routing-options).
The final [average speed-limits](#avgspeed) can be requested by adding `AvgSpeed` to the `extra_info` parameter.
The following table shows the initial speed-limits used for the main profiles:

_(all Values in km/h)_

## Driving profiles (car and HGV)
The base travel speed for any road is based on a cascading assessment:
1. When an tag explicitly stating the speed limit for a road is present on the way in OSM (`maxspeed` or `maxspeed:forward / maxspeed:backward`), this is used as the base speed.
2. If the way has a `zone:maxspeed` or `zone:traffic` tag, this is compared to values in the `max_speeds` array of the [speed value files](https://github.com/GIScience/openrouteservice/tree/master/openrouteservice/src/main/resources/resources/services/routing/speed_limits) and this value used as the base speed.
3. If neither of the above  is met, then the type of way (`highway=` tag) is used as the base speed. Values are defined in the [speed value files](https://github.com/GIScience/openrouteservice/tree/master/openrouteservice/src/main/resources/resources/services/routing/speed_limits).
4. If it is a track (`highway=track`) then set the base speed based on the `tracktype` tag compared to values in the [speed value files](https://github.com/GIScience/openrouteservice/tree/master/openrouteservice/src/main/resources/resources/services/routing/speed_limits).

Once the base speed has been determined, it is firstly modified to be 90% of the base speed as it is more common that you would be travelling below the maximum speed value.

Following that, it is further modified based on a number of other factors:
* If a surface is defined (`surface=*`) then the surface value is set to be the corresponding surface type value defined in the [speed value files](https://github.com/GIScience/openrouteservice/tree/master/openrouteservice/src/main/resources/resources/services/routing/speed_limits).
* Attempt to take into account reduced speeds in residential areas using acceleration modifier or a residential penalty
* cap speed if it is entering a roundabout (based on number of lanes and roundabout type - mini roundabout = 25km/h, 1 lane = 35km/h, 2 or more lanes = 40km/h)

## Bike profiles
Bike profiles determine their speeds based on a number of defaults set in the code based on surface, highway type, and track type. _Though there is currently a speed_limits file present in the resources for bike profiles, these values are not used in the calculation_ 

The speeds used can be found in the [CommonBikeFlagEncoder](https://github.com/GIScience/openrouteservice/blob/a493944655ecb3da6f74d393aa8aebacb116966f/openrouteservice/src/main/java/org/heigit/ors/routing/graphhopper/extensions/flagencoders/bike/CommonBikeFlagEncoder.java#L174) for the default values and regular bike profile, [MountainBikeFlagEncoder](https://github.com/GIScience/openrouteservice/blob/a493944655ecb3da6f74d393aa8aebacb116966f/openrouteservice/src/main/java/org/heigit/ors/routing/graphhopper/extensions/flagencoders/bike/MountainBikeFlagEncoder.java#L53) for the mountain bike profile, [RoadBikeFlagEncoder](https://github.com/GIScience/openrouteservice/blob/a493944655ecb3da6f74d393aa8aebacb116966f/openrouteservice/src/main/java/org/heigit/ors/routing/graphhopper/extensions/flagencoders/bike/RoadBikeFlagEncoder.java#L86) for the road bike, and [ElectroBikeFlagEncoder](https://github.com/GIScience/openrouteservice/blob/a493944655ecb3da6f74d393aa8aebacb116966f/openrouteservice/src/main/java/org/heigit/ors/routing/graphhopper/extensions/flagencoders/bike/ElectroBikeFlagEncoder.java#L42) for the electric bike.

Note that also, each bike profile has a different value set for the maximum downhill speed which is calculated when `consider_elevation=true` is set in the app.config. Currently, this option is turned off for openrouteservice live servers as it can lead to undesirable routes.

## Walking profiles
The travel speed for walking profiles (walking and hiking) are set as constant values. For both it is set to be 5km/h, except for when the way has a `sac_scale` tag other than `hiking` whereby it is reduced to 2km/h. 

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

## Wheelchair profile
The wheelchair profile has a base speed of 4km/h which is then modified based on a number of parameters. As such, based on the presence of sidewalks and type of way, the actual speed can range from 3 to 10km/h

