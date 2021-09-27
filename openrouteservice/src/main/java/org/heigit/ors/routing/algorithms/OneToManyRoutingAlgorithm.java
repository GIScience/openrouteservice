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
package org.heigit.ors.routing.algorithms;

import com.graphhopper.routing.SPTEntry;

public interface OneToManyRoutingAlgorithm {
	
    void prepare(int[] from, int[] to);
    
    SPTEntry[] calcPaths(int from, int[] to);
    
    void reset();

    /**
     * Limit the search to numberOfNodes. See #681
     */
    void setMaxVisitedNodes(int numberOfNodes);

    /**
     * @return name of this algorithm
     */
    String getName();

    /**
     * Returns the visited nodes after searching. Useful for debugging.
     */
    int getVisitedNodes();
}
