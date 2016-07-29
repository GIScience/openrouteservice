package org.freeopenls.tools;

import com.vividsolutions.jts.geom.Coordinate;

public interface IDistanceFunction {

	public double calcDistance(double lon0, double lat0, double lon1, double lat1);

	public double calcDistance(Coordinate c0, Coordinate c1);
}
