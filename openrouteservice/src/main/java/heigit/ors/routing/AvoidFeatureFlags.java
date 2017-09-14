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
package heigit.ors.routing;

public class AvoidFeatureFlags {
	public static final int Highways = 1; // 1 << 0;
	public static final int Tollways = 2; // 1 << 1;
	public static final int Steps = 2; // 1 << 1;
	public static final int Ferries = 4; // 1 << 2;
	public static final int UnpavedRoads = 8; // 1 << 3;
	public static final int Tracks = 16; // 1 << 4;
	public static final int Tunnels = 32; // 1 << 5;
	public static final int PavedRoads = 64; // 1 << 6;
	public static final int Fords = 128; // 1 << 7;
	
	public static final int Bridges = 256; // does not work as it is greater than byte limit of 255.
	public static final int Borders = 512; 
	public static final int Hills = 1024;
	
	public static int getFromString(String value)
	{
		switch(value.toLowerCase())
		{
			case "highways":
				return Highways;
			case "tollways":
				return Tollways;
			case "ferries":
				return Ferries;
			case "unpavedroads":
				return UnpavedRoads;
			case "steps":
				return Steps;
			case "tracks":
				return Tracks;
			case "tunnels":
				return Tunnels;
			case "pavedroads":
				return PavedRoads;
			case "fords":
				return Fords;
			case "bridges":
				return Bridges;
			case "borders":
				return Borders;
			case "hills":
				return Hills;
		}
		
		return 0;
	}
	
	public static boolean isValid(int profileType, int value, String featName)
	{
		if (RoutingProfileType.isDriving(profileType))
		{
			if (value == Steps)
				return "steps".equalsIgnoreCase(featName) ? false : true;
		}
		else if (RoutingProfileType.isCycling(profileType) || RoutingProfileType.isWalking(profileType))
		{
			if (value == Highways || value == Tunnels)
				return false;
		}
		
		return true;
	}
}
