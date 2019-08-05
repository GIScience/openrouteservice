/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package heigit.ors.matrix;

import com.graphhopper.util.Helper;

public class MatrixMetricsType {
	public static final int Unknown = 0;
	public static final int Duration = 1;
	public static final int Distance = 2;
	public static final int Weight = 4;
	
    public static boolean isSet(int metrics, int value) {
        return (metrics & value) == value;
    }
    
    public int getMetricsCount(int metrics)
    {
    	int res = 0;
    	
    	if (isSet(metrics, Duration))
    		res++;

    	if (isSet(metrics, Distance))
    		res++;

    	if (isSet(metrics, Weight))
    		res++;

    	return res;
    }

	public static int getFromString(String value) {
		if (Helper.isEmpty(value))
			return 0;

		String[] values = value.toLowerCase().split("\\|");
		int res = Unknown;

		for(String str : values) {
			switch(str) {
				case "duration":
					res |= Duration;
					break;
				case "distance":
					res |= Distance;
					break;
				case "weight":
					res |= Weight;
					break;
				default:
					return Unknown;
			}
		}

		return res;
	}

	public static String getMetricNameFromInt(int metric) {
		String res;
		switch (metric) {
			case MatrixMetricsType.Duration:
				res = "duration";
				break;
			case MatrixMetricsType.Distance:
				res = "distance";
				break;
			case MatrixMetricsType.Weight:
				res = "weight";
				break;
			default:
				res = "unknown";
		}
		return res;
	}
}
