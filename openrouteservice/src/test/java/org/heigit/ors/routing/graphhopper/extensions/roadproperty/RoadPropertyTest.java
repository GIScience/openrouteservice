package org.heigit.ors.routing.graphhopper.extensions.roadproperty;

import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.DijkstraBidirectionRef;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.TimeDependentFastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.PMap;
import org.heigit.ors.common.Pair;
import org.heigit.ors.routing.graphhopper.extensions.RoadPropertySpeedCalculator;
import org.heigit.ors.routing.graphhopper.extensions.RoadPropertySpeedMap;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreTestEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;
import org.heigit.ors.routing.util.WaySurfaceDescription;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RoadPropertyTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private final TimeDependentFastestWeighting weighting = new TimeDependentFastestWeighting(carEncoder, new PMap());
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
    public void testSimple() {
        GraphHopperStorage graph = ToyGraphCreationUtil.createSimpleGraph(encodingManager);
        Path path = calcPath(5, 1, graph);
        System.out.println(path.toDetailsString());

        WaySurfaceTypeGraphStorage storage = new WaySurfaceTypeGraphStorage();
        WaySurfaceDescription waySurfaceDesc = new WaySurfaceDescription();
        waySurfaceDesc.setSurfaceType(1);
        storage.setEdgeValue(6, waySurfaceDesc);
        RoadPropertySpeedMap roadPropertySpeedMap = new RoadPropertySpeedMap();
        roadPropertySpeedMap.addRoadPropertySpeed("paved", 30);
        RoadPropertySpeedCalculator roadPropertySpeedCalculator = new RoadPropertySpeedCalculator();
        roadPropertySpeedCalculator.init(graph, carEncoder, roadPropertySpeedMap);
        weighting.setSpeedCalculator(roadPropertySpeedCalculator);

        path = calcPath(5, 1, graph);
        System.out.println(path.toDetailsString());

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
