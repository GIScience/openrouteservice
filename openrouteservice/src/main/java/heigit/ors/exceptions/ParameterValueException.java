package heigit.ors.exceptions;

import heigit.ors.common.StatusCode;

public class ParameterValueException extends StatusCodeException 
{
   private static final long serialVersionUID = 507243355121086541L;

   public ParameterValueException(int errorCode, String paramName)
   {
	   super(StatusCode.BAD_REQUEST, errorCode, "'" + paramName + "' parameter has incorrect format.");
   }
   
   public ParameterValueException(String paramName)
   {
	   this(0, paramName);
   }
}
