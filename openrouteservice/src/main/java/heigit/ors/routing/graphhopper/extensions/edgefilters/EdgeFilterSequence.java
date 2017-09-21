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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import java.util.ArrayList;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;

public class EdgeFilterSequence implements EdgeFilter {

	private ArrayList<EdgeFilter> edgeFilters;
	private int filtersCount;

	/**
	 * Creates an edges filter which accepts both direction of the specified
	 * vehicle.
	 */
	public EdgeFilterSequence(ArrayList<EdgeFilter> edgeFilters) {
		this.edgeFilters = edgeFilters;
		this.filtersCount = edgeFilters.size();
	}

	public void addFilter(EdgeFilter e) {
		edgeFilters.add(e);
		filtersCount++;
	}
	
	public EdgeFilter getEdgeFilter(Class<?> type)
	{
		for (int i = 0; i < filtersCount; i++) {
			if (type.isAssignableFrom(edgeFilters.get(i).getClass()))
				return edgeFilters.get(i);
		}
		
		return null;
	}
	
	public boolean containsEdgeFilter(Class<?> type)
	{
		for (int i = 0; i < filtersCount; i++) {
			if (type.isAssignableFrom(edgeFilters.get(i).getClass()))
				return true;
		}
		
		return false;
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {
		for (int i = 0; i < filtersCount; i++) {
			if (!edgeFilters.get(i).accept(iter))
				return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "EdgeFilter Sequence :" + filtersCount;
	}
}
