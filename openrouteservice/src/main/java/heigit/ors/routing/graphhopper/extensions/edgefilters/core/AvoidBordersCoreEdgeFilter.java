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
package heigit.ors.routing.graphhopper.extensions.edgefilters.core;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.CHEdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;

public class AvoidBordersCoreEdgeFilter implements EdgeFilter {
    private BordersGraphStorage storage;
    private int[] avoidCountries;
    private boolean isAvoidCountries = false;

    //Used to avoid all borders
    public AvoidBordersCoreEdgeFilter(GraphStorage graphStorage) {
        this.storage = GraphStorageUtils.getGraphExtension(graphStorage, BordersGraphStorage.class);
    }
    //Used to specify multiple countries to avoid (For a specific LM set)
    public AvoidBordersCoreEdgeFilter(GraphStorage graphStorage, int[] avoidCountries) {
        this.storage = GraphStorageUtils.getGraphExtension(graphStorage, BordersGraphStorage.class);
        this.avoidCountries = avoidCountries;
        if(avoidCountries.length > 0) isAvoidCountries = true;
    }

    public int[] getAvoidCountries(){
        return avoidCountries;
    }
    /**
     *Determine whether or not an edge is to be filtered
     * @param iter iterator pointing to a given edge
     * @return <tt>true</tt> iff the edge pointed to by the iterator is not to be filtered
     */
    @Override
    public final boolean accept(EdgeIteratorState iter) {
        //If a specific country was given, just check if its one of the country borders
        if(iter instanceof CHEdgeIterator)
            if(((CHEdgeIterator)iter).isShortcut()) return true;
        if(isAvoidCountries)
            return !restrictedCountry(iter.getEdge());
        //else check if there is ANY border
        if (storage == null) {
            return true;
        } else {
            return storage.getEdgeValue(iter.getEdge(), BordersGraphStorage.Property.TYPE) == BordersGraphStorage.NO_BORDER;
        }

    }

    public boolean restrictedCountry(int edgeId) {
        int startCountry = storage.getEdgeValue(edgeId, BordersGraphStorage.Property.START);
        int endCountry = storage.getEdgeValue(edgeId, BordersGraphStorage.Property.END);

        for(int i=0; i<avoidCountries.length; i++) {
            if(startCountry == avoidCountries[i] || endCountry == avoidCountries[i] ) {
                return true;
            }
        }

        return false;
    }
}
