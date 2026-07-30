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

    /**
     * Set the filters that are used while calculating landmarks and their distance
     *
     * @param tmpCoreLMSets
     */
    public void setRestrictionFilters(List<String> tmpCoreLMSets) {
        this.coreLMSets = tmpCoreLMSets;
    }

    /**
     * Creates all LMSet-Filters from the sets specified in the ors-config.json
     * The filter is an LMEdgeFilterSequence, consisting of at most ONE AvoidFeaturesFilter and ONE AvoidCountriesFilter
     * These can contain multiple avoidfeatures and avoidcountries
     */
    public List<LMEdgeFilterSequence> createRestrictionFilters(GraphHopperStorage ghStorage, int profileType) {
        List<LMEdgeFilterSequence> filters = new ArrayList<>();
        for (String set : coreLMSets) {
            filters.add(createEdgeFilterSequence(set, ghStorage, profileType));
        }
        return filters;
    }

    private LMEdgeFilterSequence createEdgeFilterSequence(String set, GraphHopperStorage ghStorage, int profileType) {
        LMEdgeFilterSequence edgeFilterSequence = new LMEdgeFilterSequence();
        String[] tmpFilters = set.split(",");
        int avoidFeatures = 0;
        List<Integer> countries = new ArrayList<>();

        for (String filterType : tmpFilters) {
            // do not add any filter if it is "allow_all"
            if (filterType.equalsIgnoreCase("allow_all")) {
                edgeFilterSequence.appendName("allow_all");
                break;
            }
            avoidFeatures = processAvoidFeaturesFilter(filterType, edgeFilterSequence, avoidFeatures);
            processAvoidCountriesFilter(filterType, edgeFilterSequence, countries);
        }

        if (avoidFeatures != 0) {
            edgeFilterSequence.add(new AvoidFeaturesCoreEdgeFilter(ghStorage, profileType, avoidFeatures));
        }
        if (!countries.isEmpty()) {
            int[] avoidCountries = countries.stream().mapToInt(Integer::intValue).toArray();
            edgeFilterSequence.add(new AvoidBordersCoreEdgeFilter(ghStorage, avoidCountries));
        }
        return edgeFilterSequence;
    }

    private int processAvoidFeaturesFilter(String filterType, LMEdgeFilterSequence edgeFilterSequence, int avoidFeatures) {
        int feature = AvoidFeatureFlags.getFromString(filterType);
        if (feature != 0) {
            avoidFeatures |= feature;
            edgeFilterSequence.appendName(filterType.toLowerCase());
        }
        return avoidFeatures;
    }

    private void processAvoidCountriesFilter(String filterType, LMEdgeFilterSequence edgeFilterSequence, List<Integer> countries) {
        String countryPattern = "(?i)country_(\\d+)";
        if (filterType.matches(countryPattern)) {
            int country = parseCountry(filterType, countryPattern);
            if (country != 0 && !countries.contains(country)) {
                countries.add(country);
                edgeFilterSequence.appendName(filterType.toLowerCase());
            }
        }
    }

    private int parseCountry(String filterType, String countryPattern) {
        try {
            return Integer.parseInt(filterType.replaceFirst(countryPattern, "$1"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
