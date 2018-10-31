# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
### Fixed
### Changed
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

