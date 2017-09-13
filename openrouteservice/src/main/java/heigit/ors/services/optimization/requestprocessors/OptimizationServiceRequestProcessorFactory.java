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
package heigit.ors.services.optimization.requestprocessors;

import javax.servlet.http.HttpServletRequest;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.optimization.OptimizationErrorCodes;
import heigit.ors.routing.RoutingProfileManagerStatus;
import heigit.ors.services.matrix.MatrixServiceSettings;
import heigit.ors.services.optimization.OptimizationServiceSettings;
import heigit.ors.services.optimization.requestprocessors.json.JsonOptimizationRequestProcessor;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;

import com.graphhopper.util.Helper;

public class OptimizationServiceRequestProcessorFactory {

	public static AbstractHttpRequestProcessor createProcessor(HttpServletRequest request) throws Exception  
	{
		if (!(OptimizationServiceSettings.getEnabled() && MatrixServiceSettings.getEnabled()))
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, OptimizationErrorCodes.UNKNOWN,  "Optimizaiton service is not enabled.");

		if (!RoutingProfileManagerStatus.isReady())
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, OptimizationErrorCodes.UNKNOWN, "Optimizaiton service is not ready yet.");

		String formatParam = request.getParameter("format");

		if (Helper.isEmpty(formatParam))
			formatParam = "json";

		if (formatParam.equalsIgnoreCase("json"))
			return new JsonOptimizationRequestProcessor(request);
		else 
			throw new UnknownParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "format", formatParam);
	}
}
