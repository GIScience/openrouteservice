package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreparePartitionTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);

    @Test
    void testPrepareCellIds() {
        GraphHopperStorage gs = ToyGraphCreationUtil.createMediumGraph(encodingManager);
        PreparePartition partition = new PreparePartition(gs, null);
        partition.prepare();
        CellStorage cs = partition.getCellStorage();
        assertEquals(2, partition.getIsochroneNodeStorage().getCellIds().size());

        int[] cellIds = partition.getIsochroneNodeStorage().getCellIds().toArray();
        IntHashSet expectedIds0 = new IntHashSet(5);
        expectedIds0.addAll(0, 1, 2, 3, 8);
        IntHashSet expectedIds1 = new IntHashSet(4);
        expectedIds1.addAll(4, 5, 6, 7);
        assertTrue(cs.getNodesOfCell(cellIds[0]).equals(expectedIds0) && cs.getNodesOfCell(cellIds[1]).equals(expectedIds1)
                || cs.getNodesOfCell(cellIds[0]).equals(expectedIds1) && cs.getNodesOfCell(cellIds[1]).equals(expectedIds0));
    }

    @Test
    void testPrepareBorderness() {
        GraphHopperStorage gs = ToyGraphCreationUtil.createMediumGraph(encodingManager);
        PreparePartition partition = new PreparePartition(gs, null);
        partition.prepare();
        IsochroneNodeStorage ins = partition.getIsochroneNodeStorage();

        assertFalse(ins.getBorderness(0));
        assertFalse(ins.getBorderness(1));
        assertFalse(ins.getBorderness(2));
        assertTrue(ins.getBorderness(3));
        assertTrue(ins.getBorderness(8));

        assertFalse(ins.getBorderness(5));
        assertFalse(ins.getBorderness(6));
        assertTrue(ins.getBorderness(4));
        assertTrue(ins.getBorderness(7));
    }
}