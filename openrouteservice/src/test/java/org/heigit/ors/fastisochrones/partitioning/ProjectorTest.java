package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.getSplitValue;
import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.setSplitValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectorTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);

    @Test
    void testCalculateProjections() {
        Projector projector = new Projector();
        projector.setGHStorage(ToyGraphCreationUtil.createMediumGraph2(encodingManager));
        Map<Projector.Projection, IntArrayList> projections = projector.calculateProjections();
        //Projection of nodes onto horizontal axis; Ordered by value
        IntArrayList expected_m00 = new IntArrayList();
        expected_m00.add(1, 2, 3, 0, 8, 4, 6, 5, 7);
        assertEquals(expected_m00, projections.get(Projector.Projection.LINE_M00));
        //Projection of nodes onto vertical axis; Ordered by value
        IntArrayList expected_p90 = new IntArrayList();
        expected_p90.add(1, 8, 0, 2, 6, 7, 3, 4, 5);
        assertEquals(expected_p90, projections.get(Projector.Projection.LINE_P90));
        //Projection of nodes onto positive diagonal axis; Ordered by value
        IntArrayList expected_p45 = new IntArrayList();
        expected_p45.add(1, 2, 8, 0, 3, 6, 4, 7, 5);
        assertEquals(expected_p45, projections.get(Projector.Projection.LINE_P45));
    }

    @Test
    void testCalculateProjectionOrder() {
        double originalSplitValue = getSplitValue();
        //Set to 0 to incorporate all nodes for splitting. Useful for a small graph like this
        setSplitValue(0);
        Projector projector = new Projector();
        projector.setGHStorage(ToyGraphCreationUtil.createMediumGraph2(encodingManager));
        Map<Projector.Projection, IntArrayList> projections = projector.calculateProjections();
        List<Projector.Projection> projectionOrder = projector.calculateProjectionOrder(projections);
        //p675 and p45 should be best as they lead to max flow of 2
        assertEquals(Projector.Projection.LINE_P675, projectionOrder.get(0));
        assertEquals(Projector.Projection.LINE_P45, projectionOrder.get(1));
        //m225 should be worst as it leads to max flow 3 or 4
        assertEquals(Projector.Projection.LINE_M225, projectionOrder.get(7));
        //Reset
        setSplitValue(originalSplitValue);
    }

    @Test
    void testPartitionProjection() {
        Projector projector = new Projector();
        projector.setGHStorage(ToyGraphCreationUtil.createMediumGraph2(encodingManager));
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
        assertEquals(expectedPart0_m00, biPartitionProjection.getProjection(0).get(Projector.Projection.LINE_M00));

        IntArrayList expectedPart0_p45 = new IntArrayList();
        expectedPart0_p45.add(1, 2, 8, 0, 3);
        assertEquals(expectedPart0_p45, biPartitionProjection.getProjection(0).get(Projector.Projection.LINE_P45));

        IntArrayList expectedPart1_m00 = new IntArrayList();
        expectedPart1_m00.add(4, 6, 5, 7);
        assertEquals(expectedPart1_m00, biPartitionProjection.getProjection(1).get(Projector.Projection.LINE_M00));

        IntArrayList expectedPart1_p45 = new IntArrayList();
        expectedPart1_p45.add(6, 4, 7, 5);
        assertEquals(expectedPart1_p45, biPartitionProjection.getProjection(1).get(Projector.Projection.LINE_P45));
    }

    @Test
    void testCalculateProjectionsWithoutLatLon() {
        //All projections are the same if there is no data on where the nodes are. This creates no usable projections and throws an exception.
        Projector projector = new Projector();
        projector.setGHStorage(ToyGraphCreationUtil.createSimpleGraphWithoutLatLon(encodingManager));
        assertThrows(IllegalStateException.class, projector::calculateProjections);
    }
}