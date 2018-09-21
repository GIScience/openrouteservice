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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.HillIndexGraphStorage;

public class AvoidSteepnessEdgeFilter implements EdgeFilter {
	private byte[] buffer;
    private double maximumSteepness;
    private HillIndexGraphStorage gsHillIndex;
    
	public AvoidSteepnessEdgeFilter(GraphStorage graphStorage, double maxSteepness) {
		this.maximumSteepness = maxSteepness;
        this.buffer = new byte[1];
        
        gsHillIndex = GraphStorageUtils.getGraphExtension(graphStorage, HillIndexGraphStorage.class);
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {
		if (gsHillIndex != null)
		{
			boolean revert = iter.getBaseNode() < iter.getAdjNode();
			int hillIndex = gsHillIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(iter), revert, buffer);

			if (hillIndex > maximumSteepness)
				return false;
		}

		return true;

	}

}
