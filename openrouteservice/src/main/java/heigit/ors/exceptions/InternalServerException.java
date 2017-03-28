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

public class InternalServerException extends StatusCodeException 
{
	private static final long serialVersionUID = -1504840251219099113L;

	public InternalServerException()
	{
	  this(1);
	}
	
	public InternalServerException(int code)
	{
		super(StatusCode.INTERNAL_SERVER_ERROR, code);
	}
	
	public InternalServerException(int code, String message)
	{
		super(StatusCode.INTERNAL_SERVER_ERROR, code, message);
	}
}
