package org.heigit.ors.routing.graphhopper.extensions.roadproperty;

import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.userspeed.RoadPropertySpeedCalculator;
import org.heigit.ors.routing.graphhopper.extensions.userspeed.RoadPropertySpeedMap;
import org.heigit.ors.routing.util.WaySurfaceDescription;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoadPropertyTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);

    @Test
    public void testWeightAndTimeFastest() {
        GraphHopperStorage graph = ToyGraphCreationUtil.createSimpleGraph(encodingManager);

        WaySurfaceTypeGraphStorage storage = new WaySurfaceTypeGraphStorage();
        storage.init(graph, new RAMDirectory());
        storage.create(10);
        WaySurfaceDescription waySurfaceDesc = new WaySurfaceDescription();
        waySurfaceDesc.setSurfaceType(1);
        storage.setEdgeValue(6, waySurfaceDesc);
        RoadPropertySpeedMap roadPropertySpeedMap = new RoadPropertySpeedMap();
        roadPropertySpeedMap.addRoadPropertySpeed("paved", 30);
        Weighting weighting = new FastestWeighting(carEncoder, new PMap());
        RoadPropertySpeedCalculator roadPropertySpeedCalculator = new RoadPropertySpeedCalculator(weighting.getSpeedCalculator(), graph, carEncoder);
        roadPropertySpeedCalculator.setRoadPropertySpeedMap(roadPropertySpeedMap);
        roadPropertySpeedCalculator.setWaySurfaceTypeGraphStorage(storage);
        weighting.setSpeedCalculator(roadPropertySpeedCalculator);

        Path path = calcPath(5, 1, graph, weighting);
        roadPropertySpeedMap.addRoadPropertySpeed("paved", 20);
        Path path0 = calcPath(5, 1, graph, weighting);
        assertTrue(path.getWeight() < path0.getWeight());
        assertTrue(path.getTime() < path0.getTime());
        assertEquals(path.getDistance(), path0.getDistance(), 0);
    }

    @Test
    public void testWeightAndTimeShortest() {
        GraphHopperStorage graph = ToyGraphCreationUtil.createSimpleGraph(encodingManager);

        WaySurfaceTypeGraphStorage storage = new WaySurfaceTypeGraphStorage();
        storage.init(graph, new RAMDirectory());
        storage.create(10);
        WaySurfaceDescription waySurfaceDesc = new WaySurfaceDescription();
        waySurfaceDesc.setSurfaceType(1);
        storage.setEdgeValue(6, waySurfaceDesc);
        RoadPropertySpeedMap roadPropertySpeedMap = new RoadPropertySpeedMap();
        roadPropertySpeedMap.addRoadPropertySpeed("paved", 30);
        Weighting weighting = new ShortestWeighting(carEncoder);
        RoadPropertySpeedCalculator roadPropertySpeedCalculator = new RoadPropertySpeedCalculator(weighting.getSpeedCalculator(), graph, carEncoder);
        roadPropertySpeedCalculator.setRoadPropertySpeedMap(roadPropertySpeedMap);
        roadPropertySpeedCalculator.setWaySurfaceTypeGraphStorage(storage);
        weighting.setSpeedCalculator(roadPropertySpeedCalculator);

        Path path = calcPath(5, 1, graph, weighting);
        roadPropertySpeedMap.addRoadPropertySpeed("paved", 20);
        Path path0 = calcPath(5, 1, graph, weighting);
        assertEquals(path.getWeight(), path0.getWeight(), 0);
        assertTrue(path.getTime() < path0.getTime());
        assertEquals(path.getDistance(), path0.getDistance(), 0);
    }

    @Test
    public void testTooLargeValue() {
        GraphHopperStorage graph = ToyGraphCreationUtil.createSimpleGraph(encodingManager);

        WaySurfaceTypeGraphStorage storage = new WaySurfaceTypeGraphStorage();
        storage.init(graph, new RAMDirectory());
        storage.create(10);
        WaySurfaceDescription waySurfaceDesc = new WaySurfaceDescription();
        waySurfaceDesc.setSurfaceType(1);
        storage.setEdgeValue(6, waySurfaceDesc);
        RoadPropertySpeedMap roadPropertySpeedMap = new RoadPropertySpeedMap();
        Weighting weighting = new FastestWeighting(carEncoder, new PMap());
        RoadPropertySpeedCalculator roadPropertySpeedCalculator = new RoadPropertySpeedCalculator(weighting.getSpeedCalculator(), graph, carEncoder);
        roadPropertySpeedCalculator.setRoadPropertySpeedMap(roadPropertySpeedMap);
        roadPropertySpeedCalculator.setWaySurfaceTypeGraphStorage(storage);
        weighting.setSpeedCalculator(roadPropertySpeedCalculator);

        Path path = calcPath(5, 1, graph, weighting);
        roadPropertySpeedMap.addRoadPropertySpeed("paved", 3000);
        Path path0 = calcPath(5, 1, graph, weighting);
        assertEquals(path.getWeight(), path0.getWeight(), 0);
        assertEquals(path.getTime(), path0.getTime(), 0);
        assertEquals(path.getDistance(), path0.getDistance(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooSmallValue() {
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
    public void testUnknownValue() {
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

    private Path calcPath(int source, int target, Graph graph, Weighting w) {
        Dijkstra algo = createAlgo(graph, w);
        return algo.calcPath(source, target);
    }

    private Dijkstra createAlgo(Graph graph, Weighting weighting) {
        return new Dijkstra(graph, weighting, TraversalMode.EDGE_BASED);
    }
}
