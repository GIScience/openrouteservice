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
import javax.servlet.http.HttpServletResponse;

import heigit.ors.routing.RoutingServiceUtils;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;

public class RoutingServiceInformationRequestProcessor extends AbstractHttpRequestProcessor {

	public RoutingServiceInformationRequestProcessor(HttpServletRequest request) throws Exception {
		super(request);
	}

	@Override
	public void process(HttpServletResponse response) throws Exception  {
			RoutingServiceUtils.writeRouteInfo(_request, response);
	}
}
