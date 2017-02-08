package heigit.ors.util;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class GeomUtility {
	
	private static MathTransform TRANSFORM_WGS84_SPHERICALMERCATOR = null;// CRS.findMathTransform(DefaultGeographicCRS.WGS84,
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
	
	public static double getArea(Geometry geom, Boolean inMeters) throws Exception
	{
		if (TRANSFORM_WGS84_SPHERICALMERCATOR == null) {
			String wkt = "PROJCS[\"WGS 84 / Pseudo-Mercator\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"central_meridian\",0],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"X\",EAST],AXIS[\"Y\",NORTH],AUTHORITY[\"EPSG\",\"3857\"]]";
			CoordinateReferenceSystem crs = CRS.parseWKT(wkt);//  CRS.decode("EPSG:3857");
			TRANSFORM_WGS84_SPHERICALMERCATOR = CRS.findMathTransform(DefaultGeographicCRS.WGS84, crs, true);
		}

		if (inMeters) {
			Geometry transformedGeometry = JTS.transform(geom, TRANSFORM_WGS84_SPHERICALMERCATOR);
			return transformedGeometry.getArea();
		} else {
			return geom.getArea();
		}
	}
}
