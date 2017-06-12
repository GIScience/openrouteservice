package heigit.ors.exceptions;

import heigit.ors.common.StatusCode;

public class ParameterValueException extends StatusCodeException 
{
   private static final long serialVersionUID = 507243355121086541L;

   public ParameterValueException(int errorCode, String paramName)
   {
	   super(StatusCode.BAD_REQUEST, errorCode, "Parameter '" + paramName + "' has incorrect value or format.");
   }
   
   public ParameterValueException(int errorCode, String paramName, String paramValue)
   {
	   super(StatusCode.BAD_REQUEST, errorCode, "Parameter '" + paramName + "' has incorrect value of '" + paramValue + "'.");
   }
   
   public ParameterValueException(String paramName)
   {
	   this(0, paramName);
   }
}
