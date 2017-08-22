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
package heigit.ors.geocoding.geocoders;

import java.util.Comparator;

import com.graphhopper.util.Helper;

public class GeocodingResultComparator implements Comparator<GeocodingResult> {
	@Override
	public int compare(GeocodingResult gr1, GeocodingResult gr2) {
		if (gr1 != null && gr2 != null)
		{
			// compare results with the same accuracy value
			if (gr1.confidence == gr2.confidence)
			{
				boolean gr1HasHouseNumber = !Helper.isEmpty(gr1.houseNumber) && !Helper.isEmpty(gr1.street);
				boolean gr2HasHouseNumber = !Helper.isEmpty(gr2.houseNumber) && !Helper.isEmpty(gr2.street);

				if (gr1HasHouseNumber != gr2HasHouseNumber)
				{
					if (gr1HasHouseNumber && !gr2HasHouseNumber)
						return -1;
					else if (!gr1HasHouseNumber && gr2HasHouseNumber)
						return 1;
				}
			}
			else
				return Float.compare(gr2.confidence, gr1.confidence);
		}
		
		return 0;
	}
}
