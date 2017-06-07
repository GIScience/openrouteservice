package heigit.ors.matrix;

import com.graphhopper.util.Helper;

public class MatrixMetricsType {
	public static final int Unknown = 0;
	public static final int Duration = 1;
	public static final int Distance = 2;
	public static final int Weight = 4;
	
	public static int getFromString(String value)
	{
        if (Helper.isEmpty(value))
            return 0;
        
        String[] values = value.toLowerCase().split("\\|");
        int res = Unknown;
        
        for(String str : values)
        {
        	switch(str)
        	{
        	case "duration":
        		res |= Duration;
        		break;
        	case "distance":
        		res |= Distance;
        		break;
        	case "weight":
        		res |= Distance;
        		break;
       		default:
        		return Unknown;
        	}
        }
        
		return res;
	}
}
