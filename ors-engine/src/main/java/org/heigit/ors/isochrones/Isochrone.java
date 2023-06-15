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
package org.heigit.ors.isochrones;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.heigit.ors.common.AttributeValue;
import org.heigit.ors.util.FormatUtility;
import org.heigit.ors.util.GeomUtility;
import org.heigit.ors.util.UnitsConverter;

import java.util.ArrayList;
import java.util.List;

public class Isochrone {
    private final Geometry geometry;
    private final double value;
    private double area = 0.0;
    private boolean hasArea = false;
    private boolean hasReachfactor = false;
    private double reachfactor;
    private final double meanRadius;
    private Envelope envelope;
    private List<AttributeValue> attributes;

    public Isochrone(Geometry geometry, double value, double meanRadius) {
        this.geometry = geometry;
        this.value = value;
        this.meanRadius = meanRadius;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public double getValue() {
        return value;
    }

    private double getMeanRadius(String units) {
        if (units == null) units = "m";
        switch (units) {
            default:
            case "m":
                return meanRadius;
            case "mi":
                return UnitsConverter.metersToMiles(meanRadius);
            case "km":
                return UnitsConverter.metersToKilometers(meanRadius);
        }
    }

    public double calcArea(String units) throws Exception {
        if (area == 0.0) {
            area = FormatUtility.roundToDecimals(GeomUtility.getArea(geometry, true), 2);
        }
        hasArea = true;
        if (units == null) units = "m";
        switch (units) {
            default:
            case "m":
                return area;
            case "mi":
                return UnitsConverter.sqMetersToSqMiles(area);
            case "km":
                return UnitsConverter.sqMetersToSqKilometers(area);
        }
    }

    public void setArea(double area) {
        this.area = area;
    }

    public double getArea() {
        return area;
    }

    public boolean hasArea() {
        return hasArea;
    }

    public double calcReachfactor(String units) {
        double r = getMeanRadius(units);
        double maxArea = Math.PI * r * r;
        hasReachfactor = true;
        return FormatUtility.roundToDecimals(area / maxArea, 4);

    }

    public void setReachfactor(double reachfactor) {
        this.reachfactor = reachfactor;
    }

    public double getReachfactor() {
        return reachfactor;
    }

    public boolean hasReachfactor() {
        return hasReachfactor;
    }


    public Envelope getEnvelope() {
        if (envelope == null)
            envelope = geometry.getEnvelopeInternal();
        return envelope;
    }

    public List<AttributeValue> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<String> statNames, double[] statValues, String source) {
        if (statNames == null)
            return;
        if (attributes == null)
            attributes = new ArrayList<>();
        for (int i = 0; i < statNames.size(); i++)
            attributes.add(new AttributeValue(statNames.get(i), statValues[i], source));
    }
}
