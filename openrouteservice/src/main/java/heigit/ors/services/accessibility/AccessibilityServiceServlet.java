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
package heigit.ors.services.accessibility;

import javax.servlet.*;
import javax.servlet.http.*;

import heigit.ors.services.accessibility.requestprocessors.AccessibilityServiceRequestProcessorFactory;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.http.BaseHttpServlet;

public class AccessibilityServiceServlet extends BaseHttpServlet {
	/** Serial Version UID */
	private static final long serialVersionUID = 12983452345L;

	public void init() throws ServletException {
	}

	public void destroy() {
		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException   {
		try
		{
			AbstractHttpRequestProcessor reqProcessor = AccessibilityServiceRequestProcessorFactory.createProcessor(request);
			reqProcessor.process(response);
			reqProcessor.destroy();
		}
		catch (Exception ex) {
			writeError(response, ex);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try
		{
			AbstractHttpRequestProcessor reqProcessor = AccessibilityServiceRequestProcessorFactory.createProcessor(request);
			reqProcessor.process(response);
			reqProcessor.destroy();
		}
		catch (Exception ex) {
			writeError(response, ex);
		}
	}
}
