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

import org.locationtech.jts.geom.Coordinate;

import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.routing.RouteSearchParameters;

public class IsochroneSearchParameters {
    private final int travellerId;
    private Coordinate location;
    private Boolean reverseDirection = false;
    private TravelRangeType rangeType = TravelRangeType.TIME;
    private double[] ranges;
    private RouteSearchParameters parameters;
    private String calcMethod;
    private float smoothingFactor = -1.0f;
    private String[] attributes;
    private String units;
    private String areaUnits;

    public boolean hasAttribute(String attr) {
        if (attributes == null || attr == null)
            return false;

        for (String attribute : attributes)
            if (attr.equalsIgnoreCase(attribute))
                return true;

        return false;
    }

    public IsochroneSearchParameters(int travellerId, Coordinate location, double[] ranges) {
        this.travellerId = travellerId;
        this.location = location;
        this.ranges = ranges;
    }

    public int getTravellerId() {
        return travellerId;
    }

    public Coordinate getLocation() {
        return location;
    }

    public void setLocation(Coordinate location) {
        this.location = location;
    }

    public Boolean getReverseDirection() {
        return reverseDirection;
    }

    public void setReverseDirection(Boolean value) {
        reverseDirection = value;
    }

    public void setRangeType(TravelRangeType rangeType) {
        this.rangeType = rangeType;
    }

    public TravelRangeType getRangeType() {
        return rangeType;
    }

    public void setRanges(double[] values) {
        ranges = values;
    }

    public double[] getRanges() {
        return ranges;
    }

    public double getMaximumRange() {
        if (ranges.length == 1)
            return ranges[0];
        else {
            double maxValue = Double.MIN_VALUE;
            for (int i = 0; i < ranges.length; ++i) {
                double v = ranges[i];
                if (v > maxValue)
                    maxValue = v;
            }

            return maxValue;
        }
    }

    public float getSmoothingFactor() {
        return smoothingFactor;
    }

    public void setSmoothingFactor(float smoothingFactor) {
        this.smoothingFactor = smoothingFactor;
    }

    public RouteSearchParameters getRouteParameters() {
        return parameters;
    }

    public void setRouteParameters(RouteSearchParameters parameters) {
        this.parameters = parameters;
    }

    public String getCalcMethod() {
        return calcMethod;
    }

    public void setCalcMethod(String calcMethod) {
        this.calcMethod = calcMethod;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setAreaUnits(String areaUnits) {
        this.areaUnits = areaUnits;
    }

    public String getUnits() {
        return units;
    }

    public String getAreaUnits() {
        return areaUnits;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public boolean isTimeDependent() {
        return (getRouteParameters().isTimeDependent());
    }
}
