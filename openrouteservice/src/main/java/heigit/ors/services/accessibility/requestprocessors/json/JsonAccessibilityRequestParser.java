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
package heigit.ors.services.accessibility.requestprocessors.json;

import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;

import heigit.ors.accessibility.AccessibilityErrorCodes;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.services.accessibility.AccessibilityRequest;

public class JsonAccessibilityRequestParser {
	public static AccessibilityRequest parseFromStream(InputStream stream) throws Exception 
	{
		AccessibilityRequest req = null;

		try {

		} catch (Exception ex) {
			throw new StatusCodeException(AccessibilityErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document.");
		}

		return req;
	}

	public static AccessibilityRequest parseFromRequestParams(HttpServletRequest request) throws Exception
	{
		AccessibilityRequest req = null;
		
		return req;
	}
}
