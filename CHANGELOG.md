# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)

<!--
This is how a Changelog entry should look like:

## [version] - YYYY-MM-DD

### Added
- for new features.
### Changed
- existing functionality.
### Deprecated
- soon-to-be removed features.
### Removed
- now removed features.
### Fixed
- any bug.
### Security
- in case of vulnerabilities. (Use for vulnerability fixes)

RELEASING:
1. Change Unreleased to new release number
2. Add today's Date
3. Change unreleased link to compare new release:
[unreleased]: https://github.com/GIScience/openrouteservice/compare/vnew...HEAD
4. Add new compare link below
[new]: https://github.com/GIScience/openrouteservice/compare/vlast...vnew
5. Git tag release commit with vX.X.X to enable links
6. Double check issue links are valid
7. Bump version in pom.xml
 -->

## [Unreleased]
### Fixed
- Updated documentation for running in Docker  ([#798](https://github.com/GIScience/openrouteservice/issues/798))

## [6.3.0] - 2020-09-14
### Added
- New fast isochrone algorithm based on preprocessed data
### Fixed
- Fixed handling of invalid extra info requests ([#795](https://github.com/GIScience/openrouteservice/issues/795))

## [6.2.1] - 2020-08-13
### Added
- Check whether routing points are within different countries before routing and break if they are and all borders should be avoided
### Fixed
- Updated Docker process to use Java 11 ([#777](https://github.com/GIScience/openrouteservice/issues/777))
- Correctly resolve routing profile categories when initializing core edge filters in preprocessing ([#785](https://github.com/GIScience/openrouteservice/issues/785))

## [6.2.0] - 2020-07-15
### Added
- New `maximum_speed` parameter to the driving profiles of the directions API, for specifying a speed limit, above a certain threshold set in the config file.
- Polish translation ([#690](https://github.com/GIScience/openrouteservice/issues/690))
- Configuration parameter to enable elevation smoothing ([#725](https://github.com/GIScience/openrouteservice/issues/725))
### Fixed
- Fixed fallback to dynamic routing methods if bearings parameter set ([#702](https://github.com/GIScience/openrouteservice/issues/702))
- Enable elevation interpolation for bridges and tunnels ([#685](https://github.com/GIScience/openrouteservice/issues/685))
- Fixed erroneous duration computation of soft weightings such as green and quiet weightings
- Enable recommended weighting for hgv profile and robustify the matching of routing algorithm to the request ([#755](https://github.com/GIScience/openrouteservice/issues/755))
### Changed
- Improve recommended weighting for cycling and walking profiles ([#665](https://github.com/GIScience/openrouteservice/issues/665))
- Restructure AdditionWeighting
- Upgrade to Java 11
### Deprecated
- Use recommended weighting instead of fastest ([#763](https://github.com/GIScience/openrouteservice/issues/763))

## [6.1.1] - 2020-06-02
### Added
- Configuration option to read elevation tags from pbf data
- Configuration parameters to set location index resolution and the maximum number of iterations in coordinates lookup ([#712](https://github.com/GIScience/openrouteservice/issues/712))
### Fixed
- Removing maintenance burden of two `app.config` files for native and docker setup ([#742](https://github.com/GIScience/openrouteservice/issues/742))
- Allowed the usage of green and noise in extra info parameter ([#688](https://github.com/GIScience/openrouteservice/issues/688))
- Fixed extra info grouping with alternative routes ([#681](https://github.com/GIScience/openrouteservice/issues/681))
- Fixed way surface/type encoding issue ([#677](https://github.com/GIScience/openrouteservice/issues/677))
- Querying shortest weighting can now use CH shortest preparation if available
- Roads tagged with destination access are penalized the same way for hgv as for car ([#525](https://github.com/GIScience/openrouteservice/issues/525))
- JAVA_OPTS and CATALINA_OPTS were not correctly set in Docker setup ([#696](https://github.com/GIScience/openrouteservice/issues/696))
- Suitability values in extra info are not underestimated ([#722](https://github.com/GIScience/openrouteservice/issues/722))
- Fixed problem with incorrect way point values being referenced for round trip ([#724](https://github.com/GIScience/openrouteservice/issues/724))
- Fixed oneway handling for bike routing ([#389](https://github.com/GIScience/openrouteservice/issues/389)) [by integrating GH PR [#1769](https://github.com/graphhopper/graphhopper/pull/1769/files/ad4fe02d3d9b5deb66dc0b88d02b61b28b52871c) of BikeCommonFlagEncoder]
### Changed
- Refactor the algorithm selection process
- Use ALT/A* Beeline for roundtrips. Enable Core-ALT-only for pedestrian profile.
- Enable CH and Core-ALT preprocessing with recommended weighting for all profiles.
- Refactor wheelchair builder
- Running a Docker container will now create a `app.config` on the host machine, so it's now usable from Dockerhub

## [6.1.0] - 2020-03-06
### Added
- Hebrew language support (thanks to [citizen-dror](https://github.com/GIScience/openrouteservice/commits?author=citizen-dror) for the translation)
- Configuration options to limit avoid_polygon routing option by area and/or extent ([#629](https://github.com/GIScience/openrouteservice/issues/629))
- Configuration options to limit count parameter and distance when using alternative routes algorithm ([#651](https://github.com/GIScience/openrouteservice/issues/651))
- Configuration options to limit distance when using round trip routing algorithm ([#658](https://github.com/GIScience/openrouteservice/issues/658))
- Enable CALT routing algorithm for cycling profiles ([#662](https://github.com/GIScience/openrouteservice/issues/662))
- Configuration options to send conditional system messages with API responses ([#664](https://github.com/GIScience/openrouteservice/issues/664))
### Fixed
- more consistent language API parameters (ISO 639-1 codes & IETF tags)
- Nepali language support can be selected through API
- Fixed invalid JSON and GeoJSON when including elevation ([#640](https://github.com/GIScience/openrouteservice/issues/640))
- Added graph date for isochrones and matrix service and fixed the 0 output ([#648](https://github.com/GIScience/openrouteservice/issues/648))
- Fixed memory issue at graph building ([#659](https://github.com/GIScience/openrouteservice/issues/659))
- Improve way category assignment for ferry connections ([#678](https://github.com/GIScience/openrouteservice/issues/678))
### Changed
- improve french translation (directions)
- Make Docker setup more flexible wrt customizations ([#627](https://github.com/GIScience/openrouteservice/issues/627))
- Updated GraphHopper to newer version (0.13)
- Give more details to green and quiet routing API descriptions ([#632](https://github.com/GIScience/openrouteservice/issues/632))

## [6.0.0] - 2019-12-03
### Added
- Indonesian Translation Language
- Allow specifying a config file with -Dors_app_config=<file> anywhere on the filesystem
- Enabled round trip routing ([#391](https://github.com/GIScience/openrouteservice/issues/391))
- Enabled aternative routes in API ([#377](https://github.com/GIScience/openrouteservice/issues/377))
- Added information to the response about when graphs were last built for the profile ([#542](https://github.com/GIScience/openrouteservice/issues/542))
- Added default value for maximum snapping radius
### Fixed
- Pass JAVA_OPTS and CATALINA_OPTS as Docker build arguments ([#587](https://github.com/GIScience/openrouteservice/issues/587))
- Encoding of waytype ferry ([#573](https://github.com/GIScience/openrouteservice/issues/573))
- Refactored Core-ALT algorithm so that it can be used globally
### Changed
- Updated GraphHopper to newer version (0.12)
- Reworked flag encoders to use the methods provided by GraphHopper 0.12
- Renamed packages to follow naming conventions
- Cleanup of a number of code files
### Deprecated
- Removed geocoding endpoint and code
- Removed accessibilty endpoint and code
- Removed Brotil encoder from servlet filter

## [5.0.2] - 2019-07-29
### Added
- Added a gpx schema validator into the api-tests, testing all gpx outputs while fixing the bug from ([#496](https://github.com/GIScience/openrouteservice/issues/496))
- Added information for countries a route traverses ([#349](https://github.com/GIScience/openrouteservice/issues/349))
- Added scanning of master with sonarcloud (2019-11-29)
### Fixed
- isochrone reachfactor gives now more realistic results ([#325](https://github.com/GIScience/openrouteservice/issues/325))
- Fixed the wrong gpx header for api v2 ([#496](https://github.com/GIScience/openrouteservice/issues/496))
- Make sure external storages contain entries for all edge IDs ([#535](https://github.com/GIScience/openrouteservice/issues/535))
- Check if BordersStorage exists before calling it in AvoidBordersCoreEdgeFilter
- Take into account shortcut direction in LM selection weighting ([#550](https://github.com/GIScience/openrouteservice/issues/550))
- Updated Matrix api v2 response to correctly display sources ([#560](https://github.com/GIScience/openrouteservice/issues/560))
- Check for null pointer in LM selection weighting ([#550](https://github.com/GIScience/openrouteservice/issues/550))
- Use commas rather than pipes for weighting options in app.config.sample ([#564](https://github.com/GIScience/openrouteservice/issues/564))
- Update point references when point is not found for routing ([#567](https://github.com/GIScience/openrouteservice/issues/567))
- Fix concurrency issues when requesting extra info in routing ([#571](https://github.com/GIScience/openrouteservice/issues/571))
### Changed
- Moved walking and hiking flag encoders to the ORS core system ([#440](https://github.com/GIScience/openrouteservice/issues/440))
- Remove route optimization code ([#499](https://github.com/GIScience/openrouteservice/issues/499))
- Reduced distance for neighbourhood point search in isochrones when small isochrones are generated ([#494](https://github.com/GIScience/openrouteservice/issues/494))
- Removed obsolete storages ([#536](https://github.com/GIScience/openrouteservice/issues/536))
- Refactor fallback to preprocessing-independent algorithm for certain routing request params
- Removed some landmark sets as default from app.config.sample

## [5.0.1] - 2019-04-08
### Added
- CALT routing algorithm - Not for production ([Issue #433](https://github.com/GIScience/openrouteservice/issues/433))
- Makes docker and docker-compose deployment of openrouteservice more customizable ([Issue #434](https://github.com/GIScience/openrouteservice/issues/434))
- Add the possibility to predefine standard maximum search radii in general and for each used profile in the config file ([Issue #418](https://github.com/GIScience/openrouteservice/issues/418))
### Fixed
- fix the GPX output of the APIv2. It was broken since release of api v2. ([Issue #533](https://github.com/GIScience/openrouteservice/issues/533))
- fix SRTM URL in GH fork ([#394](https://github.com/GIScience/openrouteservice/issues/394))
- fix classpath issues for resources, Windows builds now ([#489](https://github.com/GIScience/openrouteservice/issues/489))
- isochrone geojson bbox now format compliant ([#493](https://github.com/GIScience/openrouteservice/issues/493))
- v2 isochrones now respects max_locations in app.config ([#482](https://github.com/GIScience/openrouteservice/issues/482))
- Updated documentation to reflect correct isochrone smoothing algorithm ([Issue #471](https://github.com/GIScience/openrouteservice/issues/471))
- Enable > 2 waypoints when geometry_simplify=true ([#457](https://github.com/GIScience/openrouteservice/issues/457))
- Made it so that the wheelchair profile only goes over bridleways if they are set to be foot or wheelchair accessible ([#415](https://github.com/GIScience/openrouteservice/issues/415))
- Fixed the build fail bug when `routing_name` was set in the config file ([#424](https://github.com/GIScience/openrouteservice/issues/424))
- Fixed problem with border crossings where the way crosses three polygons ([#491](https://github.com/GIScience/openrouteservice/issues/491))
### Changed
- Updated pom to always build ors.war ([Issue #432](https://github.com/GIScience/openrouteservice/issues/432))
- Replace usage of packages incompatible with Java >8 ([#474](https://github.com/GIScience/openrouteservice/issues/474))
- Updated Matrix to have a maximum number of routes to calculate rather than locations ([#518](https://github.com/GIScience/openrouteservice/issues/518))
### Deprecated
- Removed the code that was inserted for the prototype traffic weightings as it was not used and made GH updates more complicated.


## [5.0.0] - 2019-02-25
### Added
- Updated api code to use the Spring framework, with the v2 api being added ([Issue #233](https://github.com/GIScience/openrouteservice/issues/233))
- Added support for ISO 3166-1 Alpha-2 / Alpha-3 codes for routing directions option avoid_countries ([Issue #195](https://github.com/GIScience/openrouteservice/issues/195))
- Added support for free hand route option/ skip segments ([Issue #167](https://github.com/GIScience/openrouteservice/issues/167))
- Added check on matrix service to make sure that the requested locations are within the bounding area of the graph ([Issue #408](https://github.com/GIScience/openrouteservice/issues/408))
- Makes docker and docker-compose deployment of openrouteservice more customizable ([Issue #434](https://github.com/GIScience/openrouteservice/issues/434))
- Added support for GH alternative_route algorithm (Issue #377)
### Fixed
- Fixed `geometry_simplify` parameter, which had no effect before. `geometry_simplify` is incompatible with `extra_info` ([#381](https://github.com/GIScience/openrouteservice/issues/381))
### Changed
- Updated rural speed limit in France to be 80km/h ([Issue #355](https://github.com/GIScience/openrouteservice/issues/355))
- Modified smoothing and buffer distances for small isochrones, aswell as other fixes for smaller isochrones ([Issue #382](https://github.com/GIScience/openrouteservice/issues/382))
- Updated pom to use correct opengeo repo and reordered so this is the last in the list, and use latest ORS-Graphhopper library ([Issue #398](https://github.com/GIScience/openrouteservice/issues/398))
- Added /directions as an endpoint for routing ([Issue #384](https://github.com/GIScience/openrouteservice/issues/384))
- Removed the following avoid features: pavedroads, unpavedroads, tunnels, tracks and hills, as well as the option to set maximum speed; for cycling and walking profiles the option to specify difficulty settings such as fitness level and maximum steepness ([issue #396](https://github.com/GIScience/openrouteservice/issues/396))
- Updated pom to always build ors.war ([Issue #432](https://github.com/GIScience/openrouteservice/issues/432))

## [4.7.2] - 2018-12-10
### Added
- Added Unit Tests for RouteSearchParameters.class() ([while fixing Issue #291](https://github.com/GIScience/openrouteservice/issues/291))
- Added ability to return warning messages in the route response which can be used for showing info to a user when warning criteria have been met based on extended storages.
- Added a RoadAccessRestrictions extended storage as a warning extended storage for when a route goes of ways with access restrictions ([Issue #342](https://github.com/GIScience/openrouteservice/issues/342))
### Fixed
- If residential penalty reduces speed to <5, set it to 5
- Added a new ParameterValueException in RouteSearchParameters if the profile is driving-car and profile_params are set in the options ([Issue #291](https://github.com/GIScience/openrouteservice/issues/291))
- Fixed API Test to consider the new ParameterValueException ([while fixing Issue #291](https://github.com/GIScience/openrouteservice/issues/291))
- Improved range and resolution of values encoding dimension/weight road restrictions in order to properly resolve them when corresponding hgv parameters are set ([fixes issue #263](https://github.com/GIScience/openrouteservice/issues/263))
- Fixed empty BBox error if the route is located in the southern hemisphere ([Issue #348](https://github.com/GIScience/openrouteservice/issues/348))
- Take into account access restrictions specific to hgv subprofiles ([fixes issue #235](https://github.com/GIScience/openrouteservice/issues/235))
- Properly resolve all tolls, especially hgv-specific ones ([fixes issue #358](https://github.com/GIScience/openrouteservice/issues/358))
- Updated checks on pedestrian way filter for access restrictions
### Changed
- Allowed access for cars and hgvs on access=destination roads ([Issue #342](https://github.com/GIScience/openrouteservice/issues/342))

## [4.7.1] - 2018-10-24
### Added
Added instructions to readme for installing without Docker ([Issue #272](https://github.com/GIScience/openrouteservice/issues/272))
Added area_units for isochrones API as units being misleading ([Issue #272](https://github.com/GIScience/openrouteservice/issues/272))
### Fixed
- Area calculation for isochrones using metric crs ([Issue #130](https://github.com/GIScience/openrouteservice/issues/130))
- Decreases maximum peed for bike-regular for more realistic reachability scores ([Issue #325](https://github.com/GIScience/openrouteservice/issues/325))
- Fixes self intersecting polygons when requesting population for isochrones ([Issue #297](https://github.com/GIScience/openrouteservice/issues/297))
- Changes center in isochrones response to snapped edge coordinate on graph ([Issue #336](https://github.com/GIScience/openrouteservice/issues/336))
- Enable HGV axleload restriction ([Issue #262](https://github.com/GIScience/openrouteservice/issues/262))
### Changed
- Changed app.config.sample for docker to consider split profiles ([Issue #320](https://github.com/GIScience/openrouteservice/issues/320))
- Changed minor information in pom.xml
- Updated API test starting coordinates to be on a road ([Issue #328](https://github.com/GIScience/openrouteservice/issues/328))

## [4.7.0] - 2018-10-10
### Added
- Removed locations code as this will be handled by openpoiservice in the future ([Issue #120](https://github.com/GIScience/openrouteservice/issues/120))
- Removed Geocoding code as this will be handled by the geocoder service rather than within ORS
- Added smoothing option for isochrones ([Issue #137](https://github.com/GIScience/openrouteservice/issues/137))
- Added ExtraInfo storage for osm way id so that this information can be stored (and accessed) agianst the edges ([Issue #217](https://github.com/GIScience/openrouteservice/issues/217))
- Added a new GeometryUtility function and its unit test to calculate the bbox for a set of coordinates ([Issue #241](https://github.com/GIScience/openrouteservice/issues/241))
- Added support for elevation data above & and below the 60 deg N/S. When you run your own instance make sure that you specify the `elevation_provider: multi` (instead of just 'cgiar') ([Issue #220](https://github.com/GIScience/openrouteservice/issues/220))
- Added support to keep elevation source data over various path generation processes - add to your app.config: `elevation_cache_clear: false`
- Added support for new keep left/right turn instructions

### Fixed
- Correct logic of determining vehicle type flags in heavy vehicle storage ([Issue #211](https://github.com/GIScience/openrouteservice/issues/211))
- Enable OSM "key:access" tag values to take effect for heavy vehicle profile ([Issue #209](https://github.com/GIScience/openrouteservice/issues/209))
- Fixed problem with avoid polygons excluding ways that should have been accepted ([Issue #95](https://github.com/GIScience/openrouteservice/issues/95))
- Updated code to remove merging of instructions as this resulted in missing important turn instructions ([Issue #177](https://github.com/GIScience/openrouteservice/issues/177))
- Added missing translations for arrival instructions ([Issue #171](https://github.com/GIScience/openrouteservice/issues/171))
- Updated code so that acceleration is taken into account when speeds are calculated for edges ([Issue #178](https://github.com/GIScience/openrouteservice/issues/178))
- Fixed the mising rte tag in gpx issue ([Issue #196](https://github.com/GIScience/openrouteservice/issues/196))
- Fixed the gpx validation errror ([Issue #168](https://github.com/GIScience/openrouteservice/issues/168))
- Added unit conversion so that isochrone response is in user specified unit ([issue #91](https://github.com/GIScience/openrouteservice/issues/91))
- Enabled the reporting of multiple missing points in error response ([issue #246](https://github.com/GIScience/openrouteservice/issues/246))
- Fixed wrong bounding box error ([Issue #241](https://github.com/GIScience/openrouteservice/issues/241))
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
are attached to roads. ([Issue #162](https://github.com/GIScience/openrouteservice/issues/162))
- Replaced "Sand" surface encoding with "Paving stone"
- Changed the bbox api test ([Issue #241](https://github.com/GIScience/openrouteservice/issues/241))
- Changed the way the bbox is calculated internally ([Issue #241](https://github.com/GIScience/openrouteservice/issues/241))
- Change the license from apache 2.0 to LGPL3 ([PR #296](https://github.com/GIScience/openrouteservice/issues/296))

### Deprecated
- Removed references to locations and accessibilty services from web.xml ([Issue #186](https://github.com/GIScience/openrouteservice/issues/186))

## [4.5.1] - 2018-03-20
### Fixed
- Fixed the parameters being the wrong way around in isochrones request when maximum range has been exceeded ([Issue #126](https://github.com/GIScience/openrouteservice/issues/126))
- Fixed the coordinate precision in the geojson export from 4 to 6 decimals ([Issue #136](https://github.com/GIScience/openrouteservice/issues/136))
- Fixed the instructions='false' error when exporting as geojson ([Issue #138](https://github.com/GIScience/openrouteservice/issues/138))
- Fixed missing summary in the geojson output ([Issue #139](https://github.com/GIScience/openrouteservice/issues/139))
- Fixed error when a high exit number for a roundabout is used in instructions ([Issue #145](https://github.com/GIScience/openrouteservice/issues/145))

### Changed
- Updated error response code for routing when no route can be found between locations ([Issue #144](https://github.com/GIScience/openrouteservice/issues/144))
- Updated logging so that stack traces are only output when debug logging is enabled ([Issue #148](https://github.com/GIScience/openrouteservice/issues/148))
- Updated the error response for geocding when no address found ([Issue #134](https://github.com/GIScience/openrouteservice/issues/134))


## [4.5.0] - 2018-02-27
### Added
- Functionality has been added to restrict routes so that they do not cross all borders, controlled borders, or the borders of specific countries ([Issue #41](https://github.com/GIScience/openrouteservice/issues/41))
- Added GeoJson export for routing exports ([Issue #54](https://github.com/GIScience/openrouteservice/issues/54))
- Added global export class to combine all exports there ([Issue #123](https://github.com/GIScience/openrouteservice/issues/123))
- Option to specify maximum locations for matrix request when using non-standard weightings ([Issue #94](https://github.com/GIScience/openrouteservice/issues/94))

### Fixed
- Fix exception when roundabout exit is not correctly found ([Issue #89](https://github.com/GIScience/openrouteservice/issues/89))
- Option to specify maximum locations for matrix request when using non-standard weightings ([Issue #94](https://github.com/GIScience/openrouteservice/issues/94))
- Geocoder now returns a 404 response if no address is found for reverse geocoding ([Issue #113](https://github.com/GIScience/openrouteservice/issues/113))
- Fixed error codes ([Issue #109](https://github.com/GIScience/openrouteservice/issues/109))
- Correct querying of population statistics data for isochrones ([Issue #106](https://github.com/GIScience/openrouteservice/issues/106))

### Changed
- RoutingProfile was changed to make sure whenever pop_total or pop_area is queried, both are present in the attributes ([Issue #106](https://github.com/GIScience/openrouteservice/issues/106))
- Response with a detour factor now uses "detourfactor" rather than "detour_factor" ([Issue #61](https://github.com/GIScience/openrouteservice/issues/61))
- Changed the gpx export to the new global export processor ([Issue #123](https://github.com/GIScience/openrouteservice/issues/123))

### Deprecated
- getStatisticsOld | Connected to the old statistics library ([Issue #106](https://github.com/GIScience/openrouteservice/issues/106))
- geometryToWKB | Connected to the old statistics library ([Issue #106](https://github.com/GIScience/openrouteservice/issues/106))

## [4.4.2] - 2018-01-31
### Added
- Ability to get routes in GPX format ([Issue #8](https://github.com/GIScience/openrouteservice/issues/8))
- Ability to read HGV tags from OSM Nodes ([Issue #49](https://github.com/GIScience/openrouteservice/issues/49))
- No need to add optimisation parameter in request ([PR #87](https://github.com/GIScience/openrouteservice/issues/87))
- Option to respond md5 of osm file used for graph generation ([Issue #48](https://github.com/GIScience/openrouteservice/issues/48))

### Fixed
- Updated code to not use empty bearings when continue_straight=true is set ([Issue #51](https://github.com/GIScience/openrouteservice/issues/51))
- Fixed problem with HGV restrictions only being taken into account if less than three provided ([Issue #75](https://github.com/GIScience/openrouteservice/issues/75))
- RPHAST performance optimisations ([Issue #64](https://github.com/GIScience/openrouteservice/issues/64))
- Updated duration calculations for urban areas ([Issue #44](https://github.com/GIScience/openrouteservice/issues/44))
- Increase hikari pool size for db connections ([PR #52](https://github.com/GIScience/openrouteservice/issues/52))

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

[unreleased]: https://github.com/GIScience/openrouteservice/compare/v6.3.0...HEAD
[6.3.0]: https://github.com/GIScience/openrouteservice/compare/v6.2.1...v6.3.0
[6.2.1]: https://github.com/GIScience/openrouteservice/compare/v6.2.0...v6.2.1
[6.2.0]: https://github.com/GIScience/openrouteservice/compare/v6.1.1...v6.2.0
[6.1.1]: https://github.com/GIScience/openrouteservice/compare/v6.1.0...v6.1.1
[6.1.0]: https://github.com/GIScience/openrouteservice/compare/v6.0.0...v6.1.0
[6.0.0]: https://github.com/GIScience/openrouteservice/compare/v5.0.2...v6.0.0
[5.0.2]: https://github.com/GIScience/openrouteservice/compare/v5.0.1...v5.0.2
[5.0.1]: https://github.com/GIScience/openrouteservice/compare/5.0.0...v5.0.1
[5.0.0]: https://github.com/GIScience/openrouteservice/compare/v4.7.2...5.0.0
[4.7.2]: https://github.com/GIScience/openrouteservice/compare/4.7.1...v4.7.2
[4.7.1]: https://github.com/GIScience/openrouteservice/compare/4.7.0...4.7.1
[4.7.0]: https://github.com/GIScience/openrouteservice/compare/4.5.1...4.7.0
[4.5.1]: https://github.com/GIScience/openrouteservice/compare/4.5.0...4.5.1
[4.5.0]: https://github.com/GIScience/openrouteservice/compare/4.4.2...4.5.0
[4.4.2]: https://github.com/GIScience/openrouteservice/compare/4.4.1...4.4.2
[4.4.1]: https://github.com/GIScience/openrouteservice/compare/4.4.0...4.4.1
[4.4.0]: https://github.com/GIScience/openrouteservice/compare/4.3.0...4.4.0
