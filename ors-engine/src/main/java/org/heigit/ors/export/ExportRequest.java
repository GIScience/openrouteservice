package org.heigit.ors.export;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.BBox;
import org.apache.log4j.Logger;
import org.heigit.ors.common.Pair;
import org.heigit.ors.common.ServiceRequest;
import org.heigit.ors.export.ExportResult.TopoArc;
import org.heigit.ors.export.ExportResult.TopoGeometry;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.OsmIdGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import org.heigit.ors.util.ProfileTools;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExportRequest extends ServiceRequest {
    private static final Logger LOGGER = Logger.getLogger(ExportRequest.class);
    private static final GeometryFactory geometryFactory = new GeometryFactory();
    private BBox boundingBox;

    private String profileName;
    private int profileType = -1;

    private boolean additionalEdgeInfo;
    private boolean topoJson;
    private boolean useRealGeometry;

    private static final int NO_TIME = -1;
    private OsmIdGraphStorage osmIdGraphStorage;
    private WheelchairAttributesGraphStorage wheelchairAttributesGraphStorage;
    private Weighting weighting;

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public void setBoundingBox(BBox bbox) {
        this.boundingBox = bbox;
    }

    public void setProfileType(int profileType) {
        this.profileType = profileType;
    }

    public void setAdditionalEdgeInfo(boolean additionalEdgeInfo) {
        this.additionalEdgeInfo = additionalEdgeInfo;
    }

    public void setTopoJson(boolean equals) {
        this.topoJson = equals;
    }

    public void setUseRealGeometry(boolean useRealGeometry) {
        this.useRealGeometry = useRealGeometry;
    }

    public ExportResult computeExport(RoutingProfile routingProfile) {
        ExportResult res = new ExportResult();

        // Prepare graph data access
        GraphHopper gh = routingProfile.getGraphhopper();
        String encoderName = RoutingProfileType.getEncoderName(profileType);
        Graph graph = gh.getGraphHopperStorage().getBaseGraph();
        NodeAccess nodeAccess = graph.getNodeAccess();
        PMap hintsMap = new PMap();
        ProfileTools.setWeightingMethod(hintsMap, WeightingMethod.FASTEST, profileType, false);
        weighting = gh.createWeighting(gh.getProfile(ProfileTools.makeProfileName(encoderName, hintsMap.getString("weighting_method", ""), false)), hintsMap);
        osmIdGraphStorage = GraphStorageUtils.getGraphExtension(gh.getGraphHopperStorage(), OsmIdGraphStorage.class);
        wheelchairAttributesGraphStorage = GraphStorageUtils.getGraphExtension(gh.getGraphHopperStorage(), WheelchairAttributesGraphStorage.class);

        // filter graph for nodes in Bounding Box
        Set<Integer> nodesInBBox = nodesInBBox(gh.getLocationIndex(), nodeAccess, graph);
        LOGGER.debug("Found %d nodes in bbox.".formatted(nodesInBBox.size()));
        if (nodesInBBox.isEmpty()) { // without nodes, no export can be calculated
            res.setWarning(new ExportWarning(ExportWarning.EMPTY_BBOX));
            return res;
        }

        // iterate over all edges and add them to the result object
        for (int from : nodesInBBox) {
            Coordinate fromCoords = new Coordinate(nodeAccess.getLon(from), nodeAccess.getLat(from));
            res.addLocation(from, fromCoords);
            EdgeIterator iter = graph.createEdgeExplorer(AccessFilter.outEdges(gh.getEncodingManager().getEncoder(encoderName).getAccessEnc())).setBaseNode(from);
            while (iter.next()) {
                int to = iter.getAdjNode();
                if (nodesInBBox.contains(to)) {
                    LOGGER.debug("Edge %d: from %d to %d".formatted(iter.getEdge(), from, to));
                    LineString geo;
                    if (useRealGeometry) {
                        geo = iter.fetchWayGeometry(FetchMode.ALL).toLineString(false);
                    } else {
                        Coordinate toCoords = new Coordinate(nodeAccess.getLon(to), nodeAccess.getLat(to));
                        geo = geometryFactory.createLineString(new Coordinate[]{fromCoords, toCoords});
                    }

                    if (topoJson && osmIdGraphStorage != null) {
                        addEdgeToTopoGeometries(res, iter, geo);
                    } else {
                        addEdgeToResultObject(res, iter, geo, from, to);
                    }
                }
            }
        }
        return res;
    }

    private Set<Integer> nodesInBBox(LocationIndex index, NodeAccess nodeAccess, Graph graph) {
        Set<Integer> ret = new HashSet<>();
        index.query(boundingBox, edgeId -> {
            // According to GHUtility.getEdgeFromEdgeKey, edgeIds are calculated as edgeKey/2.
            EdgeIteratorState edge = graph.getEdgeIteratorStateForKey(edgeId * 2);
            int baseNode = edge.getBaseNode();
            int adjNode = edge.getAdjNode();
            if (this.boundingBox.contains(nodeAccess.getLat(baseNode), nodeAccess.getLon(baseNode))) {
                ret.add(baseNode);
            }
            if (this.boundingBox.contains(nodeAccess.getLat(adjNode), nodeAccess.getLon(adjNode))) {
                ret.add(adjNode);
            }
        });
        return ret;
    }

    private void addEdgeToTopoGeometries(ExportResult res, EdgeIterator iter, LineString geo) {
        boolean reverse = iter.getEdgeKey() % 2 == 1;
        TopoGeometry topoGeometry = res.getTopoGeometries().computeIfAbsent(osmIdGraphStorage.getEdgeValue(iter.getEdge()), x ->
                new TopoGeometry(weighting.getSpeedCalculator().getSpeed(iter, reverse, NO_TIME),
                        weighting.getSpeedCalculator().getSpeed(iter, !reverse, NO_TIME))
        );
        topoGeometry.getArcs().compute(iter.getEdge(), (k, v) -> {
            if (v != null) {
                topoGeometry.setBothDirections(true);
                return v;
            } else {
                return reverse ? new TopoArc(geo.reverse(), iter.getDistance(), iter.getAdjNode(), iter.getBaseNode()) :
                        new TopoArc(geo, iter.getDistance(), iter.getBaseNode(), iter.getAdjNode());
            }
        });
    }

    private void addEdgeToResultObject(ExportResult res, EdgeIterator iter, LineString geo, int from, int to) {
        Pair<Integer, Integer> p = new Pair<>(from, to);
        res.addEdge(p, weighting.calcEdgeWeight(iter, false, NO_TIME));
        res.getEdgeGeometries().put(p, geo);
        if (additionalEdgeInfo) {
            Map<String, Object> extra = new HashMap<>();
            if (osmIdGraphStorage != null) {
                extra.put("osm_id", osmIdGraphStorage.getEdgeValue(iter.getEdge()));
            }
            extra.put("ors_id", iter.getEdge());
            if (wheelchairAttributesGraphStorage != null) {
                WheelchairAttributes attributes = new WheelchairAttributes();
                byte[] buffer = new byte[WheelchairAttributesGraphStorage.BYTE_COUNT];
                wheelchairAttributesGraphStorage.getEdgeValues(iter.getEdge(), attributes, buffer);
                if (attributes.hasValues()) {
                    extra.put("incline", attributes.getIncline());
                    extra.put("surface_quality_known", attributes.isSurfaceQualityKnown());
                    extra.put("suitable", attributes.isSuitable());
                }
            }
            res.addEdgeExtra(p, extra);
        }
    }
}
