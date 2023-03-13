package org.heigit.ors.routing.graphhopper.extensions.corelm;

import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHConfig;
import com.graphhopper.storage.RAMDirectory;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperStorage;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLMConfig;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLMOptions;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLMPreparationHandler;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoreLMPreparationHandlerTest {
    private static final String CONF_1 = "conf1";
    private static final String CONF_2 = "conf2";

    @Test
    void testEnabled() {
        CoreLMPreparationHandler instance = new CoreLMPreparationHandler();
        assertFalse(instance.isEnabled());
        instance.setLMProfiles(new LMProfile("myconfig"));
        assertTrue(instance.isEnabled());
    }

    @Test
    void maximumLMWeight() {
        FlagEncoder car = new CarFlagEncoder();
        EncodingManager em = EncodingManager.create(car);
        Weighting shortest = new ShortestWeighting(car);
        Weighting fastest = new FastestWeighting(car);
        CHConfig chShortest = new CHConfig(CONF_1, shortest, false, CHConfig.TYPE_CORE);
        CHConfig chFastest = new CHConfig(CONF_2, fastest, false, CHConfig.TYPE_CORE);
        ORSGraphHopperStorage g = new ORSGraphHopperStorage(new RAMDirectory(), em, false, false, -1);
        g.addCoreGraph(chShortest).addCoreGraph(chFastest);

        CoreLMPreparationHandler coreLMhandler = new CoreLMPreparationHandler();
        coreLMhandler.setLMProfiles(
                new LMProfile(CONF_1).setMaximumLMWeight(65_000),
                new LMProfile(CONF_2).setMaximumLMWeight(20_000)
        );
        coreLMhandler
                .addLMConfig(new CoreLMConfig(CONF_1, fastest).setEdgeFilter(new LMEdgeFilterSequence()))
                .addLMConfig(new CoreLMConfig(CONF_2, shortest).setEdgeFilter(new LMEdgeFilterSequence()));

        String coreLMSets = "allow_all";
        List<String> tmpCoreLMSets = Arrays.asList(coreLMSets.split(";"));
        CoreLMOptions coreLMOptions = coreLMhandler.getCoreLMOptions();
        coreLMOptions.setRestrictionFilters(tmpCoreLMSets);
        coreLMOptions.createRestrictionFilters(g);
        coreLMhandler.createPreparations(g, null);
        assertEquals(1, coreLMhandler.getPreparations().get(0).getLandmarkStorage().getFactor(), .1);
        assertEquals(0.3, coreLMhandler.getPreparations().get(1).getLandmarkStorage().getFactor(), .1);
    }

    @Test
    void testPrepareWeightingNo() {
        GraphHopperConfig ghConfig = new GraphHopperConfig();
        ghConfig.setProfiles(List.of(new Profile("profile")));
        ghConfig.setLMProfiles(List.of(new LMProfile("profile")));
        CoreLMPreparationHandler handler = new CoreLMPreparationHandler();
        handler.init(ghConfig);
        assertTrue(handler.isEnabled());

        // See #1076
        ghConfig.setLMProfiles(Collections.<LMProfile>emptyList());
        handler = new CoreLMPreparationHandler();
        handler.init(ghConfig);
        assertFalse(handler.isEnabled());
    }
}