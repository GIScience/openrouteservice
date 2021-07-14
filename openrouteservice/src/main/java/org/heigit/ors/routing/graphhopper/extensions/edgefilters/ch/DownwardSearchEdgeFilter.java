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

import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

public class DownwardSearchEdgeFilter extends CHLevelEdgeFilter {
	protected final BooleanEncodedValue accessEnc;
	private boolean useCore = false;
	private boolean useCoreTurnRestrictions = false;
	private int coreNodeLevel = -1;


	public DownwardSearchEdgeFilter(CHGraph g, FlagEncoder encoder) {
		super(g, encoder);
		accessEnc = encoder.getAccessEnc();
	}

	public DownwardSearchEdgeFilter(CHGraph g, FlagEncoder encoder, boolean useCore) {
		this(g, encoder);
		this.useCore = useCore;
		this.coreNodeLevel = maxNodes + 1;

	}

	public DownwardSearchEdgeFilter(CHGraph g, FlagEncoder encoder, boolean useCore, boolean useCoreTurnRestrictions) {
		this(g, encoder, useCore);
		if(useCore == false && useCoreTurnRestrictions == true)
			throw new IllegalArgumentException("If turn restrictions in core should be respected, core must be respected, too.");
	}

	@Override
	public boolean accept(EdgeIteratorState edgeIterState) {
		int adj = edgeIterState.getAdjNode(); 
		if(useCore && !useCoreTurnRestrictions) {
			if (baseNode >= maxNodes || adj >= maxNodes || baseNodeLevel < graph.getLevel(adj))
				return edgeIterState.getReverse(accessEnc);
			else
				return false;
		}
		if(useCore && useCoreTurnRestrictions) {
			if(baseNodeLevel == coreNodeLevel && graph.getLevel(adj) == coreNodeLevel + 1)
				return false;
			if (baseNode >= maxNodes || adj >= maxNodes || baseNodeLevel < graph.getLevel(adj))
				return edgeIterState.getReverse(accessEnc);
			else
				return false;
		}
		if (baseNode >= maxNodes || adj >= maxNodes || baseNodeLevel <= graph.getLevel(adj))
			return edgeIterState.getReverse(accessEnc);
		else
			return false;
	}
}
