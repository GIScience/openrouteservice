/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.routing.RouteStep;
import heigit.ors.util.FormatUtility;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"elevation", "name", "instructionDescription", "element"})
public class GPXRoutePointElement {
    private static final int COORDINATE_DECIMAL_PLACES = 6;
    private static final int ELEVATION_DECIMAL_PLACES = 2;

    private double latitude;
    private double longitude;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "desc")
    private String instructionDescription;

    private Double elevation;

    @XmlElement(name = "extensions")
    private GPXRoutePointExtensionElement element;

    public GPXRoutePointElement() { }

    public GPXRoutePointElement( RouteStep step, double longitude, double latitude, double elevation, int stepNumber) {
        this.latitude = latitude;
        this.longitude = longitude;
        if(!Double.isNaN(elevation))
            this.elevation = elevation;

        if(step != null) {
            this.name = step.getName();
            this.instructionDescription = step.getInstruction();

            this.element = new GPXRoutePointExtensionElement(step, stepNumber);
        }
    }

    @XmlElement(name = "ele")
    public Double getElevation() {
        if(elevation != null)
            return FormatUtility.roundToDecimals(elevation, ELEVATION_DECIMAL_PLACES);
        else
            return null;
    }

    @XmlAttribute(name = "lat")
    public double getLatitude() {
        return FormatUtility.roundToDecimals(latitude, COORDINATE_DECIMAL_PLACES);
    }

    @XmlAttribute(name = "lon")
    public double getLongitude() {
        return FormatUtility.roundToDecimals(longitude, COORDINATE_DECIMAL_PLACES);
    }
}
