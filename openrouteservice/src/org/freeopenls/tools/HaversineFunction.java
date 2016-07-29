package org.freeopenls.tools;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class HaversineFunction extends DistanceFunction {

	public double calcDistance(double lon0, double lat0, double lon1, double lat1) {
		// return CoordTools.calculateLengthWGS84(lon0, lat0, lon1, lat1);
		double sinDLat = sin(DEG_TO_RAD_HALF * (lat1 - lat0));
		double sinDLon = sin(DEG_TO_RAD_HALF * (lon1 - lon0));
		double c = sinDLat * sinDLat + sinDLon * sinDLon * cos(DEG_TO_RAD * (lat0)) * cos(DEG_TO_RAD * (lat1));

		return R2 * asin(sqrt(c));
	}
}