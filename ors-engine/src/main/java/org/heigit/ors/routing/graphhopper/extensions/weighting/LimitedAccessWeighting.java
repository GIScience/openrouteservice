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
package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.RoadAccess;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.AbstractAdjustedWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

/**
 * Modifies weight of edges marked with access destination or private by multiplying it by a factor.
 *
 * @author Andrzej Oles
 */
public class LimitedAccessWeighting extends AbstractAdjustedWeighting {
    public static double DEFAULT_DESTINATION_FACTOR = 1;
    public static double VEHICLE_DESTINATION_FACTOR = 10;
    public static double DEFAULT_PRIVATE_FACTOR = 1.2;
    public static double VEHICLE_PRIVATE_FACTOR = 10;

    static double MIN_DESTINATION_FACTOR = 1;
    static double MAX_DESTINATION_FACTOR = 10;
    static double MIN_PRIVATE_FACTOR = 1;
    static double MAX_PRIVATE_FACTOR = 10;

    private final EnumEncodedValue<RoadAccess> roadAccessEnc;
    // this factor puts a penalty on roads with a "destination"-only or private access, see GH#733 and GH#1936
    private final double destinationPenalty, privatePenalty;

    public LimitedAccessWeighting(Weighting superWeighting, PMap map) {
        super(superWeighting);
        FlagEncoder encoder = superWeighting.getFlagEncoder();
        if (!encoder.hasEncodedValue(RoadAccess.KEY))
            throw new IllegalArgumentException("road_access is not available");

        // ensure that we do not need to change getMinWeight, i.e. road_access_factor >= 1
        double defaultDestinationFactor = encoder.getTransportationMode().isMotorVehicle()? VEHICLE_DESTINATION_FACTOR : DEFAULT_DESTINATION_FACTOR;
        destinationPenalty = checkBounds("road_access_destination_factor", map.getDouble("road_access_destination_factor", defaultDestinationFactor), MIN_DESTINATION_FACTOR, MAX_DESTINATION_FACTOR);
        double defaultPrivateFactor = encoder.getTransportationMode().isMotorVehicle()? VEHICLE_PRIVATE_FACTOR : DEFAULT_PRIVATE_FACTOR;
        privatePenalty = checkBounds("road_access_private_factor", map.getDouble("road_access_private_factor", defaultPrivateFactor), MIN_PRIVATE_FACTOR, MAX_PRIVATE_FACTOR);
        roadAccessEnc = destinationPenalty > 1 || privatePenalty > 1 ? encoder.getEnumEncodedValue(RoadAccess.KEY, RoadAccess.class) : null;
    }

    static double checkBounds(String key, double val, double from, double to) {
        if (val < from || val > to)
            throw new IllegalArgumentException(key + " has invalid range should be within [" + from + ", " + to + "]");

        return val;
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse, long edgeEnterTime) {
        double weight = super.calcEdgeWeight(edgeState, reverse, edgeEnterTime);

        weight = modifyWeight(weight, edgeState);

        return weight;
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
        double weight = super.calcEdgeWeight(edgeState, reverse);

        weight = modifyWeight(weight, edgeState);

        return weight;
    }

    private double modifyWeight(double weight, EdgeIteratorState edgeState) {
        if (roadAccessEnc != null) {
            RoadAccess access = edgeState.get(roadAccessEnc);
            if (access == RoadAccess.DESTINATION)
                weight *= destinationPenalty;
            else if (access == RoadAccess.PRIVATE)
                weight *= privatePenalty;
        }
        return weight;
    }

    @Override
    public String toString() {
        return "limited_access|" + this.superWeighting.toString();
    }

    public String getName() {
        return this.superWeighting.getName();
    }
}
