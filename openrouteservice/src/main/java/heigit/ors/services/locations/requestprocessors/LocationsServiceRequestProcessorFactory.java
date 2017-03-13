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
package heigit.ors.services.locations.requestprocessors;

import javax.servlet.http.HttpServletRequest;

import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.services.locations.LocationsServiceSettings;
import heigit.ors.services.locations.requestprocessors.json.JsonLocationsRequestProcessor;

import com.graphhopper.util.Helper;

public class LocationsServiceRequestProcessorFactory {

	public static AbstractHttpRequestProcessor createProcessor(HttpServletRequest request) throws Exception  
	{
		if (!LocationsServiceSettings.getEnabled())
			throw new Exception("Location service is not enabled.");
		
		String formatParam = request.getParameter("format");

		if (Helper.isEmpty(formatParam))
			formatParam = "json";

		if (formatParam.equalsIgnoreCase("json"))
			return new JsonLocationsRequestProcessor(request);
		else 
			throw new Exception("Unknown parameter: 'format'.");
	}
}
