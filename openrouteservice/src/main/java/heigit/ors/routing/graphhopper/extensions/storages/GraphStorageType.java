/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.util.Helper;

public class GraphStorageType {
	public static final int VehicleType = 1;
	public static final int Restrictions = 2;
	public static final int WayCategory = 4;
	public static final int WaySurfaceType = 8;
	public static final int HillIndex = 16;
	
	public static boolean isSet(int type, int value)
	{
		return (type & value) == value;
	}

	public static int getFomString(String value)
	{
		if (Helper.isEmpty(value))
			return 0;

		int res = 0;

		String[] values = value.split("\\|");
		for (int i = 0; i < values.length; ++i) {
			switch (values[i].toLowerCase()) {
			case "vehicletype":
				res |= VehicleType;
				break;
			case "restrictions":
				res |= Restrictions;
				break;
			case "waycategory":
				res |= WayCategory;
				break;
			case "waysurfacetype":
				res |= WaySurfaceType;
				break;
			case "hillindex":
				res |= HillIndex;
				break;
			}
		}

		return res;
	}
}
