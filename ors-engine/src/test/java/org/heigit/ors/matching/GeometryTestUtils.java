package org.heigit.ors.matching;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;

/**
 * Utility class for creating JTS geometries with proper SRID (4326 - WGS84) in tests.
 * Ensures all test geometries follow consistent spatial reference system conventions.
 */
public final class GeometryTestUtils {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final int SRID = 4326;

    private GeometryTestUtils() {
        // Utility class, not instantiable
    }

    /**
     * Creates a Point geometry with SRID 4326.
     *
     * @param lon longitude (x-coordinate)
     * @param lat latitude (y-coordinate)
     * @return Point with SRID 4326
     */
    public static Point point(double lon, double lat) {
        Point pt = GEOMETRY_FACTORY.createPoint(new Coordinate(lon, lat));
        pt.setSRID(SRID);
        return pt;
    }

    /**
     * Creates a LineString geometry with SRID 4326.
     *
     * @param coordinates alternating lon/lat pairs (lon1, lat1, lon2, lat2, ...)
     * @return LineString with SRID 4326
     */
    public static LineString lineString(double... coordinates) {
        if (coordinates.length < 4 || coordinates.length % 2 != 0) {
            throw new IllegalArgumentException("Coordinates must be provided in lon/lat pairs (minimum 2 points)");
        }

        Coordinate[] coords = new Coordinate[coordinates.length / 2];
        for (int i = 0; i < coordinates.length; i += 2) {
            coords[i / 2] = new Coordinate(coordinates[i], coordinates[i + 1]);
        }

        LineString line = GEOMETRY_FACTORY.createLineString(coords);
        line.setSRID(SRID);
        return line;
    }

    /**
     * Creates a Polygon geometry with SRID 4326 from a coordinate array.
     *
     * @param coordinates 2D array of [lon, lat] pairs
     * @return Polygon with SRID 4326
     */
    public static Polygon polygon(double[][] coordinates) {
        if (coordinates.length < 3) {
            throw new IllegalArgumentException("Polygon requires at least 3 points");
        }

        // Ensure ring is closed
        Coordinate[] coords = new Coordinate[coordinates.length + 1];
        for (int i = 0; i < coordinates.length; i++) {
            coords[i] = new Coordinate(coordinates[i][0], coordinates[i][1]);
        }
        // Close the ring
        coords[coordinates.length] = new Coordinate(coordinates[0][0], coordinates[0][1]);

        Polygon poly = GEOMETRY_FACTORY.createPolygon(coords);
        poly.setSRID(SRID);
        return poly;
    }

    /**
     * Creates a Polygon from coordinate array without automatic closing.
     * Assumes the coordinate array already forms a closed ring (first and last points are identical).
     *
     * @param coordinates 2D array of [lon, lat] pairs (already closed)
     * @return Polygon with SRID 4326
     */
    public static Polygon polygonClosed(double[][] coordinates) {
        Coordinate[] coords = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            coords[i] = new Coordinate(coordinates[i][0], coordinates[i][1]);
        }

        Polygon poly = GEOMETRY_FACTORY.createPolygon(coords);
        poly.setSRID(SRID);
        return poly;
    }

    /**
     * Creates a MultiPoint geometry with SRID 4326.
     *
     * @param coordinates alternating lon/lat pairs
     * @return MultiPoint with SRID 4326
     */
    public static MultiPoint multiPoint(double... coordinates) {
        Point[] points = new Point[coordinates.length / 2];
        for (int i = 0; i < coordinates.length; i += 2) {
            Point pt = GEOMETRY_FACTORY.createPoint(new Coordinate(coordinates[i], coordinates[i + 1]));
            pt.setSRID(SRID);
            points[i / 2] = pt;
        }
        MultiPoint mp = GEOMETRY_FACTORY.createMultiPointFromCoords(
            java.util.Arrays.stream(points)
                .map(Geometry::getCoordinate)
                .toArray(Coordinate[]::new)
        );
        mp.setSRID(SRID);
        return mp;
    }

    /**
     * Creates a geometry from WKT string with SRID 4326.
     *
     * @param wkt Well-Known Text representation
     * @return Geometry with SRID 4326
     */
    public static Geometry fromWKT(String wkt) {
        try {
            WKTReader reader = new WKTReader(GEOMETRY_FACTORY);
            Geometry geom = reader.read(wkt);
            geom.setSRID(SRID);
            return geom;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid WKT: " + wkt, e);
        }
    }

    /**
     * Gets the standard SRID used for all test geometries.
     *
     * @return SRID value (4326)
     */
    public static int getSRID() {
        return SRID;
    }

    /**
     * Gets the shared GeometryFactory configured with SRID 4326.
     *
     * @return GeometryFactory instance
     */
    public static GeometryFactory getGeometryFactory() {
        return GEOMETRY_FACTORY;
    }
}
