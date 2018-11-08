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
    private double maxRadius;
    private Envelope envelope;
    private List<AttributeValue> _attributes;

    public Isochrone(Geometry geometry, double value, double maxRadius) {
        this.geometry = geometry;
        this.value = value;
        this.maxRadius = maxRadius;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public double getValue() {
        return value;
    }

    public double getMaxRadius(String units) {

        if (units != null) {
            switch (units) {
                case "m":
                    return maxRadius;
                case "mi":
                    return UnitsConverter.MetersToMiles(maxRadius);
                case "km":
                    return UnitsConverter.MetersToKilometers(maxRadius);
            }
        }

        // return default meter
        return maxRadius;
    }

    public double getArea(String units) throws Exception {
        double area = getArea(true);
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

    public double getArea(Boolean inMeters) throws Exception {
        if (area == 0.0) {
            area = FormatUtility.roundToDecimals(GeomUtility.getArea(geometry, inMeters), 2);
        }

        return area;
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
