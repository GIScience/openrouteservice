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
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LMEdgeFilterSequence extends EdgeFilterSequence implements EdgeFilter {

	/**
	 * Checks if an LMSet fits the PMap of the query
	 * An LMSet can be used if it as the same properties as the request, or a subset of the properties.
	 * Then the landmarks heuristic is admissible, meaning it can only underestimate the actual distance
	 * If an LMSet has more restrictions that the query, it might overestimate and then the heuristic is wrong
	 *
	 * @return true if the lmset can be used to calc the query
	 *
	 * */
	public boolean isFilter(PMap pmap){
		//true if the avoidFeaturespart fits the query
		RouteSearchParameters routeSearchParameters = pmap.getObject("avoid_features", new RouteSearchParameters());
		boolean avoidFeatures = isAvoidFeature(routeSearchParameters.getAvoidFeatureTypes());
		boolean avoidCountries = isAvoidCountry(pmap.getString("avoid_countries", ""));
		return avoidFeatures && avoidCountries;


	}
	/**
	 * Checks if the avoid countries specified in the LMSet are a subset of the avoid countries requested in the query
	 *
	 * @return true if the lmset can be used to calc the query
	 *
	 * */
	private boolean isAvoidCountry(String countries){
		ArrayList<Integer> queryCountries = new ArrayList<>();
		if(!(countries.equals("") || countries.equals("[]"))) {
			//Cut away the brackets
			countries = countries.substring(1, countries.length() - 1);
			String[] countryList = countries.split(", ");
			//Make an int arraylist
			for (int i = 0; i < countryList.length; i++) {
				queryCountries.add(Integer.parseInt(countryList[i]));
			}
		}
		//Check if the avoidBordersFilter has the same countries or a subset
		for (EdgeFilter edgeFilter: filters) {
			if (edgeFilter instanceof AvoidBordersCoreEdgeFilter){
				//There are no countries queried, but there are some in the lmset
				if(queryCountries.isEmpty())
					return false;
				return queryCountries.containsAll(Arrays.stream(((AvoidBordersCoreEdgeFilter) edgeFilter).getAvoidCountries()).boxed().collect(Collectors.toList()));
			}
		}

		return true;
	}
	/**
	 * Checks if the avoid features specified in the LMSet are a subset of the avoid features requested in the query
	 *
	 * @return true if the lmset can be used to calc the query
	 *
	 * */
	private boolean isAvoidFeature(int avoidable){
		for (EdgeFilter edgeFilter: filters) {
			//There is only one AvoidFeaturesCoreEdgeFilter per EdgeFilterSequence
			if (edgeFilter instanceof AvoidFeaturesCoreEdgeFilter){
				//Some bit magic to find if the storage bits are a subset of the query bits, but not the other way around
				int reverseQueryFeatures = Integer.MAX_VALUE ^ avoidable;
				int filterFeatures = ((AvoidFeaturesCoreEdgeFilter) edgeFilter).getAvoidFeatures();
                return (reverseQueryFeatures & filterFeatures) == 0;
            }
		}
		return true;
	}

}
