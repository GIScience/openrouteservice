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

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;

public class AvoidBordersEdgeFilter implements EdgeFilter {
    private BordersExtractor.Avoid avoidBorders = BordersExtractor.Avoid.NONE;
    private boolean avoidCountries = false;
    private boolean isStorageBuilt;

    private BordersExtractor bordersExtractor;

    public AvoidBordersEdgeFilter(RouteSearchParameters searchParams, BordersGraphStorage extBorders) {
        init(searchParams, extBorders);
    }

    public AvoidBordersEdgeFilter(RouteSearchParameters searchParams, GraphHopperStorage graphStorage) {
        BordersGraphStorage extBorders = GraphStorageUtils.getGraphExtension(graphStorage, BordersGraphStorage.class);
        init(searchParams, extBorders);
    }

    /**
     * Initialise the edge filter object based on the type of borders to filter
     *
     * @param searchParams      The search parameters passed into the request
     * @param extBorders        The extended borders graph storage to use
     */
    private void init(RouteSearchParameters searchParams, BordersGraphStorage extBorders) {
        // Init the graph storage
        isStorageBuilt = extBorders != null;
        if(isStorageBuilt) {
            int[] countriesToAvoid;
            if(searchParams.hasAvoidCountries())
                countriesToAvoid = searchParams.getAvoidCountries();
            else
                countriesToAvoid = new int[0];

            this.avoidCountries = countriesToAvoid.length > 0;

            if(searchParams.hasAvoidBorders()) {
                avoidBorders = searchParams.getAvoidBorders();
            }

            bordersExtractor = new BordersExtractor(extBorders, countriesToAvoid);
        }
    }

    /**
     * Determine whether the edge should be accepted for processing or reject. Depending on whether the request was to
     * not cross any border or not cross controlled borders determines the type of border to reject.
     *
     * @param iter      An iterator to the edges that need to be filtered
     * @return
     */
    @Override
    public final boolean accept(EdgeIteratorState iter) {
        if (!isStorageBuilt)
            return true;

        if (avoidBorders != BordersExtractor.Avoid.NONE) {
            // We have been told to avoid some form of border
            switch(avoidBorders) {
                case ALL:
                    if(bordersExtractor.isBorder(iter.getEdge())) {
                        // It is a border, and we want to avoid all borders
                        return false;
                    }
                    break;
                case CONTROLLED:
                    if(bordersExtractor.isControlledBorder(iter.getEdge())) {
                        // We want to only avoid controlled borders
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }

        return !avoidCountries || !bordersExtractor.restrictedCountry(iter.getEdge());
    }

}
