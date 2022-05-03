package org.heigit.ors.centrality;

import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.PMap;
import junit.framework.TestCase;
import org.heigit.ors.centrality.algorithms.CentralityAlgorithm;
import org.heigit.ors.centrality.algorithms.brandes.BrandesCentralityAlgorithm;
import org.heigit.ors.common.Pair;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import org.junit.Test;

import java.util.*;

public class CentralityAlgorithmTest extends TestCase {
    private CentralityAlgorithm alg = new BrandesCentralityAlgorithm();
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private ORSGraphHopper graphHopper;

    private GraphHopperStorage createGHStorage() {
        return new GraphBuilder(encodingManager).create();
    }

    public GraphHopperStorage createMediumDirectedGraph() {
        //    3---4--5
        //   /\   |  |
        //  2--0  6--7
        //  | / \   /
        //  |/   \ /
        //  1-----8
        GraphHopperStorage g = createGHStorage();

        // explicitly create directed edges instead of using edge(a, b, dist, bothDirections)
        // this will also avoid problems with flagEncoder.getAverageSpeedEnc().isStoreTwoDirections() == false
        g.edge(0, 1).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(1, 0).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(0, 2).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(2, 0).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(0, 3).setDistance(5d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(3, 0).setDistance(5d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(0, 8).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(8, 0).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(1, 2).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(2, 1).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(1, 8).setDistance(2d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(8, 1).setDistance(2d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(2, 3).setDistance(2d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(3, 2).setDistance(2d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(3, 4).setDistance(2d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(4, 3).setDistance(2d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(4, 5).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(5, 4).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(4, 6).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(6, 4).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(5, 7).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(7, 5).setDistance(1d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(6, 7).setDistance(2d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(7, 6).setDistance(2d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(7, 8).setDistance(3d).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(8, 7).setDistance(3d).setFlags(encodingManager.flagsDefault(true, false));

        //Set test lat lon
        g.getBaseGraph().getNodeAccess().setNode(0, 3, 3);
        g.getBaseGraph().getNodeAccess().setNode(1, 1, 1);
        g.getBaseGraph().getNodeAccess().setNode(2, 3, 1);
        g.getBaseGraph().getNodeAccess().setNode(3, 4, 2);
        g.getBaseGraph().getNodeAccess().setNode(4, 4, 4);
        g.getBaseGraph().getNodeAccess().setNode(5, 4, 5);
        g.getBaseGraph().getNodeAccess().setNode(6, 3, 4);
        g.getBaseGraph().getNodeAccess().setNode(7, 3, 5);
        g.getBaseGraph().getNodeAccess().setNode(8, 1, 4);

        return g;
    }

    public GraphHopperStorage createTwoComponentDirectedGraph() {
        //    3   4--5
        //   /\   |  |
        //  2--0  6--7
        //  | / \
        //  |/   \
        //  1-----8
        GraphHopperStorage g = createGHStorage();
        g.edge(0, 1).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(1, 0).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(0, 2).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(2, 0).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(0, 3).setDistance(5).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(3, 0).setDistance(5).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(0, 8).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(8, 0).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(1, 2).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(2, 1).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(1, 8).setDistance(2).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(8, 1).setDistance(2).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(2, 3).setDistance(2).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(3, 2).setDistance(2).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(4, 5).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(5, 4).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(4, 6).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(6, 4).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(5, 7).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(7, 5).setDistance(1).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(6, 7).setDistance(2).setFlags(encodingManager.flagsDefault(true, false));
        g.edge(7, 6).setDistance(2).setFlags(encodingManager.flagsDefault(true, false));

        //Set test lat lo
        g.getBaseGraph().getNodeAccess().setNode(0, 3, 3);
        g.getBaseGraph().getNodeAccess().setNode(1, 1, 1);
        g.getBaseGraph().getNodeAccess().setNode(2, 3, 1);
        g.getBaseGraph().getNodeAccess().setNode(3, 4, 2);
        g.getBaseGraph().getNodeAccess().setNode(4, 4, 4);
        g.getBaseGraph().getNodeAccess().setNode(5, 4, 5);
        g.getBaseGraph().getNodeAccess().setNode(6, 3, 4);
        g.getBaseGraph().getNodeAccess().setNode(7, 3, 5);
        g.getBaseGraph().getNodeAccess().setNode(8, 1, 4);

        return g;
    }

       @Test
    public void testMediumDirectedGraphNodeCentrality() {
        graphHopper = new ORSGraphHopper();
        graphHopper.setCHEnabled(false);
        graphHopper.setCoreEnabled(false);
        graphHopper.setCoreLMEnabled(false);
        graphHopper.setEncodingManager(encodingManager);
        graphHopper.setGraphHopperStorage(createMediumDirectedGraph());
        graphHopper.postProcessing();

        Graph graph = graphHopper.getGraphHopperStorage().getBaseGraph();
        String encoderName = "car";
        FlagEncoder flagEncoder = graphHopper.getEncodingManager().getEncoder(encoderName);
        EdgeExplorer explorer = graph.createEdgeExplorer(AccessFilter.outEdges(flagEncoder.getAccessEnc()));

        PMap hintsMap = new PMap();
        //the following two lines represent the setWeighting()-Method of RoutingProfile
        hintsMap.putObject("weighting", "fastest");
        hintsMap.putObject("weighting_method", "fastest");
        Weighting weighting = new ORSWeightingFactory(graphHopper.getGraphHopperStorage(), flagEncoder).createWeighting(hintsMap, false);
        alg = new BrandesCentralityAlgorithm();
        alg.init(graph, weighting, explorer);


        List<Integer> nodes = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
        List<Double> expectedScores = new ArrayList<>(Arrays.asList(26d / 3d, 0d, 41d / 3d, 41d / 3d, 47d / 3d, 6d, 0d, 31d / 3d, 31d / 3d));

        Map<Integer, Double> betweenness = null;
        try {
            betweenness = alg.computeNodeCentrality(nodes);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        for (Integer v : nodes) {
            assertEquals(expectedScores.get(v), betweenness.get(v), 0.0001d);
        }
    }

    @Test
    public void testMediumDirectedGraphEdgeCentrality() {
        graphHopper = new ORSGraphHopper();
        graphHopper.setCHEnabled(false);
        graphHopper.setCoreEnabled(false);
        graphHopper.setCoreLMEnabled(false);
        graphHopper.setEncodingManager(encodingManager);
        graphHopper.setGraphHopperStorage(createMediumDirectedGraph());
        graphHopper.postProcessing();

        Graph graph = graphHopper.getGraphHopperStorage().getBaseGraph();
        String encoderName = "car";
        FlagEncoder flagEncoder = graphHopper.getEncodingManager().getEncoder(encoderName);
        EdgeExplorer explorer = graph.createEdgeExplorer(AccessFilter.outEdges(flagEncoder.getAccessEnc()));

        PMap hintsMap = new PMap();
        //the following two lines represent the setWeighting()-Method of RoutingProfile
        hintsMap.put("weighting", "fastest");
        hintsMap.put("weighting_method", "fastest");
        Weighting weighting = new ORSWeightingFactory(graphHopper.getGraphHopperStorage(), flagEncoder).createWeighting(hintsMap, false);
        alg = new BrandesCentralityAlgorithm();
        alg.init(graph, weighting, explorer);


        List<Integer> nodes = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
        List<Pair<Integer, Integer>> edges = Arrays.asList(new Pair(0,1), new Pair(0,2), new Pair(0,3), new Pair(0,8), new Pair(1,0), new Pair(1,2), new Pair(1,8), new Pair(2,0), new Pair(2,1), new Pair(2,3), new Pair(3,0), new Pair(3,2), new Pair(3,4), new Pair(4,3), new Pair(4,5), new Pair(4,6), new Pair(5,4), new Pair(5,7), new Pair(6,4), new Pair(6,7), new Pair(7,5), new Pair(7,6), new Pair(7,8), new Pair(8,0), new Pair(8,1), new Pair(8,7));
        List<Double> expectedScores = new ArrayList<>(Arrays.asList(7d/3d, 6.5d, 0d, 47d/6d, 7d/3d, 13d/3d, 4d/3d, 6.5d, 13d/3d, 65d/6d, 0d, 65d/6d, 65d/6d, 65d/6d, 22d/3d, 5.5d, 22d/3d, 20d/3d, 5.5d, 2.5d, 20d/3d, 2.5d, 55d/6d, 47d/6d, 4d/3d, 55d/6d));
        assertEquals(edges.size(), expectedScores.size());

        Map<Pair<Integer, Integer>, Double> betweenness = null;
        try {
            betweenness = alg.computeEdgeCentrality(nodes);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        for (Pair<Integer, Integer> p : edges) {
            assertEquals(expectedScores.get(edges.indexOf(p)), betweenness.get(p), 0.0001d);
        }
    }

    @Test
    public void testTwoComponentDirectedGraphNodeCentrality() {
        graphHopper = new ORSGraphHopper();
        graphHopper.setCHEnabled(false);
        graphHopper.setCoreEnabled(false);
        graphHopper.setCoreLMEnabled(false);
        graphHopper.setEncodingManager(encodingManager);
        graphHopper.setGraphHopperStorage(createTwoComponentDirectedGraph());
        graphHopper.postProcessing();

        Graph graph = graphHopper.getGraphHopperStorage().getBaseGraph();
        String encoderName = "car";
        FlagEncoder flagEncoder = graphHopper.getEncodingManager().getEncoder(encoderName);
        EdgeExplorer explorer = graph.createEdgeExplorer(AccessFilter.outEdges(flagEncoder.getAccessEnc()));

        PMap hintsMap = new PMap();
        //the following two lines represent the setWeighting()-Method of RoutingProfile
        hintsMap.put("weighting", "fastest");
        hintsMap.put("weighting_method", "fastest");
        Weighting weighting = new ORSWeightingFactory(graphHopper.getGraphHopperStorage(), flagEncoder).createWeighting(hintsMap, false);
        alg = new BrandesCentralityAlgorithm();
        alg.init(graph, weighting, explorer);


        List<Integer> nodes = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
        List<Double> expectedScores = new ArrayList<>(Arrays.asList(5d, 0d, 6d, 0d, 2d, 2d, 0d, 0d, 0d));

        Map<Integer, Double> betweenness = null;
        try {
            betweenness = alg.computeNodeCentrality(nodes);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        for (Integer v : nodes) {
            assertEquals(expectedScores.get(v), betweenness.get(v), 0.0001d);
        }
    }

    @Test
    public void testTwoComponentDirectedGraphEdgeCentrality() {
        graphHopper = new ORSGraphHopper();
        graphHopper.setCHEnabled(false);
        graphHopper.setCoreEnabled(false);
        graphHopper.setCoreLMEnabled(false);
        graphHopper.setEncodingManager(encodingManager);
        graphHopper.setGraphHopperStorage(createTwoComponentDirectedGraph());
        graphHopper.postProcessing();

        Graph graph = graphHopper.getGraphHopperStorage().getBaseGraph();
        String encoderName = "car";
        FlagEncoder flagEncoder = graphHopper.getEncodingManager().getEncoder(encoderName);
        EdgeExplorer explorer = graph.createEdgeExplorer(AccessFilter.outEdges(flagEncoder.getAccessEnc()));

        PMap hintsMap = new PMap();
        //the following two lines represent the setWeighting()-Method of RoutingProfile
        hintsMap.putObject("weighting", "fastest");
        hintsMap.putObject("weighting_method", "fastest");
        Weighting weighting = new ORSWeightingFactory(graphHopper.getGraphHopperStorage(), flagEncoder).createWeighting(hintsMap, false);
        alg = new BrandesCentralityAlgorithm();
        alg.init(graph, weighting, explorer);

        List<Integer> nodes = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
        List<Pair<Integer, Integer>> edges = Arrays.asList(new Pair(0,1), new Pair(0,2), new Pair(0,3), new Pair(0,8), new Pair(1,0), new Pair(1,2), new Pair(1,8), new Pair(2,0), new Pair(2,1), new Pair(2,3), new Pair(3,0), new Pair(3,2),  new Pair(4,5), new Pair(4,6), new Pair(5,4), new Pair(5,7), new Pair(6,4), new Pair(6,7), new Pair(7,5), new Pair(7,6), new Pair(8,0), new Pair(8,1));
        List<Double> expectedScores = new ArrayList<>(Arrays.asList(1.5d, 4.0d, 0.0d, 3.5d, 1.5d, 2.0d, 0.5d, 4.0d, 2.0d, 4.0d, 0.0d, 4.0d, 3.0d, 2.0d, 3.0d, 2.0d, 2.0d, 1.0d, 2.0d, 1.0d, 3.5d, 0.5d));

        Map<Pair<Integer, Integer>, Double> betweenness = null;
        try {
            betweenness = alg.computeEdgeCentrality(nodes);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        for (Pair<Integer, Integer> p : edges) {
            assertEquals(expectedScores.get(edges.indexOf(p)), betweenness.get(p), 0.0001d);
        }
    }
}