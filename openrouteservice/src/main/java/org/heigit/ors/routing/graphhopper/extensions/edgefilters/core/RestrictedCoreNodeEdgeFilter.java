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
package org.heigit.ors.routing.graphhopper.extensions.edgefilters.core;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Filter that accepts only edges that have no node in the core, given by the corenodes boolean array
 *
 * @author Hendrik Leuschner
 */
public class RestrictedCoreNodeEdgeFilter implements EdgeFilter {
    private boolean[] restrictedCoreNodes;

    /**
     * Create new filter. restrictedCoreNodes is true for a node in the core, false otherwise.
     *
     * @param restrictedCoreNodes the restricted core nodes
     */
    public RestrictedCoreNodeEdgeFilter(boolean[] restrictedCoreNodes) {
        this.restrictedCoreNodes = restrictedCoreNodes;
    }

    @Override
    public final boolean accept(EdgeIteratorState iter) {
        return restrictedCoreNodes[iter.getBaseNode()] == false || restrictedCoreNodes[iter.getAdjNode()] == false;
    }
}
