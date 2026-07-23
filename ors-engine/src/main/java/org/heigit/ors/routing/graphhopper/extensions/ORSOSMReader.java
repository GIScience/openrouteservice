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
package org.heigit.ors.routing.graphhopper.extensions;

import com.carrotsearch.hppc.LongArrayList;
import com.graphhopper.coll.GHLongObjectHashMap;
import com.graphhopper.reader.ConditionalSpeedInspector;
import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.routing.OSMReaderConfig;
import com.graphhopper.routing.ev.HillIndex;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.EncodingManager.AcceptWay;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.ConditionalEdges;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMFeatureFilter;
import org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.PedestrianWayFilter;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.*;
import org.heigit.ors.routing.util.HillIndexCalculator;
import org.locationtech.jts.geom.Coordinate;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.graphhopper.reader.osm.OSMNodeData.isPillarNode;
import static com.graphhopper.reader.osm.OSMNodeData.isTowerNode;

public class ORSOSMReader extends OSMReader {

    private static final Logger LOGGER = Logger.getLogger(ORSOSMReader.class.getName());

    private final GraphProcessContext procCntx;
    private boolean processNodeTags;
    private static final String KEY_COUNTRY = "country";
    private Map<Long, String> countries;
    private final GHLongObjectHashMap<Map<String, String>> nodeTags = new GHLongObjectHashMap<>(200, 0.5);
    private boolean processGeom = false;
    private boolean processSimpleGeom = false;
    private boolean processWholeGeom = false;
    private boolean detachSidewalksFromRoad = false;
    private HillIndexCalculator hillIndexCalculator = null;

    private final List<OSMFeatureFilter> filtersToApply = new ArrayList<>();

    private final HashSet<String> extraTagKeys;
    private final Set<String> nodeTagsToStore;
    private final GHLongObjectHashMap<Map<String, Object>> osmNodeTagValues;

    private final AtomicInteger barrierNodesTotal = new AtomicInteger(
            0);
    private final AtomicInteger barrierNodesSkipped = new AtomicInteger(
            0);

    public ORSOSMReader(GraphHopperStorage storage, OSMReaderConfig osmReaderConfig, GraphProcessContext procCntx) {
        super(storage, osmReaderConfig);

        distCalc.enforce2D();
        this.procCntx = procCntx;
        this.procCntx.initArrays();

        extraTagKeys = new HashSet<>();
        nodeTagsToStore = new HashSet<>(Arrays.asList("maxheight", "maxweight", "maxweight:hgv", "maxwidth", "maxlength", "maxlength:hgv", "maxaxleload"));
        osmNodeTagValues = new GHLongObjectHashMap<>(200, .5f);

        // Look if we should do border processing - if so then we have to process the geometry
        for (GraphStorageBuilder b : this.procCntx.getStorageBuilders()) {
            if (b instanceof BordersGraphStorageBuilder) {
                this.processNodeTags = true;
                this.countries = new HashMap<>();
                this.processGeom = true;
            }

            if (b instanceof HereTrafficGraphStorageBuilder) {
                this.processGeom = true;
                this.processWholeGeom = true;
            }

            if (b instanceof WheelchairGraphStorageBuilder) {
                this.processNodeTags = true;
                this.processSimpleGeom = true;
                extraTagKeys.add("kerb");
                extraTagKeys.add("kerb:both");
                extraTagKeys.add("kerb:left");
                extraTagKeys.add("kerb:right");
                extraTagKeys.add("kerb:height");
                extraTagKeys.add("kerb:both:height");
                extraTagKeys.add("kerb:left:height");
                extraTagKeys.add("kerb:right:height");
            }
        }

        if (procCntx.isUseSidewalks()) {
            detachSidewalksFromRoad = true;
            filtersToApply.add(new PedestrianWayFilter());
        }

        if (encodingManager.hasEncodedValue(HillIndex.KEY)) {
            hillIndexCalculator = new HillIndexCalculator();
        }
    }

    private void storeNodeTags(ReaderNode node) {
        Iterator<Map.Entry<String, Object>> it = node.getTags().entrySet().iterator();
        Map<String, Object> temp = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry<String, Object> pairs = it.next();
            String key = pairs.getKey();
            if (!nodeTagsToStore.contains(key)) {
                continue;
            }
            temp.put(key, pairs.getValue());
        }
        if (!temp.isEmpty()) {
            osmNodeTagValues.put(node.getId(), temp);
        }
    }

    @Override
    protected void processNode(ReaderNode node) {
        // On OSM, nodes are separate entities which are used to make up ways. So basically, a node is read before a
        // way and if it has some properties that could affect routing, these properties need to be stored so that they
        // can be accessed when it comes to using ways
        if (processNodeTags && node.hasTags()) {
            // Check each node and store the tags that are required
            HashMap<String, String> tagValues = new HashMap<>();
            Set<String> nodeKeys = node.getTags().keySet();
            for (String key : nodeKeys) {
                if (extraTagKeys.contains(key)) {
                    tagValues.put(key, node.getTag(key));
                }
            }

            // Now if we have tag data, we need to store it
            if (!tagValues.isEmpty()) {
                nodeTags.put(node.getId(), tagValues);
            }
        }

        if (countries != null  && node.hasTag(KEY_COUNTRY)) {
            countries.put(node.getId(), node.getTag(KEY_COUNTRY));
        }

        if (isPillarNode(nodeData.getId(node.getId()))) {
            storeNodeTags(node);
        }
    }

    @Override
    protected void preprocessWay(GHPoint first, GHPoint last, ReaderWay way) {
        // As a first step we need to check to see if we should try to split the way
        if (detachSidewalksFromRoad) {
            // If we are requesting to split sidewalks, then we need to create multiple ways from a single road
            // For example, if a road way has been tagged as having sidewalks on both sides (sidewalk=both), then we
            // need to create two ways - one for the left sidewalk and one for the right. The Graph Builder would then
            // process these ways separately so that additional edges are created in the graph.
            for (OSMFeatureFilter filter : filtersToApply) {
                try {
                    filter.assignFeatureForFiltering(way);
                } catch (InvalidObjectException ioe) {
                    LOGGER.error("Invalid object for filtering - " + ioe.getMessage());
                }
                if (filter.accept()) {
                    // We can only perform the processing of the ways here and so we cannot delegate it to another object.
                    while (!filter.isWayProcessingComplete()) {
                        filter.prepareForProcessing();
                        super.preprocessWay(first, last, way);
                    }
                }
            }
        }
        else {
            // Normal processing
            super.preprocessWay(first, last, way);
        }

        applyNodeTagsToWay(way);
        onProcessWay(way);
        recordExactWayDistance(way);
        recordEstimatedWayDistance(way);// Required for backward compatibility of the acceleration heuristic
    }

    private void recordExactWayDistance(ReaderWay way) {
        // compute exact way distance for ferries in order to improve travel time estimate, see #1037
        if (way.hasTag("route", "ferry", "shuttle_train")) {
            var osmNodeIds = way.getNodes();
            double totalDist = 0d;
            long nodeId = osmNodeIds.get(0);
            GHPoint3D ghPoint = nodeData.getCoordinates(nodeData.getId(nodeId));
            double firstLat = ghPoint.getLat();
            double firstLon = ghPoint.getLon();
            double currLat = firstLat;
            double currLon = firstLon;
            double latSum = currLat;
            double lonSum = currLon;
            int sumCount = 1;
            int len = osmNodeIds.size();
            for (int i = 1; i < len; i++) {
                long nextNodeId = osmNodeIds.get(i);
                ghPoint = nodeData.getCoordinates(nodeData.getId(nextNodeId));
                double nextLat = ghPoint.getLat();
                double nextLon = ghPoint.getLon();
                if (!Double.isNaN(currLat) && !Double.isNaN(currLon) && !Double.isNaN(nextLat) && !Double.isNaN(nextLon)) {
                    latSum = latSum + nextLat;
                    lonSum = lonSum + nextLon;
                    sumCount++;
                    totalDist = totalDist + distCalc.calcDist(currLat, currLon, nextLat, nextLon);

                    currLat = nextLat;
                    currLon = nextLon;
                }
            }
            if (totalDist > 0) {
                way.setTag("exact_distance", totalDist);
                way.setTag("exact_center", new GHPoint(latSum / sumCount, lonSum / sumCount));
            }
        }
    }

    private void recordEstimatedWayDistance(ReaderWay way) {
        var osmNodeIds = way.getNodes();
        GHPoint3D firstPoint = nodeData.getCoordinates(nodeData.getId(osmNodeIds.get(0)));
        GHPoint3D lastPoint = nodeData.getCoordinates(nodeData.getId(osmNodeIds.get(osmNodeIds.size() - 1)));
        double firstLat = firstPoint.getLat(), firstLon = firstPoint.getLon();
        double lastLat = lastPoint.getLat(), lastLon = lastPoint.getLon();
        if (!Double.isNaN(firstLat) && !Double.isNaN(firstLon) && !Double.isNaN(lastLat) && !Double.isNaN(lastLon)) {
            way.setTag("estimated_way_distance", distCalc.calcDist(firstLat, firstLon, lastLat, lastLon));
        }
    }

    /**
     * Method to be run against each way obtained from the data. If one of the storage builders needs geometry
     * determined in the constructor then we need to get the geometry as well as the tags.
     * Also we need to pass through any important tag values obtained from nodes through to the processing stage so
     * that they can be evaluated.
     *
     * @param way The way object read from the OSM data (not including geometry)
     */
    private void onProcessWay(ReaderWay way) {
        Map<Integer, Map<String, String>> tags = new HashMap<>();
        ArrayList<Coordinate> coords = new ArrayList<>();
        ArrayList<Coordinate> allCoordinates = new ArrayList<>();

        if (processNodeTags) {
            // If we are processing the node tags then we need to obtain the tags for nodes that are on the way. We
            // should store the internal node id though rather than the osm node as during the edge processing, we
            // do not know the osm node id

            LongArrayList osmNodeIds = way.getNodes();
            int size = osmNodeIds.size();

            for (int i = 0; i < size; i++) {
                // find the node
                long osmId = osmNodeIds.get(i);
                // replace the osm id with the internal id
                int internalId = nodeData.getId(osmId);
                Map<String, String> tagsForNode = nodeTags.get(osmId);

                if (countries != null && countries.containsKey(osmId)) {
                    if (tagsForNode == null)
                        tagsForNode = new HashMap<>();
                    tagsForNode.put(KEY_COUNTRY, countries.get(osmId));
                }

                if (tagsForNode != null) {
                    tags.put(internalId, tagsForNode);
                }
            }
        }

        if (processGeom || processSimpleGeom) {
            // We need to pass the geometry of the way aswell as the ReaderWay object
            // This is slower so should only be done when needed

            // First we need to generate the geometry
            LongArrayList osmNodeIds = new LongArrayList();
            LongArrayList allOsmNodes = way.getNodes();

            if (allOsmNodes.size() > 1) {
                if (processSimpleGeom) {
                    // We only want the start and end nodes
                    osmNodeIds.add(allOsmNodes.get(0));
                    osmNodeIds.add(allOsmNodes.get(allOsmNodes.size() - 1));
                } else {
                    // Process all nodes
                    osmNodeIds = allOsmNodes;
                }
            }

            if (osmNodeIds.size() > 1) {

                for (int i = 0; i < osmNodeIds.size(); i++) {
                    long osmId = osmNodeIds.get(i);
                    int nodeId = nodeData.getId(osmId);
                    try {
                        GHPoint3D ghPoint = nodeData.getCoordinates(nodeId);
                        double lat = ghPoint.getLat();
                        double lon = ghPoint.getLon();
                        boolean validPoint = !(lat == 0 || lon == 0 || Double.isNaN(lat) || Double.isNaN(lon));
                        if (!validPoint) {
                            LOGGER.warn("Invalid geometry for node " + osmNodeIds.get(i) + " on way " + way.getId());
                            continue;
                        }
                        Coordinate coordinate = new Coordinate(lon, lat);
                        if (processWholeGeom) {
                            allCoordinates.add(coordinate);
                        }
                        if (isTowerNode(nodeId)) {
                            coords.add(coordinate);
                        }
                        else {// TODO: check if we actually need to add  "empty" points
                            coords.add(new Coordinate(Double.NaN, Double.NaN));
                        }
                    } catch (Exception e) {
                        LOGGER.error("Could not process node " + osmNodeIds.get(i));
                    }
                }
            }

        }

        if (tags.size() > 0 || coords.size() > 1) {
            // Use an overloaded method that allows the passing of parameters from this reader
            procCntx.processWay(way, coords.toArray(new Coordinate[0]), tags, allCoordinates.toArray(new Coordinate[0]));
        } else {
            procCntx.processWay(way);
        }
    }

    /**
     * Applies tags of nodes that lie on a way onto the way itself so that they are
     * regarded in the following storage building process. E.g. a maxheight tag on a node will
     * be treated like a maxheight tag on the way the node belongs to.
     *
     * @param way the way to process
     */
    private void applyNodeTagsToWay(ReaderWay way) {
        LongArrayList osmNodeIds = way.getNodes();
        int size = osmNodeIds.size();
        if (size > 2) {
            // If it is a crossing then we need to apply any kerb tags to the way, but we need to make sure we keep the "worse" one
            for (int i = 1; i < size - 1; i++) {
                long nodeId = osmNodeIds.get(i);
                if (osmNodeTagValues.containsKey(nodeId)) {
                  osmNodeTagValues.get(nodeId).forEach((key, value) -> way.setTag(key, value.toString()));
                }
            }
        }
    }

    @Override
    protected void onProcessEdge(ReaderWay way, EdgeIteratorState edge, IntsRef edgeFlags, EncodingManager.AcceptWay acceptWay) {
        try {
            Map<Integer, Map<String, String>> tags = new HashMap<>();
            if (processNodeTags) {
                // If we are processing the node tags then we need to obtain the tags for nodes that are on the way. We
                // should store the internal node id though rather than the osm node as during the edge processing, we
                // do not know the osm node id
                Arrays.asList(edge.getBaseNode(), edge.getAdjNode()).forEach(nodeId -> {
                    long osmId = nodeData.getOsmId(nodeData.towerNodeToId(nodeId));

                    Map<String, String> tagsForNode = nodeTags.get(osmId);

                    if (countries != null && countries.containsKey(osmId)) {
                        if (tagsForNode == null)
                            tagsForNode = new HashMap<>();
                        tagsForNode.put(KEY_COUNTRY, countries.get(osmId));
                    }

                    if (tagsForNode != null) {
                        tags.put(nodeId, tagsForNode);
                    }
                });
            }

            procCntx.processEdge(way, edge, tags);

            if (acceptWay.hasConditional()) {
                storeConditionalAccess(acceptWay, edge);
            }

            storeConditionalSpeed(edgeFlags, edge);
        } catch (Exception ex) {
            LOGGER.warn(ex.getMessage() + ". Way id = " + way.getId());
        }
    }

    private void storeConditionalAccess(AcceptWay acceptWay, EdgeIteratorState edge) {
        for (FlagEncoder encoder : encodingManager.fetchEdgeEncoders()) {
            String encoderName = encoder.toString();
            if (acceptWay.getAccess(encoderName).isConditional() && encodingManager.hasEncodedValue(EncodingManager.getKey(encoderName, ConditionalEdges.ACCESS))) {
                String value = ((AbstractFlagEncoder) encoder).getConditionalTagInspector().getTagValue();
                ghStorage.getConditionalAccess(encoderName).addEdges(Collections.singletonList(edge), value);
            }
        }
    }

    private void storeConditionalSpeed(IntsRef edgeFlags, EdgeIteratorState edge) {
        for (FlagEncoder encoder : encodingManager.fetchEdgeEncoders()) {
            String encoderName = EncodingManager.getKey(encoder, ConditionalEdges.SPEED);

            if (encodingManager.hasEncodedValue(encoderName) && encodingManager.getBooleanEncodedValue(encoderName).getBool(false, edgeFlags)) {
                ConditionalSpeedInspector conditionalSpeedInspector = ((AbstractFlagEncoder) encoder).getConditionalSpeedInspector();

                if (conditionalSpeedInspector.hasLazyEvaluatedConditions()) {
                    String value = conditionalSpeedInspector.getTagValue();
                    ghStorage.getConditionalSpeed(encoder).addEdges(Collections.singletonList(edge), value);
                }
            }
        }
    }

    @Override
    protected void setArtificialWayTags(PointList pointList, ReaderWay way) {
        super.setArtificialWayTags(pointList, way);

        if (hillIndexCalculator != null) {
            calculateHillIndex(pointList, way);
        }
    }

    private void calculateHillIndex(PointList pointList, ReaderWay way) {
        byte hillIndexFwd = hillIndexCalculator.getHillIndex(pointList, false);
        byte hillIndexBwd = hillIndexCalculator.getHillIndex(pointList, true);

        way.setTag("ors:hill_index_fwd", hillIndexFwd);
        way.setTag("ors:hill_index_bwd", hillIndexBwd);
    }

    @Override
    public void readGraph() throws IOException {
        super.readGraph();
        procCntx.finish();
    }

    /**
     * We need to overwrite the basic barrier selection.
     * <p>
     * Split the topology if ANY encoder is blocked by this barrier. The barrier edge then
     * carries per-encoder access (EncodingManager.handleNodeTags blocks only the encoders whose
     * isBarrier() is true), so the encoders that can pass simply traverse a zero-length no-op
     * edge. Skipping the split only when EVERY encoder can pass drops just the fully-passable
     * no-op edges. The inverse ("skip if any can pass") would be a correctness bug on any
     * multi-encoder graph. It would leak a blocked profile through. For ORS's single-encoder
     * graphs the two are equivalent, but this formulation stays correct if that ever changes.
     *
     * @param node A ReaderNode that is validated against the FlagEncoder isBarrier function.
     * @return Returns a boolean based on the isBarrierNode decision.
     */
    @Override
    protected boolean isBarrierNode(ReaderNode node) {
        if (!super.isBarrierNode(node))
            return false;

        barrierNodesTotal.incrementAndGet();

        for (FlagEncoder encoder : encodingManager.fetchEdgeEncoders()) {
            if (encoder instanceof AbstractFlagEncoder abstractEncoder
                    && abstractEncoder.isBarrier(node)) {
                return true; // at least one encoder is blocked → topology split is required
            }
        }
        barrierNodesSkipped.incrementAndGet();
        return false; // passable for every encoder → no-op barrier edge, safe to skip
    }

    int getBarrierNodesTotal() {
        return barrierNodesTotal.get();
    }

    int getBarrierNodesSkipped() {
        return barrierNodesSkipped.get();
    }

}
