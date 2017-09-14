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
package heigit.ors.routing.graphhopper.extensions;

public class HeavyVehicleAttributes {
	public static final int UNKNOWN = 0;
	//public static final int Destination = 1;
	// Vehicle type and 
	public static final int GOODS = 1;
	public static final int HGV = 2;
	public static final int BUS = 4;
	public static final int AGRICULTURE = 8;
	public static final int FORESTRY = 16;
	public static final int DELIVERY = 32;
	// Load characteristics
	public static final int HAZMAT = 128;
	
	public static int getVehiclesCount()
	{
		return 6;	
	}	
	
	public static int getFromString(String value)
	{
		if ("goods".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.GOODS;
		} else if ("hgv".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.HGV;
		} else if ("bus".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.BUS;
		} else if ("agricultural".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.AGRICULTURE;
		} else if ("forestry".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.FORESTRY;
		} else if ("delivery".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.DELIVERY;
	    }
		
		return HeavyVehicleAttributes.UNKNOWN;
	}
}