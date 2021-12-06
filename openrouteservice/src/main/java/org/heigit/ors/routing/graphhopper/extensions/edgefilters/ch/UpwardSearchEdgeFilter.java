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
package org.heigit.ors.routing.graphhopper.extensions.edgefilters.ch;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.util.EdgeIteratorState;

public class UpwardSearchEdgeFilter extends CHLevelEdgeFilter {
	BooleanEncodedValue accessEnc;
	public UpwardSearchEdgeFilter(RoutingCHGraph g, FlagEncoder encoder) {
		super(g, encoder);
		this.accessEnc = encoder.getAccessEnc();
	}
	
	@Override
	public boolean accept(EdgeIteratorState edgeIterState) {
		int adj = edgeIterState.getAdjNode(); 

		if (baseNode >= maxNodes || adj >= maxNodes || baseNodeLevel <= graph.getLevel(adj))
			return edgeIterState.get(accessEnc);
		else
			return false;
	}
}