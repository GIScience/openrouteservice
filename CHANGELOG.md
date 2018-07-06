# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Removed locations code as this will be handled by openpoiservice in the future (Issue #120)
- Removed Geocoding code as this will be handled by the geocoder service rather than within ORS

### Fixed
- Fixed problem with avoid polygons excluding ways that should have been accepted (Issue #95)
- Updated code to remove merging of instructions as this resulted in missing important turn instructions (Issue #177)
- Added missing translations for arrival instructions (Issue #171)
- Updated code so that acceleration is taken into account when speeds are calculated for edges (Issue #178)
- Fixed the mising rte tag in gpx issue (Issue #196)
- Fixed the gpx validation errror (Issue #168)

### Changed
- Updated the wheelchair profile to better handle restrictions such as width, kerb heights and instances where sidewalks are attached to roads. (Issue #162)
- Replaced "Sand" surface encoding with "Paving stone"

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

