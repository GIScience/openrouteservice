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
package heigit.ors.services.matrix.requestprocessors.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.services.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;

public class JsonMatrixRequestProcessor extends AbstractHttpRequestProcessor 
{
	public JsonMatrixRequestProcessor(HttpServletRequest request) throws Exception
	{
		super(request);
	}

	@Override
	public void process(HttpServletResponse response) throws Exception 
	{
		String reqMethod = _request.getMethod();

		MatrixRequest req = null;
		switch (reqMethod)
		{
		case "GET":
			req = JsonMatrixRequestParser.parseFromRequestParams(_request);
			break;
		///case "POST":  needs to be implemented
		//	req = JsonMatrixRequestParser.parseFromStream(_request.getInputStream());  
		//	break;
		default:
			throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED);
		}

		if (req == null)
			throw new StatusCodeException(StatusCode.BAD_REQUEST, MatrixErrorCodes.UNKNOWN, "MatrixRequest object is null.");
/*
		if (!req.isValid())
			throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.UNKNOWN, "IsochronesRequest is not valid.");

		if (IsochronesServiceSettings.getAllowComputeArea() == false && req.hasAttribute("area"))
			throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.FEATURE_NOT_SUPPORTED, "Area computation is not enabled.");

		if (req.getLocations().length > IsochronesServiceSettings.getMaximumLocations())
			throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "locations", Integer.toString(req.getLocations().length), Integer.toString(IsochronesServiceSettings.getMaximumLocations()));

		if (req.getMaximumRange() > IsochronesServiceSettings.getMaximumRange(req.getRangeType()))
			throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "range", Integer.toString(IsochronesServiceSettings.getMaximumRange(req.getRangeType())), Double.toString(req.getMaximumRange()));

		if (IsochronesServiceSettings.getMaximumIntervals() > 0)
		{
			if (IsochronesServiceSettings.getMaximumIntervals() < req.getRanges().length)
				throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "range", Integer.toString(req.getRanges().length), Integer.toString(IsochronesServiceSettings.getMaximumIntervals()));
		}
*/
	}


}
