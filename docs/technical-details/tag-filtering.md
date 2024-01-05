# Tag Filtering

During the graph build process, openrouteservice looks at various tags that are given to OSM ways as a means of identifying whether that way should be included into the routing graph or not. For example, in the foot profile any ways that are marked as `highway=motorway` are rejected and not included in the graph, meaning that they can never be routed over (for that profile).

The following tables list what tags are taken into account during the initial filtering process. `Reject` means that the tag value indicates that the way is explicitly rejected form the graph building, `Accept` means that the tag indicates that the way should be included, and `Conditional` means that the tag is taken into account during the filtering process, but the acceptance/rejection is based on other tags and logic.

**All ways are initially rejected from the graph building unless criteria for acceptance has been met!**

**The order of items is important, as in many cases a more generic "catch" is introduced towards the end of processing if it has not already been explicitly rejected/accepted.**

Notation:  
_italic words_ are variables defined for the respective section and group several tags.  
`monospace code` are tags from openstreetmap.  
`[square brackets]` denote a range of possible tags.  
`*` denotes any tag, `key != *` denotes the absence of `key`.

 
## Driving car :car:

[//]: # (TODO: :car: not rendered?)

Definitions:  
_restrictedValues_ = `[private, agricultural, forestry, no, restricted, delivery, military, emergency]`  
_intendedValues_ = `[yes, permissive, destination]`  
_restrictions_ = `[motorcar, motor_vehicle, vehicle, access]`  
_firstValue_ = value of the first encountered key from _restrictions_

| Tag combination | Reject | Accept | Conditional |
| --------------- |:------:|:------:|:-----------:|
| `highway != *` AND `route != [shuttle_train, ferry]` | :heavy_check_mark: | | |
| `highway != *` AND `route = [shuttle_train, ferry]` AND _firstValue_ = _restrictedValues_ | :heavy_check_mark: | | |
| `highway != *` AND `route = [shuttle_train, ferry]` AND ( _firstValue_ = _intendedValues_ OR _firstValue_ != `*` AND `foot != *` AND `bicycle != *` ) | | :heavy_check_mark: | |
| `highway = track` AND `tracktype = *` | | | :heavy_check_mark: |
| `highway != [motorway, motorway_link, motorroad, trunk, trunk_link, primary, primary_link, secondary, secondary_link, tertiary, tertiary_link, unclassified, residential, living_street, service, road, track]` | :heavy_check_mark: | | |
| `impassable = yes` OR `[status, smoothness] = impassable` | :heavy_check_mark: | | |
| _firstValue_ = _restrictedValues_ | :heavy_check_mark: | | |
| _firstValue_ = _intendedValues_ | | :heavy_check_mark: | |
| `highway = ford` OR `ford = *` | | | :heavy_check_mark: |
| `maxwidth < 2` | :heavy_check_mark: | | |

## Driving HGV :truck: :bus: 🚜

[//]: # (TODO: :truck: and :bus: not rendered?)

The profile differs from the above logic for driving car in the definitions of  

_restrictedValues_ = `[private, no, restricted, military]`  
_intendedValues_ = `[yes, permissive, designated, destination, hgv, goods, bus, agricultural, forestry, delivery]`

and the following addition rule:


| Tag combination | Reject | Accept | Conditional |
| --------------- |:------:|:------:|:-----------:|
| _restrictions_=_restrictedValues_ AND !(_restrictions_=_intendedValues_) AND !([hgv, goods, bus, agricultural, forestry, delivery]=_intendedValues_) | :heavy_check_mark: | | |

## cycling-regular :bike: [cycling-electric, cycling-road & cycling-mountain]

Definitions:  
_restrictions_ = `[bicycle, vehicle, access]`  
_restrictedValues_ = `[private, no, restricted, military, emergency]`  
_intendedValues_ = `[yes, designated, official, permissive]`

| Tag combination                                                                                                                                                                                                                      |       Reject       |       Accept       |    Conditional     |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------:|:------------------:|:------------------:|
| (`man_made = pier` OR `railway = platform` OR ( `route = [shuttle_train, ferry]` AND (`bicycle = yes` OR ( `bicycle != *` AND `foot != *`)))) AND !(_restrictions_ = _restrictedValues_)                                             |                    | :heavy_check_mark: |                    |
| `highway != [cycleway, path, footway, pedestrian, track, service, residential, living_street, steps, unclassified, road, trunk, trunk_link, primary, primary_link, secondary, secondary_link, tertiary, tertiary_link]`              | :heavy_check_mark: |                    |                    |
| `sac_scale = hiking` AND `highway = cycleway`                                                                                                                                                                                        |                    | :heavy_check_mark: |                    |
| _cycling-regular, cycling-electric:_<br/>`sac_scale != hiking`<br/><br/>_cycling-road:_<br/>`sac_scale = *`<br/><br/>_cycling-mountain:_<br/>`sac_scale != [hiking, mountain_hiking, demanding_mountain_hiking, alpine_hiking]`<br/> | :heavy_check_mark: |                    |                    |
| `bicycle = `_intendedValues_ OR `bicycle = dismount` OR `highway = cycleway` OR `bicycle_road = yes`                                                                                                                                 |                    | :heavy_check_mark: |                    |
| `highway = [motorway, motorway_link]` OR `motorroad = yes`                                                                                                                                                                           | :heavy_check_mark: |                    |                    |
| `highway = ford` OR `ford = *`                                                                                                                                                                                                       |                    |                    | :heavy_check_mark: |
| _restrictions_ = _restrictedValues_                                                                                                                                                                                                  | :heavy_check_mark: |                    |                    |
| ELSE `* = *`                                                                                                                                                                                                                         |                    | :heavy_check_mark: |                    |


## Foot

| Tag combination | Reject | Accept | Conditional |
| --------------- |:------:|:------:|:-----------:|
| `sac_scale != hiking` | :heavy_check_mark: | | |
| `foot = [yes, designated, official, permissive]` | | :heavy_check_mark: | |
| `foot = [private, no, restricted, military, emergency]` | | | :heavy_check_mark: |
| `access = [private, no, restricted, military, emergency]` | | | :heavy_check_mark: |
| `sidewalk = [yes, both, left, right]` | | :heavy_check_mark: | |
| `highway != [footway, path, steps, pedestrian, living_street, track, residential, service, trunk, trunk_link, primary, primary_link, secondary, secondary_link, tertiary, tertiary_link, cycleway, unclassified, road]` | :heavy_check_mark: | | |
| `motorroad = yes` | :heavy_check_mark: | | |
| `highway = ford` OR `ford = *` | | | :heavy_check_mark: |

The following are applicable only when no highway tag has been provided for the way

| Tag combination | Reject | Accept | Conditional |
| --------------- |:------:|:------:|:-----------:|
| `route = [shuttle_train, ferry]` AND (`foot != *` OR `foot = yes`) | | :heavy_check_mark: | |
| `railway = platform` | | :heavy_check_mark: | |
| `man_made = pier` | | :heavy_check_mark: | |

## Hiking

| Tag combination | Reject | Accept | Conditional |
| --------------- |:------:|:------:|:-----------:|
| `sac_scale != [hiking, mountain_hiking, demanding_mountain_hiking, alpine_hiking]` | :heavy_check_mark: | | |
| `foot = [yes, designated, official, permissive]` | | :heavy_check_mark: | |
| `foot = [private, no, restricted, military, emergency]` | | | :heavy_check_mark: |
| `access = [private, no, restricted, military, emergency]` | | | :heavy_check_mark: |
| `sidewalk = [yes, both, left, right]` | | :heavy_check_mark: | |
| `highway != [footway, path, steps, pedestrian, living_street, track, residential, service, trunk, trunk_link, primary, primary_link, secondary, secondary_link, tertiary, tertiary_link, cycleway, unclassified, road]` | :heavy_check_mark: | | |
| `motorroad = yes` | :heavy_check_mark: | | |
| `highway = ford` OR `ford = *` | | | :heavy_check_mark: |

The following are applicable only when no highway tag has been provided for the way

| Tag combination | Reject | Accept | Conditional |
| --------------- |:------:|:------:|:-----------:|
| `route = [shuttle_train, ferry]` AND (`foot != *` OR `foot = yes`) | | :heavy_check_mark: | |
| `railway = platform` | | :heavy_check_mark: | |
| `man_made = pier` | | :heavy_check_mark: | |

## Wheelchair

| Tag combination | Reject | Accept | Conditional |
| --------------- |:------:|:------:|:-----------:|
| `motorroad = yes` | :heavy_check_mark: | | |
| `wheelchair = [yes, designated, official, permissive, limited]` | | :heavy_check_mark: | |
| `wheelchair = [private, no, restricted]` | :heavy_check_mark: | | |
| `highway = steps` | :heavy_check_mark: | | |
| `foot = [yes, designated, official, permissive, limited]` | | :heavy_check_mark: | |
| `foot = [private, no, restricted]` | :heavy_check_mark: | | |
| `sac_scale = *` | :heavy_check_mark: | | |
| `sidewalk = [yes, both, left, right]` | | :heavy_check_mark: |
| `sidewalk = [no, none, separate, detached]` AND `highway = [trunk, trunk_link, primary, primary_link, secondary, secondary_link, tertiary, tertiary_link, road]` | :heavy_check_mark: | | |
| `highway = ford` OR `ford = *` | | | :heavy_check_mark: |
| (`bicycle = [designated, official]` OR `horse = [designated, official]`) AND `[foot, access, wheelchair] = [yes, designated, official, permissive, limited]` | :heavy_check_mark: | | |
| `highway = [bridleway, cycleway]` | | | :heavy_check_mark: |
| `highway = [footway, pedestrian, living_street, residential, unclassified, service, trunk, trunk_link, primary, primary_link, secondary, secondary_link, tertiary, tertiary_link, road path, track]` | | :heavy_check_mark: | |

The following are applicable only when no highway tag has been provided for the way

| Tag combination | Reject | Accept | Conditional |
| --------------- |:------:|:------:|:-----------:|
| `route = [shuttle_train, ferry]` AND `wheelchair = [yes, designated, official, permissive, limited]` | | :heavy_check_mark: | |
| `route = [shuttle_train, ferry]` AND `wheelchair = [private, no, restricted]` | :heavy_check_mark: | | |
| `route = [shuttle_train, ferry]` AND `foot = [yes, designated, official, permissive, limited]` | | :heavy_check_mark: | |
| `route = [shuttle_train, ferry]` AND `foot = [private, no, restricted]` | :heavy_check_mark: | | |
| (`public_transport = platform` OR `railway = platform`) AND `wheelchair = [yes, designated, official, permissive, limited]` | | :heavy_check_mark: | |
| (`public_transport = platform` OR `railway = platform`) AND `wheelchair = [private, no, restricted]` | :heavy_check_mark: | | |
| (`public_transport = platform` OR `railway = platform`) AND `foot = [yes, designated, official, permissive, limited]` | | :heavy_check_mark: | |
| (`public_transport = platform` OR `railway = platform`) AND `foot = [private, no, restricted]` | :heavy_check_mark: | | |
