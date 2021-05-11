package org.heigit.ors.centrality;

import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
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

    public GraphHopperStorage createMediumGraph() {
        //    3---4--5
        //   /\   |  |
        //  2--0  6--7
        //  | / \   /
        //  |/   \ /
        //  1-----8
        GraphHopperStorage g = createGHStorage();
        g.edge(0, 1, 1, true);
        g.edge(0, 2, 1, true);
        g.edge(0, 3, 5, true);
        g.edge(0, 8, 1, true);
        g.edge(1, 2, 1, true);
        g.edge(1, 8, 2, true);
        g.edge(2, 3, 2, true);
        g.edge(3, 4, 2, true);
        g.edge(4, 5, 1, true);
        g.edge(4, 6, 1, true);
        g.edge(5, 7, 1, true);
        g.edge(6, 7, 2, true);
        g.edge(7, 8, 3, true);

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

    public GraphHopperStorage createTwoComponentGraph() {
        //    3   4--5
        //   /\   |  |
        //  2--0  6--7
        //  | / \
        //  |/   \
        //  1-----8
        GraphHopperStorage g = createGHStorage();
        g.edge(0, 1, 1, true);
        g.edge(0, 2, 1, true);
        g.edge(0, 3, 5, true);
        g.edge(0, 8, 1, true);
        g.edge(1, 2, 1, true);
        g.edge(1, 8, 2, true);
        g.edge(2, 3, 2, true);
        g.edge(4, 5, 1, true);
        g.edge(4, 6, 1, true);
        g.edge(5, 7, 1, true);
        g.edge(6, 7, 2, true);

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
    public void testMediumGraph() {
        graphHopper = new ORSGraphHopper();
        graphHopper.setCHEnabled(false);
        graphHopper.setCoreEnabled(false);
        graphHopper.setCoreLMEnabled(false);
        graphHopper.setEncodingManager(encodingManager);
        graphHopper.setGraphHopperStorage(createMediumGraph());
        graphHopper.postProcessing();

        Graph graph = graphHopper.getGraphHopperStorage().getBaseGraph();
        String encoderName = "car";
        FlagEncoder flagEncoder = graphHopper.getEncodingManager().getEncoder(encoderName);
        EdgeExplorer explorer = graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));

        HintsMap hintsMap = new HintsMap();
        //the following two lines represent the setWeighting()-Method of RoutingProfile
        hintsMap.put("weighting", "fastest");
        hintsMap.put("weighting_method", "fastest");
        Weighting weighting = new ORSWeightingFactory().createWeighting(hintsMap, flagEncoder, graphHopper.getGraphHopperStorage());
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
    public void testTwoComponentGraph() {
        graphHopper = new ORSGraphHopper();
        graphHopper.setCHEnabled(false);
        graphHopper.setCoreEnabled(false);
        graphHopper.setCoreLMEnabled(false);
        graphHopper.setEncodingManager(encodingManager);
        graphHopper.setGraphHopperStorage(createTwoComponentGraph());
        graphHopper.postProcessing();

        Graph graph = graphHopper.getGraphHopperStorage().getBaseGraph();
        String encoderName = "car";
        FlagEncoder flagEncoder = graphHopper.getEncodingManager().getEncoder(encoderName);
        EdgeExplorer explorer = graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));

        HintsMap hintsMap = new HintsMap();
        //the following two lines represent the setWeighting()-Method of RoutingProfile
        hintsMap.put("weighting", "fastest");
        hintsMap.put("weighting_method", "fastest");
        Weighting weighting = new ORSWeightingFactory().createWeighting(hintsMap, flagEncoder, graphHopper.getGraphHopperStorage());
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
}