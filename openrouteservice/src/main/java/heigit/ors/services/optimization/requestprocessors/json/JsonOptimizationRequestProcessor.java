/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.services.optimization.requestprocessors.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.optimization.OptimizationErrorCodes;
import heigit.ors.optimization.RouteOptimizationRequest;
import heigit.ors.optimization.RouteOptimizationResult;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.services.optimization.OptimizationServiceSettings;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;

public class JsonOptimizationRequestProcessor extends AbstractHttpRequestProcessor 
{
	public JsonOptimizationRequestProcessor(HttpServletRequest request) throws Exception
	{
		super(request);
	}

	@Override
	public void process(HttpServletResponse response) throws Exception 
	{
		String reqMethod = _request.getMethod();

		RouteOptimizationRequest req = null;
		
		switch (reqMethod)
		{
		case "GET":
			req = JsonOptimizationRequestParser.parseFromRequestParams(_request);
			break;
		case "POST": 
			req = JsonOptimizationRequestParser.parseFromStream(_request.getInputStream());  
			break;
		default:
			throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED);
		}

		if (req == null)
			throw new StatusCodeException(StatusCode.BAD_REQUEST, OptimizationErrorCodes.UNKNOWN, "RouteOptimizationRequest object is null.");
		
		if (OptimizationServiceSettings.getMaximumLocations() > 0 && req.getLocationsCount() > OptimizationServiceSettings.getMaximumLocations())
			throw new ParameterOutOfRangeException(OptimizationErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "sources/destinations", Integer.toString(req.getLocationsCount()), Integer.toString(OptimizationServiceSettings.getMaximumLocations()));
		
		RouteOptimizationResult optResult = RoutingProfileManager.getInstance().getOptimizedRoutes(req);
		
		writeResponse(response, req, optResult);
	}
	
	private void writeResponse(HttpServletResponse response, RouteOptimizationRequest req, RouteOptimizationResult optResult)
	{
		
	}
}
