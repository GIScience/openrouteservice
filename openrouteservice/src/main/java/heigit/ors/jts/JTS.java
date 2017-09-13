/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package heigit.ors.jts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * JTS Geometry utility methods, bringing Geotools to JTS.
 * <p>
 * Offers geotools based services such as reprojection.
 * <p>
 * Responsibilities:
 * <ul>
 * <li>transformation</li>
 * <li>coordinate sequence editing</li>
 * <li>common coordinate sequence implementations for specific uses</li>
 * </ul>
 * 
 * @since 2.2
 *
 *
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 * @author Simone Giannecchini, GeoSolutions.
 * @author Michael Bedward
 */
public final class JTS {
    /**
     * A pool of direct positions for use in {@link #orthodromicDistance}.
     */
    private static final GeneralDirectPosition[] POSITIONS = new GeneralDirectPosition[4];
    private static GeometryFactory _factory;

    static {
        for (int i = 0; i < POSITIONS.length; i++) {
            POSITIONS[i] = new GeneralDirectPosition(i);
        }
        
        _factory = new GeometryFactory();
    }

    /**
     * Geodetic calculators already created for a given coordinate reference system. For use in
     * {@link #orthodromicDistance}.
     * 
     * Note: We would like to use {@link org.geotools.util.CanonicalSet}, but we can't because
     * {@link GeodeticCalculator} keep a reference to the CRS which is used as the key.
     */
    private static final Map<CoordinateReferenceSystem, GeodeticCalculator> CALCULATORS = new HashMap<CoordinateReferenceSystem, GeodeticCalculator>();

    /**
     * Do not allow instantiation of this class.
     */
    private JTS() {
    }

    /**
     * Makes sure that an argument is non-null.
     * 
     * @param name
     *            Argument name.
     * @param object
     *            User argument.
     * @throws IllegalArgumentException
     *             if {@code object} is null.
     */
    private static void ensureNonNull(final String name, final Object object)
            throws IllegalArgumentException {
        if (object == null) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NULL_ARGUMENT_$1, name));
        }
    }

  

    /**
     * Computes the orthodromic distance between two points. This method:
     * <p>
     * <ol>
     * <li>Transforms both points to geographic coordinates
     * (<var>latitude</var>,<var>longitude</var>).</li>
     * <li>Computes the orthodromic distance between the two points using ellipsoidal calculations.</li>
     * </ol>
     * <p>
     * The real work is performed by {@link GeodeticCalculator}. This convenience method simply
     * manages a pool of pre-defined geodetic calculators for the given coordinate reference system
     * in order to avoid repetitive object creation. If a large amount of orthodromic distances need
     * to be computed, direct use of {@link GeodeticCalculator} provides better performance than
     * this convenience method.
     * 
     * @param p1
     *            First point
     * @param p2
     *            Second point
     * @param crs
     *            Reference system the two points are in.
     * @return Orthodromic distance between the two points, in meters.
     * @throws TransformException
     *             if the coordinates can't be transformed from the specified CRS to a
     *             {@linkplain org.opengis.referencing.crs.GeographicCRS geographic CRS}.
     */
    public static synchronized double orthodromicDistance(final Coordinate p1, final Coordinate p2,
            final CoordinateReferenceSystem crs) throws TransformException {
        ensureNonNull("p1", p1);
        ensureNonNull("p2", p2);
        ensureNonNull("crs", crs);

        /*
         * Need to synchronize because we use a single instance of a Map (CALCULATORS) as well as
         * shared instances of GeodeticCalculator and GeneralDirectPosition (POSITIONS). None of
         * them are thread-safe.
         */
        GeodeticCalculator gc = (GeodeticCalculator) CALCULATORS.get(crs);

        if (gc == null) {
            gc = new GeodeticCalculator(crs);
            CALCULATORS.put(crs, gc);
        }
        assert crs.equals(gc.getCoordinateReferenceSystem()) : crs;

        final GeneralDirectPosition pos = POSITIONS[Math.min(POSITIONS.length - 1, crs
                .getCoordinateSystem().getDimension())];
        pos.setCoordinateReferenceSystem(crs);
        copy(p1, pos.ordinates);
        gc.setStartingPosition(pos);
        copy(p2, pos.ordinates);
        gc.setDestinationPosition(pos);

        return gc.getOrthodromicDistance();
    }

  
    /**
     * Copies the ordinates values from the specified JTS coordinates to the specified array. The
     * destination array can have any length. Only the relevant field of the source coordinate will
     * be copied. If the array length is greater than 3, then all extra dimensions will be set to
     * {@link Double#NaN NaN}.
     * 
     * @param point
     *            The source coordinate.
     * @param ordinates
     *            The destination array.
     */
    public static void copy(final Coordinate point, final double[] ordinates) {
        ensureNonNull("point", point);
        ensureNonNull("ordinates", ordinates);

        switch (ordinates.length) {
        default:
            Arrays.fill(ordinates, 3, ordinates.length, Double.NaN); // Fall through

        case 3:
            ordinates[2] = point.z; // Fall through

        case 2:
            ordinates[1] = point.y; // Fall through

        case 1:
            ordinates[0] = point.x; // Fall through

        case 0:
            break;
        }
    }

    

   

    /**
     * Creates a smoothed copy of the input Geometry. This is only useful for polygonal and lineal
     * geometries. Point objects will be returned unchanged. The smoothing algorithm inserts new
     * vertices which are positioned using Bezier splines. All vertices of the input Geometry will
     * be present in the output Geometry.
     * <p>
     * The {@code fit} parameter controls how tightly the smoothed lines conform to the input line
     * segments, with a value of 1 being tightest and 0 being loosest. Values outside this range
     * will be adjusted up or down as required.
     * <p>
     * The input Geometry can be a simple type (e.g. LineString, Polygon), a multi-type (e.g.
     * MultiLineString, MultiPolygon) or a GeometryCollection. The returned object will be of the
     * same type.
     * 
     * @param geom
     *            the input geometry
     * @param fit
     *            tightness of fit from 0 (loose) to 1 (tight)
     * 
     * @return a new Geometry object of the same class as {@code geom}
     * @throws IllegalArgumentException
     *             if {@code geom} is {@code null}
     */
    public static Geometry smooth(final Geometry geom, double fit) {
        return smooth(geom, fit, new GeometryFactory());
    }

    /**
     * Creates a smoothed copy of the input Geometry. This is only useful for polygonal and lineal
     * geometries. Point objects will be returned unchanged. The smoothing algorithm inserts new
     * vertices which are positioned using Bezier splines. All vertices of the input Geometry will
     * be present in the output Geometry.
     * <p>
     * The {@code fit} parameter controls how tightly the smoothed lines conform to the input line
     * segments, with a value of 1 being tightest and 0 being loosest. Values outside this range
     * will be adjusted up or down as required.
     * <p>
     * The input Geometry can be a simple type (e.g. LineString, Polygon), a multi-type (e.g.
     * MultiLineString, MultiPolygon) or a GeometryCollection. The returned object will be of the
     * same type.
     * 
     * @param geom
     *            the input geometry
     * @param fit
     *            tightness of fit from 0 (loose) to 1 (tight)
     * @param factory
     *            the GeometryFactory to use for creating smoothed objects
     * 
     * @return a new Geometry object of the same class as {@code geom}
     * @throws IllegalArgumentException
     *             if either {@code geom} or {@code factory} is {@code null}
     */
    public static Geometry smooth(final Geometry geom, double fit, final GeometryFactory factory) {

        ensureNonNull("geom", geom);
        ensureNonNull("factory", factory);

        // Adjust fit if necessary
        fit = Math.max(0.0, Math.min(1.0, fit));
        return smooth(geom, fit, factory, new GeometrySmoother(factory));
    }

    private static Geometry smooth(final Geometry geom, final double fit,
            final GeometryFactory factory, GeometrySmoother smoother) {

        switch (geom.getGeometryType().toUpperCase()) {
        case "POINT":
        case "MULTIPOINT":
            // For points, just return the input geometry
            return geom;

        case "LINESTRING":
            // This handles open and closed lines (LinearRings)
            return smoothLineString(factory, smoother, geom, fit);

        case "MULTILINESTRING":
            return smoothMultiLineString(factory, smoother, geom, fit);

        case "POLYGON":
            return smoother.smooth((Polygon) geom, fit);

        case "MULTIPOLYGON":
            return smoothMultiPolygon(factory, smoother, geom, fit);

        case "GEOMETRYCOLLECTION":
            return smoothGeometryCollection(factory, smoother, geom, fit);

        default:
            throw new UnsupportedOperationException("No smoothing method available for "
                    + geom.getGeometryType());
        }
    }

    private static Geometry smoothLineString(GeometryFactory factory, GeometrySmoother smoother,
            Geometry geom, double fit) {

        if (geom instanceof LinearRing) {
            // Treat as a Polygon
            Polygon poly = factory.createPolygon((LinearRing) geom, null);
            Polygon smoothed = smoother.smooth(poly, fit);
            return smoothed.getExteriorRing();

        } else {
            return smoother.smooth((LineString) geom, fit);
        }
    }

    private static Geometry smoothMultiLineString(GeometryFactory factory,
            GeometrySmoother smoother, Geometry geom, double fit) {

        final int N = geom.getNumGeometries();
        LineString[] smoothed = new LineString[N];

        for (int i = 0; i < N; i++) {
            smoothed[i] = (LineString) smoothLineString(factory, smoother, geom.getGeometryN(i),
                    fit);
        }

        return factory.createMultiLineString(smoothed);
    }

    private static Geometry smoothMultiPolygon(GeometryFactory factory, GeometrySmoother smoother,
            Geometry geom, double fit) {

        final int N = geom.getNumGeometries();
        Polygon[] smoothed = new Polygon[N];

        for (int i = 0; i < N; i++) {
            smoothed[i] = smoother.smooth((Polygon) geom.getGeometryN(i), fit);
        }

        return factory.createMultiPolygon(smoothed);
    }

    private static Geometry smoothGeometryCollection(GeometryFactory factory,
            GeometrySmoother smoother, Geometry geom, double fit) {

        final int N = geom.getNumGeometries();
        Geometry[] smoothed = new Geometry[N];

        for (int i = 0; i < N; i++) {
            smoothed[i] = smooth(geom.getGeometryN(i), fit, factory, smoother);
        }

        return factory.createGeometryCollection(smoothed);
    }
   

    /**
     * Removes collinear points from the provided linestring.
     * 
     * @param ls the {@link LineString} to be simplified.
     * @return a new version of the provided {@link LineString} with collinear points removed.
     */
    static LineString removeCollinearVertices(final LineString ls) {
        if (ls == null) {
            throw new NullPointerException("The provided linestring is null");
        }

        final int N = ls.getNumPoints();
        final boolean isLinearRing = ls instanceof LinearRing;

        List<Coordinate> retain = new ArrayList<Coordinate>();
        retain.add(ls.getCoordinateN(0));

        int i0 = 0, i1 = 1, i2 = 2;
        Coordinate firstCoord = ls.getCoordinateN(i0);
        Coordinate midCoord;
        Coordinate lastCoord;
        while (i2 < N) {
            midCoord = ls.getCoordinateN(i1);
            lastCoord = ls.getCoordinateN(i2);

            final int orientation = CGAlgorithms
                    .computeOrientation(firstCoord, midCoord, lastCoord);
            // Colllinearity test
            if (orientation != CGAlgorithms.COLLINEAR) {
                // add midcoord and change head
                retain.add(midCoord);
                i0 = i1;
                firstCoord = ls.getCoordinateN(i0);
            }
            i1++;
            i2++;
        }
        retain.add(ls.getCoordinateN(N - 1));

        //
        // Return value
        //
        final int size = retain.size();
        // nothing changed?
        if (size == N) {
            // free everything and return original
            retain.clear();

            return ls;
        }

        return isLinearRing ? ls.getFactory()
                .createLinearRing(retain.toArray(new Coordinate[size])) : ls.getFactory()
                .createLineString(retain.toArray(new Coordinate[size]));
    }

    /**
     * Removes collinear vertices from the provided {@link Polygon}.
     * 
     * @param polygon the instance of a {@link Polygon} to remove collinear vertices from.
     * @return a new instance of the provided {@link Polygon} without collinear vertices.
     */
    static Polygon removeCollinearVertices(final Polygon polygon) {
        if (polygon == null) {
            throw new NullPointerException("The provided Polygon is null");
        }

        // reuse existing factory
        final GeometryFactory gf = polygon.getFactory();

        // work on the exterior ring
        LineString exterior = polygon.getExteriorRing();
        LineString shell = removeCollinearVertices(exterior);
        if ((shell == null) || shell.isEmpty()) {
            return null;
        }

        // work on the holes
        List<LineString> holes = new ArrayList<LineString>();
        final int size = polygon.getNumInteriorRing();
        for (int i = 0; i < size; i++) {
            LineString hole = polygon.getInteriorRingN(i);
            hole = removeCollinearVertices(hole);
            if ((hole != null) && !hole.isEmpty()) {
                holes.add(hole);
            }
        }

        return gf.createPolygon((LinearRing) shell, holes.toArray(new LinearRing[holes.size()]));
    }

    /**
     * Removes collinear vertices from the provided {@link Geometry}.
     * 
     * <p>
     * For the moment this implementation only accepts, {@link Polygon}, {@link LineString} and {@link MultiPolygon} It will throw an exception if the
     * geometry is not one of those types
     * 
     * @param g the instance of a {@link Geometry} to remove collinear vertices from.
     * @return a new instance of the provided {@link Geometry} without collinear vertices.
     */
    public static Geometry removeCollinearVertices(final Geometry g) {
        if (g == null) {
            throw new NullPointerException("The provided Geometry is null");
        }
        if (g instanceof LineString) {
            return removeCollinearVertices((LineString) g);
        } else if (g instanceof Polygon) {
            return removeCollinearVertices((Polygon) g);
        } else if (g instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) g;
            Polygon[] parts = new Polygon[mp.getNumGeometries()];
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Polygon part = (Polygon) mp.getGeometryN(i);
                part = removeCollinearVertices(part);
                parts[i] = part;
            }

            return g.getFactory().createMultiPolygon(parts);
        }

        throw new IllegalArgumentException(
                "This method can work on LineString, Polygon and Multipolygon: " + g.getClass());
    }

    /**
     * Removes collinear vertices from the provided {@link Geometry} if the number of point exceeds the requested minPoints.
     * 
     * <p>
     * For the moment this implementation only accepts, {@link Polygon}, {@link LineString} and {@link MultiPolygon} It will throw an exception if the
     * geometry is not one of those types
     * 
     * @param geometry the instance of a {@link Geometry} to remove collinear vertices from.
     * @param minPoints perform removal of collinear points if num of vertices exceeds minPoints.
     * @return a new instance of the provided {@link Geometry} without collinear vertices.
     */
    public static Geometry removeCollinearVertices(final Geometry geometry, int minPoints) {
        ensureNonNull("geometry", geometry);

        if ((minPoints <= 0) || (geometry.getNumPoints() < minPoints)) {
            return geometry;
        }

        if (geometry instanceof LineString) {
            return removeCollinearVertices((LineString) geometry);
        } else if (geometry instanceof Polygon) {
            return removeCollinearVertices((Polygon) geometry);
        } else if (geometry instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) geometry;
            Polygon[] parts = new Polygon[mp.getNumGeometries()];
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Polygon part = (Polygon) mp.getGeometryN(i);
                part = removeCollinearVertices(part);
                parts[i] = part;
            }

            return geometry.getFactory().createMultiPolygon(parts);
        }

        throw new IllegalArgumentException(
                "This method can work on LineString, Polygon and Multipolygon: "
                        + geometry.getClass());
    }
    
    public static Polygon toGeometry(final Envelope env)
    {
    	return toGeometry(env, _factory);
    }
    
    public static Polygon toGeometry(final Envelope env, GeometryFactory factory) {
        ensureNonNull("env", env);
        
        Polygon polygon = factory.createPolygon(
                factory.createLinearRing(new Coordinate[] {
                        new Coordinate(env.getMinX(), env.getMinY()),
                        new Coordinate(env.getMaxX(), env.getMinY()),
                        new Coordinate(env.getMaxX(), env.getMaxY()),
                        new Coordinate(env.getMinX(), env.getMaxY()),
                        new Coordinate(env.getMinX(), env.getMinY()) }), null);
        
        return polygon;
}
}
