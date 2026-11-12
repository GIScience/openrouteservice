package org.heigit.ors.routing.graphhopper.extensions.reader;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.Rest;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.PedestrianFlagEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestPointImporterTest {
    private static final double EPSILON = 1e-6;
    private static final double NO_REST_POINT = -1;

    private PedestrianFlagEncoder encoder;
    private DecimalEncodedValue restEnc;
    private GraphHopperStorage graph;

    @BeforeEach
    void setUp() {
        encoder = (PedestrianFlagEncoder) new ORSDefaultFlagEncoderFactory()
                .createFlagEncoder(FlagEncoderNames.PEDESTRIAN_ORS, new PMap());
        EncodingManager encodingManager = EncodingManager.start().add(encoder).add(Rest.create()).build();
        restEnc = encodingManager.getDecimalEncodedValue(Rest.KEY);
        graph = new GraphBuilder(encodingManager).create();
    }

    @Test
    void restPointInsideAnEdgeIsStoredAsPositionFromTheBaseNode() {
        setNode(0, 0, 0);
        setNode(1, 0, 0.0018);
        EdgeIteratorState edge = addEdge(0, 1);

        Snap snap = snap(0.00001, 0.00045);
        createImporter(EdgeFilter.ALL_EDGES).writeRestPoint(snap);

        assertEquals(Snap.Position.EDGE, snap.getSnappedPosition());
        assertEquals(0.25, storedRestValue(edge), EPSILON);
    }

    @Test
    void restPointOnAnEdgeStoredBackwardsIsMeasuredFromItsStoredBaseNode() {
        setNode(0, 0, 0);
        setNode(1, 0, 0.0018);
        EdgeIteratorState edge = addEdge(1, 0);

        createImporter(EdgeFilter.ALL_EDGES).writeRestPoint(snap(0.00001, 0.00045));

        assertEquals(1, edge.getBaseNode());
        assertEquals(0.75, storedRestValue(edge), EPSILON);
    }

    @Test
    void restPointOnAPillarNodeIsStoredAtThePillarsPosition() {
        setNode(0, 0, 0);
        setNode(1, 0, 0.0018);
        EdgeIteratorState edge = addEdge(0, 1);
        PointList pillar = new PointList();
        pillar.add(0.0009, 0.0009);
        edge.setWayGeometry(pillar);
        edge.setDistance(DistanceCalcEarth.DIST_EARTH.calcDistance(edge.fetchWayGeometry(FetchMode.ALL)));

        Snap snap = snap(0.001, 0.0009);
        createImporter(EdgeFilter.ALL_EDGES).writeRestPoint(snap);

        assertEquals(Snap.Position.PILLAR, snap.getSnappedPosition());
        assertEquals(0.5, storedRestValue(edge), EPSILON);
    }

    @Test
    void restPointOnATowerNodeIsWrittenToEveryEdgeAtThatNode() {
        setNode(0, 0, 0);
        setNode(1, 0, 0.0018);
        setNode(2, 0, 0.0036);
        setNode(3, 0.0018, 0.0018);
        setNode(4, 0.0018, 0.0036);
        EdgeIteratorState endingAtNode = addEdge(0, 1);
        EdgeIteratorState startingAtNode = addEdge(1, 2);
        EdgeIteratorState storedTowardsNode = addEdge(3, 1);
        EdgeIteratorState elsewhere = addEdge(3, 4);

        Snap snap = snap(0, 0.0018);
        createImporter(EdgeFilter.ALL_EDGES).writeRestPoint(snap);

        assertEquals(Snap.Position.TOWER, snap.getSnappedPosition());
        assertEquals(1, storedRestValue(endingAtNode), EPSILON);
        assertEquals(0, storedRestValue(startingAtNode), EPSILON);
        assertEquals(1, storedRestValue(storedTowardsNode), EPSILON);
        assertEquals(NO_REST_POINT, storedRestValue(elsewhere), EPSILON);
    }

    @Test
    void importFileSkipsInvalidLinesAndCountsWrittenRestPoints(@TempDir Path dir) throws IOException {
        setNode(0, 0, 0);
        setNode(1, 0, 0.0018);
        EdgeIteratorState edge = addEdge(0, 1);
        Path file = writeCsv(dir, "lat,lon", "0.00001,0.00045", "0.5", "north,east");

        int imported = createImporter(EdgeFilter.ALL_EDGES).importFile(file.toString());

        assertEquals(1, imported);
        assertEquals(0.25, storedRestValue(edge), EPSILON);
    }

    @Test
    void restPointWithoutAnAcceptedEdgeNearbyIsNotImported() {
        setNode(0, 0, 0);
        setNode(1, 0, 0.0018);
        EdgeIteratorState edge = addEdge(0, 1);
        RestPointImporter importer = createImporter(e -> false);

        assertFalse(importer.importLine(List.of("0.00001", "0.00045")));
        assertTrue(createImporter(EdgeFilter.ALL_EDGES).importLine(List.of("0.00001", "0.00045")));
        assertEquals(0.25, storedRestValue(edge), EPSILON);
    }

    private RestPointImporter createImporter(EdgeFilter snapFilter) {
        LocationIndexTree index = new LocationIndexTree(graph, new RAMDirectory());
        index.prepareIndex();
        return new RestPointImporter(graph.getBaseGraph(), index, snapFilter, restEnc);
    }

    private Snap snap(double lat, double lon) {
        LocationIndexTree index = new LocationIndexTree(graph, new RAMDirectory());
        index.prepareIndex();
        return index.findClosest(lat, lon, EdgeFilter.ALL_EDGES);
    }

    private void setNode(int node, double lat, double lon) {
        graph.getNodeAccess().setNode(node, lat, lon);
    }

    private EdgeIteratorState addEdge(int from, int to) {
        EdgeIteratorState edge = graph.edge(from, to);
        edge.setDistance(DistanceCalcEarth.DIST_EARTH.calcDistance(edge.fetchWayGeometry(FetchMode.ALL)));
        return GHUtility.setSpeed(5, true, true, encoder, edge);
    }

    private double storedRestValue(EdgeIteratorState edge) {
        return graph.getEdgeIteratorStateForKey(GHUtility.createEdgeKey(edge.getEdge(), false)).get(restEnc);
    }

    private static Path writeCsv(Path dir, String... lines) throws IOException {
        Path file = dir.resolve("foot_rest_data.csv");
        Files.write(file, List.of(lines));
        return file;
    }
}
