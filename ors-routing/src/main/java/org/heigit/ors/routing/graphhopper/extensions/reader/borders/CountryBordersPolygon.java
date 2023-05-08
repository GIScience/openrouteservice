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
package org.heigit.ors.routing.graphhopper.extensions.reader.borders;

import org.locationtech.jts.geom.*;
import org.apache.log4j.Logger;

import java.io.InvalidObjectException;

public class CountryBordersPolygon {
    private static final Logger LOGGER = Logger.getLogger(CountryBordersPolygon.class);
    private final String name;
    private final MultiPolygon boundary;
    private final Geometry boundaryLine;
    private double area = 0;
    private double minLat = 180f;
    private double minLon = 180f;
    private double maxLat = -180f;
    private double maxLon = -180f;

    /**
     * Construct a CountryBordersPolygon object used for determining if a way crosses a country border
     *
     * @param name                      The local name of the country
     * @param boundary                  Geometry representing the boundary of the region
     * @throws InvalidObjectException
     */
    public CountryBordersPolygon(String name, Geometry boundary) throws InvalidObjectException {
        this.name = name;
        GeometryFactory gf = new GeometryFactory();

        if(boundary.getGeometryType().equals("Polygon"))
            this.boundary = gf.createMultiPolygon(new Polygon[] {(Polygon) boundary});
        else if(boundary.getGeometryType().equals("MultiPolygon"))
            this.boundary = (MultiPolygon) boundary;
        else {
            LOGGER.error("Invalid geometry - " + boundary.getGeometryType());
            throw new InvalidObjectException("Invalid geometry for boundary " + name);
        }
        this.boundaryLine = boundary.getBoundary();
        this.area = this.boundary.getArea();
        // calculate lat and lon values
        Geometry bbox = boundary.getEnvelope();
        for(Coordinate c : bbox.getCoordinates()) {
            if(c.x < minLon) minLon = c.x;
            if(c.x > maxLon) maxLon = c.x;
            if(c.y < minLat) minLat = c.y;
            if(c.y > maxLat) maxLat = c.y;
        }
    }

    public double[] getBBox() {
        return new double[] {minLon, maxLon, minLat, maxLat};
    }

    public boolean shares(MultiPolygon other) {
        // Check if this country polygon shares a border with the one passed
        // Assume intersection is a yes
        return this.boundary.intersects(other);
    }

    public boolean crossesBoundary(LineString line) {
        // Check if the given linestring crosses the boundary of this country
        return this.boundaryLine.intersects(line);
    }

    public String getName() {
        return this.name;
    }

    public MultiPolygon getBoundary() {
        return this.boundary;
    }

    public boolean inBbox(Coordinate c) {
        return !(c.x < minLon || c.x > maxLon || c.y < minLat || c.y > maxLat);
    }

    public boolean inArea(Coordinate c) {
        if(!Double.isNaN(c.x) && !Double.isNaN(c.y) && inBbox(c)) {
            GeometryFactory gf = new GeometryFactory();

            return boundary.contains(gf.createPoint(c));
        }

        return false;
    }

    public double getArea() {
        return this.area;
    }
}