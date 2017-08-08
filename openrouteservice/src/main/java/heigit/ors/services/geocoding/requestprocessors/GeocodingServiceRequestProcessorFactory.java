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
package heigit.ors.services.geocoding.requestprocessors;

import javax.servlet.http.HttpServletRequest;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.geocoding.geocoders.GeocodingErrorCodes;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.services.geocoding.GeocodingServiceSettings;
import heigit.ors.services.geocoding.requestprocessors.json.JsonGeocodingRequestProcessor;

import com.graphhopper.util.Helper;

public class GeocodingServiceRequestProcessorFactory {

	public static AbstractHttpRequestProcessor createProcessor(HttpServletRequest request) throws Exception  
	{
		if (!GeocodingServiceSettings.getEnabled())
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, GeocodingErrorCodes.UNKNOWN, "Geocoding service is not enabled.");
		
		String formatParam = request.getParameter("format");

		if (Helper.isEmpty(formatParam))
			formatParam = "json";

		if (formatParam.equalsIgnoreCase("json"))
			return new JsonGeocodingRequestProcessor(request);
	/*	else if (formatParam.equalsIgnoreCase("xml"))
			return new XmlGeocodingRequestProcessor(request);*/
		else 
			throw new UnknownParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_VALUE, "format", formatParam);
	}
}
