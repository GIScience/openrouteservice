package heigit.ors.util;

public class DistanceUnitUtil
{
	public static DistanceUnit getFromString(String value, DistanceUnit defaultValue)
	{
		switch (value)
		{
		case "meters":
			return DistanceUnit.Meters;
		case "m":
			return DistanceUnit.Meters;
		case "kilometers":
			return DistanceUnit.Kilometers;
		case "km":
			return DistanceUnit.Kilometers;
		case "miles":
			return DistanceUnit.Miles;
		case "mi":
			return DistanceUnit.Miles;
		}

		return defaultValue;
	}

	public static double convert(double value, DistanceUnit unitsFrom, DistanceUnit unitsTo) throws Exception
	{
		if (unitsFrom == DistanceUnit.Meters)
		{
			switch(unitsTo)
			{
			case Meters:
				return value;
			case Kilometers:
				return value / 1000.0;
			case Miles:
				return value * 0.000621371192;
			default:
				break;
			}
			return value;
		}
		else
			throw new Exception("Not implemented");
	}
}
