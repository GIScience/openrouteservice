package org.heigit.ors.fastisochrones.partitioning;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.ToyGraphCreationUtil;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PartitioningDataBuilderTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);

    @Test
    public void testPartitioningDataBuilder() {
        GraphHopperStorage ghStorage = ToyGraphCreationUtil.createMediumGraph(encodingManager);
        PartitioningData pData = new PartitioningData();
        EdgeFilter edgeFilter = new EdgeFilterSequence();
        PartitioningDataBuilder partitioningDataBuilder = new PartitioningDataBuilder(ghStorage.getBaseGraph(), pData);
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