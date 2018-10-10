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
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.graphhopper.extensions.flagencoders.HeavyVehicleFlagEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockedEdgesEdgeFilter implements EdgeFilter {
	private Set<Integer> blockedEdges;

	public BlockedEdgesEdgeFilter(FlagEncoder encoder, List<Integer> edges, List<Integer> edges_hv) {
		// use HashSet which has O(1) performance compared to O(n) for List<Integer>
		this.blockedEdges = new HashSet<Integer>();
		this.blockedEdges.addAll(edges);

		if (encoder instanceof HeavyVehicleFlagEncoder)
			this.blockedEdges.addAll(edges_hv);
	}

	@Override
	public boolean accept(EdgeIteratorState iter) {
		return !blockedEdges.contains(EdgeIteratorStateHelper.getOriginalEdge(iter));

	}

}
