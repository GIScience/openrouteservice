package heigit.ors.exceptions;

public class UnsetParameterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public UnsetParameterException(String paramName)
	{
		super("'" + paramName + "' parameter is not set.");
	}
}
