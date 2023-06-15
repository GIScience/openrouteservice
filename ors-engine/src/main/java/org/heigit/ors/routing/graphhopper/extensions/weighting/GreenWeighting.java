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
package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.GreenIndexGraphStorage;

/**
 * Created by lliu on 15/03/2017.
 */
public class GreenWeighting extends FastestWeighting {
    private final GreenIndexGraphStorage gsGreenIndex;
    private final byte[] buffer = new byte[1];
    private final double[] factors = new double[TOTAL_LEVEL];

    private static final int TOTAL_LEVEL = 64;

    public GreenWeighting(FlagEncoder encoder, PMap map, GraphHopperStorage graphStorage) {
        super(encoder, map);
        
        gsGreenIndex = GraphStorageUtils.getGraphExtension(graphStorage, GreenIndexGraphStorage.class);
        double factor = map.getDouble("factor", 1);
        
        for (int i = 0; i < TOTAL_LEVEL; i++)
        	factors[i] = calcGreenWeightFactor(i, factor);
    }

    private double calcGreenWeightFactor(int level, double factor) {
        // There is an implicit convention here:
        // the green level range is [0, total - 1].
        // And the @level will be transformed to a float number
        // falling in (0, 2] linearly
        // However, for the final green weighting,
        // a weighting factor will be taken into account
        // to control the impact of the "green consideration"
        // just like an amplifier
        double wf = (double) (level + 1) * 2.0 / TOTAL_LEVEL;
        return 1.0 - (1.0 - wf) * factor;
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
        if (gsGreenIndex != null) {
            int greenLevel = gsGreenIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edgeState), buffer);
            return factors[greenLevel];
        }

        return 1.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final GreenWeighting other = (GreenWeighting) obj;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return ("GreenWeighting" + this).hashCode();
    }
}
