package heigit.ors.exceptions;

public class MissingParameterException extends Exception {
   private static final long serialVersionUID = 507243355121086541L;

   public MissingParameterException(String paramName)
   {
	   super("'" + paramName + "' parameter is missing.");
   }
}
