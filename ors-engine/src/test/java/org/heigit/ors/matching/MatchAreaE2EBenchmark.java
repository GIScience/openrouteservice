package org.heigit.ors.matching;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class MatchAreaE2EBenchmark {

    private static final double[][] POLY_COORDS = {
        {8.684143, 49.403360},
        {8.684779, 49.403025},
        {8.685354, 49.403404},
        {8.684697, 49.403805},
        {8.684143, 49.403360}
    };

    private static final int NODES_COUNT = 10_000;
    
    private Polygon polygon;
    private PreparedGeometry preparedGeom;
    private Envelope polyEnvelope;
    
    private GraphHopperStorage graph;
    private LocationIndexTree locationIndex;

    @Setup(Level.Trial)
    public void setUp() {
        polygon = GeometryTestUtils.polygonClosed(POLY_COORDS);
        preparedGeom = PreparedGeometryFactory.prepare(polygon);
        polyEnvelope = polygon.getEnvelopeInternal();

        CarFlagEncoder encoder = new CarFlagEncoder();
        EncodingManager em = EncodingManager.create(encoder);
        graph = new GraphBuilder(em).create();
        
        NodeAccess na = graph.getNodeAccess();
        Random rand = new Random(42);

        double minX = polyEnvelope.getMinX();
        double maxX = polyEnvelope.getMaxX();
        double minY = polyEnvelope.getMinY();
        double maxY = polyEnvelope.getMaxY();
        double width = maxX - minX;
        double height = maxY - minY;
        double ex = width * 10.0;
        double ey = height * 10.0;
        
        // Generate random nodes centered around the polygon but spanning a wider area
        for (int i = 0; i < NODES_COUNT; i++) {
            double lon = (minX - ex) + rand.nextDouble() * (width + 2 * ex);
            double lat = (minY - ey) + rand.nextDouble() * (height + 2 * ey);
            na.setNode(i, lat, lon);
        }

        // Add edges bridging random nodes to populate the quad tree
        for (int i = 0; i < NODES_COUNT - 1; i++) {
            graph.edge(i, i + 1).setDistance(100.0);
            
            // Randomly add some way geometry to simulate real edges
            if (i % 5 == 0) {
                double lon = (minX - ex) + rand.nextDouble() * (width + 2 * ex);
                double lat = (minY - ey) + rand.nextDouble() * (height + 2 * ey);
                PointList pl = new PointList(1, false);
                pl.add(lat, lon);
                graph.edge(i, i + 1).setWayGeometry(pl);
            }
        }
        
        graph.freeze();
        
        locationIndex = new LocationIndexTree(graph, new com.graphhopper.storage.RAMDirectory());
        locationIndex.prepareIndex();
    }

    @Benchmark
    public int originalMatchArea() {
        BBox bbox = new BBox(
                polyEnvelope.getMinX(), polyEnvelope.getMaxX(),
                polyEnvelope.getMinY(), polyEnvelope.getMaxY());

        int[] count = {0};
        locationIndex.query(bbox, edgeId -> {
            var state = graph.getEdgeIteratorState(edgeId, Integer.MIN_VALUE);
            var lineString = state.fetchWayGeometry(com.graphhopper.util.FetchMode.ALL).toLineString(false);
            if (polygon.intersects(lineString)) {
                count[0]++;
            }
        });
        return count[0];
    }

    @Benchmark
    public int optimizedMatchArea() {
        var polyBBox = new MatchingRequest.PolygonBBox(preparedGeom,
                polyEnvelope.getMinX(), polyEnvelope.getMaxX(),
                polyEnvelope.getMinY(), polyEnvelope.getMaxY());

        int[] count = {0};
        locationIndex.query(polyBBox, edgeId -> {
            var state = graph.getEdgeIteratorState(edgeId, Integer.MIN_VALUE);

            PointList towers = state.fetchWayGeometry(com.graphhopper.util.FetchMode.TOWER_ONLY);
            if (towers.size() >= 2) {
                Envelope segEnv = new Envelope(
                        towers.getLon(0), towers.getLon(towers.size() - 1),
                        towers.getLat(0), towers.getLat(towers.size() - 1));
                if (!polyEnvelope.intersects(segEnv)) return;
            }

            var lineString = state.fetchWayGeometry(com.graphhopper.util.FetchMode.ALL).toLineString(false);
            if (preparedGeom.intersects(lineString)) {
                count[0]++;
            }
        });
        return count[0];
    }
}
