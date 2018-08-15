/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.util;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeomUtility {

	private static GeometryFactory GEOM_FACTORY = new GeometryFactory();
	
	private static MathTransform TRANSFORM_WGS84_SPHERICALMERCATOR = null;// CRS.findMathTransform(DefaultGeographicCRS.WGS84,
	
	public static Point createPoint(Coordinate c)
	{
	  return GEOM_FACTORY.createPoint(c);	
	}
	
	public static LineString createLinestring(Coordinate[] coords)
	{
	  return GEOM_FACTORY.createLineString(coords);
	}
	
	// CRS.decode("EPSG:3785",
	// true), true);
	public static double pointToLineDistance(double ax, double ay, double bx, double by, double px, double py) {
		if (ax == bx && ay == by)
			return distance2(ax, ay, px, py);

		double len2 = ((bx - ax) * (bx - ax) + (by - ay) * (by - ay));
		double  r = ((px - ax) * (bx - ax) + (py - ay) * (by - ay)) / len2;

		if (r <= 0.0)
			return distance2(px, py, ax, ay);
		if (r >= 1.0)
			return distance2(px, py, bx, by);

		double s = ((ay - py) * (bx - ax) - (ax - px) * (by - ay)) / len2;

		return Math.abs(s) * Math.sqrt(len2);
	}

	public static double distance2(double ax, double ay, double bx, double by)
	{
		return Math.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay));
	}

	public static Coordinate getProjectedPointOnLine(double ax, double ay, double bx, double by, double px, double py)
	{
		// get dot product of e1, e2
		double e1x = bx - ax;
		double e1y = by - ay;

		double e2x = px - ax;
		double e2y = py - ay;
		//Point e2 = new Point(p.x - v1.x, p.y - v1.y);
		double val = e1x*e2x +e1y*e2y;// dotProduct(e1, e2);
		// get squared length of e1
		double len2 = e1x * e1x + e1y * e1y;
		Coordinate p = new Coordinate(ax + (val * e1x) / len2, ay + (val * e1y) / len2);
		return p;
	}

	public static boolean isProjectedPointOnLineSegment(double ax, double ay, double bx, double by, double px, double py)
	{
		// get dotproduct |e1| * |e2|
		double e1x = bx - ax;
		double e1y = by - ay;
		double recArea = e1x*e1x + e1y*e1y;
		// dot product of |e1| * |e2|
		double e2x = px - ax;
		double e2y = py - ay;
		double val = e1x*e2x + e1y*e2y;
		return (val > 0 && val < recArea);
	}

	public static double getLength(Geometry geom, Boolean inMeters) throws Exception
	{
		if (!(geom instanceof LineString))
			throw new Exception("Specified geometry type is not supported.");

		LineString ls = (LineString)geom;
		if (ls.getNumPoints() == 0)
			return 0.0;

		if (inMeters)
		{
			double length = 0.0;
			DistanceCalc dc = new DistanceCalcEarth();

			Coordinate c0  = ls.getCoordinateN(0);
			for (int i = 1; i < ls.getNumPoints(); ++i)
			{
				Coordinate c1 = ls.getCoordinateN(i);
				length += dc.calcDist(c0.y, c0.x, c1.y, c1.x);
				c0 = c1;
			}

			return length;
		}
		else
			return ls.getLength();
	}

	public static double metresToDegrees(double metres) {
		// One degree latitude is approximately 111,139 metres on a spherical earth
		return metres / 111139;

	}

	public  static double degreesToMetres(double degrees) {
		return degrees * 111139;
	}

	public static double getArea(Geometry geom, Boolean inMeters) throws Exception
	{
		if (inMeters) {
			if (geom instanceof Polygon)
			{
				Polygon poly = (Polygon) geom;
				double area = Math.abs(getSignedArea(poly.getExteriorRing().getCoordinateSequence()));
				
				for (int i = 0; i < poly.getNumInteriorRing(); i++) {
					LineString hole =	poly.getInteriorRingN(i);
					area -= Math.abs(getSignedArea(hole.getCoordinateSequence()));
				}
				
				return area;
			}
			else if (geom instanceof LineString)
			{
				LineString ring = (LineString)geom;
				return getSignedArea(ring.getCoordinateSequence());
			}
			else
			{
				if (TRANSFORM_WGS84_SPHERICALMERCATOR == null) {
					String wkt = "PROJCS[\"WGS 84 / Pseudo-Mercator\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"central_meridian\",0],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"X\",EAST],AXIS[\"Y\",NORTH],AUTHORITY[\"EPSG\",\"3857\"]]";
					CoordinateReferenceSystem crs = CRS.parseWKT(wkt);//  CRS.decode("EPSG:3857");
					TRANSFORM_WGS84_SPHERICALMERCATOR = CRS.findMathTransform(DefaultGeographicCRS.WGS84, crs, true);
				}


				Geometry transformedGeometry = JTS.transform(geom, TRANSFORM_WGS84_SPHERICALMERCATOR);
				return transformedGeometry.getArea();
			}
		} else {
			return geom.getArea();
		}
	}

	/**
	 * Determine the 2D bearing between two points. Note that this does not take into account the spheroid shape of
	 * the Earth.
	 *
	 * @param lat1		Latitude of point 1
	 * @param lon1		Longitude of point 1
	 * @param lat2		Latitude of point 2
	 * @param lon2		Longitude of point 2
	 *
	 * @return			The bearing from point 1 to point 2 in degrees from North
	 */
	public static double getSimpleBearing(double lat1, double lon1, double lat2, double lon2) {
		// if points are equal, do nothing
		if(lat1 == lat2 && lon1 == lon2)
			return -1;

		double theta = Math.atan2(lon2 - lon1, lat2 - lat1);
		if(theta < 0.0)
			theta += (Math.PI * 2);

		return Math.toDegrees(theta);
	}

	private static double getSignedArea(CoordinateSequence ring)
	{
		int n = ring.size();
		if (n < 3)
			return 0.0;
		/**
		 * Based on the Shoelace formula.
		 * http://en.wikipedia.org/wiki/Shoelace_formula
		 */
		Coordinate p0 = new Coordinate();
		Coordinate p1 = new Coordinate();
		Coordinate p2 = new Coordinate();
		getMercatorCoordinate(ring, 0, p1);
		getMercatorCoordinate(ring, 1, p2);
		double x0 = p1.x;
		p2.x -= x0;
		double sum = 0.0;
		for (int i = 1; i < n - 1; i++) {
			p0.y = p1.y;
			p1.x = p2.x;
			p1.y = p2.y;
			getMercatorCoordinate(ring, i + 1, p2);
			p2.x -= x0;
			sum += p1.x * (p0.y - p2.y);
		}
		return sum / 2.0;
	}
	
	private static void getMercatorCoordinate(CoordinateSequence seq, int index, Coordinate coord)
	{
		seq.getCoordinate(index, coord);
		coord.x = SphericalMercator.lonToX(coord.x);
		coord.y = SphericalMercator.latToY(coord.y);
	}
}
