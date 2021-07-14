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
package org.heigit.ors.routing.graphhopper.extensions;

import java.util.Collection;

import com.carrotsearch.hppc.LongIndexedContainer;
import com.graphhopper.coll.LongIntMap;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.EdgeIteratorState;

public interface DataReaderContext {
	   LongIntMap getNodeMap();
	   
	   double getNodeLongitude(int nodeId);
	   
	   double getNodeLatitude(int nodeId);

	   Collection<EdgeIteratorState> addWay(final LongIndexedContainer subgraphNodes, final IntsRef wayFlags, final long wayId);
}
