/*
 *  Licensed to GraphHopper and Peter Karich under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.freeopenls.routeservice.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.OSMWay;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.util.Helper;

/**
 * Defines bit layout for cars. (speed, access, ferries, ...)
 * <p>
 */
public class CarTmcFlagEncoder extends CarFlagEncoder {
	
	private String[] TMC_ROAD_TYPES = new String[] { "motorway", "motorway_link", "trunk", "trunk_link", "primary",
			"primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified", "residential" };

	/**
	 * Should be only instantied via EncodingManager
	 */
	public CarTmcFlagEncoder() {
		this(5, 5, 0);
	}

    public CarTmcFlagEncoder( String propertiesStr )
    {
		     this((int) parseLong(propertiesStr, "speedBits", 5),
		                parseDouble(propertiesStr, "speedFactor", 5),
		                parseBoolean(propertiesStr, "turnCosts", false) ? 3 : 0);
    }

	public CarTmcFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
		super(speedBits, speedFactor, maxTurnCosts);
		
		defaultSpeedMap.put("unclassified", 10);  
        defaultSpeedMap.put("residential", 10);
	}

	@Override
	public long acceptWay(OSMWay way) {
		String highwayValue = way.getTag("highway");

		if (Helper.isEmpty(highwayValue))
			return 0;

		boolean accept = false;
		for (int i = 0; i < TMC_ROAD_TYPES.length; i++) {
			if (TMC_ROAD_TYPES[i].equalsIgnoreCase(highwayValue)) {
				accept = true;
				break;
			}
		}

		if (!accept)
			return 0;

		return super.acceptWay(way);
	}

	@Override
	public String toString() {
		return "cartmc";
	}
}
