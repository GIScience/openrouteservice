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

    private boolean debug;
    private boolean topoJson;
    private boolean osmIdsAvailable;
    private boolean useRealGeometry;

    private static final int NO_TIME = -1;

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

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

    public void setTopoJson(boolean equals) {
        this.topoJson = equals;
    }

    public void setUseRealGeometry(boolean useRealGeometry) {
        this.useRealGeometry = useRealGeometry;
    }

    public ExportResult computeExport(RoutingProfile routingProfile) {
        ExportResult res = new ExportResult();

        GraphHopper gh = routingProfile.getGraphhopper();
        String encoderName = RoutingProfileType.getEncoderName(getProfileType());
        Graph graph = gh.getGraphHopperStorage().getBaseGraph();

        PMap hintsMap = new PMap();
        int weightingMethod = WeightingMethod.FASTEST;
        ProfileTools.setWeightingMethod(hintsMap, weightingMethod, getProfileType(), false);
        String localProfileName = ProfileTools.makeProfileName(encoderName, hintsMap.getString("weighting_method", ""), false);
        Weighting weighting = gh.createWeighting(gh.getProfile(localProfileName), hintsMap);

        FlagEncoder flagEncoder = gh.getEncodingManager().getEncoder(encoderName);
        EdgeExplorer explorer = graph.createEdgeExplorer(AccessFilter.outEdges(flagEncoder.getAccessEnc()));


        // filter graph for nodes in Bounding Box
        LocationIndex index = gh.getLocationIndex();
        NodeAccess nodeAccess = graph.getNodeAccess();
        BBox bbox = getBoundingBox();

        Set<Integer> nodesInBBox = new HashSet<>();
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
        Map<Long, TopoGeometry> topoGeometries = res.getTopoGeometries();
        OsmIdGraphStorage osmIdGraphStorage = GraphStorageUtils.getGraphExtension(gh.getGraphHopperStorage(), OsmIdGraphStorage.class);
        osmIdsAvailable = osmIdGraphStorage != null;

        // calculate node coordinates
        for (int from : nodesInBBox) {
            Coordinate coord = new Coordinate(nodeAccess.getLon(from), nodeAccess.getLat(from));
            res.addLocation(from, coord);
            EdgeIterator iter = explorer.setBaseNode(from);
            while (iter.next()) {
                int to = iter.getAdjNode();
                if (nodesInBBox.contains(to)) {
                    Pair<Integer, Integer> p = new Pair<>(from, to);
                    Map<String, Object> extra = new HashMap<>();
                    double weight = weighting.calcEdgeWeight(iter, false, NO_TIME);
                    Coordinate toCoords = new Coordinate(nodeAccess.getLon(to), nodeAccess.getLat(to));
                    res.addEdge(p, weight);
                    LOGGER.debug("Edge %d: from %d to %d".formatted(iter.getEdge(), from, to));

                    if (topoJson) {
                        LineString geo;
                        if (useRealGeometry) {
                            geo = iter.fetchWayGeometry(FetchMode.ALL).toLineString(false);
                        } else {
                            geo = geometryFactory.createLineString(new Coordinate[]{coord, toCoords});
                        }
                        if (osmIdsAvailable) {
                            assert osmIdGraphStorage != null;
                            boolean reverse = iter.getEdgeKey() % 2 == 1;
                            TopoGeometry topoGeometry = topoGeometries.computeIfAbsent(osmIdGraphStorage.getEdgeValue(iter.getEdge()), x ->
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
                        } else {
                            extra.put("geometry", geo);
                        }
                    }

                    if (debug) {
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
                        if (osmIdGraphStorage != null) {
                            extra.put("osm_id", osmIdGraphStorage.getEdgeValue(iter.getEdge()));
                        }
                    }

                    if (!extra.isEmpty()) {
                        res.addEdgeExtra(p, extra);
                    }
                }
            }
        }
        return res;
    }
}
