package heigit.ors.exceptions;

import heigit.ors.common.StatusCode;

public class MissingParameterException extends StatusCodeException 
{
   private static final long serialVersionUID = 507243355121086541L;

   public MissingParameterException(int errorCode, String paramName)
   {
	   super(StatusCode.BAD_REQUEST, errorCode, "Parameter '" + paramName + "' is missing.");
   }
   
   public MissingParameterException(String paramName)
   {
	   this(0, paramName);
   }
}
