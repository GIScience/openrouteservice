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
package heigit.ors.services.routing.requestprocessors;

import javax.servlet.http.HttpServletRequest;

import heigit.ors.services.routing.requestprocessors.json.JsonRoutingRequestProcessor;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.routing.RoutingErrorCodes;
import heigit.ors.routing.RoutingProfileManagerStatus;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.services.routing.requestprocessors.TmcInformationRequestProcessor;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;

import com.graphhopper.util.Helper;

public class RoutingServiceRequestProcessorFactory {

	public static AbstractHttpRequestProcessor createProcessor(HttpServletRequest request) throws Exception  
	{
		if (!RoutingServiceSettings.getEnabled())
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, RoutingErrorCodes.UNKNOWN, "Routing service is not enabled.");

		if (!RoutingProfileManagerStatus.isReady())
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, RoutingErrorCodes.UNKNOWN, "Routing service is not ready yet.");
		
		String requestParam = request.getParameter("request");

		if (Helper.isEmpty(requestParam))
			requestParam = "route";

		switch (requestParam.toLowerCase()) 
		{
		case "tmc":
			return new TmcInformationRequestProcessor(request);
		case "route":
			String formatParam = request.getParameter("format");
			if (Helper.isEmpty(formatParam))
				formatParam = "json";

			if (formatParam.equalsIgnoreCase("json"))
				return new JsonRoutingRequestProcessor(request);
			//else if (formatParam.equalsIgnoreCase("xml"))
			//	return new XmlRouteRequestProcessor(request);
			else 
				throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "format", formatParam);
		default:
			throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "request", requestParam);		
		}
	}
}
