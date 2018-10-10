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

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import org.geotools.referencing.crs.DefaultGeographicCRS;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

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

	private final static double R = 6371000;// 6372797.560856;
	private final static double R2 = 2 * R;
	private final static double DEG_TO_RAD = 0.017453292519943295769236907684886;
	private final static double DEG_TO_RAD_HALF = 0.017453292519943295769236907684886 / 2.0;

	public static double calcDistHaversine(double lon0, double lat0, double lon1, double lat1) {
		double sinDLat = sin(DEG_TO_RAD_HALF * (lat1 - lat0));
		double sinDLon = sin(DEG_TO_RAD_HALF * (lon1 - lon0));
		double c = sinDLat * sinDLat + sinDLon * sinDLon * cos(DEG_TO_RAD * lat0) * cos(DEG_TO_RAD * lat1);

		return R2 * asin(sqrt(c));
	}

	public static double calcDistSphericalCosines(double lon0, double lat0, double lon1, double lat1) {
		lat0 = DEG_TO_RAD * lat0;
		lat1 = DEG_TO_RAD * lat1;

		return R
				* Math.acos(Math.sin(lat0) * Math.sin(lat1) + Math.cos(lat0) * Math.cos(lat1)
						* Math.cos(DEG_TO_RAD * (lon1 - lon0)));
	}

	// / taken from
	// http://stackoverflow.com/questions/1006654/fastest-way-to-find-distance-between-two-lat-long-points
	public double calcDistEarthApprox(double lon0, double lat0, double lon1, double lat1) {
		double dLat = DEG_TO_RAD * (lat1 - lat0);
		double dLon = DEG_TO_RAD * (lon1 - lon0);
		double tmp = cos(DEG_TO_RAD * ((lat0 + lat1) / 2)) * dLon;
		double d = dLat * dLat + tmp * tmp;
		return R * sqrt(d);
	}

	public static double calcDistEarthApprox2(double lon0, double lat0, double lon1, double lat1) {
		return Math.acos(sin(lat0) * Math.sin(lat1) + Math.cos(lat0) * Math.cos(lat1) * Math.cos(lon1 - lon0)) * R;
	}

	/**
	 * Method that calculate the totallength between the points of a coordinate
	 * array
	 * 
	 * @param cArray
	 *            Array of Coordinate
	 * @return dLength double Value Length
	 */
	public static double calculateLength(Coordinate[] cArray) {
		double dLength = 0;

		for (int i = 0; i < cArray.length - 1; i++) {
			dLength = dLength
					+ Math.sqrt(Math.pow((cArray[i].x - cArray[i + 1].x), 2)
							+ Math.pow((cArray[i].y - cArray[i + 1].y), 2));
		}
		return dLength;
	}

	/**
	 * Method that calculate the total length between the points of a coordinate
	 * array
	 * 
	 * @param cArray
	 *            Array of Coordinate
	 * @return dLength double Value Length
	 */
	public static double calculateLengthWGS84(Coordinate[] cArray) {
		double length = 0;
		int nLength = cArray.length;

		if (nLength > 0) {
			Coordinate c0 = cArray[0];
			Coordinate c1 = null;
			for (int index = 1; index < nLength; index++) {
				c1 = cArray[index];

				length += calcDistHaversine(c0.x, c0.y, c1.x, c1.y);

				c0 = c1;
			}
		}
		return length;
	}

	public static double calculateLengthWGS84(double x0, double y0, double x1, double y1) {
		return getLengthWGS84(new double[] { x0, y0 }, new double[] { x1, y1 });
	}

	public static double getLengthWGS84(double[] coord1, double[] coord2) {
		DefaultGeographicCRS GDC = DefaultGeographicCRS.WGS84;
		return GDC.distance(coord1, coord2).doubleValue();
	}

	/**
	 * Method that calculate the length between two coordinates
	 * 
	 * @param c1
	 *            Coordinate 1
	 * @param c2
	 *            Coordinate 2
	 * @return double Value Length
	 */
	public static double calculateLength(Coordinate c1, Coordinate c2) {
		return Math.sqrt(Math.pow((c1.x - c2.x), 2) + Math.pow((c1.y - c2.y), 2));
	}

	/**
	 * Method that calculate a Point by the given AzimuthAngle and Distance
	 * 
	 * @param c
	 *            Coordinate of the StayPoint
	 * @param dAzimuthAngle
	 *            double Value of the AzimuthAngle
	 * @param dDistance
	 *            double Value of the Distance
	 * @return Coordinate Calculated Point
	 */
	public static Coordinate calculateBearingPoint(Coordinate c, double dAzimuthAngle, double dDistance) {
		double dY = 0;
		double dX = 0;
		Coordinate cNEW = new Coordinate();

		// Quadranten
		// Y
		// II | I
		// ------------- X
		// III | IV

		// I. Quadrant
		if (dAzimuthAngle > 0 && dAzimuthAngle < 90) {
			dY = Math.cos(dAzimuthAngle * Math.PI / 180) * dDistance;
			dX = Math.sin(dAzimuthAngle * Math.PI / 180) * dDistance;

			cNEW.x = c.x + dX;
			cNEW.y = c.y + dY;
		}
		// II. Quadrant
		if (dAzimuthAngle > 270 && dAzimuthAngle < 360) {
			dAzimuthAngle = 360 - dAzimuthAngle;
			dY = Math.cos(dAzimuthAngle * Math.PI / 180) * dDistance;
			dX = Math.sin(dAzimuthAngle * Math.PI / 180) * dDistance;

			cNEW.x = c.x - dX;
			cNEW.y = c.y + dY;
		}
		// III. Quadrant
		if (dAzimuthAngle > 180 && dAzimuthAngle < 270) {
			dAzimuthAngle = dAzimuthAngle - 180;
			dY = Math.cos(dAzimuthAngle * Math.PI / 180) * dDistance;
			dX = Math.sin(dAzimuthAngle * Math.PI / 180) * dDistance;

			cNEW.x = c.x - dX;
			cNEW.y = c.y - dY;
		}
		// IV. Quadrant
		if (dAzimuthAngle > 90 && dAzimuthAngle < 180) {
			dAzimuthAngle = dAzimuthAngle - 90;
			dX = Math.cos(dAzimuthAngle * Math.PI / 180) * dDistance;
			dY = Math.sin(dAzimuthAngle * Math.PI / 180) * dDistance;
			cNEW.x = c.x + dX;
			cNEW.y = c.y - dY;
		}

		if (dAzimuthAngle == 0) {
			cNEW.x = c.x;
			cNEW.y = c.y + dDistance;
		}
		if (dAzimuthAngle == 270) {
			cNEW.x = c.x - dDistance;
			cNEW.y = c.y;
		}
		if (dAzimuthAngle == 180) {
			cNEW.x = c.x;
			cNEW.y = c.y - dDistance;
		}
		if (dAzimuthAngle == 90) {
			cNEW.x = c.x + dDistance;
			cNEW.y = c.y;
		}

		return cNEW;
	}

	/**
	 * Method that calculate the BearingAngle of c1 to c2 in RAD
	 * 
	 * @param c1
	 *            Coordinate of the firstPoint
	 * @param c2
	 *            Coordinate of the secondPoint
	 * @return double double Value of the Angle in RAD
	 * @deprecated not USE!!!!
	 */
	public static double calculateAzimuthAngleRAD(Coordinate c1, Coordinate c2) {
		double t = 0;
		double dY = 0;
		double dX = 0;

		dX = c2.x - c1.x;
		if (dX < 0.00000000001)
			dX = dX * -1;
		dY = c2.y - c1.y;
		if (dY < 0.00000000001)
			dY = dY * -1;

		// Quadranten
		// II | I
		// -------------
		// III | IV

		if (c1.x < c2.x && c1.y < c2.y) // I. Quadrant
			t = Math.atan(dX / dY);
		if (c1.x > c2.x && c1.y < c2.y) // II. Quadrant
			t = 2 * Math.PI - Math.atan(dX / dY);
		if (c1.x > c2.x && c1.y > c2.y) // III. Quadrant
			t = Math.PI + Math.atan(dX / dY);
		if (c1.x < c2.x && c1.y > c2.y) // IV. Quadrant
			t = Math.PI - Math.atan(dX / dY);

		return t;
	}

	public static double calculateAzimuthAngle(Coordinate c1, Coordinate c2) {
		double t = 0;
		double dY = 0;
		double dX = 0;

		dX = c2.x - c1.x;
		if (dX < 0.00000000001)
			dX = dX * -1;
		dY = c2.y - c1.y;
		if (dY < 0.00000000001)
			dY = dY * -1;

		// Quadranten
		// II | I
		// -------------
		// III | IV

		if (c1.x < c2.x && c1.y < c2.y) // I. Quadrant
		{
			t = Math.atan(dX / dY);
			return t;
		} else if (c1.x > c2.x && c1.y < c2.y) // II. Quadrant
		{
			t = 2 * Math.PI - Math.atan(dX / dY);
			return t;
		} else if (c1.x > c2.x && c1.y > c2.y) // III. Quadrant,
		{
			t = Math.PI + Math.atan(dX / dY);
			return t;
		} else if (c1.x < c2.x && c1.y > c2.y) // IV. Quadrant
		{
			t = Math.PI - Math.atan(dX / dY);
			return t;
		} else if (c1.x < c2.x && c1.y == c2.y) // between I. Quadrant & IV.
												// Quadrant
		{
			t = Math.PI / 2;
			return t;
		} else if (c1.x == c2.x && c1.y < c2.y) // between I. Quadrant & II.
												// Quadrant
		{
			t = 0;
			return t;
		} else if (c1.x > c2.x && c1.y == c2.y) // between II. Quadrant & III.
												// Quadrant
		{
			t = Math.PI * 1.5;
			return t;
		} else if (c1.x == c2.x && c1.y > c2.y) // between III. Quadrant & IV.
												// Quadrant
		{
			t = Math.PI;
			return t;
		} else
			return t;
	}



	/**
	 * Method that calculate the BearingAngle of c1 to c2 in Deegree
	 * 
	 * @param c1
	 *            Coordinate of the firstPoint
	 * @param c2
	 *            Coordinate of the secondPoint
	 * @return double double Value of the Angle in Deegree
	 * @deprecated not USE!!!!
	 */
	public static double calculateAzimuthAngleDEEGREE(Coordinate c1, Coordinate c2) {
		double t = 0;
		double dY = 0;
		double dX = 0;

		dX = c2.x - c1.x;
		if (dX < 0.00000000001)
			dX = dX * -1;
		dY = c2.y - c1.y;
		if (dY < 0.00000000001)
			dY = dY * -1;

		// Quadranten
		// II | I
		// -------------
		// III | IV

		if (c1.y < c2.y && c1.x < c2.x) // I. Quadrant
			t = Math.atan(dX / dY);
		if (c1.x > c2.x && c1.y < c2.y) // II. Quadrant
			t = 2 * Math.PI - Math.atan(dX / dY);
		if (c1.x > c2.x && c1.y > c2.y) // III. Quadrant
			t = Math.PI + Math.atan(dX / dY);
		if (c1.y > c2.y && c1.x < c2.x) // IV. Quadrant
			t = Math.PI - Math.atan(dX / dY);

		return t * 180 / Math.PI;
	}

	/**
	 * Method that calculate all distance to the points of a geometry and return
	 * the shortest one.
	 * 
	 * @param obj
	 *            Object geometry - only LineString and MultiLineString are
	 *            supported
	 * @param cPoint
	 *            Coordinate to calculate the distance to
	 * @param d
	 *            start-value for distance ; is no distance shorter then that
	 *            one, this one will be responsed
	 * @return double shortest distance
	 */
	public static double findShortestDistanceToGeometry(Object obj, Coordinate cPoint, double d) {
		double dDistance = d;
		LineString ls = null;

		if (obj instanceof MultiLineString)
			ls = (LineString) ((MultiLineString) obj).getGeometryN(0);
		else
			ls = (LineString) obj;

		Coordinate c[] = ls.getCoordinates();

		for (int k = 0; k < c.length; k++) {
			double dDistanceTMP = CoordTools.calculateLength(cPoint, c[k]);
			if (dDistance > dDistanceTMP)
				dDistance = dDistanceTMP;
		}

		return dDistance;
	}

	/**
	 * Computes the distance from a point p to a line segment AB
	 * 
	 * Note: NON-ROBUST!
	 * 
	 * @param p
	 *            the point to compute the distance for
	 * @param A
	 *            one point of the line
	 * @param B
	 *            another point of the line (must be different to A)
	 * @return the distance from p to line segment AB
	 */
	public static double distancePointLine(Coordinate p, Coordinate A, Coordinate B) {
		// if start==end, then use pt distance
		if (A.equals(B))
			return p.distance(A);

		// otherwise use comp.graphics.algorithms Frequently Asked Questions
		// method
		/*
		 * (1) AC dot AB r = --------- ||AB||^2 r has the following meaning: r=0
		 * P = A r=1 P = B r<0 P is on the backward extension of AB r>1 P is on
		 * the forward extension of AB 0<r<1 P is interior to AB
		 */

		double r = ((p.x - A.x) * (B.x - A.x) + (p.y - A.y) * (B.y - A.y))
				/ ((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y));

		if (r <= 0.0)
			return p.distance(A);
		if (r >= 1.0)
			return p.distance(B);

		/*
		 * (2) (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay) s = -----------------------------
		 * L^2
		 * 
		 * Then the distance from C to P = |s|*L.
		 */

		double s = ((A.y - p.y) * (B.x - A.x) - (A.x - p.x) * (B.y - A.y))
				/ ((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y));

		return Math.abs(s) * Math.sqrt(((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y)));
	}

	/**
	 * Computes a point from a point p to a line segment AB
	 * 
	 * @param P
	 *            the point to compute the point for
	 * @param A
	 *            one point of the line
	 * @param B
	 *            another point of the line (must be different to A)
	 * @return the point on line segment AB
	 */
	public static Coordinate pointLine(Coordinate P, Coordinate A, Coordinate B) {
		/*
		 * (1) AC dot AB r = --------- ||AB||^2 r has the following meaning: r=0
		 * P = A r=1 P = B r<0 P is on the backward extension of AB r>1 P is on
		 * the forward extension of AB 0<r<1 P is interior to AB
		 */
		double r = ((P.x - A.x) * (B.x - A.x) + (P.y - A.y) * (B.y - A.y))
				/ ((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y));

		if (r <= 0.0)
			return A;
		if (r >= 1.0)
			return B;

		/*
		 * (2)
		 */
		double a2 = (B.y - P.y) * (B.y - P.y) + (B.x - P.x) * (B.x - P.x);
		double b2 = (A.y - P.y) * (A.y - P.y) + (A.x - P.x) * (A.x - P.x);
		double c2 = (A.y - B.y) * (A.y - B.y) + (A.x - B.x) * (A.x - B.x);

		double p = (b2 + c2 - a2) / (2 * Math.sqrt(c2));
		return new Coordinate(calculateBearingPoint(A, calculateAzimuthAngleDEEGREE(A, B), p));

		// System.out.println("A: "+A+" B: "+B+" Angle: "+calculateAzimuthAngleDEEGREE(A,
		// B)+" P: "+P);
		// double b2 = Math.pow((A.x-P.x), 2) + Math.pow((A.y-P.y), 2);
		// double a2 = Math.pow((B.x-P.x), 2) + Math.pow((B.y-P.y), 2);
		// double c2 = Math.pow((A.x-B.x), 2) + Math.pow((A.y-B.y), 2);
		// System.out.println("a: "+Math.sqrt(a2)+" a2: "+a2);
		// System.out.println("b: "+Math.sqrt(b2)+" b2: "+b2);
		// System.out.println("c: "+Math.sqrt(c2)+" c2: "+c2);
		// double p = ( b2+c2-a2 ) / ( 2*Math.sqrt(c2) );
		// double q = ( a2+c2-b2 ) / ( 2*Math.sqrt(c2) );
		//
		// double c = p+q;
		// System.out.println("Distance: p "+p+" q "+q);
		// System.out.println("c "+c+" c "+Math.sqrt(c2));
		//
		// System.out.println("c check "+(p+q));
		// double h = Math.sqrt(b2-(p*p));
		// System.out.println("h check "+Math.sqrt(b2-(p*p)));
		// System.out.println("h check "+Math.sqrt(a2-(q*q)));
		//
		// System.out.println("q check "+Math.sqrt(a2 - (h*h)));
		// System.out.println("p check "+Math.sqrt(b2 - (h*h)));
	}
	
	public static Coordinate[] parse(String value, String separator, boolean is3D, boolean inverseXY)
	{
		String[] coordValues = value.split(separator);
		Coordinate[] coords = new Coordinate[coordValues.length];

		for (int i = 0; i < coordValues.length; i++)
		{
			String[] locations = coordValues[i].split(",");
			if (inverseXY)
			{
				if (is3D && locations.length == 3)
					coords[i] = new Coordinate(Double.parseDouble(locations[1]), Double.parseDouble(locations[0]), Double.parseDouble(locations[2]));
				else
					coords[i] = new Coordinate(Double.parseDouble(locations[1]), Double.parseDouble(locations[0]));
			}
			else
			{
				if (is3D && locations.length == 3)
					coords[i] = new Coordinate(Double.parseDouble(locations[0]), Double.parseDouble(locations[1]), Double.parseDouble(locations[2]));
				else
					coords[i] = new Coordinate(Double.parseDouble(locations[0]), Double.parseDouble(locations[1]));
			}
		}

		return coords;
	}
}
