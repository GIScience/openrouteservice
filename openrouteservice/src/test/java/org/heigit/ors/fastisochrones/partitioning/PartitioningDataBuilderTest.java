package org.heigit.ors.fastisochrones.partitioning;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PartitioningDataBuilderTest {
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
        return g;
    }

    @Test
    public void testPartitioningDataBuilder() {
        GraphHopperStorage ghStorage = createMediumGraph();
        PartitioningData pData = new PartitioningData();
        EdgeFilter edgeFilter = new EdgeFilterSequence();
        PartitioningDataBuilder partitioningDataBuilder = new PartitioningDataBuilder(ghStorage.getBaseGraph(), pData);
        partitioningDataBuilder.setAdditionalEdgeFilter(edgeFilter);
        partitioningDataBuilder.run();
        assertEquals(28, pData.flowEdgeBaseNode.length);
        assertEquals(28, pData.flow.length);
        assertEquals(0, pData.flowEdgeBaseNode[0]);
        assertEquals(1, pData.flowEdgeBaseNode[1]);
        assertEquals(0, pData.flowEdgeBaseNode[2]);
        assertEquals(2, pData.flowEdgeBaseNode[3]);

        assertEquals(10, pData.visited.length);
        assertEquals(0, pData.visited[0]);
    }
}