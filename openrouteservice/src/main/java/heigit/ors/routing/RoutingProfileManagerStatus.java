package heigit.ors.routing;

public class RoutingProfileManagerStatus 
{
	private static boolean _isReady = false;

	public static boolean isReady()
	{
		return _isReady;
	}
	
	public static void setReady(boolean ready)
	{
		_isReady = ready;		
	}
}
