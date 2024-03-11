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
package org.heigit.ors.routing.pathprocessors;

import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import org.heigit.ors.routing.graphhopper.extensions.JunctionType;
import org.heigit.ors.routing.graphhopper.extensions.storages.JunctionGraphStorage;
import org.heigit.ors.routing.parameters.ProfileParameters;
import org.heigit.ors.routing.parameters.VehicleParameters;

public class JunctionExtractor {
    private VehicleParameters vehicleParams;
    private final int profileType;
    private final JunctionGraphStorage storage;

    public JunctionExtractor(JunctionGraphStorage storage, int profileType, ProfileParameters vehicleParams) {
        this.storage = storage;
        this.profileType = profileType;
        if (vehicleParams instanceof VehicleParameters parameters)
            this.vehicleParams = parameters;
    }

    /**
     * return if a way is a tollway for the configured vehicle.
     *
     * @param edgeId The edgeId for which toll should be checked
     * @see HeavyVehicleAttributes
     */
    public int getValue(int edgeId) {
        int value = storage.getJunctionEdgeValue(edgeId);

        switch (value) {
            // toll=no
            case JunctionType.NONE:
                return 0;
            // toll=yes
            case JunctionType.GENERAL:
                return 1;
            default:
                switch (profileType) {
                    // toll:motorcar
                    case RoutingProfileType.DRIVING_CAR:
                        return JunctionType.isSet(JunctionType.MOTORCAR, value) ? 1 : 0;

                    case RoutingProfileType.DRIVING_HGV:
                        // toll:hgv
                        if (JunctionType.isSet(JunctionType.HGV, value))
                            return 1;

                        // check for weight specific toll tags even when weight is unset
                        double weight = vehicleParams == null ? 0 : vehicleParams.getWeight();
                        if ((weight == 0 && JunctionType.isNType(value))
                                || (weight < 3.5 && JunctionType.isSet(JunctionType.N1, value))
                                || (weight >= 3.5 && weight < 12 && JunctionType.isSet(JunctionType.N2, value))
                                || (weight >= 12 && JunctionType.isSet(JunctionType.N3, value)))
                            return 1;
                        return 0;
                    default:
                        return 0;

                    case RoutingProfileType.CYCLING_CARGO, RoutingProfileType.CYCLING_ELECTRIC:
                         return JunctionType.isSet(JunctionType.CYCLING_CARGO, value) ? 1 : 0;

                }
        }

    }

}
