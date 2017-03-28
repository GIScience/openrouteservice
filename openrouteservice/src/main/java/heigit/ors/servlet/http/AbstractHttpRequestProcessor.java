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
package heigit.ors.servlet.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import heigit.ors.exceptions.InternalServerException;

public abstract class AbstractHttpRequestProcessor implements HttpRequestProcessor {
	protected static Logger logger = LoggerFactory.getLogger(AbstractHttpRequestProcessor.class);
	
	protected HttpServletRequest _request;
	
	public AbstractHttpRequestProcessor(HttpServletRequest request) throws Exception
	{
		if (request == null)
			throw new InternalServerException();
		
		_request = request;
	}
	
	public abstract void process(HttpServletResponse response) throws Exception;
	
	public void destroy()
	{
		
	}
}
