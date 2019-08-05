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
package heigit.ors.routing.graphhopper.extensions.reader.borders;

import com.vividsolutions.jts.geom.Coordinate;

import java.util.ArrayList;

/**
 * Object used for storing country boundary polygons in a hiearchical structure.
 */
public class CountryBordersHierarchy {
    private long id;
    private double minLat = 180f, minLon = 180f, maxLat = -180f, maxLon = -180f;
    private ArrayList<CountryBordersPolygon> polygons = new ArrayList<>();

    public CountryBordersHierarchy(long id) {

        this.id = id;
    }

    /**
     * Add a boundary polygon to the hierarchy and update the hiearchies extent.
     * @param cp
     */
    public void add(CountryBordersPolygon cp) {
        this.polygons.add(cp);
        // Update bounding box
        double[] bb = cp.getBBox();
        if(bb[0] < minLon) minLon = bb[0];
        if(bb[1] > maxLon) maxLon = bb[1];
        if(bb[2] < minLat) minLat = bb[2];
        if(bb[3] > maxLat) maxLat = bb[3];
    }

    /**
     * Check if the given coordinate is within the bounding box of this hierarchy. The bounding box is one that
     * surrounds all of the country polygons that the hierarchy object contains
     *
     * @param c     The coordinate to lookup
     * @return
     */
    public boolean inBbox(Coordinate c) {
        if(c.x <= minLon || c.x >= maxLon || c.y <= minLat || c.y >= maxLat)
            return false;
        else
            return true;
    }


    public double[] getBBox() {
        return new double[] {minLon, maxLon, minLat, maxLat};
    }


    public ArrayList<CountryBordersPolygon> getPolygons() {
        return polygons;
    }

    /**
     * Loop through the country polygons of this hierarchy and find those that the given coordinate is located within
     *
     * @param c     The coordinate to lookup
     * @return      An array list of the polygon objects that the coordinate is found in
     */
    public ArrayList<CountryBordersPolygon> getContainingPolygons(Coordinate c) {

        ArrayList<CountryBordersPolygon> containing = new ArrayList<>();
        if(!Double.isNaN(c.x) && !Double.isNaN(c.y) && inBbox(c)) {
            for (CountryBordersPolygon cbp : polygons) {
                if (cbp.inBbox(c)) {
                    containing.add(cbp);
                }
            }
        }
        return containing;
    }
}
