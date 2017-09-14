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

public class WayType {
	public static final int Unknown = 0;
	public static final int StateRoad = 1;
	public static final int Road = 2;
	public static final int Street = 3;
	public static final int Path = 4;
	public static final int Track = 5;
	public static final int Cycleway = 6;
	public static final int Footway = 7;
	public static final int Steps = 8;
	public static final int Ferry = 9;
	public static final int Construction = 10;
	
	public static int getFromString(String highway) {
		if ("primary".equalsIgnoreCase(highway) || "primary_link".equalsIgnoreCase(highway)
				|| "motorway".equalsIgnoreCase(highway) || "motorway_link".equalsIgnoreCase(highway)
				|| "trunk".equalsIgnoreCase(highway) || "trunk_link".equalsIgnoreCase(highway)) {
			return WayType.StateRoad;
		} else if ("secondary".equalsIgnoreCase(highway) || "secondary_link".equalsIgnoreCase(highway)
				|| "tertiary".equalsIgnoreCase(highway) || "tertiary_link".equalsIgnoreCase(highway)
				|| "road".equalsIgnoreCase(highway) || "unclassified".equalsIgnoreCase(highway)) {
			return WayType.Road;
		} else if ("residential".equalsIgnoreCase(highway) || "service".equalsIgnoreCase(highway)
				|| "living_street".equalsIgnoreCase(highway) || "living_street".equalsIgnoreCase(highway)) {
			return WayType.Street;
		} else if ("path".equalsIgnoreCase(highway)) {
			return WayType.Path;
		} else if ("track".equalsIgnoreCase(highway)) {
			return WayType.Track;
		} else if ("cycleway".equalsIgnoreCase(highway)) {
			return WayType.Cycleway;
		} else if ("footway".equalsIgnoreCase(highway) || "pedestrian".equalsIgnoreCase(highway)
				|| "crossing".equalsIgnoreCase(highway)) {
			return WayType.Footway;
		} else if ("steps".equalsIgnoreCase(highway)) {
			return WayType.Steps;
		} else if ("construction".equalsIgnoreCase(highway)) {
			return WayType.Construction;
		}

		return WayType.Unknown;
	}
}
