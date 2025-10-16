package org.heigit.ors.matching;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ev.RoadEnvironment;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.querygraph.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.DefaultSnapFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
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
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.AvoidBordersEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.heigit.ors.util.ProfileTools;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MatchingRequest extends ServiceRequest {
    private static final Logger LOGGER = Logger.getLogger(MatchingRequest.class);

    @Setter
    @Getter
    private String profileName;

    @Getter
    private final int profileType;

    @Getter
    @Setter
    private Geometry geometry;

    public MatchingRequest(int profileType) {
        this.profileType = profileType;
    }

    public MatchingResult computeResult(RoutingProfile rp) throws Exception {
        GraphHopper gh = rp.getGraphhopper();
        GraphHopperStorage ghStorage = gh.getGraphHopperStorage();
        String encoderName = RoutingProfileType.getEncoderName(getProfileType());
        PMap hintsMap = new PMap();
        ProfileTools.setWeightingMethod(hintsMap, WeightingMethod.RECOMMENDED, getProfileType(), false);
        ProfileTools.setWeighting(hintsMap, WeightingMethod.RECOMMENDED, getProfileType(), false);
        String localProfileName = ProfileTools.makeProfileName(encoderName, hintsMap.getString("weighting", ""), false);
        Weighting weighting = new ORSWeightingFactory(ghStorage, gh.getEncodingManager()).createWeighting(gh.getProfile(localProfileName), hintsMap, false);
        LocationIndex locIndex = gh.getLocationIndex();
        EdgeFilter snapFilter = new DefaultSnapFilter(weighting, ghStorage.getEncodingManager().getBooleanEncodedValue(Subnetwork.key(localProfileName)));
        var mapMatcher = new GhMapMatcher(gh, localProfileName);

        int maxDistance = 500; // TODO: make configurable

        Set<Integer> matchedEdgeIDs = new HashSet<>();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry geom = geometry.getGeometryN(i);
            LOGGER.debug("Matching geometry at index " + i + ": " + geom);
            if (geom.isEmpty()) {
                throw new IllegalArgumentException("Geometry at index " + i + " is empty.");
            }
            switch (geom.getGeometryType()) {
                case "Point", "MultiPoint":
                    matchPoint(geom, locIndex, ghStorage, snapFilter, matchedEdgeIDs, maxDistance);
                    break;
                case "LineString", "MultiLineString":
                    matchLine(mapMatcher, geom, ghStorage, matchedEdgeIDs);
                    break;
                case "Polygon", "MultiPolygon":
                    matchArea(geom, locIndex, ghStorage, matchedEdgeIDs);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported geometry type: " + geom.getGeometryType());
            }
        }
        return new MatchingResult(ghStorage.getProperties().get("datareader.import.date"), matchedEdgeIDs.stream().toList());
    }

    private static void matchPoint(Geometry geom, LocationIndex locIndex, GraphHopperStorage ghStorage, EdgeFilter snapFilter, Set<Integer> matchedEdgeIDs, int maxDistance) {
        for (int j = 0; j < geom.getNumGeometries(); j++) {
            Geometry point = geom.getGeometryN(j);
            Coordinate p = point.getCoordinate();
            EdgeFilterSequence edgeFilter = new EdgeFilterSequence();
            edgeFilter.add(snapFilter);
            var properties = geom.getUserData();
            if (properties != null && properties instanceof java.util.Map<?, ?> propertiesMap) {
                var featureType = propertiesMap.get("type");
                switch (String.valueOf(featureType)) {
                    case "bridge":
                        var roadEnvironmentEnc = ghStorage.getEncodingManager().getEnumEncodedValue(RoadEnvironment.KEY, RoadEnvironment.class);
                        if (roadEnvironmentEnc != null) {
                            edgeFilter.add(edgeState -> edgeState.get(roadEnvironmentEnc) == RoadEnvironment.BRIDGE);
                            LOGGER.trace("Applying bridge filter for snapping.");
                        } else {
                            LOGGER.trace("road_environment encoded value not found, cannot apply bridge filter for snapping.");
                        }
                        break;
                    case "border":
                        BordersGraphStorage extBorders = GraphStorageUtils.getGraphExtension(ghStorage, BordersGraphStorage.class);
                        if (extBorders != null) {
                            RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
                            routeSearchParameters.setAvoidBorders(BordersExtractor.Avoid.ALL);
                            EdgeFilter borderFilter = new AvoidBordersEdgeFilter(routeSearchParameters, extBorders);
                            edgeFilter.add(edgeState -> !borderFilter.accept(edgeState));
                            LOGGER.trace("Applying border filter for snapping.");
                        } else {
                            LOGGER.trace("BordersGraphStorage not found, cannot apply border filter for snapping.");
                        }
                        break;
                    default:
                        LOGGER.trace("Missing or unknown feature type, no special filter applied for snapping.");
                        break;
                }
            }
            Snap snappedPoint = locIndex.findClosest(p.y, p.x, edgeFilter);
            if (snappedPoint.isValid() && snappedPoint.getQueryDistance() < maxDistance) {
                var edge = snappedPoint.getClosestEdge();
                var edgeId = edge.getEdge();
                matchedEdgeIDs.add(edgeId);
                LOGGER.trace("Snap: " + edgeId);
            } else {
                LOGGER.trace("No valid snap found for point: " + p);
            }
        }
    }

    private static void matchLine(GhMapMatcher mapMatcher, Geometry geom, GraphHopperStorage ghStorage, Set<Integer> matchedEdgeIDs) {
        for (int j = 0; j < geom.getNumGeometries(); j++) {
            Geometry line = geom.getGeometryN(j);
            try {
                RouteSegmentInfo[] match = mapMatcher.match(line.getCoordinates(), false);
                for (RouteSegmentInfo segment : match) {
                    for (EdgeIteratorState edge : segment.getEdgesStates()) {
                        int originalEdgeKey;
                        if (edge instanceof VirtualEdgeIteratorState iteratorState) {
                            originalEdgeKey = iteratorState.getOriginalEdgeKey();
                            LOGGER.trace("Matched virtual edge: " + edge.getEdge() + " with geometry: " + iteratorState.fetchWayGeometry(FetchMode.ALL).toLineString(false));
                            edge = ghStorage.getEdgeIteratorStateForKey(originalEdgeKey);
                        } else {
                            LOGGER.trace("Matched edge: " + edge.getEdge() + " with geometry: " + edge.fetchWayGeometry(FetchMode.ALL).toLineString(false));
                        }
                        var edgeId = edge.getEdge();
                        matchedEdgeIDs.add(edgeId);
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("matchLine failed: " + e.getMessage() + " for geometry: " + geom);
            }
        }
    }

    private static void matchArea(Geometry geom, LocationIndex locIndex, GraphHopperStorage ghStorage, Set<Integer> matchedEdgeIDs) {
        var envelope = geom.getEnvelopeInternal();
        var bbox = new BBox(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());

        locIndex.query(bbox, edgeId -> {
            EdgeIteratorState edge = ghStorage.getEdgeIteratorState(edgeId, Integer.MIN_VALUE);
            var lineString = edge.fetchWayGeometry(FetchMode.ALL).toLineString(false);
            if (geom.intersects(lineString)) {
                matchedEdgeIDs.add(edgeId);
                LOGGER.trace("Matched edge: " + edgeId);
            }
        });
    }

    public record MatchingResult(String graphTimestamp, List<Integer> matched) {
    }
}
