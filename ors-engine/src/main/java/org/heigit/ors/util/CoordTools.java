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
package org.heigit.ors.util;

import org.locationtech.jts.geom.Coordinate;

import static java.lang.Math.*;

/**
 * <p>
 * <b>Title: CoordTools</b>
 * </p>
 * <p>
 * <b>Description:</b>Class for some Operations with Coordinates -
 * (CoordinateTools)<br>
 * </p>
 * 
 * <p>
 * <b>Copyright:</b> Copyright (c) 2008 by Pascal Neis
 * </p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-05-01
 */
public class CoordTools {
	private static final double R = 6371000;
	private static final double R2 = 2 * R;
	private static final double DEG_TO_RAD = 0.017453292519943295769236907684886;
	private static final double DEG_TO_RAD_HALF = 0.017453292519943295769236907684886 / 2.0;

	private CoordTools() {}

	public static double calcDistHaversine(double lon0, double lat0, double lon1, double lat1) {
		double sinDLat = sin(DEG_TO_RAD_HALF * (lat1 - lat0));
		double sinDLon = sin(DEG_TO_RAD_HALF * (lon1 - lon0));
		double c = sinDLat * sinDLat + sinDLon * sinDLon * cos(DEG_TO_RAD * lat0) * cos(DEG_TO_RAD * lat1);

		return R2 * asin(sqrt(c));
	}

	public static Coordinate[] parse(String value, String separator, boolean is3D, boolean inverseXY) {
		String[] coordValues = value.split(separator);
		Coordinate[] coords = new Coordinate[coordValues.length];

		for (int i = 0; i < coordValues.length; i++) {
			String[] locations = coordValues[i].split(",");
			if (inverseXY) {
				if (is3D && locations.length == 3)
					coords[i] = new Coordinate(Double.parseDouble(locations[1]), Double.parseDouble(locations[0]), Double.parseDouble(locations[2]));
				else
					coords[i] = new Coordinate(Double.parseDouble(locations[1]), Double.parseDouble(locations[0]));
			} else {
				if (is3D && locations.length == 3)
					coords[i] = new Coordinate(Double.parseDouble(locations[0]), Double.parseDouble(locations[1]), Double.parseDouble(locations[2]));
				else
					coords[i] = new Coordinate(Double.parseDouble(locations[0]), Double.parseDouble(locations[1]));
			}
		}

		return coords;
	}
}
