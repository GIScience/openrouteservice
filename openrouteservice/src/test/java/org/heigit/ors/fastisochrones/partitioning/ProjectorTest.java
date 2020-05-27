package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ProjectorTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private final Weighting weighting = new ShortestWeighting(carEncoder);

    GraphHopperStorage createGHStorage() {
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

    @Test
    public void testCalculateProjections() {
        Projector projector = new Projector(createMediumGraph());
        Map<Projector.Projection, IntArrayList> projections = projector.calculateProjections();
        //Projection of nodes onto horizontal axis; Ordered by value
        IntArrayList expected_m00 = new IntArrayList();
        expected_m00.add(1, 2, 3, 0, 4, 6, 8, 5, 7);
        assertEquals(expected_m00, projections.get(Projector.Projection.Line_m00));
        //Projection of nodes onto vertical axis; Ordered by value
        IntArrayList expected_p90 = new IntArrayList();
        expected_p90.add(1, 8, 0, 2, 6, 7, 3, 4, 5);
        assertEquals(expected_p90, projections.get(Projector.Projection.Line_p90));
        //Projection of nodes onto positive diagonal axis; Ordered by value
        IntArrayList expected_p45 = new IntArrayList();
        expected_p45.add(1, 2, 8, 0, 3, 6, 4, 7, 5);
        assertEquals(expected_p45, projections.get(Projector.Projection.Line_p45));
    }

    @Test
    public void testCalculateProjectionOrder() {
        Projector projector = new Projector(createMediumGraph());
        Map<Projector.Projection, IntArrayList> projections = projector.calculateProjections();
        List<Projector.Projection> projectionOrder = projector.calculateProjectionOrder(projections);
        //m00 and p45 should be best as they lead to max flow of 2
        assertEquals(Projector.Projection.Line_m00, projectionOrder.get(0));
        assertEquals(Projector.Projection.Line_p45, projectionOrder.get(1));
        //m45 should be worst as it leads to max flow 3 or 4
        assertEquals(Projector.Projection.Line_m45, projectionOrder.get(7));
    }

    @Test
    public void testPartitionProjection() {
        Projector projector = new Projector(createMediumGraph());
        //Calculate global projection
        Map<Projector.Projection, IntArrayList> projections = projector.calculateProjections();
        //Mock partition graph
        IntHashSet part0 = new IntHashSet();
        IntHashSet part1 = new IntHashSet();
        part0.addAll(0, 1, 2, 3, 8);
        part1.addAll(4, 5, 6, 7);
        BiPartition biPartition = new BiPartition(part0, part1);
        BiPartitionProjection biPartitionProjection = projector.partitionProjections(projections, biPartition);
        //partitionProjection separates the original projection into two subsets according to biPartition while maintaining original order
        IntArrayList expectedPart0_m00 = new IntArrayList();
        expectedPart0_m00.add(1, 2, 3, 0, 8);
        assertEquals(expectedPart0_m00, biPartitionProjection.getProjection(0).get(Projector.Projection.Line_m00));

        IntArrayList expectedPart0_m45 = new IntArrayList();
        expectedPart0_m45.add(8, 0, 1, 2, 3);
        assertEquals(expectedPart0_m45, biPartitionProjection.getProjection(0).get(Projector.Projection.Line_m45));

        IntArrayList expectedPart1_m00 = new IntArrayList();
        expectedPart1_m00.add(4, 6, 5, 7);
        assertEquals(expectedPart1_m00, biPartitionProjection.getProjection(1).get(Projector.Projection.Line_m00));

        IntArrayList expectedPart1_m45 = new IntArrayList();
        expectedPart1_m45.add(7, 5, 6, 4);
        assertEquals(expectedPart1_m45, biPartitionProjection.getProjection(1).get(Projector.Projection.Line_m45));
    }
}