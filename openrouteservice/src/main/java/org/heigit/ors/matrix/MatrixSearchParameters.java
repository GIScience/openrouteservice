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
package org.heigit.ors.matrix;

import org.heigit.ors.routing.RouteSearchParameters;

/**
 * This class is used to store the search/calculation Parameters to calculate the desired Route/Isochrones etc…
 * It can be called from any class and the values be set according to the needs of the route calculation.
 */
public class MatrixSearchParameters extends RouteSearchParameters {
    private boolean dynamicSpeeds = false;

    public boolean getDynamicSpeeds() {
        return dynamicSpeeds;
    }

    public void setDynamicSpeeds(boolean dynamicSpeeds) {
        this.dynamicSpeeds = dynamicSpeeds;
    }

}
