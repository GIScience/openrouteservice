# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Option to specify maximum locations for matrix request when using non-standard weightings (Issue #94)

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
