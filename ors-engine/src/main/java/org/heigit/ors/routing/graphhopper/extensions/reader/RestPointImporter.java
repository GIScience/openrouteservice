package org.heigit.ors.routing.graphhopper.extensions.reader;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.Rest;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import org.heigit.ors.util.CSVUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Writes rest points (e.g. benches) from the {@code foot_rest_data} CSV file (header, then {@code lat,lon} per line)
 * into the {@link Rest} encoded value. An edge stores one rest point as its position along the edge, measured from the
 * edge's stored base node: 0 is the base node, 1 the adjacent node. A rest point on a tower node is written to every
 * edge at that node.
 */
public class RestPointImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestPointImporter.class);

    private final Graph graph;
    private final LocationIndex locationIndex;
    private final EdgeFilter snapFilter;
    private final DecimalEncodedValue restEnc;

    public RestPointImporter(Graph graph, LocationIndex locationIndex, EdgeFilter snapFilter, DecimalEncodedValue restEnc) {
        this.graph = graph;
        this.locationIndex = locationIndex;
        this.snapFilter = snapFilter;
        this.restEnc = restEnc;
    }

    /**
     * @return the number of rest points written to the graph
     */
    public int importFile(String filename) {
        int imported = 0;
        for (List<String> line : CSVUtility.readFile(filename)) {
            if (importLine(line))
                imported++;
        }
        LOGGER.info("Imported {} rest points from {}", imported, filename);
        return imported;
    }

    boolean importLine(List<String> line) {
        if (line.size() < 2) {
            LOGGER.error("Invalid line in foot_rest_data: {}", line);
            return false;
        }
        try {
            double lat = Double.parseDouble(line.get(0));
            double lon = Double.parseDouble(line.get(1));
            Snap snap = locationIndex.findClosest(lat, lon, snapFilter);
            if (!snap.isValid())
                return false;
            writeRestPoint(snap);
            return true;
        } catch (NumberFormatException _) {
            LOGGER.error("Invalid coordinates in foot_rest_data: {}", line);
            return false;
        }
    }

    void writeRestPoint(Snap snap) {
        if (snap.getSnappedPosition() == Snap.Position.TOWER)
            writeAtTowerNode(snap.getClosestNode());
        else
            writeOnEdge(snap);
    }

    private void writeAtTowerNode(int node) {
        EdgeIterator iter = graph.createEdgeExplorer().setBaseNode(node);
        while (iter.next()) {
            EdgeIteratorState edge = storedEdge(iter.getEdge());
            edge.set(restEnc, edge.getBaseNode() == node ? 0 : 1);
        }
    }

    /**
     * The snap lies inside the edge, on a pillar node or between two. A query graph splits the edge there into two
     * virtual pieces; the one from the snapped point back to the stored base node gives the rest point's distance
     * from that node.
     */
    private void writeOnEdge(Snap snap) {
        EdgeIteratorState edge = storedEdge(snap.getClosestEdge().getEdge());
        QueryGraph queryGraph = QueryGraph.create(graph, snap);
        EdgeIterator pieceToBase = queryGraph.createEdgeExplorer().setBaseNode(snap.getClosestNode());
        pieceToBase.next();
        if (pieceToBase.getAdjNode() != edge.getBaseNode())
            pieceToBase.next();
        edge.set(restEnc, Math.min(1, pieceToBase.getDistance() / edge.getDistance()));
    }

    private EdgeIteratorState storedEdge(int edgeId) {
        return graph.getEdgeIteratorStateForKey(GHUtility.createEdgeKey(edgeId, false));
    }
}
