/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.locations;

public enum LocationRequestType
{
	UNKNOWN,
    CATEGORIES,
    CATEGORIESLIST,
    POIS;
   
   public static LocationRequestType fromString(String value)
   {
	   if (value == null)
		   return LocationRequestType.UNKNOWN;
	   
	   if ("pois".equalsIgnoreCase(value))
		   return LocationRequestType.POIS;
	   else if ("categories".equalsIgnoreCase(value))
	   	return LocationRequestType.CATEGORIES;
	   else if ("categories_list".equalsIgnoreCase(value))
		   	return LocationRequestType.CATEGORIESLIST;
		
	   return LocationRequestType.UNKNOWN; 
   }
}
