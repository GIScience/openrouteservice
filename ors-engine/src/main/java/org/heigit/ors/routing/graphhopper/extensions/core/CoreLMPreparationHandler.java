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

import com.graphhopper.routing.lm.LMConfig;
import com.graphhopper.routing.lm.LMPreparationHandler;
import com.graphhopper.routing.lm.LandmarkSuggestion;
import com.graphhopper.routing.lm.PrepareLandmarks;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RoutingCHGraph;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperConfig;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperStorage;
import org.heigit.ors.routing.graphhopper.extensions.util.GraphUtils;
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
    private static final Logger logger = Logger.getLogger(CoreLandmarkStorage.class);

    private final CoreLMOptions coreLMOptions = new CoreLMOptions();

    public CoreLMPreparationHandler() {
        super();

        PREPARE = CoreLandmark.PREPARE;
        DISABLE = CoreLandmark.DISABLE;
        COUNT = CoreLandmark.COUNT;
    }

    public void init(ORSGraphHopperConfig ghConfig) {
        init(ghConfig, ghConfig.getCoreLMProfiles());

        //Get the landmark sets that should be calculated
        String coreLMSets = ghConfig.getString(CoreLandmark.LMSETS, "allow_all");
        if (!coreLMSets.isEmpty() && !coreLMSets.equalsIgnoreCase("no")) {
            List<String> tmpCoreLMSets = Arrays.asList(coreLMSets.split(";"));
            coreLMOptions.setRestrictionFilters(tmpCoreLMSets);
        }
    }

    @Override
    protected void createPreparationsInternal(GraphHopperStorage ghStorage, List<LandmarkSuggestion> lmSuggestions) {
        for (LMConfig lmConfig : getLMConfigs()) {
            if (!(lmConfig instanceof CoreLMConfig))
                throw(new IllegalStateException("Expected instance of CoreLMConfig"));
            if (!(ghStorage instanceof ORSGraphHopperStorage))
                throw(new IllegalStateException("Expected instance of ORSGraphHopperStorage"));

            CoreLMConfig coreLMConfig = (CoreLMConfig) lmConfig;
            String lmConfigName = coreLMConfig.getSuperName();

            RoutingCHGraph core = ((ORSGraphHopperStorage) ghStorage).getCoreGraph(lmConfigName);
            Map<Integer, Integer> coreNodeIdMap = createCoreNodeIdMap(core);
            logger.info("Created core node ID map for " + coreLMConfig.getName() + " of size " + coreNodeIdMap.size());

            Double maximumWeight = getMaximumWeights().get(lmConfigName);
            if (maximumWeight == null)
                throw new IllegalStateException("maximumWeight cannot be null. Default should be just negative. " +
                        "Couldn't find " + lmConfigName  + " in " + getMaximumWeights());

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

    /**
     * This method creates a mapping of CoreNode ids to integers from 0 to numCoreNodes to save space.
     * Otherwise we would have to store a lot of empty info
     */
    public static HashMap<Integer, Integer> createCoreNodeIdMap(RoutingCHGraph core) {
        HashMap<Integer, Integer> coreNodeIdMap = new HashMap<>();
        int maxNode = GraphUtils.getBaseGraph(core).getNodes();
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
