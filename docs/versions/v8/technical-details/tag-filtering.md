# Tag Filtering

During the graph build process, openrouteservice looks at various tags that are given to OSM ways as a means of identifying whether that way should be included into the routing graph or not. For example, in the foot profile any ways that are marked as `highway=motorway` are rejected and not included in the graph, meaning that they can never be routed over (for that profile).

The following tables list what tags are taken into account during the initial filtering process.
`Reject` means that the tag value indicates that the way is explicitly rejected from graph building.
`Accept` means that the tag indicates that the way should be included.
`Conditional` means that the tag is taken into account during the filtering process,
but the acceptance/rejection is based on other tags and logic, such as the [`:conditional` suffix](https://wiki.openstreetmap.org/wiki/Conditional_restrictions).

Support of temporal restrictions is limited to seasonal access restrictions, such as `vehicle:conditional=no @ (Nov 1-Mar 31)`. These restrictions are being resolved at graph build time.
Other time-dependent conditional restrictions are currently not being taken into account. This means, in particular, that ways which are regularly open but conditionally closed, or vice versa, remain accepted or rejected, respectively.

**The order of items is important, as in many cases a more generic "catch" is introduced towards the end of processing if it has not already been explicitly rejected/accepted.**

Notation:  
_italic words_ are variables defined for the respective section and group several tags.  
`monospace code` are tags from OpenStreetMap.  
`[square brackets]` denote a range of possible tags.  
`*` denotes any tag, `key != *` denotes the absence of `key`.

 
## Driving car

Definitions:  
_restrictedValues_ = `[private, agricultural, forestry, no, restricted, delivery, military, emergency]`  
_intendedValues_ = `[yes, permissive, destination]`  
_restrictions_ = `[motorcar, motor_vehicle, vehicle, access]`  
_firstValue_ = value of the first encountered key from _restrictions_  
_wayTypesWithDefaultSpeed_ = `[motorway, motorway_link, motorroad, trunk, trunk_link, primary, primary_link, secondary, secondary_link, tertiary, tertiary_link, unclassified, residential, living_street, service, road, track]`  

| Tag combination                                                                                                                                       |       Reject       |       Accept       |    Conditional     |
|-------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------:|:------------------:|:------------------:|
| `highway != *` AND `route != [shuttle_train, ferry]`                                                                                                  | :heavy_check_mark: |                    |                    |
| `highway != *` AND `route = [shuttle_train, ferry]` AND _firstValue_ = _restrictedValues_                                                             | :heavy_check_mark: |                    |                    |
| `highway != *` AND `route = [shuttle_train, ferry]` AND ( _firstValue_ = _intendedValues_ OR _firstValue_ != `*` AND `foot != *` AND `bicycle != *` ) |                    | :heavy_check_mark: |                    |
| `highway = track` AND `tracktype > grade3`                                                                                                            | :heavy_check_mark: |                    |                    |
| `highway !=` _wayTypesWithDefaultSpeed_                                                                                                               | :heavy_check_mark: |                    |                    |
| `impassable = yes` OR `[status, smoothness] = impassable`                                                                                             | :heavy_check_mark: |                    |                    |
| _firstValue_ = _restrictedValues_                                                                                                                     |                    |                    | :heavy_check_mark: |
| _firstValue_ = _intendedValues_                                                                                                                       |                    | :heavy_check_mark: |                    |
| `highway = ford` OR `ford = *`                                                                                                                        |                    |                    | :heavy_check_mark: |
| `maxwidth < 2`                                                                                                                                        | :heavy_check_mark: |                    |                    |
| Default case: Accept **if not conditionally restricted**                                                                                              |                    |                    | :heavy_check_mark: |

## Driving HGV

The profile differs from the above logic for driving car in the definitions of  

Definitions:  
_restrictedValues_ = `[private, no, restricted, military]`  
_intendedValues_ = `[yes, permissive, designated, destination, hgv, goods, bus, agricultural, forestry, delivery]`  
_restrictions_ = `[hgv, motorcar, motor_vehicle, vehicle, access]`  

and the following additional rule which replace the check for `ford`:

| Tag combination                                                                                                                                        |       Reject       | Accept |    Conditional     |
|--------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------:|:------:|:------------------:|
| !(_restrictions_=_intendedValues_) AND (`highway = ford` OR `ford = *`)                                                                                |                    |        | :heavy_check_mark: |
| !(_restrictions_=_intendedValues_) AND _restrictions_=_restrictedValues_ AND !(`[hgv, goods, bus, agricultural, forestry, delivery]`=_intendedValues_) | :heavy_check_mark: |        |                    |
| Default case: Accept **if not conditionally restricted**                                                                                               |                    |        | :heavy_check_mark: |

## cycling-regular [cycling-electric, cycling-road & cycling-mountain]

Definitions:  
_restrictions_ = `[bicycle, vehicle, access]`  
_restrictedValues_ = `[private, no, restricted, military, emergency]`  
_intendedValues_ = `[yes, designated, official, permissive]`  
_wayTypesWithDefaultSpeed_ = `[unclassified, tertiary_link, primary_link, bridleway, tertiary, living_street, trunk, motorway_link, steps, motorway, secondary, path, residential, road, service, footway, pedestrian, track, secondary_link, cycleway, trunk_link, primary]`  

| Tag combination                                                                                                                                                                                                                      |       Reject       |       Accept       |    Conditional     |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------:|:------------------:|:------------------:|
| `highway != *` AND (`man_made = pier` OR `railway = platform` OR ( `route = [shuttle_train, ferry]` AND (`bicycle = yes` OR ( `bicycle != *` AND `foot != *`)))) AND !(_restrictions_ = _restrictedValues_)                          |                    | :heavy_check_mark: |                    |
| `highway !=` _wayTypesWithDefaultSpeed_                                                                                                                                                                                              | :heavy_check_mark: |                    |                    |
| `sac_scale = hiking` AND `highway = cycleway`                                                                                                                                                                                        |                    | :heavy_check_mark: |                    |
| _cycling-regular, cycling-electric:_<br/>`sac_scale != hiking`<br/><br/>_cycling-road:_<br/>`sac_scale = *`<br/><br/>_cycling-mountain:_<br/>`sac_scale != [hiking, mountain_hiking, demanding_mountain_hiking, alpine_hiking]`<br/> | :heavy_check_mark: |                    |                    |
| `bicycle = `_intendedValues_ OR `bicycle = dismount` OR `highway = cycleway` OR `bicycle_road = yes`                                                                                                                                 |                    | :heavy_check_mark: |                    |
| `highway = [motorway, motorway_link, bridleway]` OR `motorroad = yes`                                                                                                                                                                | :heavy_check_mark: |                    |                    |
| `highway = ford` OR `ford = *`                                                                                                                                                                                                       |                    |                    | :heavy_check_mark: |
| _restrictions_ = _restrictedValues_ **if not conditionally permitted**                                                                                                                                                               | :heavy_check_mark: |                    |                    |
| Default case: Accept **if not conditionally restricted**                                                                                                                                                                             |                    |                    | :heavy_check_mark: |


## Foot

Definitions:  
_restrictions_ = `[foot, access]`  
_restrictedValues_ = `[private, no, restricted, military, emergency]`  
_intendedValues_ = `[yes, designated, permissive, official]`  

| Tag combination                                                                                                                                                                                                         |       Reject       | Accept |    Conditional     |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------:|:------:|:------------------:|
| `sac_scale != hiking`                                                                                                                                                                                                   | :heavy_check_mark: |        |                    |
| `foot = ` _intendedValues_                                                                                                                                                                                              |                    |        | :heavy_check_mark: |
| _restrictions_ = _restrictedValues_                                                                                                                                                                                     |                    |        | :heavy_check_mark: |
| `sidewalk = [yes, both, left, right]`                                                                                                                                                                                   |                    |        | :heavy_check_mark: |
| `highway != [footway, path, steps, pedestrian, living_street, track, residential, service, trunk, trunk_link, primary, primary_link, secondary, secondary_link, tertiary, tertiary_link, cycleway, unclassified, road]` | :heavy_check_mark: |        |                    |
| `motorroad = yes`                                                                                                                                                                                                       | :heavy_check_mark: |        |                    |
| `highway = ford` OR `ford = *`                                                                                                                                                                                          |                    |        | :heavy_check_mark: |
| Default case: Accept **if not conditionally restricted**                                                                                                                                                                |                    |        | :heavy_check_mark: |


The following are applicable only when no highway tag has been provided for the way

| Tag combination                                                               |       Reject       |       Accept       |    Conditional     |
|-------------------------------------------------------------------------------|:------------------:|:------------------:|:------------------:|
| `route = [shuttle_train, ferry]` AND (`foot != *` OR `foot = yes`)            |                    | :heavy_check_mark: |                    |
| `railway = platform`                                                          |                    | :heavy_check_mark: |                    |
| `man_made = pier`                                                             |                    | :heavy_check_mark: |                    |
| `waterway = lock_gate` AND `foot =` _intendedValues_                          |                    | :heavy_check_mark: |                    |
| _restrictions_ = _restrictedValues_ Reject **if not conditionally permitted** |                    |                    | :heavy_check_mark: |
| Default case:                                                                 | :heavy_check_mark: |                    |                    |


## Hiking

Same as [Foot](#foot) except for different `sac_scale` check with existing `highway` tag.

| Tag combination                                                                    |       Reject       | Accept | Conditional |
|------------------------------------------------------------------------------------|:------------------:|:------:|:-----------:|
| `sac_scale != [hiking, mountain_hiking, demanding_mountain_hiking, alpine_hiking]` | :heavy_check_mark: |        |             |


## Wheelchair

Definitions:  
_restrictions_ = `[foot, wheelchair, access]`  
_restrictedValues_ = `[private, no, restricted, military, emergency]`  
_intendedValues_ = `[yes, limited, designated, permissive, official]`  

The following check is done first, regardless of the `highway` value:
| Tag combination                                                                                                                                                                                      |       Reject       |       Accept       |    Conditional     |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------:|:------------------:|:------------------:|
| _restrictions_ = _restrictedValues_ AND _restrictions_ != _intendedValues_ AND `sidewalk != [yes, both, left, right]`                                                                                | :heavy_check_mark: |                    |                    |

If `highway` is present on the way:

| Tag combination                                                                                                                                                                                                  |       Reject       |       Accept       |    Conditional     |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------:|:------------------:|:------------------:|
| `sac_scale = [mountain_hiking, demanding_mountain_hiking, alpine_hiking, demanding_alpine_hiking, difficult_alpine_hiking]`                                                                                      | :heavy_check_mark: |                    |                    |
| `surface = [ sand, salt, grass, snow, earth, dirt, ice, mud]`                                                                                                                                                    | :heavy_check_mark: |                    |                    |
| `smoothness = [bad, very_bad, horrible, very_horrible]`                                                                                                                                                          | :heavy_check_mark: |                    |                    |
| `tracktype = [grade4, grade5]`                                                                                                                                                                                   | :heavy_check_mark: |                    |                    |
| `wheelchair =` _intendedValues_                                                                                                                                                                                  |                    | :heavy_check_mark: |                    |
| `wheelchair =` _restrictedValues_                                                                                                                                                                                | :heavy_check_mark: |                    |                    |
| `highway = [steps, construction]`                                                                                                                                                                                | :heavy_check_mark: |                    |                    |
| `foot =` _intendedValues_                                                                                                                                                                                        |                    | :heavy_check_mark: |                    |
| `foot =` _restrictedValues_                                                                                                                                                                                      | :heavy_check_mark: |                    |                    |
| `sidewalk = [yes, both, left, right]`                                                                                                                                                                            |                    | :heavy_check_mark: |                    |
| `sidewalk = [no, none, separate]`                                                                                                                                                                                | :heavy_check_mark: |                    |                    |
| `motorroad = yes`                                                                                                                                                                                                | :heavy_check_mark: |                    |                    |
| `highway = ford` OR `ford = *`                                                                                                                                                                                   |                    |                    | :heavy_check_mark: |
| `highway = bridleway` AND `foot !=` _intendedValues_ AND `wheelchair !=` _intendedValues_                                                                                                                        | :heavy_check_mark: |                    |                    |
| `highway = [footway, pedestrian, living_street, residential, unclassified, service, tertiary, tertiary_link, road, trunk, trunk_link, primary, primary_link, secondary, secondary_link, path, track, bridleway]` |                    | :heavy_check_mark: |                    |
| Default case:                                                                                                                                                                                                    |                    | :heavy_check_mark: |                    |

The following are applicable only when no `highway` tag has been provided for the way

| Tag combination                                                                               |       Reject       |       Accept       | Conditional |
|-----------------------------------------------------------------------------------------------|:------------------:|:------------------:|:-----------:|
| `route = [shuttle_train, ferry]` AND `wheelchair =` _intendedValues_                          |                    | :heavy_check_mark: |             |
| `route = [shuttle_train, ferry]` AND `wheelchair =` _restrictedValues_                        | :heavy_check_mark: |                    |             |
| `route = [shuttle_train, ferry]` AND `foot =` _intendedValues_                                |                    | :heavy_check_mark: |             |
| `route = [shuttle_train, ferry]` AND `foot =` _restrictedValues_                              | :heavy_check_mark: |                    |             |
| (`public_transport = platform` OR `railway = platform`) AND `wheelchair =` _intendedValues_   |                    | :heavy_check_mark: |             |
| (`public_transport = platform` OR `railway = platform`) AND `wheelchair =` _restrictedValues_ | :heavy_check_mark: |                    |             |
| (`public_transport = platform` OR `railway = platform`) AND `foot =` _intendedValues_         |                    | :heavy_check_mark: |             |
| (`public_transport = platform` OR `railway = platform`) AND `foot =` _restrictedValues_       | :heavy_check_mark: |                    |             |
| Default case:                                                                                 | :heavy_check_mark: |                    |             |
