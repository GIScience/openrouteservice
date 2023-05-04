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
import com.graphhopper.routing.lm.LandmarkStorage;
import com.graphhopper.routing.lm.PrepareLandmarks;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;

import java.util.Map;

/**
 * This class does the preprocessing for the ALT algorithm (A* , landmark, triangle inequality) in the core.
 * <p>
 * http://www.siam.org/meetings/alenex05/papers/03agoldberg.pdf
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Andrzej Oles
 */
public class PrepareCoreLandmarks extends PrepareLandmarks {
    private final LMEdgeFilterSequence landmarksFilter;

    public PrepareCoreLandmarks(Directory dir, GraphHopperStorage graph, CoreLMConfig lmConfig, int landmarks, Map<Integer, Integer> coreNodeIdMap) {
        super(dir, graph, lmConfig, landmarks);
        this.landmarksFilter = lmConfig.getEdgeFilter();
        CoreLandmarkStorage coreLandmarkStorage = (CoreLandmarkStorage) getLandmarkStorage();
        coreLandmarkStorage.setCoreNodeIdMap(coreNodeIdMap);
    }

    @Override
    public LandmarkStorage createLandmarkStorage (Directory dir, GraphHopperStorage graph, LMConfig lmConfig, int landmarks) {
        if (!(lmConfig instanceof CoreLMConfig))
            throw(new IllegalStateException("Expected instance of CoreLMConfig"));
        if (!(graph instanceof ORSGraphHopperStorage))
            throw(new IllegalStateException("Expected instance of ORSGraphHopperStorage"));

        return new CoreLandmarkStorage(dir, (ORSGraphHopperStorage) graph, (CoreLMConfig) lmConfig, landmarks);
    }

    public boolean matchesFilter(PMap pmap){
        //Returns true if the landmarkset is for the avoidables.
        //Also returns true if the query has no avoidables and the set has no avoidables
        return landmarksFilter.isFilter(pmap);
    }
}
