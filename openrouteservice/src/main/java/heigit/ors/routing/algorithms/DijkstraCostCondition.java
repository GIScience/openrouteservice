/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.algorithms;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.storage.Graph;

public class DijkstraCostCondition extends Dijkstra
{
	private double weightLimit = -1;
    public DijkstraCostCondition(Graph g, Weighting weighting, double maxCost, boolean reverseDirection, TraversalMode tMode)
    {
        super(g, weighting, tMode, -1);

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
