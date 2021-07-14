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
import org.heigit.ors.routing.graphhopper.extensions.TollwayType;
import org.heigit.ors.routing.graphhopper.extensions.storages.TollwaysGraphStorage;
import org.heigit.ors.routing.parameters.ProfileParameters;
import org.heigit.ors.routing.parameters.VehicleParameters;

public class TollwayExtractor {
	private VehicleParameters vehicleParams;
	private final int profileType;
	private final TollwaysGraphStorage storage;

	public TollwayExtractor(TollwaysGraphStorage storage, int profileType, ProfileParameters vehicleParams) {
		this.storage = storage;
		this.profileType = profileType;
		if (vehicleParams instanceof VehicleParameters)
			this.vehicleParams = (VehicleParameters) vehicleParams;
	}
	/**
	 * return if a way is a tollway for the configured vehicle.
	 *
	 * @param edgeId				The edgeId for which toll should be checked
	 * @see HeavyVehicleAttributes
	 */
	public int getValue(int edgeId) {
		int value = storage.getEdgeValue(edgeId);

		switch (value) {
			// toll=no
			case TollwayType.NONE:
				return 0;
			// toll=yes
			case TollwayType.GENERAL:
				return 1;
			default:
				switch(profileType) {
					// toll:motorcar
					case RoutingProfileType.DRIVING_CAR:
						return TollwayType.isSet(TollwayType.MOTORCAR, value) ? 1 : 0;

					case RoutingProfileType.DRIVING_HGV:
						// toll:hgv
						if (TollwayType.isSet(TollwayType.HGV, value))
							return 1;

						// check for weight specific toll tags even when weight is unset
						double weight = vehicleParams ==null ? 0 : vehicleParams.getWeight();
						if ((weight == 0 && TollwayType.isNType(value))
							|| (weight < 3.5 && TollwayType.isSet(TollwayType.N1, value))
							|| (weight >= 3.5 && weight < 12 && TollwayType.isSet(TollwayType.N2, value))
							|| (weight >= 12 && TollwayType.isSet(TollwayType.N3, value)))
							return 1;
						return 0;
					default:
						return 0;
				}
		}

	}

}
