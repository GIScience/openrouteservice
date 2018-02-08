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
package heigit.ors.routing.graphhopper.extensions.reader.borders;

import com.vividsolutions.jts.geom.*;
import org.apache.log4j.Logger;

import java.io.InvalidObjectException;

public class CountryBordersPolygon {
    private static final Logger LOGGER = Logger.getLogger(CountryBordersPolygon.class);
    private String name;
    private MultiPolygon boundary;
    private Geometry boundaryLine;
    private double area = 0;
    private long hierarchyId;
    private double minLat = 180f, minLon = 180f, maxLat = -180f, maxLon = -180f;

    /**
     * Construct a CountryBordersPolygon object used for determining if a way crosses a country border
     *
     * @param name                      The local name of the country
     * @param boundary                  Geometry representing the boundary of the region
     * @throws InvalidObjectException
     */
    public CountryBordersPolygon(String name, Geometry boundary, long hierarchyId) throws InvalidObjectException {
        this.name = name;
        this.hierarchyId = hierarchyId;
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
        if(c.x < minLon || c.x > maxLon || c.y < minLat || c.y > maxLat)
            return false;
        else
            return true;
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

    public long getHierarchyId() { return this.hierarchyId; }
}