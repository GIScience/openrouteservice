package org.heigit.ors.export;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import com.graphhopper.util.shapes.BBox;
import org.apache.log4j.Logger;
import org.heigit.ors.common.Pair;
import org.heigit.ors.common.ServiceRequest;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.OsmIdGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import org.heigit.ors.util.ProfileTools;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExportRequest extends ServiceRequest {
    private static final Logger LOGGER = Logger.getLogger(ExportRequest.class);
    private BBox boundingBox;

    private int profileType = -1;

    private boolean debug;

    public BBox getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(BBox bbox) {
        this.boundingBox = bbox;
    }

    public int getProfileType() {
        return profileType;
    }

    public void setProfileType(int profileType) {
        this.profileType = profileType;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean debug() {
        return debug;
    }

    public ExportResult computeExport(RoutingProfile routingProfile) {
        ExportResult res = new ExportResult();

        GraphHopper gh = routingProfile.getGraphhopper();
        String encoderName = RoutingProfileType.getEncoderName(getProfileType());
        Graph graph = gh.getGraphHopperStorage().getBaseGraph();

        PMap hintsMap = new PMap();
        int weightingMethod = WeightingMethod.FASTEST;
        ProfileTools.setWeightingMethod(hintsMap, weightingMethod, getProfileType(), false);
        String profileName = ProfileTools.makeProfileName(encoderName, hintsMap.getString("weighting_method", ""), false);
        Weighting weighting = gh.createWeighting(gh.getProfile(profileName), hintsMap);

        FlagEncoder flagEncoder = gh.getEncodingManager().getEncoder(encoderName);
        EdgeExplorer explorer = graph.createEdgeExplorer(AccessFilter.outEdges(flagEncoder.getAccessEnc()));


        // filter graph for nodes in Bounding Box
        LocationIndex index = gh.getLocationIndex();
        NodeAccess nodeAccess = graph.getNodeAccess();
        BBox bbox = getBoundingBox();

        ArrayList<Integer> nodesInBBox = new ArrayList<>();
        index.query(bbox, edgeId -> {
            // According to GHUtility.getEdgeFromEdgeKey, edgeIds are calculated as edgeKey/2.
            EdgeIteratorState edge = graph.getEdgeIteratorStateForKey(edgeId * 2);
            int baseNode = edge.getBaseNode();
            int adjNode = edge.getAdjNode();

            if (bbox.contains(nodeAccess.getLat(baseNode), nodeAccess.getLon(baseNode))) {
                nodesInBBox.add(baseNode);
            }
            if (bbox.contains(nodeAccess.getLat(adjNode), nodeAccess.getLon(adjNode))) {
                nodesInBBox.add(adjNode);
            }
        });

        LOGGER.debug("Found %d nodes in bbox.".formatted(nodesInBBox.size()));

        if (nodesInBBox.isEmpty()) {
            // without nodes, no export can be calculated
            res.setWarning(new ExportWarning(ExportWarning.EMPTY_BBOX));
            return res;
        }

        // calculate node coordinates
        for (int from : nodesInBBox) {
            Coordinate coord = new Coordinate(nodeAccess.getLon(from), nodeAccess.getLat(from));
            res.addLocation(from, coord);

            EdgeIterator iter = explorer.setBaseNode(from);
            while (iter.next()) {
                int to = iter.getAdjNode();
                if (nodesInBBox.contains(to)) {
                    double weight = weighting.calcEdgeWeight(iter, false, EdgeIterator.NO_EDGE);
                    Pair<Integer, Integer> p = new Pair<>(from, to);
                    res.addEdge(p, weight);

                    if (debug()) {
                        Map<String, Object> extra = new HashMap<>();
                        extra.put("edge_id", iter.getEdge());
                        WheelchairAttributesGraphStorage storage = GraphStorageUtils.getGraphExtension(gh.getGraphHopperStorage(), WheelchairAttributesGraphStorage.class);
                        if (storage != null) {
                            WheelchairAttributes attributes = new WheelchairAttributes();
                            byte[] buffer = new byte[WheelchairAttributesGraphStorage.BYTE_COUNT];
                            storage.getEdgeValues(iter.getEdge(), attributes, buffer);
                            if (attributes.hasValues()) {
                                extra.put("incline", attributes.getIncline());
                                extra.put("surface_quality_known", attributes.isSurfaceQualityKnown());
                                extra.put("suitable", attributes.isSuitable());
                            }
                        }
                        OsmIdGraphStorage storage2 = GraphStorageUtils.getGraphExtension(gh.getGraphHopperStorage(), OsmIdGraphStorage.class);
                        if (storage2 != null) {
                            extra.put("osm_id", storage2.getEdgeValue(iter.getEdge()));
                        }
                        res.addEdgeExtra(p, extra);
                    }
                }
            }
        }

        return res;
    }
}
