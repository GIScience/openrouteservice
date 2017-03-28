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

public class StatusCodeException extends Exception 
{
	private static final long serialVersionUID = 5306540089149750357L;
	
	private int _statusCode = 200;
	private int _internalCode = 200;
   
	public StatusCodeException(int statusCode, int internalCode)
	{
		super();
	}
	
	public StatusCodeException(int statusCode)
	{
		super();
		
		_statusCode = statusCode;
	}
	
	public StatusCodeException(int statusCode, String message)
	{
		super(message);
		
		_statusCode = statusCode;
	}
	
	public StatusCodeException(int statusCode, int internalCode, String message)
	{
		super(message);
		
		_statusCode = statusCode;
		_internalCode = internalCode;
	}
	
	public int getStatusCode()
	{
		return _statusCode;
	}
	
	public int getInternalCode()
	{
		return _internalCode;
	}
}
