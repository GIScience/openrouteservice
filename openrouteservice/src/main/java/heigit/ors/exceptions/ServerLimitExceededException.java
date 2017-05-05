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
package heigit.ors.exceptions;

import heigit.ors.common.StatusCode;

public class ServerLimitExceededException extends StatusCodeException 
{
	private static final long serialVersionUID = 7128944138955234463L;

	public ServerLimitExceededException(int errorCode, String message)
	{
		super(StatusCode.BAD_REQUEST, errorCode, "Request parameters exceed the server configuration limits. " + message);
	}
	
	public ServerLimitExceededException(String message)
	{
		this(-1, message);
	}
}
