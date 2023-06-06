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
package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.GraphExtension;

/**
 * Graph storage class for the Border Restriction routing
 */
public abstract class AbstractTrafficGraphStorage implements GraphExtension {

    public enum Property {ROAD_TYPE}

    public void setMatched() {
    }

    public boolean isMatched() {
        return false;
    }

    public boolean hasTrafficSpeed(int edgeId, int baseNode, int adjNode) {
        return false;
    }

    public int getSpeedValue(int edgeId, int baseNode, int adjNode, long unixMilliSeconds, int timeZoneOffset) {
        return -1;
    }

    public void setMaxTrafficSpeeds() {
    }

    public int getMaxSpeedValue(int edgeId, int baseNode, int adjNode) {
        return -1;
    }

    /**
     * Get the specified custom value of the edge that was assigned to it in the setValueEdge method<br/><br/>
     * <p>
     * The method takes an identifier to the edge and then gets the requested value for the edge from the storage
     *
     * @param edgeId Internal ID of the edge to get values for
     * @param prop   The property of the edge to get (TYPE - border type (0,1,2), START - the ID of the country
     *               the edge starts in, END - the ID of the country the edge ends in.
     * @return The value of the requested property
     */
    public int getOrsRoadProperties(int edgeId, Property prop) {
        return -1;
    }


}
