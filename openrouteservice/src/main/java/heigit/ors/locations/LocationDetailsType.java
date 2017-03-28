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

import com.graphhopper.util.Helper;

import heigit.ors.exceptions.UnknownParameterValueException;

public class LocationDetailsType
{
	public static final int NONE = 0;
	public static final int ADDRESS = 1;
	public static final int CONTACT = 2;
	public static final int ATTRIBUTES = 4;

	public static boolean isSet(int details, int value)
	{
		return (details & value) == value;
	}

	public static int fromString(String value) throws UnknownParameterValueException {
		if (Helper.isEmpty(value))
			return 0;

		int res = 0;

		String[] values = value.split("\\|");
		for (int i = 0; i < values.length; ++i) {
			switch (values[i].toLowerCase()) {
			case "address":
				res |= ADDRESS;
				break;
			case "contact":
				res |= CONTACT;
				break;
			case "attributes":
				res |= ATTRIBUTES;
				break;
			default:
				throw new UnknownParameterValueException("details", values[i]);
			}
		}

		return res;
	}
}
