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

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;

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
 * @version 1.0 2006-05-01
 */
public class CoordTools {
    private static final DistanceCalc distanceCalc = new DistanceCalcEarth();

    private CoordTools() {
    }

    public static double calcDistHaversine(double lon0, double lat0, double lon1, double lat1) {
        return distanceCalc.calcDist(lat0, lon0, lat1, lon1);
    }
}
