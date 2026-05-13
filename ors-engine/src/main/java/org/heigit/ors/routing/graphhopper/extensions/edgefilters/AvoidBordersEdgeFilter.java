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
package org.heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.ev.Border;
import com.graphhopper.routing.ev.Country;
import com.graphhopper.routing.ev.CountryOther;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;

public class AvoidBordersEdgeFilter implements EdgeFilter {
    private BordersExtractor.Avoid avoidBorders = BordersExtractor.Avoid.NONE;
    private boolean avoidCountries = false;
    private EnumEncodedValue<Border> border = null;
    private EnumEncodedValue<Country> country = null;

    private BordersExtractor bordersExtractor;


    public AvoidBordersEdgeFilter(RouteSearchParameters searchParams, GraphHopperStorage graphHopperStorage) {
        init(searchParams, graphHopperStorage.getEncodingManager());
    }

    /**
     * Initialise the edge filter object based on the type of borders to filter
     *
     * @param searchParams The search parameters passed into the request
     * @param encodingManager  EncodingManager to check for the presence of the border and country encoded values, and to retrieve them if they are present
     */
    private void init(RouteSearchParameters searchParams, EncodingManager encodingManager) {
        if (encodingManager.hasEncodedValue(Border.KEY)
                && encodingManager.hasEncodedValue(Country.KEY)
                && encodingManager.hasEncodedValue(CountryOther.KEY)) {
            int[] countriesToAvoid;
            if (searchParams.hasAvoidCountries())
                countriesToAvoid = searchParams.getAvoidCountries();
            else
                countriesToAvoid = new int[0];

            this.avoidCountries = countriesToAvoid.length > 0;

            if (searchParams.hasAvoidBorders()) {
                avoidBorders = searchParams.getAvoidBorders();
            }

            bordersExtractor = new BordersExtractor(encodingManager, countriesToAvoid);
        }
    }

    /**
     * Determine whether the edge should be accepted for processing or reject. Depending on whether the request was to
     * not cross any border or not cross controlled borders determines the type of border to reject.
     *
     * @param iter An iterator to the edges that need to be filtered
     * @return
     */
    @Override
    public final boolean accept(EdgeIteratorState iter) {
        if (bordersExtractor == null)
            return true;

        switch (avoidBorders) {
            case ALL:
                if (bordersExtractor.isBorder(iter)) {
                    // It is a border, and we want to avoid all borders
                    return false;
                }
                break;
            case CONTROLLED:
                if (bordersExtractor.isControlledBorder(iter)) {
                    // We want to only avoid controlled borders
                    return false;
                }
                break;
            default:
                break;
        }

        return !avoidCountries || !bordersExtractor.restrictedCountry(iter);
    }

}
