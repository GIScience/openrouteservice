package heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.util.Helper;

public class GraphStorageType {
	public static final int VehicleType = 1;
	public static final int Restrictions = 2;
	public static final int WayCategory = 4;
	public static final int WaySurfaceType = 8;
	public static final int HillIndex = 16;
	
	public static boolean isSet(int type, int value)
	{
		return (type & value) == value;
	}

	public static int getFomString(String value)
	{
		if (Helper.isEmpty(value))
			return 0;

		int res = 0;

		String[] values = value.split("\\|");
		for (int i = 0; i < values.length; ++i) {
			switch (values[i].toLowerCase()) {
			case "vehicletype":
				res |= VehicleType;
				break;
			case "restrictions":
				res |= Restrictions;
				break;
			case "waycategory":
				res |= WayCategory;
				break;
			case "waysurfacetype":
				res |= WaySurfaceType;
				break;
			case "hillindex":
				res |= HillIndex;
				break;
			}
		}

		return res;
	}
}
