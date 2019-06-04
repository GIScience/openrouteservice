# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
### Fixed
### Changed
### Deprecated

## [5.0.1] - 2019-04-08
### Added
- CALT routing algorithm - Not for production (Issue #433)
- Makes docker and docker-compose deployment of openrouteservice more customizable (Issue #434)
- Add the possibility to predefine standard maximum search radii in general and for each used profile in the config file (Issue #418)
### Fixed
- fix the GPX output of the APIv2. It was broken since release of api v2. (Issue #533)
- fix SRTM URL in GH fork (#394)
- fix classpath issues for resources, Windows builds now (#489)
- isochrone geojson bbox now format compliant (#493)
- v2 isochrones now respects max_locations in app.config (#482)
- Updated documentation to reflect correct isochrone smoothing algorithm (Issue #471)
- Enable > 2 waypoints when geometry_simplify=true (#457)
- Made it so that the wheelchair profile only goes over bridleways if they are set to be foot or wheelchair accessible (#415)
- Fixed the build fail bug when `routing_name` was set in the config file (#424)
- Fixed problem with border crossings where the way crosses three polygons (#491)
### Changed
- Updated pom to always build ors.war (Issue #432)
- Replace usage of packages incompatible with Java >8 (#474)
- Updated Matrix to have a maximum number of routes to calculate rather than locations (#518)
### Deprecated


## [5.0] - 2019-02-25
### Added
- Updated api code to use the Spring framework, with the v2 api being added (Issue #233)
- Added support for ISO 3166-1 Alpha-2 / Alpha-3 codes for routing directions option avoid_countries (Issue #195)
- Added support for free hand route option/ skip segments (Issue #167)
- Added check on matrix service to make sure that the requested locations are within the bounding area of the graph (Issue #408)
- Makes docker and docker-compose deployment of openrouteservice more customizable (Issue #434)
### Fixed
- Fixed `geometry_simplify` parameter, which had no effect before. `geometry_simplify` is incompatible with `extra_info` (#381)
### Changed
- Updated rural speed limit in France to be 80km/h (Issue #355)
- Modified smoothing and buffer distances for small isochrones, aswell as other fixes for smaller isochrones (Issue #382)
- Updated pom to use correct opengeo repo and reordered so this is the last in the list, and use latest ORS-Graphhopper library (Issue #398)
- Added /directions as an endpoint for routing (Issue #384)
- Removed the following avoid features: pavedroads, unpavedroads, tunnels, tracks and hills, as well as the option to set maximum speed; for cycling and walking profiles the option to specify difficulty settings such as fitness level and maximum steepness (issue #396)
- Updated pom to always build ors.war (Issue #432)
### Deprecated

## [4.7.2] - 2018-12-10
### Added
- Added Unit Tests for RouteSearchParameters.class() (while fixing Issue #291)
- Added ability to return warning messages in the route response which can be used for showing info to a user when warning criteria have been met based on extended storages.
- Added a RoadAccessRestrictions extended storage as a warning extended storage for when a route goes of ways with access restrictions (Issue #342)
### Fixed
- If residential penalty reduces speed to <5, set it to 5
- Added a new ParameterValueException in RouteSearchParameters if the profile is driving-car and profile_params are set in the options (Issue #291)
- Fixed API Test to consider the new ParameterValueException (while fixing Issue #291)
- Improved range and resolution of values encoding dimension/weight road restrictions in order to properly resolve them when corresponding hgv parameters are set (fixes issue #263)
- Fixed empty BBox error if the route is located in the southern hemisphere (Issue #348)
- Take into account access restrictions specific to hgv subprofiles (fixes issue #235)
- Properly resolve all tolls, especially hgv-specific ones (fixes issue #358)
- Updated checks on pedestrian way filter for access restrictions
### Changed
- Allowed access for cars and hgvs on access=destination roads (Issue #342)
### Deprecated

## [4.7.1] - 2018-10-24
### Added
Added instructions to readme for installing without Docker (Issue #272)
Added area_units for isochrones API as units being misleading (Issue #272)
### Fixed
- Area calculation for isochrones using metric crs (Issue #130)
- Decreases maximum peed for bike-regular for more realistic reachability scores (Issue #325)
- Fixes self intersecting polygons when requesting population for isochrones (Issue #297)
- Changes center in isochrones response to snapped edge coordinate on graph (Issue #336)
- Enable HGV axleload restriction (Issue #262)
### Changed
- Changed app.config.sample for docker to consider split profiles (Issue #320)
- Changed minor information in pom.xml
- Updated API test starting coordinates to be on a road (Issue #328)
### Deprecated

## [4.7] - 2018-10-10
### Added
- Removed locations code as this will be handled by openpoiservice in the future (Issue #120)
- Removed Geocoding code as this will be handled by the geocoder service rather than within ORS
- Added smoothing option for isochrones (Issue #137)
- Added ExtraInfo storage for osm way id so that this information can be stored (and accessed) agianst the edges (Issue #217)
- Added a new GeometryUtility function and its unit test to calculate the bbox for a set of coordinates (Issue #241)
- Added support for elevation data above & and below the 60 deg N/S. When you run your own instance make sure that you specify the `elevation_provider: multi` (instead of just 'cgiar') (Issue #220)
- Added support to keep elevation source data over various path generation processes - add to your app.config: `elevation_cache_clear: false`
- Added support for new keep left/right turn instructions

### Fixed
- Correct logic of determining vehicle type flags in heavy vehicle storage (Issue #211)
- Enable OSM "key:access" tag values to take effect for heavy vehicle profile (Issue #209)
- Fixed problem with avoid polygons excluding ways that should have been accepted (Issue #95)
- Updated code to remove merging of instructions as this resulted in missing important turn instructions (Issue #177)
- Added missing translations for arrival instructions (Issue #171)
- Updated code so that acceleration is taken into account when speeds are calculated for edges (Issue #178)
- Fixed the mising rte tag in gpx issue (Issue #196)
- Fixed the gpx validation errror (Issue #168)
- Added unit conversion so that isochrone response is in user specified unit (issue #91)
- Enabled the reporting of multiple missing points in error response (issue #246)
- Fixed wrong bounding box error (Issue #241)
- Fixed problem with mountain bike profile never using contraction hierarchies.

### Changed
- Updated the internal graphhopper libraries from 0.9.x to 0.10.1 and reduced the number of custom implementations and features.
This implies that some of the previous features is no longer available in this release of openrouteservice. Most of these
adjustments are under the hood and will not be noticeable for anyone. Have said that there is of course **one exception**:
You need to create a separate profile per vehicle. In previous versions it was possible to combine multiple vehicles (like
bike, road bike, e-bike and mtb) into a single ors-profile - this is no longer possible. Instead you need to create one
profile for bike, one for mtb one for road bike and so on.  
- Updated/refactored road bike flagencoder to make it more suitable for road cycling enthusiasts. Please note, that the
generated routs might not be compliant to the local regulations - specially when 'Biking trails are obligated to be use'
- Refactored some of the edge filters and cleaned up the code initializing them
- Updated the wheelchair profile to better handle restrictions such as width, kerb heights and instances where sidewalks
are attached to roads. (Issue #162)
- Replaced "Sand" surface encoding with "Paving stone"
- Changed the bbox api test (Issue #241)
- Changed the way the bbox is calculated internally (Issue #241)
- Change the license from apache 2.0 to LGPL3 (PR #296)

### Deprecated
- Removed references to locations and accessibilty services from web.xml (Issue #186)

## [4.5.1] - 2018-03-20
### Fixed
- Fixed the parameters being the wrong way around in isochrones request when maximum range has been exceeded (Issue #126)
- Fixed the coordinate precision in the geojson export from 4 to 6 decimals (Issue #136)
- Fixed the instructions='false' error when exporting as geojson (Issue #138)
- Fixed missing summary in the geojson output (Issue #139)
- Fixed error when a high exit number for a roundabout is used in instructions (Issue #145)

### Changed
- Updated error response code for routing when no route can be found between locations (Issue #144)
- Updated logging so that stack traces are only output when debug logging is enabled (Issue #148)
- Updated the error response for geocding when no address found (Issue #134)


## [4.5] - 2018-02-27
### Added
- Functionality has been added to restrict routes so that they do not cross all borders, controlled borders, or the borders of specific countries (Issue #41)
- Added GeoJson export for routing exports (Issue #54)
- Added global export class to combine all exports there (Issue #123)
- Option to specify maximum locations for matrix request when using non-standard weightings (Issue #94)

### Fixed
- Fix exception when roundabout exit is not correctly found (Issue #89)
- Option to specify maximum locations for matrix request when using non-standard weightings (Issue #94)
- Geocoder now returns a 404 response if no address is found for reverse geocoding (Issue #113)
- Fixed error codes (Issue #109)
- Correct querying of population statistics data for isochrones (Issue #106)

### Changed
- RoutingProfile was changed to make sure whenever pop_total or pop_area is queried, both are present in the attributes (Issue #106)
- Response with a detour factor now uses "detourfactor" rather than "detour_factor" (Issue #61)
- Changed the gpx export to the new global export processor (Issue #123)

### Deprecated
- getStatisticsOld | Connected to the old statistics library (Issue #106)
- geometryToWKB | Connected to the old statistics library (Issue #106)

## [4.4.2] - 2018-01-31
### Added
- Ability to get routes in GPX format (Issue #8)
- Ability to read HGV tags from OSM Nodes (Issue #49)
- No need to add optimisation parameter in request (PR #87)
- Option to respond md5 of osm file used for graph generation (Issue #48)

### Fixed
- Updated code to not use empty bearings when continue_straight=true is set (Issue #51)
- Fixed problem with HGV restrictions only being taken into account if less than three provided (Issue #75)
- RPHAST performance optimisations (Issue #64)
- Updated duration calculations for urban areas (Issue #44)
- Increase hikari pool size for db connections (PR #52)

## [4.4.1] - 2017-10-12

### Added
- Ability to compute and flush graphs without holding them in memory.

### Fixed
- Optionally define bearings for specific waypoints.

## [4.4.0] - 2017-10-09
### Added
- Updated functional tests.
- Bearings parameter may be passed for cycling profiles.
- Radiuses parameter may be passed for cycling profiles.
- Continue forward parameter may be passed for cycling profiles.
- Considering OSM tag smoothness=impassable in routing.
- First prototype of statistics provider in isochrones added.

### Fixed
- Remove tracking of the highest node in DownwardSearchEdgeFilter.
- Unable to find appropriate routing profile for optimized=true if not available.
- Possible Hash function in MultiTreeMetricsExtractor collision now removed.
- Minor speed up fix in ConcaveBallsIsochroneMapBuilder.
- Fix bug in RPHAST when location lies on a oneway road.
- Consider turn restrictions if optimized=false is passed.

### Changed
-

### Removed
-

### Deprecated
-
