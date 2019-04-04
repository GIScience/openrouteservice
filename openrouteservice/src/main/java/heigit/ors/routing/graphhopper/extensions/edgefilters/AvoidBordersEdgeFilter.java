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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.pathprocessors.BordersExtractor;
import org.apache.log4j.Logger;

public class AvoidBordersEdgeFilter implements EdgeFilter {
    private BordersExtractor.Avoid _avoidBorders = BordersExtractor.Avoid.NONE;
    private boolean _avoidCountries = false;

    private BordersExtractor _bordersExtractor;

    public AvoidBordersEdgeFilter(RouteSearchParameters searchParams, BordersGraphStorage extBorders) {
        init(searchParams, extBorders);
    }

    public AvoidBordersEdgeFilter(RouteSearchParameters searchParams, GraphStorage graphStorage) {
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
        if(extBorders != null) {
            int[] avoidCountries;
            if(searchParams.hasAvoidCountries())
                avoidCountries = searchParams.getAvoidCountries();
            else
                avoidCountries = new int[0];

            _avoidCountries = avoidCountries.length > 0;

            if(searchParams.hasAvoidBorders()) {
                _avoidBorders = searchParams.getAvoidBorders();
            }

            _bordersExtractor = new BordersExtractor(extBorders, searchParams.getProfileParameters(), avoidCountries);
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

        if (_avoidBorders != BordersExtractor.Avoid.NONE) {
            // We have been told to avoid some form of border
            switch(_avoidBorders) {
                case ALL:
                    if(_bordersExtractor.isBorder(iter.getEdge())) {
                        // It is a border, and we want to avoid all borders
                        return false;
                    }
                case CONTROLLED:
                    if(_bordersExtractor.isControlledBorder(iter.getEdge())) {
                        // We want to only avoid controlled borders
                        return false;
                    }
                    break;
            }
        }

        if(_avoidCountries) {
            if(_bordersExtractor.restrictedCountry(iter.getEdge())) {
                return false;
            }
        }

        return true;

    }

}
