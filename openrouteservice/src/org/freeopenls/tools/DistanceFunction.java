package org.freeopenls.tools;

import com.vividsolutions.jts.geom.Coordinate;

public abstract class DistanceFunction implements IDistanceFunction {

	protected final static double R = 6372797.560856;
	protected final static double R2 = 2 * R;
	protected final static double DEG_TO_RAD = 0.017453292519943295769236907684886;
	protected final static double DEG_TO_RAD_HALF = 0.017453292519943295769236907684886 / 2.0;

	public abstract double calcDistance(double lon0, double lat0, double lon1, double lat1);

	public double calcDistance(Coordinate c0, Coordinate c1) {
		return calcDistance(c0.x, c0.y, c1.x, c1.y);
	}
}
