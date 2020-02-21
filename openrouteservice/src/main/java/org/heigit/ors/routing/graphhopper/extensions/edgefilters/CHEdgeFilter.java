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
package org.heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.CHEdgeIterator;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.AvoidFeatureFlags;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.RoutingProfileCategory;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.TollwaysGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.WayCategoryGraphStorage;
import org.heigit.ors.routing.pathprocessors.TollwayExtractor;

public class CHEdgeFilter implements EdgeFilter {
	private EdgeFilter baseFilter;
	private boolean acceptShortcuts;

	public CHEdgeFilter(EdgeFilter baseFilter, boolean acceptShortcuts){
		this.baseFilter = baseFilter;
		this.acceptShortcuts = acceptShortcuts;
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {
		if (iter instanceof CHEdgeIteratorState) {
			if (((CHEdgeIteratorState) iter).isShortcut())
				return acceptShortcuts;
		}
		return baseFilter.accept(iter);

	}

}
