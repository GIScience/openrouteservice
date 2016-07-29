package org.freeopenls.error;

import org.freeopenls.constants.OpenLS.RequestParameter;

import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.SeverityType;


/**
 * <p><b>Title: ErrorTypes</b></p>
 * <p><b>Description:</b> Class for ErrorTypes<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-12-10
 */
public class ErrorTypes {
	
	public static ServiceError methodNameError(String requestedMethodName, String serviceMethodName){
		ServiceError se = new ServiceError(SeverityType.ERROR);
	    se.addError(ErrorCodeType.OTHER_XML,
	                         RequestParameter.methodName.toString(),
	                         "The required value of the mandatory parameter '"
	                         + RequestParameter.methodName.toString() + "'" 
	                         + " must be '" + serviceMethodName 
	                         + "'. Delivered value was: '" + requestedMethodName+"'");
	    return se;
	}

	public static ServiceError valueNotRecognized(String parameterName, String parameter){
    	ServiceError se = new ServiceError(SeverityType.ERROR);
		se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED,
				parameterName,
				"The required value of the mandatory parameter '"+parameterName+"' is missing or not right"
				+ "'. Delivered value was: '"+parameter+"'");
		return se;
	}
	
	public static ServiceError valueNotRecognized(String parameter, String message, String alternative){
    	ServiceError se = new ServiceError(SeverityType.ERROR);
		se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED,
				parameter, message + " " + alternative);
		return se;
	}
	
	public static ServiceError parameterMissing(String parameterName){
    	ServiceError se = new ServiceError(SeverityType.ERROR);
        se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED, parameterName,
                             "The required value of the mandatory parameter '"
                             + parameterName + "' is missing");
	    return se;
	}
	
	public static ServiceError wrongVersion(String requestedVersion, String serviceVersion){
		ServiceError se = new ServiceError(SeverityType.ERROR);
	    se.addError(ErrorCodeType.REQUEST_VERSION_MISMATCH,
	                         RequestParameter.version.toString(),
	                         "Version of Request Schema not supported."
	                         +"The value of the mandatory parameter '"
	                         + RequestParameter.version.toString() + "'"
	                         + "must be '" + requestedVersion
	                         + "'. Delivered value was: " + requestedVersion);
	    return se;
	}

	public static ServiceError versionMismatch(String requestedVersion, String serviceVersion){
		ServiceError se = new ServiceError(SeverityType.ERROR);
        se.addError(ErrorCodeType.REQUEST_VERSION_MISMATCH,
                             RequestParameter.version.toString(),
                             "Version of Request Schema not supported."
                             +"The value of the mandatory parameter '"
                             + RequestParameter.version.toString() + "'"
                             + "must be '" + serviceVersion
                             + "'. Delivered value was: " + requestedVersion);
        return se;
	}
	
	public static ServiceError inconsistent(String parameter, String message){
		ServiceError se = new ServiceError(SeverityType.ERROR);
        se.addError(ErrorCodeType.INCONSISTENT,
                             parameter,
                             "The value of the parameter '"+parameter+"' is inconsistent. " + message);
        return se;
	}
	
	public static ServiceError notSupported(String parameter, String possibleValues){
		ServiceError se = new ServiceError(SeverityType.ERROR);
        se.addError(ErrorCodeType.NOT_SUPPORTED,
                             parameter,
                             "The value of the parameter '"+parameter+"' is not supported! Possible values: " + possibleValues);
        return se;
	}
	
	public static ServiceError routing(String position, String message){
		ServiceError se = new ServiceError(SeverityType.ERROR);
        se.addError(ErrorCodeType.UNKNOWN, "Routing - "+position , message);
        return se;
	}
	
	public static ServiceError unknown(String position, String message){
		ServiceError se = new ServiceError(SeverityType.ERROR);
        se.addError(ErrorCodeType.UNKNOWN, position , message);
        return se;
	}
}
