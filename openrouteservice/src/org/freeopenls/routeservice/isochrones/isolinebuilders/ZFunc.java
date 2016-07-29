package org.freeopenls.routeservice.isochrones.isolinebuilders;

import com.vividsolutions.jts.geom.Coordinate;

public interface ZFunc {
	public long z(Coordinate c);
}