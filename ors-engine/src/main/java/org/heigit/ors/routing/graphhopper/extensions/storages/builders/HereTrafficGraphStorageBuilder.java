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

package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.querygraph.VirtualEdgeIteratorState;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import me.tongfei.progressbar.*;
import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.heigit.ors.mapmatching.GhMapMatcher;
import org.heigit.ors.mapmatching.MapMatcher;
import org.heigit.ors.mapmatching.RouteSegmentInfo;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.TrafficRelevantWayType;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.TrafficEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.reader.traffic.*;
import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;
import org.heigit.ors.util.ErrorLoggingUtility;
import org.heigit.ors.util.ProgressBarLogger;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HereTrafficGraphStorageBuilder extends AbstractGraphStorageBuilder {
    static final Logger LOGGER = Logger.getLogger(HereTrafficGraphStorageBuilder.class.getName());
    private int trafficWayType = TrafficRelevantWayType.RelevantWayTypes.UNWANTED.value;

    private static final String PARAM_KEY_OUTPUT_LOG = "output_log";
    private boolean outputLog = false;

    public static final String BUILDER_NAME = "HereTraffic";

    private static final Date date = Calendar.getInstance().getTime();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm");

    private static final String PARAM_KEY_ENABLED = "enabled";
    private static final String PARAM_KEY_STREETS = "streets";
    private static final String PARAM_KEY_PATTERNS_15MINUTES = "pattern_15min";
    private static final String PARAM_KEY_REFERENCE_PATTERN = "ref_pattern";
    private static final String MATCHING_RADIUS = "radius";
    private boolean enabled = true;
    private int matchingRadius = 200;
    String streetsFile = "";
    String patterns15MinutesFile = "";
    String refPatternIdsFile = "";

    private TrafficGraphStorage storage;

    private GraphHopper gh;
    private MapMatcher mMapMatcher;
    private TrafficEdgeFilter trafficEdgeFilter;
    private final IntHashSet matchedHereLinks = new IntHashSet();
    private final ArrayList<String> matchedOSMLinks = new ArrayList<>();
    ProgressBarBuilder progressBar;

    /**
     * Initialize the Here Traffic graph extension <br/><br/>
     * Files required for the process are obtained from the app.config and passed to a CountryBordersReader object
     * which stores information required for the process (i.e. country geometries and border types)
     *
     * @param graphhopper Provide a graphhopper object.
     * @throws Exception Throws an exception if the storag is already initialized.
     */
    @Override
    public GraphExtension init(GraphHopper graphhopper) throws UnsupportedOperationException {
        if (storage != null)
            throw new UnsupportedOperationException("GraphStorageBuilder has been already initialized.");

        if (parameters.containsKey(PARAM_KEY_ENABLED))
            enabled = Boolean.parseBoolean(parameters.get(PARAM_KEY_ENABLED));

        if (enabled) {
            if (parameters.containsKey(PARAM_KEY_STREETS))
                streetsFile = parameters.get(PARAM_KEY_STREETS);
            else {
                ErrorLoggingUtility.logMissingConfigParameter(HereTrafficGraphStorageBuilder.class, PARAM_KEY_STREETS);
            }
            if (parameters.containsKey(PARAM_KEY_PATTERNS_15MINUTES))
                patterns15MinutesFile = parameters.get(PARAM_KEY_PATTERNS_15MINUTES);
            else {
                ErrorLoggingUtility.logMissingConfigParameter(HereTrafficGraphStorageBuilder.class, PARAM_KEY_PATTERNS_15MINUTES);
            }
            if (parameters.containsKey(PARAM_KEY_REFERENCE_PATTERN))
                refPatternIdsFile = parameters.get(PARAM_KEY_REFERENCE_PATTERN);
            else {
                ErrorLoggingUtility.logMissingConfigParameter(HereTrafficGraphStorageBuilder.class, PARAM_KEY_REFERENCE_PATTERN);
            }
            if (parameters.containsKey(PARAM_KEY_OUTPUT_LOG))
                outputLog = Boolean.parseBoolean(parameters.get(PARAM_KEY_OUTPUT_LOG));
            else {
                ErrorLoggingUtility.logMissingConfigParameter(HereTrafficGraphStorageBuilder.class, PARAM_KEY_OUTPUT_LOG);
            }

            if (parameters.containsKey(MATCHING_RADIUS))
                matchingRadius = Integer.parseInt(parameters.get(MATCHING_RADIUS));
            else {
                ErrorLoggingUtility.logMissingConfigParameter(HereTrafficGraphStorageBuilder.class, MATCHING_RADIUS);
                LOGGER.info("The Here matching radius is not set. The default is applied!");
            }
            storage = new TrafficGraphStorage();
        } else {
            LOGGER.info("Traffic not enabled.");
        }

        gh = graphhopper;
        mMapMatcher = new GhMapMatcher(graphhopper, parameters.get("gh_profile"));

        Logger progressBarLogger = ProgressBarLogger.getLogger();

        // Initialize the progress bar with the print stream and the style of the progress bar.
        progressBar = new ProgressBarBuilder()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setUpdateIntervalMillis(5000) // slow update for better visualization and less IO. Avoids % calculation for each element.
                .setConsumer(new DelegatingProgressBarConsumer(progressBarLogger::info));
        return storage;
    }

    @Override
    public void processWay(ReaderWay way) {

        // Reset the trafficWayType
        trafficWayType = TrafficGraphStorage.RoadTypes.IGNORE.value;

        boolean hasHighway = way.hasTag("highway");
        Iterator<Map.Entry<String, Object>> it = way.getProperties();
        while (it.hasNext()) {
            Map.Entry<String, Object> pairs = it.next();
            String key = pairs.getKey();
            String value = pairs.getValue().toString();
            if (hasHighway && key.equals("highway")) {
                trafficWayType = TrafficGraphStorage.getWayTypeFromString(value);
            }
        }
    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        throw new UnsupportedOperationException("Call without coords parameter Not supported.");
    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge, org.locationtech.jts.geom.Coordinate[] coords) {
        if (enabled) {
            short converted = TrafficRelevantWayType.getHereTrafficClassFromOSMRoadType((short) trafficWayType);
            storage.setOrsRoadProperties(edge.getEdge(), TrafficGraphStorage.Property.ROAD_TYPE, converted);
        }
    }

    private void writeLogFiles(TrafficData hereTrafficData) throws SchemaException {
        if (outputLog) {
            LOGGER.info("Write log files.");
            SimpleFeatureType featureType = null;
            featureType = DataUtilities.createType("my", "geom:MultiLineString");
            File osmMatchedFile;
            File hereMatchedFile;
            int decimals = 14;
            GeometryJSON gjson = new GeometryJSON(decimals);
            FeatureJSON featureJSON = new FeatureJSON(gjson);
            osmMatchedFile = new File(dateFormat.format(date) + "_radius_" + matchingRadius + "_OSM_matched_edges_output.geojson");
            hereMatchedFile = new File(dateFormat.format(date) + "_radius_" + matchingRadius + "_Here_matched_edges_output.geojson");

            DefaultFeatureCollection matchedOSMCollection = new DefaultFeatureCollection();
            DefaultFeatureCollection matchedHereCollection = new DefaultFeatureCollection();

            GeometryFactory gf = new GeometryFactory();
            WKTReader reader = new WKTReader(gf);


            SimpleFeatureType finalTYPE = featureType;
            matchedOSMLinks.forEach(value -> {
                try {
                    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(finalTYPE);
                    org.locationtech.jts.geom.Geometry linestring = reader.read(value);
                    featureBuilder.add(linestring);
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    matchedOSMCollection.add(feature);
                } catch (ParseException e) {
                    LOGGER.error("Error adding machedOSMLinks", e);
                }
            });
            for (IntCursor linkID : matchedHereLinks) {
                try {
                    String hereLinkGeometry = hereTrafficData.getLink(linkID.value).getLinkGeometry().toString();
                    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
                    org.locationtech.jts.geom.Geometry linestring = reader.read(hereLinkGeometry);
                    featureBuilder.add(linestring);
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    matchedHereCollection.add(feature);
                } catch (ParseException e) {
                    LOGGER.error("Error adding machedHEreLinks", e);
                }
            }

            if (!matchedOSMCollection.isEmpty()) {
                try {
                    if (osmMatchedFile.createNewFile()) {
                        featureJSON.writeFeatureCollection(matchedOSMCollection, osmMatchedFile);
                    } else {
                        LOGGER.error("Error creating log file for matched OSM data.");
                    }
                } catch (IOException e) {
                    LOGGER.error("Error writing matched OSM data to log file.", e);
                }
            }
            if (!matchedHereCollection.isEmpty()) {
                try {
                    if (hereMatchedFile.createNewFile()) {
                        featureJSON.writeFeatureCollection(matchedHereCollection, hereMatchedFile);
                    } else {
                        LOGGER.error("Error creating log file for matched Here data.");
                    }
                } catch (IOException e) {
                    LOGGER.error("Error writing matched Here data to log file.", e);
                }
            }
        }
    }

    /**
     * Method identifying the name of the extension which is used in various building processes
     *
     * @return The name of this extension.
     */
    @Override
    public String getName() {
        return BUILDER_NAME;
    }

    public void addHereSegmentForLogging(Integer linkID) {
        matchedHereLinks.add(linkID);
    }

    private int getMatchedHereLinksCount() {
        return matchedHereLinks.size();
    }

    public void addOSMGeometryForLogging(String osmGeometry) {
        matchedOSMLinks.add(osmGeometry);
    }

    private RouteSegmentInfo[] matchLinkToSegments(ORSGraphHopper graphHopper, int trafficLinkFunctionalClass,
                                                   double originalTrafficLinkLength, Geometry geometry, boolean bothDirections) {
        RouteSegmentInfo[] matchedSegments = new RouteSegmentInfo[0];
        if (geometry == null) {
            LOGGER.info("Teadrop node.");
            return matchedSegments;
        }
        try {
            matchedSegments = getMatchedSegmentsInternal(geometry, originalTrafficLinkLength, trafficLinkFunctionalClass, bothDirections, matchingRadius);
        } catch (Exception e) {
            if (e.getMessage().startsWith("Sequence is broken for submitted track"))
                LOGGER.debug("Error while matching: " + e);
            else
                LOGGER.warn("Error while matching: " + e);
        }
        return matchedSegments;
    }

    public void postProcess(ORSGraphHopper graphHopper) throws SchemaException {
        HereTrafficReader hereTrafficReader = new HereTrafficReader(streetsFile, patterns15MinutesFile, refPatternIdsFile);
        if (enabled && !storage.isMatched()) {
            try {
                hereTrafficReader.readData();
            } catch (IOException e) {
                LOGGER.error("Severe error reading " + HereTrafficReader.class, e);
                return;
            }
            if (hereTrafficReader.isInitialized()) {
                LOGGER.info("Starting MapMatching traffic data");
                processTrafficPatterns(hereTrafficReader.getHereTrafficData().getPatterns());
                processLinks(graphHopper, hereTrafficReader.getHereTrafficData().getLinks());
                storage.setMaxTrafficSpeeds();
                storage.setMatched();
                storage.flush();
                LOGGER.info("Flush and lock storage.");
                writeLogFiles(hereTrafficReader.getHereTrafficData());
                LOGGER.info("Traffic data successfully processed");
            } else {
                throw new MissingResourceException("Here traffic is not build, enabled but the Here data sets couldn't be initialized. Make sure the config contains the path variables and they're correct.", this.getClass().toString(), "streets || pattern_15min || ref_pattern");
            }
        } else if (!enabled) {
            LOGGER.debug("Traffic not enabled or already matched. Skipping match making.");
        } else {
            LOGGER.info("Traffic data already matched. Skipping match making.");
        }
    }

    private void processTrafficPatterns(IntObjectHashMap<TrafficPattern> patterns) {
        try (ProgressBar pb = progressBar.setInitialMax(patterns.size()).setTaskName("Processing Here traffic patterns").build()) {
            for (ObjectCursor<TrafficPattern> pattern : patterns.values()) {
                storage.setTrafficPatterns(pattern.value.getPatternId(), pattern.value.getValues());
                pb.step();
            }
        } catch (Exception e) {
            LOGGER.error("Error processing here traffic patterns with error: " + e);
        } finally {
            LOGGER.info("Processed " + storage.getPatternCount() + " traffic patterns");
        }
    }

    private void processLinks(ORSGraphHopper graphHopper, IntObjectHashMap<TrafficLink> links) {
        int trafficLinksCount = links.values().size();
        try (ProgressBar pb = progressBar.setInitialMax(links.size()).setTaskName("Matching Here links").build()) {
            for (ObjectCursor<TrafficLink> trafficLink : links.values()) {
                processLink(graphHopper, trafficLink.value);
                pb.step();
            }
        } catch (Exception e) {
            LOGGER.error("Error processing here traffic links with error: " + e);
        } finally {
            LOGGER.info("Matched " + 100 * getMatchedHereLinksCount() / trafficLinksCount + "% Here links (" + getMatchedHereLinksCount() + " out of " + trafficLinksCount + ")");
        }
    }

    private void processLink(ORSGraphHopper graphHopper, TrafficLink hereTrafficLink) {
        if (hereTrafficLink == null || !hereTrafficLink.isPotentialTrafficSegment())
            return;
        RouteSegmentInfo[] matchedSegmentsFrom = new RouteSegmentInfo[]{};
        RouteSegmentInfo[] matchedSegmentsTo = new RouteSegmentInfo[]{};

        if (hereTrafficLink.isBothDirections()) {
            // Both Directions
            // Split
            matchedSegmentsFrom = matchLinkToSegments(graphHopper, hereTrafficLink.getFunctionalClass(), hereTrafficLink.getLinkLength(), hereTrafficLink.getFromGeometry(), false);
            matchedSegmentsTo = matchLinkToSegments(graphHopper, hereTrafficLink.getFunctionalClass(), hereTrafficLink.getLinkLength(), hereTrafficLink.getToGeometry(), false);
        } else if (hereTrafficLink.isOnlyFromDirection()) {
            // One Direction
            matchedSegmentsFrom = matchLinkToSegments(graphHopper, hereTrafficLink.getFunctionalClass(), hereTrafficLink.getLinkLength(), hereTrafficLink.getFromGeometry(), false);
        } else {
            // One Direction
            matchedSegmentsTo = matchLinkToSegments(graphHopper, hereTrafficLink.getFunctionalClass(), hereTrafficLink.getLinkLength(), hereTrafficLink.getToGeometry(), false);
        }

        processSegments(graphHopper, hereTrafficLink.getLinkId(), hereTrafficLink.getTrafficPatternIds(TrafficEnums.TravelDirection.FROM), matchedSegmentsFrom);
        processSegments(graphHopper, hereTrafficLink.getLinkId(), hereTrafficLink.getTrafficPatternIds(TrafficEnums.TravelDirection.TO), matchedSegmentsTo);
    }

    private void processSegments(GraphHopper gh, int linkId, Map<TrafficEnums.WeekDay, Integer> trafficPatternIds, RouteSegmentInfo[] matchedSegments) {
        if (matchedSegments == null)
            return;
        for (RouteSegmentInfo routeSegment : matchedSegments) {
            if (routeSegment == null) continue;
            processSegment(gh, trafficPatternIds, linkId, routeSegment);
        }
    }

    private void processSegment(GraphHopper gh, Map<TrafficEnums.WeekDay, Integer> trafficPatternIds,
                                int trafficLinkId, RouteSegmentInfo routeSegment) {
        for (EdgeIteratorState edge : routeSegment.getEdgesStates()) {
            int originalEdgeKey;
            if (edge instanceof VirtualEdgeIteratorState iteratorState) {
                originalEdgeKey = iteratorState.getOriginalEdgeKey();
            } else {
                originalEdgeKey = edge.getEdgeKey();
            }
            final int priority = (int) Math.round(edge.getDistance() / gh.getGraphHopperStorage().getEdgeIteratorStateForKey(originalEdgeKey).getDistance() * 255);
            trafficPatternIds.forEach((weekDay, patternId) -> storage.setEdgeIdTrafficPatternLookup(originalEdgeKey, patternId, weekDay, priority));
            addHereSegmentForLogging(trafficLinkId);
            if (outputLog) {
                LineString lineString = edge.fetchWayGeometry(FetchMode.ALL).toLineString(false);
                addOSMGeometryForLogging(lineString.toString());
            }
        }
    }

    public RouteSegmentInfo[] getMatchedSegmentsInternal(Geometry geometry,
                                                         double originalTrafficLinkLength,
                                                         int trafficLinkFunctionalClass,
                                                         boolean bothDirections,
                                                         int matchingRadius) {

        if (trafficEdgeFilter == null) {
            trafficEdgeFilter = new TrafficEdgeFilter(gh.getGraphHopperStorage());
            mMapMatcher.setEdgeFilter(trafficEdgeFilter);
        }
        trafficEdgeFilter.setHereFunctionalClass(trafficLinkFunctionalClass);

        RouteSegmentInfo[] routeSegmentInfos;
        mMapMatcher.setSearchRadius(matchingRadius);
        routeSegmentInfos = matchInternalSegments(geometry, originalTrafficLinkLength, bothDirections);
        for (RouteSegmentInfo routeSegmentInfo : routeSegmentInfos) {
            if (routeSegmentInfo != null) {
                return routeSegmentInfos;
            }
        }
        return routeSegmentInfos;
    }

    private RouteSegmentInfo[] matchInternalSegments(Geometry geometry, double originalTrafficLinkLength, boolean bothDirections) {

        org.locationtech.jts.geom.Coordinate[] locations = geometry.getCoordinates();
        int originalFunctionalClass = trafficEdgeFilter.getHereFunctionalClass();
        try {
            RouteSegmentInfo[] match = mMapMatcher.match(locations, bothDirections);
            match = validateRouteSegment(originalTrafficLinkLength, match);

            if (match.length <= 0 && (originalFunctionalClass != TrafficRelevantWayType.RelevantWayTypes.CLASS1.value && originalFunctionalClass != TrafficRelevantWayType.RelevantWayTypes.CLASS1LINK.value)) {
                // Test a higher functional class based from the original class
                //            ((TrafficEdgeFilter) edgeFilter).setHereFunctionalClass(originalFunctionalClass);
                trafficEdgeFilter.higherFunctionalClass();
                mMapMatcher.setEdgeFilter(trafficEdgeFilter);
                match = mMapMatcher.match(locations, bothDirections);
                match = validateRouteSegment(originalTrafficLinkLength, match);
            }
            if (match.length <= 0 && (originalFunctionalClass != TrafficRelevantWayType.RelevantWayTypes.UNCLASSIFIED.value && originalFunctionalClass != TrafficRelevantWayType.RelevantWayTypes.CLASS4LINK.value)) {
                // Try matching in the next lower functional class.
                trafficEdgeFilter.setHereFunctionalClass(originalFunctionalClass);
                trafficEdgeFilter.lowerFunctionalClass();
                mMapMatcher.setEdgeFilter(trafficEdgeFilter);
                match = mMapMatcher.match(locations, bothDirections);
                match = validateRouteSegment(originalTrafficLinkLength, match);
            }
            if (match.length <= 0 && (originalFunctionalClass != TrafficRelevantWayType.RelevantWayTypes.UNCLASSIFIED.value && originalFunctionalClass != TrafficRelevantWayType.RelevantWayTypes.CLASS4LINK.value)) {
                // But always try UNCLASSIFIED before. CLASS5 hast way too many false-positives!
                trafficEdgeFilter.setHereFunctionalClass(TrafficRelevantWayType.RelevantWayTypes.UNCLASSIFIED.value);
                mMapMatcher.setEdgeFilter(trafficEdgeFilter);
                match = mMapMatcher.match(locations, bothDirections);
                match = validateRouteSegment(originalTrafficLinkLength, match);
            }
            if (match.length <= 0 && (originalFunctionalClass == TrafficRelevantWayType.RelevantWayTypes.UNCLASSIFIED.value || originalFunctionalClass == TrafficRelevantWayType.RelevantWayTypes.CLASS4LINK.value || originalFunctionalClass == TrafficRelevantWayType.RelevantWayTypes.CLASS1.value)) {
                // If the first tested class was unclassified, try CLASS5. But always try UNCLASSIFIED before. CLASS5 hast way too many false-positives!
                trafficEdgeFilter.setHereFunctionalClass(TrafficRelevantWayType.RelevantWayTypes.CLASS5.value);
                mMapMatcher.setEdgeFilter(trafficEdgeFilter);
                match = mMapMatcher.match(locations, bothDirections);
                match = validateRouteSegment(originalTrafficLinkLength, match);
            }
            return match;
        } catch (IllegalArgumentException e) {
            // Graphhopper throws an IllegalArgumentException when the matching fails. This is to be expected when matching here traffic on osm files without the corresponding edges.
            // The exception is caught and logged as a trace to avoid cluttering the log with expected exceptions.
            LOGGER.trace("Error while matching: " + e);
            return new RouteSegmentInfo[]{};
        } catch (Exception e) {
            LOGGER.error("Error while matching: " + e);
            return new RouteSegmentInfo[]{};
        }
    }

    private RouteSegmentInfo[] validateRouteSegment(double originalTrafficLinkLength, RouteSegmentInfo[] routeSegmentInfo) {
        if (routeSegmentInfo == null || routeSegmentInfo.length == 0)
            // Cases that shouldn't happen while matching Here data correctly. Return empty array to potentially restart the matching.
            return new RouteSegmentInfo[]{};
        int nullCounter = 0;
        for (int i = 0; i < routeSegmentInfo.length; i++) {
            if (routeSegmentInfo[i] == null || routeSegmentInfo[i].getEdgesStates() == null) {
                nullCounter += 1;
                break;
            }
            RouteSegmentInfo routeSegment = routeSegmentInfo[i];
            if (routeSegment.getDistance() > (originalTrafficLinkLength * 1.8)) {
                // Worst case scenario!
                routeSegmentInfo[i] = null;
                nullCounter += 1;
            }
        }

        if (nullCounter == routeSegmentInfo.length)
            return new RouteSegmentInfo[]{};
        else
            return routeSegmentInfo;
    }

    private static class setTaskName {
        public setTaskName(String matchingHereLinks) {
        }
    }
}
