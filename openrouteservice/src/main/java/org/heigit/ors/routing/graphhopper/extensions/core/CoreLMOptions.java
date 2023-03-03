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

import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.routing.AvoidFeatureFlags;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.AvoidBordersCoreEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.AvoidFeaturesCoreEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hendrik Leuschner, Andrzej Oles
 */

public class CoreLMOptions {
    List<String> coreLMSets = new ArrayList<>();
    List<LMEdgeFilterSequence> filters = new ArrayList<>();

    /**
     * Set the filters that are used while calculating landmarks and their distance
     * @param tmpCoreLMSets
     */
    public void setRestrictionFilters(List<String> tmpCoreLMSets) {
        this.coreLMSets = tmpCoreLMSets;
    }

    /**
     * Creates all LMSet-Filters from the sets specified in the ors-config.json
     * The filter is an LMEdgeFilterSequence, consisting of at most ONE AvoidFeaturesFilter and ONE AvoidCountriesFilter
     * These can contain multiple avoidfeatures and avoidcountries
     *
     *
     * */
    public void createRestrictionFilters(GraphHopperStorage ghStorage){
        //Create one edgefiltersequence for each lmset
        for(String set : coreLMSets) {
            //Now iterate over all comma separated values in one lm set
            String[] tmpFilters = set.split(",");
            LMEdgeFilterSequence edgeFilterSequence = new LMEdgeFilterSequence();
            int feature;
            int country;
            int avoidFeatures = 0;
            List<Integer> countries = new ArrayList<>();

            for (String filterType : tmpFilters) {

                //Do not add any filter if it is allow_all
                if (filterType.equalsIgnoreCase("allow_all")) {
                    edgeFilterSequence.appendName("allow_all");
                    break;
                }

                feature = AvoidFeatureFlags.getFromString(filterType);

                //process avoid features
                if (feature != 0) {
                    avoidFeatures = avoidFeatures | feature;
                    edgeFilterSequence.appendName(filterType.toLowerCase());
                    continue;
                }

                //process avoid countries
                String countryPattern = "(?i)country_(\\d+)";

                if (filterType.matches(countryPattern)) {
                    try {
                        country = Integer.parseInt(filterType.replaceFirst(countryPattern, "$1"));
                    }
                    catch (NumberFormatException e)
                    {
                        country = 0;
                    }
                    // todo check for valid country
                    if (country != 0 && !countries.contains(country)) {
                        countries.add(country);
                        edgeFilterSequence.appendName(filterType.toLowerCase());
                    }
                }
            }

            if(avoidFeatures != 0)
                edgeFilterSequence.add(new AvoidFeaturesCoreEdgeFilter(ghStorage, -1, avoidFeatures));

            if(!countries.isEmpty()){
                int[] avoidCountries = new int[countries.size()];
                for(int i = 0; i < countries.size(); i++){
                    avoidCountries[i] = countries.get(i);
                }
                //Only one avoidBordersCoreEdgeFilter per set
                edgeFilterSequence.add(new AvoidBordersCoreEdgeFilter(ghStorage, avoidCountries));
            }

            this.filters.add(edgeFilterSequence);
        }
    }

    public List<LMEdgeFilterSequence> getFilters(){
        return filters;
    }
}
