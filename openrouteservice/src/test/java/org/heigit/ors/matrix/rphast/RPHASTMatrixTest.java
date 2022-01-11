package org.heigit.ors.matrix.rphast;

import com.graphhopper.routing.ch.NodeOrderingProvider;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.TurnCost;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import org.heigit.ors.routing.graphhopper.extensions.core.PrepareCore;
import org.heigit.ors.util.DebugUtility;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.Before;
import org.junit.Test;

import static com.graphhopper.util.GHUtility.getEdge;
import static org.junit.Assert.assertEquals;

public class RPHASTMatrixTest {
    private final TraversalMode tMode = TraversalMode.NODE_BASED;
    private final CarFlagEncoder carEncoder = new CarFlagEncoder(5, 5, 3);
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private final Weighting weighting = new ShortestWeighting(carEncoder);
    private final CHConfig chConfig = new CHConfig("c", weighting, false, CHConfig.TYPE_CORE);
    private GraphHopperStorage g;
    private RoutingCHGraph routingCHGraph;

    private void addRestrictedTurn(GraphHopperStorage g, int from, int via, int to) {
        setTurnCost(g, Double.POSITIVE_INFINITY, from, via, to);
    }

    private void setTurnCost(GraphHopperStorage g, double cost, int from, int via, int to) {
        g.getTurnCostStorage().set(((EncodedValueLookup) g.getEncodingManager()).getDecimalEncodedValue(TurnCost.key(carEncoder.toString())), getEdge(g, from, via).getEdge(), via, getEdge(g, via, to).getEdge(), cost);
    }

    @Before
    public void setUp() {
        g = new GraphBuilder(encodingManager).setCHConfigs(chConfig).create();
        routingCHGraph = g.getRoutingCHGraph();
    }

    private PrepareContractionHierarchies createPrepareContractionHierarchies(GraphHopperStorage g) {
        return createPrepareContractionHierarchies(g, chConfig);
    }

    private PrepareContractionHierarchies createPrepareContractionHierarchies(GraphHopperStorage g, CHConfig p) {
        g.freeze();
        return PrepareContractionHierarchies.fromGraphHopperStorage(g, p);
    }

    private void useNodeOrdering(PrepareContractionHierarchies prepare, int[] nodeOrdering) {
        prepare.useFixedNodeOrdering(NodeOrderingProvider.fromArray(nodeOrdering));
    }

    private void contractGraph(EdgeFilter restrictedEdges) {
        contractGraph(restrictedEdges, null);
    }

    private void contractGraph(EdgeFilter restrictedEdges, int[] nodeOrdering) {
        g.freeze();

        PrepareCore prepare = new PrepareCore(g, chConfig, restrictedEdges);

        if (nodeOrdering != null)
            prepare.useFixedNodeOrdering(NodeOrderingProvider.fromArray(nodeOrdering));

        // set contraction parameters to prevent test results from changing when algorithm parameters are tweaked
        //prepare.setParams(new PMap(CONTRACTED_NODES+"=100"));

        prepare.doWork();

        if (DebugUtility.isDebug()) {
            for (int i = 0; i < routingCHGraph.getNodes(); i++)
                System.out.println("nodeId " + i + " level: " + routingCHGraph.getLevel(i));
            for (int i = 0; i < routingCHGraph.getNodes(); i++) {
                RoutingCHEdgeIterator iter = routingCHGraph.createOutEdgeExplorer().setBaseNode(i);
                while (iter.next()) {
                    System.out.print(iter.getBaseNode() + " -> " + iter.getAdjNode() + " via edge " + iter.getEdge());
                    if (iter.isShortcut())
                        System.out.print(" (shortcut)");
                    System.out.println(" [weight: " + iter.getWeight(false) + "]");
                }
            }
        }
    }

    @Test
    public void testMoreComplexGraph() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(g, encodingManager);

        int oldCount = routingCHGraph.getEdges();
        PrepareContractionHierarchies prepare = createPrepareContractionHierarchies(graphHopperStorage);
        useNodeOrdering(prepare, new int[]{0, 5, 6, 7, 8, 10, 11, 13, 15, 1, 3, 9, 14, 16, 12, 4, 2});
        prepare.doWork();
        assertEquals(oldCount, g.getEdges());
        assertEquals(oldCount + 7, routingCHGraph.getEdges());
    }
}
