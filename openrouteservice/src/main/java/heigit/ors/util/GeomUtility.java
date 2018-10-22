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

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.*;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;


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

	/**
	 * Creates the correct bbox from a Graphhopper pointlist. Instead of using the > or < operators to compare double
	 * values this function uses the Math library which is more accurate and precise and creates correct bboxes even if
	 * the coordinates only differ in some small extend.
	 * The Fallback bbox is used when the pointlist is empty.
	 * @param pointList
	 * @return Returns a graphhopper bounding box
	 */
	public static BBox CalculateBoundingBox(PointList pointList, BBox _fallback) {
		if (pointList.getSize() <= 0) {
			return _fallback;
		} else {
			double min_lon = Double.MAX_VALUE;
			double max_lon = -Double.MAX_VALUE;
			double min_lat = Double.MAX_VALUE;
			double max_lat = -Double.MAX_VALUE;
			double min_ele = Double.MAX_VALUE;
			double max_ele = -Double.MAX_VALUE;
			for (int i = 0; i < pointList.getSize(); ++i) {
				min_lon = Math.min(min_lon, pointList.getLon(i));
				max_lon = Math.max(max_lon, pointList.getLon(i));
				min_lat = Math.min(min_lat, pointList.getLat(i));
				max_lat = Math.max(max_lat, pointList.getLat(i));
				if (pointList.is3D()) {
					min_ele = Math.min(min_ele, pointList.getEle(i));
					max_ele = Math.max(max_ele, pointList.getEle(i));
				}
			}
			if (pointList.is3D()) {
				BBox summary_bbox = new BBox(min_lon, max_lon, min_lat, max_lat, min_ele, max_ele);
				return summary_bbox;
			} else {
				BBox summary_bbox = new BBox(min_lon, max_lon, min_lat, max_lat);
				return summary_bbox;
			}
		}
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

    public static double degreesToMetres(double degrees) {
		return degrees * 111139;
	}

	public static double getArea(Geometry geom, Boolean inMeters) throws Exception {
        if (inMeters) {
            if (geom instanceof Polygon) {

                // https://gis.stackexchange.com/questions/265481/geotools-unexpected-result-reprojecting-bounding-box-to-epsg3035
                System.setProperty("org.geotools.referencing.forceXY", "true");

                Polygon poly = (Polygon) geom;

                CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");

                String mollweideProj = "PROJCS[\"World_Mollweide\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Mollweide\"],PARAMETER[\"False_Easting\",0],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",0],UNIT[\"Meter\",1],AUTHORITY[\"EPSG\",\"54009\"]]";

                CoordinateReferenceSystem targetCRS = CRS.parseWKT(mollweideProj);

                MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
                Geometry targetGeometry = JTS.transform(poly, transform);

                double area = targetGeometry.getArea();

                return area;

            } else {
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

}
