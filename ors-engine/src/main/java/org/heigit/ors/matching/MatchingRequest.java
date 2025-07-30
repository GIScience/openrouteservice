package org.heigit.ors.matching;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ev.Subnetwork;
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
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import org.heigit.ors.util.ProfileTools;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

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

        Map<Integer, Map> matched = new HashMap();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
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
                        LOGGER.trace("Snap: " + snappedPoint.getClosestEdge().getEdge());
                        matched.put(snappedPoint.getClosestEdge().getEdge(), properties.get(i));
                    } else {
                        LOGGER.warn("No valid snap found for point: " + p);
                    }
                    break;
                case "LineString":
                    LOGGER.warn("LineString matching is not implemented yet.");
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
                            matched.put(edgeId, properties.get(finalI));
                            LOGGER.trace("Matched edge: " + edgeId);
                        }

                    });
                    LOGGER.warn("Polygon matching is not implemented yet.");
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported geometry type: " + geom.getGeometryType());
            }
        }
        LOGGER.debug(matched.size() + " edges matched");
        String graphDate = ghStorage.getProperties().get("datareader.import.date");
        return new MatchingResult(graphDate, matched.size());
    }
}
