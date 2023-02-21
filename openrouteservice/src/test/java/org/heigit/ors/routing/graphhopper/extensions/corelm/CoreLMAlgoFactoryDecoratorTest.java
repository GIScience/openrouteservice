/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions.corelm;

import com.graphhopper.routing.lm.LMAlgoFactoryDecorator;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Parameters;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLMAlgoFactoryDecorator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoreLMAlgoFactoryDecoratorTest {

    @Test
    void addWeighting() {
        CoreLMAlgoFactoryDecorator dec = new CoreLMAlgoFactoryDecorator().setEnabled(true);
        dec.addWeighting("fastest");
        assertEquals(Collections.singletonList("fastest"), dec.getWeightingsAsStrings());

        // special parameters like the maximum weight
        dec = new CoreLMAlgoFactoryDecorator().setEnabled(true);
        dec.addWeighting("fastest|maximum=65000");
        dec.addWeighting("shortest|maximum=20000");
        assertEquals(Arrays.asList("fastest", "shortest"), dec.getWeightingsAsStrings());

        FlagEncoder car = new CarFlagEncoder();
        EncodingManager em = EncodingManager.create(car);
        Weighting weighting = new ShortestWeighting(car);
        dec.addWeighting(weighting);
        String coreLMSets = "allow_all";
        List<String> tmpCoreLMSets = Arrays.asList(coreLMSets.split(";"));
        dec.getCoreLMOptions().setRestrictionFilters(tmpCoreLMSets);

        GraphHopperStorage graph = new GraphBuilder(em).setCHProfiles(new ArrayList<>()).setCoreGraph(weighting).create();
        dec.createPreparations(graph, null);
        assertEquals(0.3, dec.getPreparations().get(0).getLandmarkStorage().getFactor(), .1);
    }

    @Test
    void testPrepareWeightingNo() {
        CmdArgs args = new CmdArgs();
        args.put(Parameters.Landmark.PREPARE + "weightings", "fastest");
        LMAlgoFactoryDecorator dec = new LMAlgoFactoryDecorator();
        dec.init(args);
        assertTrue(dec.isEnabled());

        // See #1076
        args.put(Parameters.Landmark.PREPARE + "weightings", "no");
        dec = new LMAlgoFactoryDecorator();
        dec.init(args);
        assertFalse(dec.isEnabled());
    }
}