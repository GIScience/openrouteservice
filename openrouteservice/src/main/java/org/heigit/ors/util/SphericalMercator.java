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
package heigit.ors.util;

import java.lang.Math;

public class SphericalMercator 
{
	public static final double RADIUS = 6378137.0; /* in meters on the equator */
	public static final double PI_DIV_2 = Math.PI/2.0;
	public static final double PI_DIV_4 = Math.PI/4.0;

	public static double yToLat(double aY) {
		return Math.toDegrees(Math.atan(Math.exp(aY / RADIUS)) * 2 - PI_DIV_2);
	}
	public static double xToLon(double aX) {
		return Math.toDegrees(aX / RADIUS);
	}

	public static double latToY(double aLat) {
		return Math.log(Math.tan(PI_DIV_4 + Math.toRadians(aLat) / 2)) * RADIUS;
	}  

	public static double lonToX(double aLong) {
		return Math.toRadians(aLong) * RADIUS;
	}
}