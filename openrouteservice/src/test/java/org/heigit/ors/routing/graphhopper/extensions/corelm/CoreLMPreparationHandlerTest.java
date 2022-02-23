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
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLMConfig;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLMPreparationHandler;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CoreLMPreparationHandlerTest {

    @Test
    public void testEnabled() {
        CoreLMPreparationHandler instance = new CoreLMPreparationHandler();
        assertFalse(instance.isEnabled());
        instance.setLMProfiles(new LMProfile("myconfig"));
        assertTrue(instance.isEnabled());
    }

    @Test
    public void maximumLMWeight() {
        FlagEncoder car = new CarFlagEncoder();
        EncodingManager em = EncodingManager.create(car);
        Weighting shortest = new ShortestWeighting(car);
        Weighting fastest = new FastestWeighting(car);
        CHConfig chShortest = new CHConfig("shortest", shortest, false, CHConfig.TYPE_CORE);
        CHConfig chFastest = new CHConfig("fastest", fastest, false, CHConfig.TYPE_CORE);
        GraphHopperStorage g = new GraphBuilder(em).setCHConfigs(chShortest, chFastest).create();

        CoreLMPreparationHandler coreLMhandler = new CoreLMPreparationHandler();
        coreLMhandler.setLMProfiles(
                new LMProfile("conf1").setMaximumLMWeight(65_000),
                new LMProfile("conf2").setMaximumLMWeight(20_000)
        );
        coreLMhandler
                .addLMConfig(new CoreLMConfig("conf1", fastest))
                .addLMConfig(new CoreLMConfig("conf2", shortest));

        String coreLMSets = "allow_all";
        List<String> tmpCoreLMSets = Arrays.asList(coreLMSets.split(";"));
        coreLMhandler.getCoreLMOptions().setRestrictionFilters(tmpCoreLMSets);

        coreLMhandler.createPreparations(g, null);
        assertEquals(1, coreLMhandler.getPreparations().get(0).getLandmarkStorage().getFactor(), .1);
        assertEquals(0.3, coreLMhandler.getPreparations().get(1).getLandmarkStorage().getFactor(), .1);
    }

    @Test
    public void testPrepareWeightingNo() {
        GraphHopperConfig ghConfig = new GraphHopperConfig();
        ghConfig.setProfiles(Collections.singletonList(new Profile("profile")));
        ghConfig.setLMProfiles(Collections.singletonList(new LMProfile("profile")));
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