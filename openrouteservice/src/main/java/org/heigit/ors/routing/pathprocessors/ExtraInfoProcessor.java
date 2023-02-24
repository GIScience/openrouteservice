/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.pathprocessors;

import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.routing.RouteExtraInfo;
import org.heigit.ors.routing.RouteExtraInfoFlag;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderKeys;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersPolygon;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import org.heigit.ors.routing.graphhopper.extensions.storages.*;
import org.heigit.ors.routing.parameters.ProfileParameters;
import org.heigit.ors.routing.util.ElevationSmoother;
import org.heigit.ors.routing.util.WaySurfaceDescription;
import org.heigit.ors.routing.util.extrainfobuilders.AppendableSteepnessExtraInfoBuilder;
import org.heigit.ors.routing.util.extrainfobuilders.RouteExtraInfoBuilder;
import org.heigit.ors.routing.util.extrainfobuilders.AppendableRouteExtraInfoBuilder;
import org.heigit.ors.routing.util.extrainfobuilders.SteepnessExtraInfoBuilder;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.graphhopper.routing.util.EncodingManager.getKey;

public class ExtraInfoProcessor implements PathProcessor {
	private WaySurfaceTypeGraphStorage extWaySurface;
	private WayCategoryGraphStorage extWayCategory;
	private GreenIndexGraphStorage extGreenIndex;
	private NoiseIndexGraphStorage extNoiseIndex;
	private TollwaysGraphStorage extTollways;
	private TrailDifficultyScaleGraphStorage extTrailDifficulty;
	private HillIndexGraphStorage extHillIndex;
	private OsmIdGraphStorage extOsmId;
	private RoadAccessRestrictionsGraphStorage extRoadAccessRestrictions;
	private BordersGraphStorage extCountryTraversalInfo;
	private CsvGraphStorage extCsvData;
	private ShadowIndexGraphStorage extShadowIndex;

	private RouteExtraInfo surfaceInfo;
	private RouteExtraInfoBuilder surfaceInfoBuilder;

	private RouteExtraInfo wayTypeInfo;
	private RouteExtraInfoBuilder wayTypeInfoBuilder;

	private RouteExtraInfo steepnessInfo;
	private SteepnessExtraInfoBuilder steepnessInfoBuilder;

	private RouteExtraInfo waySuitabilityInfo;
	private RouteExtraInfoBuilder waySuitabilityInfoBuilder;

	private RouteExtraInfo wayCategoryInfo;
	private RouteExtraInfoBuilder wayCategoryInfoBuilder;

	private RouteExtraInfo greenInfo;
	private RouteExtraInfoBuilder greenInfoBuilder;

	private RouteExtraInfo noiseInfo;
	private RouteExtraInfoBuilder noiseInfoBuilder;

	private RouteExtraInfo avgSpeedInfo;
	private RouteExtraInfoBuilder avgSpeedInfoBuilder;

	private RouteExtraInfo tollwaysInfo;
	private RouteExtraInfoBuilder tollwaysInfoBuilder;
	private TollwayExtractor tollwayExtractor;

	private RouteExtraInfo trailDifficultyInfo;
	private RouteExtraInfoBuilder trailDifficultyInfoBuilder;

	private RouteExtraInfo osmIdInfo;
	private RouteExtraInfoBuilder osmIdInfoBuilder;

	private RouteExtraInfo roadAccessRestrictionsInfo;
	private RouteExtraInfoBuilder roadAccessRestrictionsInfoBuilder;

	private RouteExtraInfo countryTraversalInfo;
	private RouteExtraInfoBuilder countryTraversalInfoBuilder;

	private RouteExtraInfo csvInfo;
	private RouteExtraInfoBuilder csvInfoBuilder;
	int csvColumn = 0;

  private RouteExtraInfo shadowInfo;
	private RouteExtraInfoBuilder shadowInfoBuilder;

	private List<Integer> warningExtensions;

	private int profileType = RoutingProfileType.UNKNOWN;
	private final FlagEncoder encoder;
	private final boolean encoderWithPriority;
	private final byte[] buffer;
	private static final Logger LOGGER = Logger.getLogger(ExtraInfoProcessor.class.getName());

	private String skippedExtraInfo = "";

	private CountryBordersReader countryBordersReader;

	ExtraInfoProcessor(PMap opts, GraphHopperStorage graphHopperStorage, FlagEncoder enc, CountryBordersReader cbReader) throws Exception {
		this(opts, graphHopperStorage, enc);
		this.countryBordersReader = cbReader;
	}

	ExtraInfoProcessor(PMap opts, GraphHopperStorage graphHopperStorage, FlagEncoder enc) throws Exception {
		encoder = enc;
		encoderWithPriority = encoder.supports(PriorityWeighting.class);
		List<String> skippedExtras = new ArrayList<>();

		try {
			PMap params = opts;
			if (params == null) {
				params = new PMap();
			}

			int extraInfo = params.getInt("routing_extra_info", 0);
			profileType = params.getInt("routing_profile_type", 0);
			ProfileParameters profileParameters = params.getObject("routing_profile_params", new ProfileParameters());
			boolean suppressWarnings = params.getBool("routing_suppress_warnings", false);

			warningExtensions = new ArrayList<>();

			if(!suppressWarnings)
				applyWarningExtensions(graphHopperStorage);

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.WAY_CATEGORY)) {
				extWayCategory = GraphStorageUtils.getGraphExtension(graphHopperStorage, WayCategoryGraphStorage.class);
				if (extWayCategory != null) {
					wayCategoryInfo = new RouteExtraInfo("waycategory");
					wayCategoryInfoBuilder = new AppendableRouteExtraInfoBuilder(wayCategoryInfo);
				} else {
					skippedExtras.add("waycategory");
				}
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.SURFACE) || includeExtraInfo(extraInfo, RouteExtraInfoFlag.WAY_TYPE)) {
				extWaySurface = GraphStorageUtils.getGraphExtension(graphHopperStorage, WaySurfaceTypeGraphStorage.class);
				if (extWaySurface != null) {
					if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.SURFACE)) {
						surfaceInfo = new RouteExtraInfo("surface");
						surfaceInfoBuilder = new AppendableRouteExtraInfoBuilder(surfaceInfo);
					}
					if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.WAY_TYPE)) {
						wayTypeInfo = new RouteExtraInfo("waytypes");
						wayTypeInfoBuilder = new AppendableRouteExtraInfoBuilder(wayTypeInfo);
					}
				} else {
					skippedExtras.add("surface/waytypes");
				}
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.STEEPNESS)) {
				if ("true".equals(graphHopperStorage.getProperties().get("elevation"))) {
					steepnessInfo = new RouteExtraInfo("steepness");
					steepnessInfoBuilder = new AppendableSteepnessExtraInfoBuilder(steepnessInfo);
				} else {
					skippedExtras.add("steepness");
				}
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.SUITABILITY)) {
				waySuitabilityInfo = new RouteExtraInfo("suitability");
				waySuitabilityInfoBuilder = new AppendableRouteExtraInfoBuilder(waySuitabilityInfo);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.AVG_SPEED)) {
				avgSpeedInfo = new RouteExtraInfo("avgspeed");
				avgSpeedInfo.setFactor(10);
				avgSpeedInfoBuilder = new AppendableRouteExtraInfoBuilder(avgSpeedInfo);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.TOLLWAYS)) {
				extTollways = GraphStorageUtils.getGraphExtension(graphHopperStorage, TollwaysGraphStorage.class);
				if (extTollways != null) {
					tollwaysInfo = new RouteExtraInfo("tollways", extTollways);
					tollwaysInfoBuilder = new AppendableRouteExtraInfoBuilder(tollwaysInfo);
					tollwayExtractor = new TollwayExtractor(extTollways, profileType, profileParameters);
				} else {
					skippedExtras.add("tollways");
				}
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.TRAIL_DIFFICULTY)) {
				extTrailDifficulty = GraphStorageUtils.getGraphExtension(graphHopperStorage, TrailDifficultyScaleGraphStorage.class);
				extHillIndex = GraphStorageUtils.getGraphExtension(graphHopperStorage, HillIndexGraphStorage.class);

				trailDifficultyInfo = new RouteExtraInfo("traildifficulty");
				trailDifficultyInfoBuilder = new AppendableRouteExtraInfoBuilder(trailDifficultyInfo);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.GREEN)) {
				extGreenIndex = GraphStorageUtils.getGraphExtension(graphHopperStorage, GreenIndexGraphStorage.class);
				if (extGreenIndex != null) {
					greenInfo = new RouteExtraInfo("green");
					greenInfoBuilder = new AppendableRouteExtraInfoBuilder(greenInfo);
				} else {
					skippedExtras.add("green");
				}
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.NOISE)) {
				extNoiseIndex = GraphStorageUtils.getGraphExtension(graphHopperStorage, NoiseIndexGraphStorage.class);
				if (extNoiseIndex != null) {
					noiseInfo = new RouteExtraInfo("noise");
					noiseInfoBuilder = new AppendableRouteExtraInfoBuilder(noiseInfo);
				} else {
					skippedExtras.add("noise");
				}
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.SHADOW)) {
				extShadowIndex = GraphStorageUtils.getGraphExtension(graphHopperStorage, ShadowIndexGraphStorage.class);
				if (extShadowIndex != null) {
					shadowInfo = new RouteExtraInfo("shadow");
					shadowInfoBuilder = new AppendableRouteExtraInfoBuilder(shadowInfo);
				} else {
					skippedExtras.add("shadow");
				}
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.OSM_ID)) {
				extOsmId = GraphStorageUtils.getGraphExtension(graphHopperStorage, OsmIdGraphStorage.class);
				if (extOsmId != null) {
					osmIdInfo = new RouteExtraInfo("osmId");
					osmIdInfoBuilder = new AppendableRouteExtraInfoBuilder(osmIdInfo);
				} else {
					skippedExtras.add("osmid");
				}
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.ROAD_ACCESS_RESTRICTIONS)) {
				extRoadAccessRestrictions = GraphStorageUtils.getGraphExtension(graphHopperStorage, RoadAccessRestrictionsGraphStorage.class);
				if (extRoadAccessRestrictions != null) {
					roadAccessRestrictionsInfo = new RouteExtraInfo("roadaccessrestrictions", extRoadAccessRestrictions);
					roadAccessRestrictionsInfoBuilder = new AppendableRouteExtraInfoBuilder(roadAccessRestrictionsInfo);
				} else {
					skippedExtras.add("roadaccessrestrictions");
				}
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.COUNTRY_INFO)) {
				extCountryTraversalInfo = GraphStorageUtils.getGraphExtension(graphHopperStorage, BordersGraphStorage.class);
				if (extCountryTraversalInfo != null) {
					countryTraversalInfo = new RouteExtraInfo("countryinfo", extCountryTraversalInfo);
					countryTraversalInfoBuilder = new AppendableRouteExtraInfoBuilder(countryTraversalInfo);
				} else {
					skippedExtras.add("countryinfo");
				}
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.CSV)) {
				extCsvData = GraphStorageUtils.getGraphExtension(graphHopperStorage, CsvGraphStorage.class);
				if (extCsvData != null) {
					csvInfo = new RouteExtraInfo("csv");
					csvInfoBuilder = new AppendableRouteExtraInfoBuilder(csvInfo);
					csvColumn = extCsvData.columnIndex(params.getString("weighting_#csv#column", ""));
				} else {
					skippedExtras.add("csv");
				}
			}
		} catch (Exception ex) {
			LOGGER.error(ex);
		}
		if (!skippedExtras.isEmpty()) {
			skippedExtraInfo = String.join(", ", skippedExtras);
		}
		buffer = new byte[4];
	}

	/**
	 * Loop through the GraphExtensions of the storage and store in the warningExtensions object those that implement
	 * the WarningGraphExtension interface and are set to be used for generating warnings.
	 *
	 * @param graphHopperStorage the storage containing the warnings
	 */
	private void applyWarningExtensions(GraphHopperStorage graphHopperStorage) {
		GraphExtension[] extensions = graphHopperStorage.getExtensions().getExtensions();
		for(GraphExtension ge : extensions) {
			if (ge instanceof WarningGraphExtension && ((WarningGraphExtension)ge).isUsedForWarning()) {
				warningExtensions.add(RouteExtraInfoFlag.getFromString(((WarningGraphExtension) ge).getName()));
			}
		}
	}

	/**
	 * Check if the extra info should be included in the generation or not by looking at the encoded extras value and
	 * the list of warning extras.
	 *
	 * @param encodedExtras		The encoded value stating which extras were passed explicitly
	 * @param infoFlag			The id of the extra info whose inclusion needs to be decided
	 *
	 */
	private boolean includeExtraInfo(int encodedExtras, int infoFlag) {
		return RouteExtraInfoFlag.isSet(encodedExtras, infoFlag) || warningExtensions.contains(infoFlag);
	}

	public List<RouteExtraInfo> getExtras() {
		List<RouteExtraInfo> extras = new ArrayList<>();
		if (surfaceInfo != null) {
			surfaceInfoBuilder.finish();
			extras.add(surfaceInfo);
		}
		if (wayTypeInfo != null) {
			wayTypeInfoBuilder.finish();
			extras.add(wayTypeInfo);
		}
		if (steepnessInfo != null) {
			steepnessInfoBuilder.finish();
			extras.add(steepnessInfo);
		}
		if (waySuitabilityInfo != null) {
			waySuitabilityInfoBuilder.finish();
			extras.add(waySuitabilityInfo);
		}
		if (wayCategoryInfo != null) {
			wayCategoryInfoBuilder.finish();
			extras.add(wayCategoryInfo);
		}
		if (avgSpeedInfo != null) {
			avgSpeedInfoBuilder.finish();
			extras.add(avgSpeedInfo);
		}
		if (greenInfo != null) {
			greenInfoBuilder.finish();
			extras.add(greenInfo);
		}
		if (noiseInfo != null) {
			noiseInfoBuilder.finish();
			extras.add(noiseInfo);
		}
		if (tollwaysInfo != null) {
			tollwaysInfoBuilder.finish();
			extras.add(tollwaysInfo);
		}
		if (trailDifficultyInfo != null) {
			trailDifficultyInfoBuilder.finish();
			extras.add(trailDifficultyInfo);
		}
		if (osmIdInfo != null) {
			osmIdInfoBuilder.finish();
			extras.add(osmIdInfo);
		}
		if (roadAccessRestrictionsInfo != null) {
			roadAccessRestrictionsInfoBuilder.finish();
			extras.add(roadAccessRestrictionsInfo);
		}
		if (countryTraversalInfo != null) {
			countryTraversalInfoBuilder.finish();
			extras.add(countryTraversalInfo);
		}
		if (csvInfo != null) {
			csvInfoBuilder.finish();
			extras.add(csvInfo);
		}
		if (shadowInfo != null) {
			shadowInfoBuilder.finish();
			extras.add(shadowInfo);
		}
		return extras;
	}

	public void appendData(ExtraInfoProcessor more) {
		if (surfaceInfo != null)
			((AppendableRouteExtraInfoBuilder) surfaceInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.surfaceInfoBuilder);
		if (wayTypeInfo != null)
			((AppendableRouteExtraInfoBuilder) wayTypeInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.wayTypeInfoBuilder);
		if (steepnessInfo != null)
			((AppendableSteepnessExtraInfoBuilder) steepnessInfoBuilder).append((AppendableSteepnessExtraInfoBuilder) more.steepnessInfoBuilder);
		if (waySuitabilityInfo != null)
			((AppendableRouteExtraInfoBuilder) waySuitabilityInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.waySuitabilityInfoBuilder);
		if (wayCategoryInfo != null)
			((AppendableRouteExtraInfoBuilder) wayCategoryInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.wayCategoryInfoBuilder);
		if (avgSpeedInfo != null)
			((AppendableRouteExtraInfoBuilder) avgSpeedInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.avgSpeedInfoBuilder);
		if (greenInfo != null)
			((AppendableRouteExtraInfoBuilder) greenInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.greenInfoBuilder);
		if (noiseInfo != null)
			((AppendableRouteExtraInfoBuilder) noiseInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.noiseInfoBuilder);
		if (tollwaysInfo != null)
			((AppendableRouteExtraInfoBuilder) tollwaysInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.tollwaysInfoBuilder);
		if (trailDifficultyInfo != null)
			((AppendableRouteExtraInfoBuilder) trailDifficultyInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.trailDifficultyInfoBuilder);
		if (osmIdInfo != null)
			((AppendableRouteExtraInfoBuilder) osmIdInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.osmIdInfoBuilder);
		if (roadAccessRestrictionsInfo != null)
			((AppendableRouteExtraInfoBuilder) roadAccessRestrictionsInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.roadAccessRestrictionsInfoBuilder);
		if (countryTraversalInfoBuilder != null)
			((AppendableRouteExtraInfoBuilder) countryTraversalInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.countryTraversalInfoBuilder);
		if (csvInfo != null)
			((AppendableRouteExtraInfoBuilder) csvInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.csvInfoBuilder);
		if (shadowInfo != null)
			((AppendableRouteExtraInfoBuilder) shadowInfoBuilder).append((AppendableRouteExtraInfoBuilder)more.shadowInfoBuilder);
	}

	@Override
	public void processPathEdge(EdgeIteratorState edge, PointList geom) {
		double dist = edge.getDistance();

		// TODO Add extra info for crossed countries
		if (extCountryTraversalInfo != null && countryBordersReader != null) {
			short country1 = extCountryTraversalInfo.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), BordersGraphStorage.Property.START);
			short country2 = extCountryTraversalInfo.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), BordersGraphStorage.Property.END);
			// This check will correct the countries of an edge if the starting coordinate of the route lies in a different country than the start of the edge.
			if (country1 != country2 && geom.size() > 0) {
				Coordinate coordinate = new Coordinate();
				coordinate.x = geom.getLon(0);
				coordinate.y = geom.getLat(0);
				CountryBordersPolygon[] countries = countryBordersReader.getCountry(coordinate);
				if (countries.length >= 1) {
					country1 = Short.parseShort(countryBordersReader.getId(countryBordersReader.getCountry(coordinate)[0].getName()));
				}
			}
			if (countryTraversalInfoBuilder != null && country1 != 0) {
				countryTraversalInfoBuilder.addSegment(country1, country1, geom, dist);
			}
		}

		if (extWaySurface != null && wayTypeInfo != null || surfaceInfo != null) {
			WaySurfaceDescription wsd = extWaySurface.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);

			if (surfaceInfoBuilder != null)
				surfaceInfoBuilder.addSegment(wsd.getSurfaceType(), wsd.getSurfaceType(), geom, dist);

			if (wayTypeInfo != null)
				wayTypeInfoBuilder.addSegment(wsd.getWayType(), wsd.getWayType(), geom, dist);
		}

		if (wayCategoryInfoBuilder != null) {
			int value = extWayCategory.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			wayCategoryInfoBuilder.addSegment(value, value, geom, dist);
		}

		if (trailDifficultyInfoBuilder != null) {
			int value = 0;
			if (RoutingProfileType.isCycling(profileType)) {
				boolean uphill = false;
				if (extHillIndex != null) {
					boolean revert = edge.getBaseNode() > edge.getAdjNode();
					int hillIndex = extHillIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), revert, buffer);
					if (hillIndex > 0)
						uphill = true;
				}
				value = extTrailDifficulty.getMtbScale(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer, uphill);
			}
			else if (RoutingProfileType.isWalking(profileType))
				value = extTrailDifficulty.getHikingScale(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);

			trailDifficultyInfoBuilder.addSegment(value, value, geom, dist);
		}

		if (avgSpeedInfoBuilder != null) {
		    double speed = encoder.getAverageSpeedEnc().getDecimal(false, edge.getFlags());
		    avgSpeedInfoBuilder.addSegment(speed, (int)Math.round(speed* avgSpeedInfo.getFactor()), geom, dist);
		}

		if (tollwaysInfoBuilder != null) {
			int value = tollwayExtractor.getValue(EdgeIteratorStateHelper.getOriginalEdge(edge));
		    tollwaysInfoBuilder.addSegment(value, value, geom, dist);
		}

		if (waySuitabilityInfoBuilder != null) {
			double priority;
			int priorityIndex;
			if (encoderWithPriority) {
				priority = edge.get(encoder.getDecimalEncodedValue(getKey(encoder, FlagEncoderKeys.PRIORITY_KEY)));
				priorityIndex = (int) Math.round(3 + priority * PriorityCode.BEST.getValue()); // normalize values between 3 and 10
			} else {
				priority = encoder.getAverageSpeedEnc().getDecimal(false, edge.getFlags()) / encoder.getMaxSpeed();
				if (priority < 0.3)
					priority = 0.3;
				priorityIndex = (int) Math.round(priority * 10);
			}
			waySuitabilityInfoBuilder.addSegment(priority, priorityIndex, geom, dist);
		}

		if (greenInfoBuilder != null) {
			int value = extGreenIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			// This number is how many levels client can display in the stats bar
			// FIXME should be changed when the specific bar legend for green routing is finished
			int minClientVal = 3;
			int maxClientVal = 10;
			int clientVal = minClientVal + value * (maxClientVal - minClientVal + 1) / 64;
			greenInfoBuilder.addSegment(value, clientVal, geom, dist);
		}

		if (noiseInfoBuilder != null) {
			int noiseLevel = extNoiseIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			// convert the noise level (from 0 to 3) to the values (from 7 to 10) for the client
			if (noiseLevel > 3)
				noiseLevel = 3;

			int clientNoiseLevel = noiseLevel + 7;
			noiseInfoBuilder.addSegment(noiseLevel, clientNoiseLevel, geom, dist);
		}

		if (osmIdInfoBuilder != null) {
			long osmId = extOsmId.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge));

			osmIdInfoBuilder.addSegment((double)osmId, osmId, geom, dist);
		}

		if (roadAccessRestrictionsInfoBuilder != null) {
			int value = extRoadAccessRestrictions.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			roadAccessRestrictionsInfoBuilder.addSegment(value, value, geom, dist);
		}

		if (csvInfoBuilder != null) {
			int value = extCsvData.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), csvColumn, buffer);
			csvInfoBuilder.addSegment(value, value, geom, dist);
		}

    if (shadowInfoBuilder != null) {
			int shadowLevel = extShadowIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			shadowInfoBuilder.addSegment(shadowLevel, shadowLevel, geom, dist);
		}
	}

	@Override
	public PointList processPoints(PointList points) {
        PointList result = points;

		if (points.is3D())
			result = ElevationSmoother.smooth(points);

		if (steepnessInfoBuilder != null) {
			// compute steepness information only after elevation data is smoothed.
			steepnessInfoBuilder.addPoints(result);
		}

		return result;
	}

	public String getSkippedExtraInfo() {
		return skippedExtraInfo;
	}
}
