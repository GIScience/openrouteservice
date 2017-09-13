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