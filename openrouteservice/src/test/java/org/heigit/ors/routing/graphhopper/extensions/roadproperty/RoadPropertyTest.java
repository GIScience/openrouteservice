package org.heigit.ors.routing.graphhopper.extensions.roadproperty;

import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.userspeed.RoadPropertySpeedCalculator;
import org.heigit.ors.routing.graphhopper.extensions.userspeed.RoadPropertySpeedMap;
import org.heigit.ors.routing.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.weighting.FastestSpeedCalculatorWeighting;
import org.heigit.ors.routing.util.WaySurfaceDescription;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoadPropertyTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private final FastestSpeedCalculatorWeighting weighting = new FastestSpeedCalculatorWeighting(carEncoder, new PMap());
    private final TraversalMode tMode = TraversalMode.NODE_BASED;
    private Directory dir;

    @Before
    public void setUp() {
        dir = new GHDirectory("", DAType.RAM_INT);
    }

    GraphHopperStorage createGHStorage() {
        return new GraphBuilder(encodingManager).setCHProfiles(new ArrayList<>()).setCoreGraph(weighting).create();
    }

    @Test
    public void testWeightAndTime() {
        GraphHopperStorage graph = ToyGraphCreationUtil.createSimpleGraph(encodingManager);

        WaySurfaceTypeGraphStorage storage = new WaySurfaceTypeGraphStorage();
        storage.init(graph, new RAMDirectory());
        storage.create(10);
        WaySurfaceDescription waySurfaceDesc = new WaySurfaceDescription();
        waySurfaceDesc.setSurfaceType(1);
        storage.setEdgeValue(6, waySurfaceDesc);
        RoadPropertySpeedMap roadPropertySpeedMap = new RoadPropertySpeedMap();
        roadPropertySpeedMap.addRoadPropertySpeed("paved", 30);
        RoadPropertySpeedCalculator roadPropertySpeedCalculator = new RoadPropertySpeedCalculator(graph, carEncoder);
        roadPropertySpeedCalculator.setRoadPropertySpeedMap(roadPropertySpeedMap);
        roadPropertySpeedCalculator.setWaySurfaceTypeGraphStorage(storage);
        weighting.setSpeedCalculator(roadPropertySpeedCalculator);

        Path path = calcPath(5, 1, graph);
        roadPropertySpeedMap.addRoadPropertySpeed("paved", 20);
        Path path0 = calcPath(5, 1, graph);
        assertTrue(path.getWeight() < path0.getWeight());
        assertTrue(path.getTime() < path0.getTime());
        assertEquals(path.getDistance(), path0.getDistance(), 0);
    }

    @Test
    public void testTooLargeValue(){
        GraphHopperStorage graph = ToyGraphCreationUtil.createSimpleGraph(encodingManager);

        WaySurfaceTypeGraphStorage storage = new WaySurfaceTypeGraphStorage();
        storage.init(graph, new RAMDirectory());
        storage.create(10);
        WaySurfaceDescription waySurfaceDesc = new WaySurfaceDescription();
        waySurfaceDesc.setSurfaceType(1);
        storage.setEdgeValue(6, waySurfaceDesc);
        RoadPropertySpeedMap roadPropertySpeedMap = new RoadPropertySpeedMap();
        RoadPropertySpeedCalculator roadPropertySpeedCalculator = new RoadPropertySpeedCalculator(graph, carEncoder);
        roadPropertySpeedCalculator.setRoadPropertySpeedMap(roadPropertySpeedMap);
        roadPropertySpeedCalculator.setWaySurfaceTypeGraphStorage(storage);
        weighting.setSpeedCalculator(roadPropertySpeedCalculator);

        Path path = calcPath(5, 1, graph);
        roadPropertySpeedMap.addRoadPropertySpeed("paved", 3000);
        Path path0 = calcPath(5, 1, graph);
        assertEquals(path.getWeight(), path0.getWeight(), 0);
        assertEquals(path.getTime(), path0.getTime(), 0);
        assertEquals(path.getDistance(), path0.getDistance(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooSmallValue(){
        GraphHopperStorage graph = ToyGraphCreationUtil.createSimpleGraph(encodingManager);

        WaySurfaceTypeGraphStorage storage = new WaySurfaceTypeGraphStorage();
        storage.init(graph, new RAMDirectory());
        storage.create(10);
        WaySurfaceDescription waySurfaceDesc = new WaySurfaceDescription();
        waySurfaceDesc.setSurfaceType(1);
        storage.setEdgeValue(6, waySurfaceDesc);
        RoadPropertySpeedMap roadPropertySpeedMap = new RoadPropertySpeedMap();
        roadPropertySpeedMap.addRoadPropertySpeed("paved", -10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownValue(){
        GraphHopperStorage graph = ToyGraphCreationUtil.createSimpleGraph(encodingManager);

        WaySurfaceTypeGraphStorage storage = new WaySurfaceTypeGraphStorage();
        storage.init(graph, new RAMDirectory());
        storage.create(10);
        WaySurfaceDescription waySurfaceDesc = new WaySurfaceDescription();
        waySurfaceDesc.setSurfaceType(1);
        storage.setEdgeValue(6, waySurfaceDesc);
        RoadPropertySpeedMap roadPropertySpeedMap = new RoadPropertySpeedMap();
        roadPropertySpeedMap.addRoadPropertySpeed("pavedD", 50);
    }

    private Path calcPath(int source, int target, Graph graph) {
        return calcPath(source, target, graph, weighting);
    }

    private Path calcPath(int source, int target, Graph graph, Weighting w) {
        Dijkstra algo = createAlgo(graph, w);
        return algo.calcPath(source, target);
    }

    private Dijkstra createAlgo(Graph graph, Weighting weighting) {
        return new Dijkstra(graph, weighting, TraversalMode.EDGE_BASED);
    }
}
