package org.freeopenls.routeservice.isochrones.isolinebuilders;

import com.vividsolutions.jts.geom.Geometry;

public interface AbstractIsolineBuilder {
	Geometry computeIsoline(long level); 
}
