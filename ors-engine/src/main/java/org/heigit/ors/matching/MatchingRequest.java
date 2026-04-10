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
import org.heigit.ors.util.AppInfo;
import org.heigit.ors.util.ProfileTools;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import java.util.*;

public class MatchingRequest extends ServiceRequest {
    private static final Logger LOGGER = Logger.getLogger(MatchingRequest.class);
    private final int maximumSearchRadius;

    @Setter
    @Getter
    private String profileName;

    @Getter
    private final int profileType;

    @Getter
    @Setter
    private Geometry geometry;

    private GraphHopperStorage ghStorage;


    public MatchingRequest(int profileType, int maximumSearchRadius) {
        this.profileType = profileType;
        this.maximumSearchRadius = maximumSearchRadius;
    }

    public MatchingResult computeResult(RoutingProfile rp) throws Exception {
        GraphHopper gh = rp.getGraphhopper();
        ghStorage = gh.getGraphHopperStorage();
        String encoderName = RoutingProfileType.getEncoderName(getProfileType());
        PMap hintsMap = new PMap();
        ProfileTools.setWeightingMethod(hintsMap, WeightingMethod.RECOMMENDED, getProfileType(), false);
        ProfileTools.setWeighting(hintsMap, WeightingMethod.RECOMMENDED, getProfileType(), false);
        String localProfileName = ProfileTools.makeProfileName(encoderName, hintsMap.getString("weighting", ""), false);
        Weighting weighting = new ORSWeightingFactory(ghStorage, gh.getEncodingManager()).createWeighting(gh.getProfile(localProfileName), hintsMap, false);
        LocationIndex locIndex = gh.getLocationIndex();
        EdgeFilter snapFilter = new DefaultSnapFilter(weighting, ghStorage.getEncodingManager().getBooleanEncodedValue(Subnetwork.key(localProfileName)));

        if (geometry == null || geometry.isEmpty()) {
            throw new IllegalArgumentException("No geometry provided for matching.");
        }
        List<Set<Integer>> matchedEdgeIDs = new ArrayList<>();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry geom = geometry.getGeometryN(i);
            matchedEdgeIDs.add(new HashSet<>());

            LOGGER.trace("Matching geometry at index " + i + ": " + geom);
            if (geom.isEmpty()) {
                throw new IllegalArgumentException("Geometry at index " + i + " is empty.");
            }
            switch (geom.getGeometryType()) {
                case "Point", "MultiPoint":
                    matchPoint(geom, locIndex, snapFilter, maximumSearchRadius, matchedEdgeIDs.get(i));
                    break;
                case "LineString", "MultiLineString":
                    matchLine(geom, new GhMapMatcher(gh, localProfileName), matchedEdgeIDs.get(i));
                    break;
                case "Polygon", "MultiPolygon":
                    matchArea(geom, locIndex, matchedEdgeIDs.get(i));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported geometry type: " + geom.getGeometryType());
            }
        }
        return new MatchingResult(ghStorage.getProperties().get("datareader.import.date"), AppInfo.VERSION, matchedEdgeIDs);
    }

    private void matchPoint(Geometry geom, LocationIndex locIndex, EdgeFilter snapFilter, int maxDistance, Set<Integer> matchedIds) {
        for (int j = 0; j < geom.getNumGeometries(); j++) {
            Geometry point = geom.getGeometryN(j);
            Coordinate p = point.getCoordinate();
            EdgeFilter edgeFilter = createEdgeFilter(geom, ghStorage, snapFilter);
            snapPointToEdge(p, locIndex, edgeFilter, maxDistance, matchedIds);
        }
    }

    private static EdgeFilter createEdgeFilter(Geometry geom, GraphHopperStorage ghStorage, EdgeFilter snapFilter) {
        EdgeFilterSequence edgeFilter = new EdgeFilterSequence().add(snapFilter);
        var properties = geom.getUserData();
        if (properties instanceof Map<?, ?> propertiesMap) {
            Object typeObj = propertiesMap.get("type");
            if (typeObj == null) {
                LOGGER.trace("Missing feature type, no special filter applied for snapping.");
            } else {
                String featureType = typeObj.toString();
                switch (featureType) {
                    case "bridge" -> addBridgeFilter(edgeFilter, ghStorage);
                    case "border" -> addBorderFilter(edgeFilter, ghStorage);
                    default -> LOGGER.trace("Unknown feature type '" + featureType + "', no special filter applied for snapping.");
                }
            }
        }
        return edgeFilter;
    }

    private static void addBridgeFilter(EdgeFilterSequence edgeFilter, GraphHopperStorage ghStorage) {
        var roadEnvironmentEnc = ghStorage.getEncodingManager().getEnumEncodedValue(RoadEnvironment.KEY, RoadEnvironment.class);
        if (roadEnvironmentEnc != null) {
            edgeFilter.add(edgeState -> edgeState.get(roadEnvironmentEnc) == RoadEnvironment.BRIDGE);
            LOGGER.trace("Applying bridge filter for snapping.");
        } else {
            LOGGER.trace("road_environment encoded value not found, cannot apply bridge filter for snapping.");
        }
    }

    private static void addBorderFilter(EdgeFilterSequence edgeFilter, GraphHopperStorage ghStorage) {
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
    }

    private void snapPointToEdge(Coordinate p, LocationIndex locIndex, EdgeFilter edgeFilter, int maxDistance, Set<Integer> matchedIds) {
        Snap snappedPoint = locIndex.findClosest(p.y, p.x, edgeFilter);
        if (!snappedPoint.isValid() || snappedPoint.getQueryDistance() > maxDistance) {
            LOGGER.trace("No valid edge found for point: " + p);
            return;
        }
        addMatchedEdge(snappedPoint.getClosestEdge(), matchedIds);
    }

    private void matchLine(Geometry geom, GhMapMatcher mapMatcher, Set<Integer> matchedIds) {
        for (int j = 0; j < geom.getNumGeometries(); j++) {
            Geometry line = geom.getGeometryN(j);
            try {
                RouteSegmentInfo[] match = mapMatcher.match(line.getCoordinates(), false);
                for (RouteSegmentInfo segment : match) {
                    for (EdgeIteratorState edge : segment.getEdgesStates()) {
                        addMatchedEdge(edge, matchedIds);
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("matchLine failed: " + e.getMessage() + " for geometry: " + geom);
            }
        }
    }

    private void addMatchedEdge(EdgeIteratorState edge, Set<Integer> matchedIds) {
        int originalEdgeKey;
        if (edge instanceof VirtualEdgeIteratorState iteratorState) {
            originalEdgeKey = iteratorState.getOriginalEdgeKey();
            LOGGER.trace("Matched virtual edge: " + edge.getEdge() + " with geometry: " + iteratorState.fetchWayGeometry(FetchMode.ALL).toLineString(false));
            edge = ghStorage.getEdgeIteratorStateForKey(originalEdgeKey);
        } else {
            LOGGER.trace("Matched edge: " + edge.getEdge() + " with geometry: " + edge.fetchWayGeometry(FetchMode.ALL).toLineString(false));
        }
        var edgeId = edge.getEdge();
        matchedIds.add(edgeId);
    }

    void matchArea(Geometry geom, LocationIndex locIndex, Set<Integer> matchedIds) {
        var polyEnvelope = geom.getEnvelopeInternal();
        var preparedGeom = PreparedGeometryFactory.prepare(geom);
        var bbox = new PolygonBBox(preparedGeom,
                polyEnvelope.getMinX(), polyEnvelope.getMaxX(),
                polyEnvelope.getMinY(), polyEnvelope.getMaxY());

        locIndex.query(bbox, edgeId -> {
            EdgeIteratorState edge = ghStorage.getEdgeIteratorState(edgeId, Integer.MIN_VALUE);

            // Phase 1: cheap two-node envelope rejection
            var towers = edge.fetchWayGeometry(FetchMode.TOWER_ONLY);
            if (towers.size() >= 2) {
                Envelope segEnv = new Envelope(
                        towers.getLon(0), towers.getLon(1),
                        towers.getLat(0), towers.getLat(1));
                if (!polyEnvelope.intersects(segEnv))
                    return; // fast-exit: no intersection possible
            }

            // Phase 2: full decode + prepared intersection
            var lineString = edge.fetchWayGeometry(FetchMode.ALL).toLineString(false);
            if (preparedGeom.intersects(lineString)) {
                matchedIds.add(edgeId);
            }
        });
    }

    public record MatchingResult(String graphTimestamp, String orsVersion, List<Set<Integer>> matched) {
    }

    /**
     * A {@link BBox} subclass that intercepts {@code intersects} and {@code contains} calls
     * to add polygon-level precision during GraphHopper's quad-tree traversal in
     * {@link com.graphhopper.storage.index.LineIntIndex}.
     *
     * <p>The super class provides cheap axis-aligned envelope fast-fails; if the envelope
     * check passes, a JTS {@link PreparedGeometry} check prunes quadtree cells that lie
     * inside the polygon's bounding box but outside (or only partially inside) the true
     * polygon — which is the dominant source of over-fetching for concave or irregular
     * polygons.
     *
     * <p>Thread safety: {@code GEOMETRY_FACTORY} is static and thread-safe; {@code preparedGeom}
     * is effectively immutable after construction.
     */
    public static final class PolygonBBox extends BBox {

        private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

        private final PreparedGeometry preparedGeom;

        PolygonBBox(PreparedGeometry preparedGeom,
                    double minLon, double maxLon, double minLat, double maxLat) {
            super(minLon, maxLon, minLat, maxLat);
            this.preparedGeom = preparedGeom;
        }

        @Override
        public boolean intersects(double minLon, double maxLon, double minLat, double maxLat) {
            if (!super.intersects(minLon, maxLon, minLat, maxLat)) return false;
            Geometry cellGeom = GEOMETRY_FACTORY.toGeometry(new Envelope(minLon, maxLon, minLat, maxLat));
            return preparedGeom.intersects(cellGeom);
        }

        @Override
        public boolean intersects(BBox o) {
            if (!super.intersects(o)) return false;
            Geometry cellGeom = GEOMETRY_FACTORY.toGeometry(new Envelope(o.minLon, o.maxLon, o.minLat, o.maxLat));
            return preparedGeom.intersects(cellGeom);
        }

        @Override
        public boolean contains(BBox b) {
            if (!super.contains(b)) return false;
            Geometry cellGeom = GEOMETRY_FACTORY.toGeometry(new Envelope(b.minLon, b.maxLon, b.minLat, b.maxLat));
            return preparedGeom.covers(cellGeom);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof PolygonBBox other)) return false;
            if (!super.equals(obj)) return false;
            return preparedGeom.equals(other.preparedGeom);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(super.hashCode(), preparedGeom);
        }
    }
}
