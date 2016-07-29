package org.freeopenls.tools;

import org.freeopenls.error.ServiceError;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.geometry.DirectPosition;

import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.SeverityType;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * <b>Title: CoordTransform</b>
 * </p>
 * <p>
 * <b>Description:</b>Class for transform coordinates<br>
 * ATTENTION: Need EPSG-Datstore!!<br>
 * </p>
 * 
 * <p>
 * <b>Copyright:</b> Copyright (c) 2008 by Pascal Neis
 * </p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-07-24
 */
public class CoordTransform {

	private static final String mCoordSource = "GEOGCS[\"WGS 84\"," + "	DATUM[\"World Geodetic System 1984\","
			+ "	SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]],"
			+ "	AUTHORITY[\"EPSG\",\"6326\"]]," + "	PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]],"
			+ "	UNIT[\"degree\", 0.017453292519943295]," + "	AXIS[\"Geodetic latitude\", NORTH],"
			+ "	AXIS[\"Geodetic longitude\", EAST]," + "	AUTHORITY[\"EPSG\",\"4326\"]]";

	private static final String mGoogle = "PROJCS[\"Google Mercator\", \r\n" + "  GEOGCS[\"WGS 84\", \r\n"
			+ "    DATUM[\"World Geodetic System 1984\", \r\n"
			+ "      SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], \r\n"
			+ "      AUTHORITY[\"EPSG\",\"6326\"]], \r\n"
			+ "    PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], \r\n"
			+ "    UNIT[\"degree\", 0.017453292519943295], \r\n" + "    AXIS[\"Geodetic latitude\", NORTH], \r\n"
			+ "    AXIS[\"Geodetic longitude\", EAST], \r\n" + "    AUTHORITY[\"EPSG\",\"4326\"]], \r\n"
			+ "  PROJECTION[\"Mercator (1SP)\", AUTHORITY[\"EPSG\",\"9804\"]], \r\n"
			+ "  PARAMETER[\"semi_major\", 6378137.0], \r\n" + "  PARAMETER[\"semi_minor\", 6378137.0], \r\n"
			+ "  PARAMETER[\"latitude_of_origin\", 0.0], \r\n" + "  PARAMETER[\"central_meridian\", 0.0], \r\n"
			+ "  PARAMETER[\"scale_factor\", 1.0], \r\n" + "  PARAMETER[\"false_easting\", 0.0], \r\n"
			+ "  PARAMETER[\"false_northing\", 0.0], \r\n" + "  UNIT[\"m\", 1.0], \r\n"
			+ "  AXIS[\"Northing\", NORTH], \r\n" + "  AXIS[\"Easting\", EAST], \r\n"
			+ "  AUTHORITY[\"EPSG\",\"900913\"]]";

	/**
	 * Method that transform a Coordinate from the SoureSRS (EPSG:XXXXX) into
	 * the DestinationSRS (EPSG:XXXXX) by EPSG Database
	 * 
	 * @param sSourceCRS
	 * @param sTargetCRS
	 * @param cSource
	 * @return Coordinate
	 * @throws ServiceError
	 */
	public static Coordinate transformGetCoord(String sSourceCRS, String sTargetCRS, Coordinate cSource)
			throws ServiceError {

		try {
			CoordinateReferenceSystem sourceCRS;
			CoordinateReferenceSystem targetCRS;

			if (sSourceCRS.equals("EPSG:4326") && sTargetCRS.equals("EPSG:900913")) {
				sourceCRS = CRS.parseWKT(mCoordSource);
				targetCRS = CRS.parseWKT(mGoogle);
			} else if (sTargetCRS.equals("EPSG:4326") && sSourceCRS.equals("EPSG:900913")) {
				targetCRS = CRS.parseWKT(mCoordSource);
				sourceCRS = CRS.parseWKT(mGoogle);
			} else {
				sourceCRS = CRS.decode(sSourceCRS);
				targetCRS = CRS.decode(sTargetCRS);
			}

			// TODO
			MathTransform math = CRS.findMathTransform(sourceCRS, targetCRS);
			// ***Original***
			// DirectPosition ptSource = (DirectPosition)new
			// GeneralDirectPosition(cSource.x,cSource.y);
			// ***EDIT***
			DirectPosition ptSource = new GeneralDirectPosition(cSource.y, cSource.x);

			DirectPosition ptTarget = math.transform(ptSource, null);

			// ***Original***
			// return new
			// Coordinate(ptTarget.getCoordinates()[0],ptTarget.getCoordinates()[1]);
			// ***EDIT***
			return new Coordinate(ptTarget.getCoordinates()[1], ptTarget.getCoordinates()[0]);

		} catch (Exception ex) {
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.UNKNOWN, "Coordinate-Transform Exception", "Message: " + ex);
			throw se;
		}
	}

	/**
	 * Method that transform a Coordinate from the SoureSRS (EPSG:XXXXX) into
	 * the DestinationSRS (EPSG:XXXXX) by EPSG Database
	 * 
	 * @param sSourceCRS
	 * @param sTargetCRS
	 * @param cSource
	 * @return String
	 * @throws ServiceError
	 * @Deprecated Don't use this!!!!
	 */
	public static String transformGetString(String sSourceCRS, String sTargetCRS, Coordinate cSource)
			throws ServiceError {

		CRSAuthorityFactory factory = CRS.getAuthorityFactory(true);

		try {
			CoordinateReferenceSystem sourceCRS = factory.createCoordinateReferenceSystem(sSourceCRS);
			CoordinateReferenceSystem targetCRS = factory.createCoordinateReferenceSystem(sTargetCRS);
			// TODO
			MathTransform math = CRS.findMathTransform(sourceCRS, targetCRS);

			// ***Original***
			// DirectPosition ptSource = (DirectPosition)new
			// GeneralDirectPosition(cSource.x,cSource.y);
			// ***EDIT***
			DirectPosition ptSource = new GeneralDirectPosition(cSource.y, cSource.x);

			DirectPosition ptTarget = math.transform(ptSource, null);

			// ***Original***
			// return
			// ptTarget.getCoordinates()[0]+" "+ptTarget.getCoordinates()[1];
			// ***EDIT***
			return ptTarget.getCoordinates()[1] + " " + ptTarget.getCoordinates()[0];

		} catch (Exception ex) {
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.UNKNOWN, "Coordinate-Transform Exception", "Message: " + ex);
			throw se;
		}
	}

	/**
	 * Method that return from a String the "EPSG:XXXX" Code.<br>
	 * e.g. "4326" -> "EPSG:4326"<br>
	 * or "http://www.opengis.net/gml/srs/epsg.xml#31467" -> "EPSG:31467"
	 * 
	 * @param sInputEPSG
	 * @return String EPSG:XXXX Code
	 */
	public static String getEPSGCode(String sInputEPSG) {
		// /////////////////////////////////////
		// Get EPSG Number
		if (sInputEPSG.indexOf("#") >= 0)
			sInputEPSG = sInputEPSG.substring(sInputEPSG.indexOf("#") + 1, sInputEPSG.length());
		if (sInputEPSG.indexOf(":") >= 0)
			sInputEPSG = sInputEPSG.substring(sInputEPSG.indexOf(":") + 1, sInputEPSG.length());

		return "EPSG:" + sInputEPSG;
	}

	/**
	 * Method that return from a String the Code.<br>
	 * e.g. "EPSG:4326" -> "4326"<br>
	 * or "http://www.opengis.net/gml/srs/epsg.xml#31467" -> "31467"
	 * 
	 * @param sInputEPSG
	 * @return String EPSG:XXXX Code
	 */
	public static String getEPSGCodeNumber(String sInputEPSG) {
		// Get EPSG Number
		if (sInputEPSG.indexOf("#") >= 0)
			sInputEPSG = sInputEPSG.substring(sInputEPSG.indexOf("#") + 1, sInputEPSG.length());
		if (sInputEPSG.indexOf(":") >= 0)
			sInputEPSG = sInputEPSG.substring(sInputEPSG.indexOf(":") + 1, sInputEPSG.length());

		return sInputEPSG;
	}
}
