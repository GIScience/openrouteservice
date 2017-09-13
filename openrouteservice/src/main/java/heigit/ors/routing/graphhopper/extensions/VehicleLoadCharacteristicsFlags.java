package heigit.ors.routing.graphhopper.extensions;

public class VehicleLoadCharacteristicsFlags {
	public static final int NONE = 0;
	public static final int  HAZMAT = 1;
			
	public int getFromString(String value)
	{
		int res = NONE;
		
		if ("hazmat".equalsIgnoreCase(value))
			res |= HAZMAT;
		
		return res;
	}
	
	public static boolean isSet(int characteristics, int value)
	{
		return (characteristics & value) == value;
	}
}
