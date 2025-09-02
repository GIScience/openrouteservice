package org.heigit.ors.matching;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.querygraph.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.DefaultSnapFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PMap;
import com.graphhopper.util.shapes.BBox;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.heigit.ors.common.ServiceRequest;
import org.heigit.ors.mapmatching.GhMapMatcher;
import org.heigit.ors.mapmatching.RouteSegmentInfo;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import org.heigit.ors.routing.graphhopper.extensions.ev.DynamicData;
import org.heigit.ors.util.ProfileTools;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchingRequest extends ServiceRequest {
    private static final Logger LOGGER = Logger.getLogger(RoutingProfile.class);

    @Setter
    @Getter
    private String profileName;

    @Getter
    private final int profileType;

    @Getter
    @Setter
    private String key;

    @Getter
    @Setter
    private Geometry geometry;

    @Getter
    @Setter
    List<Map<String, String>> properties;

    public MatchingRequest(int profileType) {
        this.profileType = profileType;
    }

    public MatchingResult computeResult(RoutingProfile rp) throws Exception {
        GraphHopper gh = rp.getGraphhopper();
        GraphHopperStorage ghStorage = gh.getGraphHopperStorage();
        String encoderName = RoutingProfileType.getEncoderName(getProfileType());
        PMap hintsMap = new PMap();
        int weightingMethod = WeightingMethod.RECOMMENDED; // Only needed to create the profile string
        ProfileTools.setWeightingMethod(hintsMap, weightingMethod, getProfileType(), false);
        ProfileTools.setWeighting(hintsMap, weightingMethod, getProfileType(), false);
        String localProfileName = ProfileTools.makeProfileName(encoderName, hintsMap.getString("weighting", ""), false);
        Weighting weighting = new ORSWeightingFactory(ghStorage, gh.getEncodingManager()).createWeighting(gh.getProfile(localProfileName), hintsMap, false);
        LocationIndex locIndex = gh.getLocationIndex();
        EdgeFilter snapFilter = new DefaultSnapFilter(weighting, ghStorage.getEncodingManager().getBooleanEncodedValue(Subnetwork.key(localProfileName)));
        var mapMatcher = new GhMapMatcher(gh, localProfileName);


        List<Map<Integer, Map<String, String>>> edgePropertiesList = new ArrayList<>();
        List<Map<Integer, EdgeIteratorState>> matchedEdgesList = new ArrayList<>();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Map<Integer, Map<String, String>> edgeProperties = new HashMap<>();
            Map<Integer, EdgeIteratorState> matchedEdges = new HashMap<>();
            Geometry geom = geometry.getGeometryN(i);
            LOGGER.debug("Matching geometry at index " + i + ": " + geom);
            if (geom.isEmpty()) {
                throw new IllegalArgumentException("Geometry at index " + i + " is empty.");
            }
            switch (geom.getGeometryType()) {
                case "Point":
                    Coordinate p = geom.getCoordinate();
                    // TODO: improve snapping to consider the type of the point (border, bridge, etc.)
                    Snap snappedPoint = locIndex.findClosest(p.y, p.x, snapFilter);
                    if (snappedPoint.isValid()) {
                        var edge = snappedPoint.getClosestEdge();
                        var edgeId = edge.getEdge();
                        edgeProperties.put(edgeId, properties.get(i));
                        matchedEdges.put(edgeId, edge);
                        LOGGER.trace("Snap: " + edgeId);
                    } else {
                        LOGGER.warn("No valid snap found for point: " + p);
                    }
                    break;
                case "LineString":
                    RouteSegmentInfo[] match = mapMatcher.match(geom.getCoordinates(), false);
                    for (RouteSegmentInfo segment : match) {
                        for (EdgeIteratorState edge : segment.getEdgesStates()) {
                            int originalEdgeKey;
                            if (edge instanceof VirtualEdgeIteratorState iteratorState) {
                                originalEdgeKey = iteratorState.getOriginalEdgeKey();
                                LOGGER.trace("Matched virtual edge: " + edge.getEdge() + " with geometry: " + iteratorState.fetchWayGeometry(FetchMode.ALL).toLineString(false));
                                edge = ghStorage.getEdgeIteratorStateForKey(originalEdgeKey);
                            }
                            else {
                                LOGGER.trace("Matched edge: " + edge.getEdge() + " with geometry: " + edge.fetchWayGeometry(FetchMode.ALL).toLineString(false));
                            }
                            var edgeId = edge.getEdge();
                            edgeProperties.put(edgeId, properties.get(i));
                            matchedEdges.put(edgeId, edge);
                        }
                    }
                    break;
                case "Polygon":
                case "MultiPolygon":
                    var envelope = geom.getEnvelopeInternal();
                    var bbox = new BBox(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());

                    int finalI = i;
                    locIndex.query(bbox, edgeId -> {
                        EdgeIteratorState edge = ghStorage.getEdgeIteratorState(edgeId, Integer.MIN_VALUE);
                        var lineString = edge.fetchWayGeometry(FetchMode.ALL).toLineString(false);
                        if (geom.intersects(lineString)) {
                            edgeProperties.put(edgeId, properties.get(finalI));
                            matchedEdges.put(edgeId, edge);
                            LOGGER.trace("Matched edge: " + edgeId);
                        }
                    });
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported geometry type: " + geom.getGeometryType());
            }
            edgePropertiesList.add(edgeProperties);
            matchedEdgesList.add(matchedEdges);
        }

        // TODO: refactor: the branches of this switch statements contain almost identical code.
        // TODO:   Unfortunately it is not obvious how to refactor this due to EnumEncodedValue<E>
        // TODO:   depending on an enum E.
        switch (key) {
            case LogieBorders.KEY:
                EnumEncodedValue<LogieBorders> bordersEnc = gh.getEncodingManager().getEnumEncodedValue(LogieBorders.KEY, LogieBorders.class);
                if (bordersEnc == null) {
                    throw new IllegalStateException("Dynamic data '" + LogieBorders.KEY + "' is not available for the profile: " + localProfileName);
                }
                for (int i = 0; i < matchedEdgesList.size(); i++) {
                    Map<Integer, EdgeIteratorState> matchedEdges = matchedEdgesList.get(i);
                    for (Map.Entry<Integer,EdgeIteratorState> edge : matchedEdges.entrySet()) {
                        IntsRef edgeFlags = edge.getValue().getFlags();
                        try {
                            LogieBorders borderState = LogieBorders.valueOf(edgePropertiesList.get(i).get(edge.getKey()).get("value"));
                            bordersEnc.setEnum(false, edgeFlags, borderState);
                            edge.getValue().setFlags(edgeFlags);
                        } catch (IllegalArgumentException | NullPointerException e) {
                            // do nothing
                        }
                    }
                }
                break;
            case LogieBridges.KEY:
                EnumEncodedValue<LogieBridges> bridgesEnc = gh.getEncodingManager().getEnumEncodedValue(LogieBridges.KEY, LogieBridges.class);
                if (bridgesEnc == null) {
                    throw new IllegalStateException("Dynamic data '" + LogieBorders.KEY + "' is not available for the profile: " + localProfileName);
                }
                for (int i = 0; i < matchedEdgesList.size(); i++) {
                    Map<Integer, EdgeIteratorState> matchedEdges = matchedEdgesList.get(i);
                    for (Map.Entry<Integer,EdgeIteratorState> edge : matchedEdges.entrySet()) {
                        IntsRef edgeFlags = edge.getValue().getFlags();
                        try {
                            LogieBridges bridgesState = LogieBridges.valueOf(edgePropertiesList.get(i).get(edge.getKey()).get("value"));
                            bridgesEnc.setEnum(false, edgeFlags, bridgesState);
                            edge.getValue().setFlags(edgeFlags);
                        } catch (IllegalArgumentException | NullPointerException e) {
                            // do nothing
                        }
                    }
                }
                break;
            case LogieRoads.KEY:
                EnumEncodedValue<LogieRoads> roadsEnc = gh.getEncodingManager().getEnumEncodedValue(LogieRoads.KEY, LogieRoads.class);
                if (roadsEnc == null) {
                    throw new IllegalStateException("Dynamic data '" + LogieBorders.KEY + "' is not available for the profile: " + localProfileName);
                }
                for (int i = 0; i < matchedEdgesList.size(); i++) {
                    Map<Integer, EdgeIteratorState> matchedEdges = matchedEdgesList.get(i);
                    for (Map.Entry<Integer,EdgeIteratorState> edge : matchedEdges.entrySet()) {
                        IntsRef edgeFlags = edge.getValue().getFlags();
                        try {
                            LogieRoads roadsState = LogieRoads.valueOf(edgePropertiesList.get(i).get(edge.getKey()).get("value"));
                            roadsEnc.setEnum(false, edgeFlags, roadsState);
                            edge.getValue().setFlags(edgeFlags);
                        } catch (IllegalArgumentException | NullPointerException e) {
                            // do nothing
                        }
                    }
                }
                break;
            default:
                // do nothing
                break;
        }
        { // TODO: remove this code block when not needed as reference any more
            BooleanEncodedValue dynamicData = gh.getEncodingManager().getBooleanEncodedValue(EncodingManager.getKey(encoderName, DynamicData.KEY));
            if (dynamicData == null) {
                throw new IllegalStateException("Dynamic data is not available for the profile: " + localProfileName);
            }
            for (Map<Integer, EdgeIteratorState> matchedEdges : matchedEdgesList) {
                for (EdgeIteratorState edge : matchedEdges.values()) {
                    IntsRef edgeFlags = edge.getFlags();
                    dynamicData.setBool(false, edgeFlags, true);
                    edge.setFlags(edgeFlags);
                }

            }
        }
        String graphDate = ghStorage.getProperties().get("datareader.import.date");
        return new MatchingResult(graphDate, matchedEdgesList.stream().map(Map::size).toList());
    }
}
