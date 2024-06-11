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
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.heigit.ors.exceptions.InternalServerException;
import org.locationtech.jts.geom.*;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;


public class GeomUtility {
    private static final int ONE_DEGREE_LATITUDE_IN_METRES = 111139;// One degree latitude is approximately 111,139 metres on a spherical earth
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    private static MathTransform transformWgs84Sphericalmercator = null;// CRS.findMathTransform(DefaultGeographicCRS.WGS84,

    private GeomUtility() {
    }

    public static LineString createLinestring(Coordinate[] coords) {
        return geometryFactory.createLineString(coords);
    }

    /**
     * Creates the correct bbox from a Graphhopper pointlist. Instead of using the > or < operators to compare double
     * values this function uses the Math library which is more accurate and precise and creates correct bboxes even if
     * the coordinates only differ in some small extend.
     *
     * @param pointList the points to consider
     * @return Returns a graphhopper bounding box
     */
    public static BBox calculateBoundingBox(PointList pointList) {
        if (pointList == null || pointList.size() <= 0) {
            return new BBox(0, 0, 0, 0);
        } else {
            double minLon = Double.MAX_VALUE;
            double maxLon = -Double.MAX_VALUE;
            double minLat = Double.MAX_VALUE;
            double maxLat = -Double.MAX_VALUE;
            double minEle = Double.MAX_VALUE;
            double maxEle = -Double.MAX_VALUE;
            for (int i = 0; i < pointList.size(); ++i) {
                minLon = Math.min(minLon, pointList.getLon(i));
                maxLon = Math.max(maxLon, pointList.getLon(i));
                minLat = Math.min(minLat, pointList.getLat(i));
                maxLat = Math.max(maxLat, pointList.getLat(i));
                if (pointList.is3D()) {
                    minEle = Math.min(minEle, pointList.getEle(i));
                    maxEle = Math.max(maxEle, pointList.getEle(i));
                }
            }
            if (pointList.is3D()) {
                return new BBox(minLon, maxLon, minLat, maxLat, minEle, maxEle);
            } else {
                return new BBox(minLon, maxLon, minLat, maxLat);
            }
        }
    }

    /**
     * Takes an array of bounding boxes and calculates a bounding box that covers them all.
     *
     * @param boundingBoxes array of bounding boxes
     * @return A graphhopper bounding box that covers all provided bounding boxes
     */
    public static BBox generateBoundingFromMultiple(BBox[] boundingBoxes) {
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minEle = Double.MAX_VALUE;
        double maxEle = -Double.MAX_VALUE;

        for (BBox bbox : boundingBoxes) {
            minLon = Math.min(minLon, bbox.minLon);
            maxLon = Math.max(maxLon, bbox.maxLon);
            minLat = Math.min(minLat, bbox.minLat);
            maxLat = Math.max(maxLat, bbox.maxLat);
            if (!Double.isNaN(bbox.minEle))
                minEle = Math.min(minEle, bbox.minEle);
            if (!Double.isNaN(bbox.maxEle))
                maxEle = Math.max(maxEle, bbox.maxEle);
        }

        if (minEle != Double.MAX_VALUE && maxEle != Double.MAX_VALUE)
            return new BBox(minLon, maxLon, minLat, maxLat, minEle, maxEle);
        else
            return new BBox(minLon, maxLon, minLat, maxLat);
    }

    public static double getLength(Geometry geom, boolean inMeters) throws Exception {
        if (!(geom instanceof LineString ls))
            throw new Exception("Specified geometry type is not supported.");

        if (ls.getNumPoints() == 0)
            return 0.0;

        if (inMeters) {
            double length = 0.0;
            DistanceCalc dc = new DistanceCalcEarth();

            Coordinate c0 = ls.getCoordinateN(0);
            for (int i = 1; i < ls.getNumPoints(); ++i) {
                Coordinate c1 = ls.getCoordinateN(i);
                length += dc.calcDist(c0.y, c0.x, c1.y, c1.x);
                c0 = c1;
            }

            return length;
        } else
            return ls.getLength();
    }

    public static double metresToDegrees(double metres) {
        return metres / ONE_DEGREE_LATITUDE_IN_METRES;
    }

    public static double degreesToMetres(double degrees) {
        return degrees * ONE_DEGREE_LATITUDE_IN_METRES;
    }

    public static double getArea(Geometry geom, boolean inMeters) throws InternalServerException {
        try {
            if (inMeters) {
                if (geom instanceof Polygon poly) {

                    // https://gis.stackexchange.com/questions/265481/geotools-unexpected-result-reprojecting-bounding-box-to-epsg3035
                    System.setProperty("org.geotools.referencing.forceXY", "true");

                    CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");

                    String mollweideProj = "PROJCS[\"World_Mollweide\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Mollweide\"],PARAMETER[\"False_Easting\",0],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",0],UNIT[\"Meter\",1],AUTHORITY[\"EPSG\",\"54009\"]]";

                    CoordinateReferenceSystem targetCRS = CRS.parseWKT(mollweideProj);

                    MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
                    Geometry targetGeometry = JTS.transform(poly, transform);

                    return targetGeometry.getArea();
                } else {
                    if (transformWgs84Sphericalmercator == null) {
                        String wkt = "PROJCS[\"WGS 84 / Pseudo-Mercator\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"central_meridian\",0],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"X\",EAST],AXIS[\"Y\",NORTH],AUTHORITY[\"EPSG\",\"3857\"]]";
                        CoordinateReferenceSystem crs = CRS.parseWKT(wkt);//  CRS.decode("EPSG:3857")
                        transformWgs84Sphericalmercator = CRS.findMathTransform(DefaultGeographicCRS.WGS84, crs, true);
                    }

                    Geometry transformedGeometry = JTS.transform(geom, transformWgs84Sphericalmercator);
                    return transformedGeometry.getArea();
                }
            } else {
                return geom.getArea();
            }
        } catch (NoSuchAuthorityCodeException e) {
            throw new InternalServerException("Could not set CRS authority (getting area of feature)");
        } catch (FactoryException fe) {
            throw new InternalServerException("Problem setting up Geometry (getting area of feature)");
        } catch (MismatchedDimensionException e) {
            throw new InternalServerException("Problem with feature dimensions (getting area of feature)");
        } catch (TransformException e) {
            throw new InternalServerException("Could not transform features (getting area of feature)");
        }
    }

    public static double calculateMaxExtent(Geometry geom) throws InternalServerException {
        try {
            // https://gis.stackexchange.com/questions/265481/geotools-unexpected-result-reprojecting-bounding-box-to-epsg3035
            System.setProperty("org.geotools.referencing.forceXY", "true");

            Polygon poly = (Polygon) geom;

            CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");

            String mollweideProj = "PROJCS[\"World_Mollweide\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Mollweide\"],PARAMETER[\"False_Easting\",0],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",0],UNIT[\"Meter\",1],AUTHORITY[\"EPSG\",\"54009\"]]";

            CoordinateReferenceSystem targetCRS = CRS.parseWKT(mollweideProj);

            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
            Geometry targetGeometry = JTS.transform(poly, transform);

            Envelope envelope = targetGeometry.getEnvelopeInternal();
            return Math.max(envelope.getHeight(), envelope.getWidth());
        } catch (NoSuchAuthorityCodeException e) {
            throw new InternalServerException("Could not set CRS authority (getting area of feature)");
        } catch (FactoryException fe) {
            throw new InternalServerException("Problem setting up Geometry (getting area of feature)");
        } catch (MismatchedDimensionException e) {
            throw new InternalServerException("Problem with feature dimensions (getting area of feature)");
        } catch (TransformException e) {
            throw new InternalServerException("Could not transform features (getting area of feature)");
        }
    }
}
