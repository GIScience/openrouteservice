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
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RoutingCHEdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;

import static org.heigit.ors.routing.graphhopper.extensions.util.EncodedValues.hasCountryBorders;

public class AvoidBordersCoreEdgeFilter implements EdgeFilter {
    private int[] avoidCountries;
    private BordersExtractor bordersExtractor;


    //Used to avoid all borders
    public AvoidBordersCoreEdgeFilter(GraphHopperStorage graphStorage) {
        this(graphStorage, new int[0]);
    }

    //Used to specify multiple countries to avoid (For a specific LM set)
    public AvoidBordersCoreEdgeFilter(GraphHopperStorage graphStorage, int[] avoidCountries) {
        EncodingManager encodingManager = graphStorage.getEncodingManager();
        this.avoidCountries = avoidCountries;
        if (hasCountryBorders(encodingManager)) {
            bordersExtractor = new BordersExtractor(encodingManager, avoidCountries);
        }
    }

    public int[] getAvoidCountries() {
        return avoidCountries;
    }

    /**
     * Determine whether or not an edge is to be filtered
     *
     * @param iter iterator pointing to a given edge
     * @return <tt>true</tt> iff the edge pointed to by the iterator is not to be filtered
     */
    @Override
    public final boolean accept(EdgeIteratorState iter) {
        if (bordersExtractor == null)
            return true;

        //If a specific country was given, just check if its one of the country borders
        if (iter instanceof RoutingCHEdgeIterator iterator && iterator.isShortcut())
            return true;
        if (avoidCountries.length > 0)
            return !bordersExtractor.restrictedCountry(iter);

        return !bordersExtractor.isBorder(iter);
    }
}
