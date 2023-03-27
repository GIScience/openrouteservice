package org.heigit.ors.fastisochrones.partitioning;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperConfig;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FastIsochroneFactoryTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);

    private FastIsochroneFactory intitFastIsochroneFactory() {
        FastIsochroneFactory fastIsochroneFactory = new FastIsochroneFactory();

        List<Profile> fastisochronesProfiles = new ArrayList<>();
        String vehicle = "car";
        String weighting = "fastest";
        String profileName = RoutingProfile.makeProfileName(vehicle, weighting, true);
        Profile profile = new Profile(profileName).setVehicle(vehicle).setWeighting(weighting).setTurnCosts(true);
        fastisochronesProfiles.add(profile);

        ORSGraphHopperConfig orsGraphHopperConfig = new ORSGraphHopperConfig();
        orsGraphHopperConfig.setFastisochroneProfiles(fastisochronesProfiles);

        fastIsochroneFactory.init(orsGraphHopperConfig);
        return fastIsochroneFactory;
    }

    @Test
    void testInit() {
        FastIsochroneFactory fastIsochroneFactory = intitFastIsochroneFactory();
        assertTrue(fastIsochroneFactory.isEnabled());
        assertTrue(fastIsochroneFactory.isDisablingAllowed());
    }

    @Test
    void testAddPreparation() {
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
    void testPrepare() {
        GraphHopperStorage gs = ToyGraphCreationUtil.createMediumGraph(encodingManager);
        FastIsochroneFactory fastIsochroneFactory = intitFastIsochroneFactory();
        fastIsochroneFactory.createPreparation(gs, null);

        fastIsochroneFactory.prepare(gs.getProperties());
        assertNotNull(fastIsochroneFactory.getCellStorage());
        assertNotNull(fastIsochroneFactory.getIsochroneNodeStorage());
    }
}
