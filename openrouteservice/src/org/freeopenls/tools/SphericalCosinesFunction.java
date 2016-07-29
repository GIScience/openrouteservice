package org.freeopenls.tools;

public class SphericalCosinesFunction extends DistanceFunction {

	public double calcDistance(double lon0, double lat0, double lon1, double lat1) {
		lat0 = DEG_TO_RAD * lat0;
		lat1 = DEG_TO_RAD * lat1;

		return R
				* Math.acos(Math.sin(lat0) * Math.sin(lat1) + Math.cos(lat0) * Math.cos(lat1)
						* Math.cos(DEG_TO_RAD * (lon1 - lon0)));
	}
}