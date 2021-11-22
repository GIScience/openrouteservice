package org.heigit.ors.fastisochrones.partitioning;

import com.graphhopper.GraphHopperConfig;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FastIsochroneFactoryTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);

    @Before
    public void setUp() {
        System.setProperty("ors_config", "target/test-classes/ors-config-test.json");
    }

    private FastIsochroneFactory intitFastIsochroneFactory() {
        FastIsochroneFactory fastIsochroneFactory = new FastIsochroneFactory();
        fastIsochroneFactory.init(new GraphHopperConfig().putObject(ORSParameters.FastIsochrone.PREPARE + "weightings", "fastest"));
        return fastIsochroneFactory;
    }

    @Test
    public void testInit() {
        FastIsochroneFactory fastIsochroneFactory = intitFastIsochroneFactory();
        assertTrue(fastIsochroneFactory.isEnabled());
        assertTrue(fastIsochroneFactory.isDisablingAllowed());
        assertEquals("fastest", fastIsochroneFactory.getFastisochroneProfileStrings().iterator().next());
    }

    @Test
    public void testAddPreparation() {
        GraphHopperStorage gs = ToyGraphCreationUtil.createMediumGraph(encodingManager);
        FastIsochroneFactory fastIsochroneFactory = intitFastIsochroneFactory();
        fastIsochroneFactory.createPreparation(gs, null);
        assertNotNull(fastIsochroneFactory.getPartition().getIsochroneNodeStorage());
        assertNotNull(fastIsochroneFactory.getPartition().getCellStorage());

        assertTrue(fastIsochroneFactory.getPartition().getIsochroneNodeStorage().getCellIds().isEmpty());
        assertEquals(0, fastIsochroneFactory.getPartition().getIsochroneNodeStorage().getCapacity());

        assertFalse(fastIsochroneFactory.getPartition().getCellStorage().isContourPrepared());
        assertEquals(0, fastIsochroneFactory.getPartition().getCellStorage().getCapacity());
    }

    @Test
    public void testPrepare() {
        GraphHopperStorage gs = ToyGraphCreationUtil.createMediumGraph(encodingManager);
        FastIsochroneFactory fastIsochroneFactory = intitFastIsochroneFactory();
        fastIsochroneFactory.createPreparation(gs, null);

        fastIsochroneFactory.prepare(gs.getProperties());
        assertNotNull(fastIsochroneFactory.getCellStorage());
        assertNotNull(fastIsochroneFactory.getIsochroneNodeStorage());
    }
}
