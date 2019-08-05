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
package heigit.ors.routing.pathprocessors;

import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import heigit.ors.routing.graphhopper.extensions.TollwayType;
import heigit.ors.routing.graphhopper.extensions.storages.TollwaysGraphStorage;
import heigit.ors.routing.parameters.ProfileParameters;
import heigit.ors.routing.parameters.VehicleParameters;

public class TollwayExtractor {
	private VehicleParameters _vehicleParams;
	private int _profileType;
	private TollwaysGraphStorage _storage;

	public TollwayExtractor(TollwaysGraphStorage storage, int profileType, ProfileParameters vehicleParams) {
		_storage = storage;
		_profileType = profileType;
		if (vehicleParams instanceof VehicleParameters)
			_vehicleParams = (VehicleParameters) vehicleParams;
	}
	/**
	 * return if a way is a tollway for the configured vehicle.
	 *
	 * @param edgeId				The edgeId for which toll should be checked
	 * @see HeavyVehicleAttributes
	 */
	public int getValue(int edgeId) {
		int value = _storage.getEdgeValue(edgeId);

		switch (value) {
			// toll=no
			case TollwayType.None:
				return 0;
			// toll=yes
			case TollwayType.General:
				return 1;
			default:
				switch(_profileType) {
					// toll:motorcar
					case RoutingProfileType.DRIVING_CAR:
						return TollwayType.isSet(TollwayType.Motorcar, value) ? 1 : 0;

					case RoutingProfileType.DRIVING_HGV:
						// toll:hgv
						if (TollwayType.isSet(TollwayType.Hgv, value))
							return 1;

						// check for weight specific toll tags even when weight is unset
						double weight = _vehicleParams==null ? 0 : _vehicleParams.getWeight();
						if (weight == 0 && TollwayType.isNType(value))
							return 1;
							//Check in which weight range the hgv falls and return accordingly
							//toll:N1=yes - Für Fahrzeuge bis 3,5 Tonnen eingesetzt, Bsp. Pick-up-Truck. (siehe Europäische Fahrzeugklassifikation N1)
							//toll:N2=yes - Für Fahrzeuge von 3,5 Tonnen bis zu 12 Tonnen eingesetzt, Bsp. Commercial Truck.
							//toll:N3=yes Für LKW mit einem zulässigen Gesamtgewicht > 12 t
						else {
							if (weight < 3.5 && TollwayType.isSet(TollwayType.N1, value))
								return 1;
							else if (weight >= 3.5 && weight < 12 && TollwayType.isSet(TollwayType.N2, value))
								return 1;
							else if (weight >= 12 && TollwayType.isSet(TollwayType.N3, value))
								return 1;
						}
					default:
						return 0;
				}
		}

	}

}
