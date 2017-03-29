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
package heigit.ors.services.isochrones.requestprocessors;

import javax.servlet.http.HttpServletRequest;

import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.isochrones.IsochronesErrorCodes;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import heigit.ors.services.isochrones.requestprocessors.json.JsonIsochronesRequestProcessor;

import com.graphhopper.util.Helper;

public class IsochronesServiceRequestProcessorFactory {

	public static AbstractHttpRequestProcessor createProcessor(HttpServletRequest request) throws Exception  
	{
		if (!IsochronesServiceSettings.getEnabled())
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, "Isochrones service is not enabled.");		

		String formatParam = request.getParameter("format");

		if (Helper.isEmpty(formatParam))
			formatParam = "json";

		if (formatParam.equalsIgnoreCase("json"))
			return new JsonIsochronesRequestProcessor(request);
		/*else if (formatParam.equalsIgnoreCase("xml"))
			return new XmlAccessibilityRequestProcessor(request);*/
		else 
			throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "format", formatParam);
	}
}
