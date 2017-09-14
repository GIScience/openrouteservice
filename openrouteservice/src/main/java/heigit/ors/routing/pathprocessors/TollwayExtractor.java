/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.pathprocessors;

import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import heigit.ors.routing.graphhopper.extensions.TollwayType;
import heigit.ors.routing.graphhopper.extensions.storages.TollwaysGraphStorage;
import heigit.ors.routing.parameters.ProfileParameters;
import heigit.ors.routing.parameters.VehicleParameters;

public class TollwayExtractor 
{
	private VehicleParameters _vehicleParams;
	private int _vehicleType;
	private TollwaysGraphStorage _storage;
	private byte[] _buffer = new byte[4];

	public TollwayExtractor(TollwaysGraphStorage storage, int vehileType, ProfileParameters vehicleParams)
	{
		_storage = storage;

		if (vehicleParams instanceof VehicleParameters)
			_vehicleParams = (VehicleParameters)vehicleParams;
	}   

	public int getValue(int edgeId)
	{
		int value = _storage.getEdgeValue(edgeId, _buffer);

		if (value != TollwayType.None)
		{
			// normal car
			if (_vehicleParams == null)
			{
				if (TollwayType.isLType(value) || TollwayType.isMType(value))
					return 1;
				else
					return 0;
			}

			double weight = _vehicleParams.getWeight();

			//toll:N1=yes - Für Fahrzeuge bis 3,5 Tonnen eingesetzt, Bsp. Pick-up-Truck. (siehe Europäische Fahrzeugklassifikation N1)
			//toll:N2=yes - Für Fahrzeuge von 3,5 Tonnen bis zu 12 Tonnen eingesetzt, Bsp. Commercial Truck.
			//toll:N3=yes Für LKW mit einem zulässigen Gesamtgewicht > 12 t

			if (TollwayType.isNType(value))
			{
				if (weight < 3.5 && value == TollwayType.N1)
					return 1;
				else if (weight >= 3.5 && weight < 12 && value == TollwayType.N2)
					return 1;
				else if (weight >= 12 && value == TollwayType.N3)
					return 1;
				else if (_vehicleType == HeavyVehicleAttributes.GOODS && value == TollwayType.N)
					return 1;
			}
		}

		return 0;
	}
}
