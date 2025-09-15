package org.heigit.ors.routing;

import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHConfig;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.BooleanEncodedValueEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.ev.DynamicData;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicDataTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder().setSpeedTwoDirections(true);
    private final EncodingManager encodingManager = new EncodingManager.Builder()
            .add(DynamicData.create())
            .add(carEncoder)
            .build();

    private final Weighting weighting = new ShortestWeighting(carEncoder);
    private final CHConfig chConfig = CHConfig.nodeBased("c", weighting);
    private GraphHopperStorage g;

    @BeforeEach
    void setUp() {
        g = createGHStorage();
    }

    private GraphHopperStorage createGHStorage() {
        return createGHStorage(chConfig);
    }

    private GraphHopperStorage createGHStorage(CHConfig c) {
        return new GraphBuilder(encodingManager).setCHConfigs(c).create();
    }


    @Test
    void testDynamicDataUpdate() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);

        RoutingAlgorithm algoBeforeUpdate = createRoutingAlgorithm();
        List<Path> pathsBeforeUpdate = algoBeforeUpdate.calcPaths(0, 1);
        assertEquals(1.0, pathsBeforeUpdate.get(0).getWeight(), 0.01);

        EdgeIteratorState edgeToUpdate = g.getEdgeIteratorState(0, 1);
        BooleanEncodedValue dynamicData = encodingManager.getBooleanEncodedValue(DynamicData.KEY);
        IntsRef edgeflags = edgeToUpdate.getFlags();
        dynamicData.setBool(false, edgeflags, true);
        edgeToUpdate.setFlags(edgeflags);

        RoutingAlgorithm algoAfterUpdate = createRoutingAlgorithm();
        List<Path> pathsAfterUpdate = algoAfterUpdate.calcPaths(0, 1);
        assertEquals(2.0, pathsAfterUpdate.get(0).getWeight(), 0.02);

    }

    private RoutingAlgorithm createRoutingAlgorithm() {
        return new Dijkstra(g, weighting, TraversalMode.NODE_BASED)
                .setEdgeFilter(new BooleanEncodedValueEdgeFilter(encodingManager.getBooleanEncodedValue(DynamicData.KEY)));
    }
}
