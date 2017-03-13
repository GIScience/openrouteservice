package heigit.ors.services;

public class ParameterOutOfRangeException extends Exception 
{
	private static final long serialVersionUID = 7728944138955234463L;

	public ParameterOutOfRangeException(String paramName, String value, String maxRangeValue)
	{
		super("Parameter '" + paramName + "="+ value +"' is out of range. Maximum possible value is " + maxRangeValue + ".");
	}
}
