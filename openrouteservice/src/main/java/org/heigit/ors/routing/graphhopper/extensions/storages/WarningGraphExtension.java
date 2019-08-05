/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.routing.graphhopper.extensions.storages;

import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteWarning;

/**
 * Interface for declaring a graph storage as bein able to be used for producing warning messages
 */
public interface WarningGraphExtension {
    void setIsUsedForWarning(boolean isWarning);
    boolean isUsedForWarning();
    String getName();

    /**
     * Method that determines whether a warning should be generated for the graph storage.
     * @param extra     The {@link RouteExtraInfo} object for the route for this particular extension. It's values
     *                  should be used for determining whether a warning should be made.
     * @return          Whether there should be a warning generated
     */
    boolean generatesWarning(RouteExtraInfo extra);
    RouteWarning getWarning();
}
