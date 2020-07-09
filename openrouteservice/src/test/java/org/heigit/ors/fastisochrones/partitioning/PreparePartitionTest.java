package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.junit.Test;

import static org.junit.Assert.*;

public class PreparePartitionTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);

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
    public void testPrepareCellIds() {
        GraphHopperStorage gs = createMediumGraph();
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
    public void testPrepareBorderness() {
        GraphHopperStorage gs = createMediumGraph();
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