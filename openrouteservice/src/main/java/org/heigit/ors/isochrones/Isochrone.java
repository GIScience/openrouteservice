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
package heigit.ors.isochrones;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import heigit.ors.common.AttributeValue;
import heigit.ors.util.FormatUtility;
import heigit.ors.util.GeomUtility;
import heigit.ors.util.UnitsConverter;

import java.util.ArrayList;
import java.util.List;

public class Isochrone {
    private Geometry geometry;
    private double value;
    private double area = 0.0;
    private boolean hasArea = false;
    private boolean hasReachfactor = false;
    private double reachfactor;
    private double meanRadius;
    private Envelope envelope;
    private List<AttributeValue> _attributes;

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

    public double getMeanRadius(String units) {

        if (units != null) {
            switch (units) {
                case "m":
                    return meanRadius;
                case "mi":
                    return UnitsConverter.MetersToMiles(meanRadius);
                case "km":
                    return UnitsConverter.MetersToKilometers(meanRadius);
            }
        }

        // return default meter
        return meanRadius;
    }

    public double calcArea(String units) throws Exception {
        double area = calcArea(true);
        if (units != null) {
            switch (units) {
                case "m":
                    return area;
                case "mi":
                    return UnitsConverter.SqMetersToSqMiles(area);
                case "km":
                    return UnitsConverter.SqMetersToSqKilometers(area);
            }
        }

        // return default square meter
        return area;

    }

    public double calcArea(Boolean inMeters) throws Exception {
        if (area == 0.0) {
            area = FormatUtility.roundToDecimals(GeomUtility.getArea(geometry, inMeters), 2);
        }

        hasArea = true;
        return area;
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

    public double calcReachfactor(String units) throws Exception {

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
        return _attributes;
    }

    public void setAttributes(List<String> statNames, double[] statValues, String source) {
        if (statNames == null)
            return;

        if (_attributes == null)
            _attributes = new ArrayList<AttributeValue>();

        for (int i = 0; i < statNames.size(); i++)
            _attributes.add(new AttributeValue(statNames.get(i), statValues[i], source));
    }

}
