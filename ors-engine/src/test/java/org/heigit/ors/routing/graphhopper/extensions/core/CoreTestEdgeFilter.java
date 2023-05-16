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

import java.util.HashSet;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;

public class CoreTestEdgeFilter extends HashSet<Integer> implements EdgeFilter {

	/**
	 *Determine whether or not an edge is to be filtered
	 * @param iter iterator pointing to a given edge
	 * @return <tt>true</tt> iff the edge pointed to by the iterator is not to be filtered
	 */
	@Override
	public final boolean accept(EdgeIteratorState iter) {
		return !contains(iter.getEdgeKey());
	}

	public void add(int edge) {
		add(edge, false);
		add(edge, true);
	}

	public void add(int edge, boolean reverse) {
		int edgeKey = GHUtility.createEdgeKey(edge, reverse);
		super.add(edgeKey);
	}

}
