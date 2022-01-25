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
package org.heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.GraphHopperConfig;
import com.graphhopper.routing.lm.LMConfig;
import com.graphhopper.routing.lm.LMPreparationHandler;
import com.graphhopper.routing.lm.LandmarkSuggestion;
import com.graphhopper.routing.lm.PrepareLandmarks;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RoutingCHGraph;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters.CoreLandmark;
import java.util.*;

/**
 * This class implements the A*, landmark and triangulation (ALT) decorator for Core.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 * @author Andrzej Oles
 */
public class CoreLMPreparationHandler extends LMPreparationHandler {

    private final CoreLMOptions coreLMOptions = new CoreLMOptions();

    public CoreLMPreparationHandler() {
        super();

        PREPARE = CoreLandmark.PREPARE;
        DISABLE = CoreLandmark.DISABLE;
        COUNT = CoreLandmark.COUNT;
    }

    @Override
    public void init(GraphHopperConfig ghConfig) {
        super.init(ghConfig);

        //Get the landmark sets that should be calculated
        String coreLMSets = ghConfig.getString(CoreLandmark.LMSETS, "allow_all");
        if (!coreLMSets.isEmpty() && !coreLMSets.equalsIgnoreCase("no")) {
            List<String> tmpCoreLMSets = Arrays.asList(coreLMSets.split(";"));
            coreLMOptions.setRestrictionFilters(tmpCoreLMSets);
        }
    }

    @Override
    protected void createPreparationsInternal(GraphHopperStorage ghStorage, List<LandmarkSuggestion> lmSuggestions) {
        coreLMOptions.createRestrictionFilters(ghStorage);

        for (LMConfig lmConfig : getLMConfigs()) {
            Map<Integer, Integer> coreNodeIdMap = createCoreNodeIdMap(ghStorage, lmConfig.getWeighting());

            for (LMEdgeFilterSequence edgeFilterSequence : coreLMOptions.getFilters()) {
                Double maximumWeight = getMaximumWeights().get(lmConfig.getName());
                if (maximumWeight == null)
                    throw new IllegalStateException("maximumWeight cannot be null. Default should be just negative. " +
                            "Couldn't find " + lmConfig.getName() + " in " + getMaximumWeights());

                CoreLMConfig coreLMConfig = new CoreLMConfig(lmConfig.getName(), lmConfig.getWeighting(), edgeFilterSequence);
                PrepareLandmarks tmpPrepareLM = new PrepareCoreLandmarks(ghStorage.getDirectory(), ghStorage,
                        coreLMConfig, getLandmarks(), coreNodeIdMap).
                        setLandmarkSuggestions(lmSuggestions).
                        setMaximumWeight(maximumWeight).
                        setLogDetails(getLogDetails());
                if (getMinNodes() > 1)
                    tmpPrepareLM.setMinimumNodes(getMinNodes());
                addPreparation(tmpPrepareLM);
            }
        }
    }

    /**
     * This method creates a mapping of CoreNode ids to integers from 0 to numCoreNodes to save space.
     * Otherwise we would have to store a lot of empty info
     */
    public Map<Integer, Integer> createCoreNodeIdMap(GraphHopperStorage graph, Weighting weighting) {
        RoutingCHGraph core = graph.getCoreGraph(weighting);
        HashMap<Integer, Integer> coreNodeIdMap = new HashMap<>();
        int maxNode = graph.getNodes();
        int coreNodeLevel = maxNode;
        int index = 0;
        for (int i = 0; i < maxNode; i++){
            if (core.getLevel(i) < coreNodeLevel)
                continue;
            coreNodeIdMap.put(i, index);
            index++;
        }
        return coreNodeIdMap;
    }

    public CoreLMOptions getCoreLMOptions(){
        return coreLMOptions;
    }

}
