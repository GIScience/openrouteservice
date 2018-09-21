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
package heigit.ors.routing.algorithms;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;

public class DijkstraCostCondition extends Dijkstra
{
	private double weightLimit = -1;
    public DijkstraCostCondition(Graph g, Weighting weighting, double maxCost, boolean reverseDirection, TraversalMode tMode)
    {
        super(g, weighting, tMode);
        initCollections(1000);
        this.weightLimit = maxCost;
        setReverseDirection(reverseDirection);
    }

    @Override
    protected boolean finished() {
        return  super.finished() || currEdge.weight > weightLimit;
    }
    
    public IntObjectMap<SPTEntry> getMap()
    {
    	return fromMap;
    }
    
    public SPTEntry getCurrentEdge()
    {
    	if (currEdge == null || !finished())
    		return  null;
    	else
    		return currEdge;
    }

    @Override
    public String getName()
    {
        return "dijkstracc";
    }
}
