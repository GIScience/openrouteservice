package heigit.ors.exceptions;

import heigit.ors.common.StatusCode;

public class MissingParameterException extends StatusCodeException {
   private static final long serialVersionUID = 507243355121086541L;

   public MissingParameterException(String paramName)
   {
	   super(StatusCode.BAD_REQUEST, "'" + paramName + "' parameter is missing.");
   }
}
