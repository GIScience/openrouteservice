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

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.TravelRangeType;
import heigit.ors.routing.RouteSearchParameters;

public class IsochroneSearchParameters {
    private int _travellerId;
    private Coordinate _location;
    private Boolean _reverseDirection = false;
    private TravelRangeType _rangeType = TravelRangeType.Time;
    private double[] _ranges;
    private RouteSearchParameters _parameters;
    private String _calcMethod;
    private float _smoothingFactor = -1.0f;
    private String[] _attributes;
    private String _units;
    private String _area_units;

    public boolean hasAttribute(String attr) {
        if (_attributes == null || attr == null)
            return false;

        for (int i = 0; i < _attributes.length; i++)
            if (attr.equalsIgnoreCase(_attributes[i]))
                return true;

        return false;
    }

    public IsochroneSearchParameters(int travellerId, Coordinate location, double[] ranges) {
        _travellerId = travellerId;
        _location = location;
        _ranges = ranges;
    }

    public int getTravellerId() {
        return _travellerId;
    }

    public Coordinate getLocation() {
        return _location;
    }

    public void setLocation(Coordinate location) {
        _location = location;
    }

    public Boolean getReverseDirection() {
        return _reverseDirection;
    }

    public void setReverseDirection(Boolean value) {
        _reverseDirection = value;
    }

    public void setRangeType(TravelRangeType rangeType) {
        _rangeType = rangeType;
    }

    public TravelRangeType getRangeType() {
        return _rangeType;
    }

    public void setRanges(double[] values) {
        _ranges = values;
    }

    public double[] getRanges() {
        return _ranges;
    }

    public double getMaximumRange() {
        if (_ranges.length == 1)
            return _ranges[0];
        else {
            double maxValue = Double.MIN_VALUE;
            for (int i = 0; i < _ranges.length; ++i) {
                double v = _ranges[i];
                if (v > maxValue)
                    maxValue = v;
            }

            return maxValue;
        }
    }

    public float getSmoothingFactor() {
        return _smoothingFactor;
    }

    public void setSmoothingFactor(float smoothingFactor) {
        this._smoothingFactor = smoothingFactor;
    }

    public RouteSearchParameters getRouteParameters() {
        return _parameters;
    }

    public void setRouteParameters(RouteSearchParameters parameters) {
        _parameters = parameters;
    }

    public String getCalcMethod() {
        return _calcMethod;
    }

    public void setCalcMethod(String calcMethod) {
        _calcMethod = calcMethod;
    }

    public void setAttributes(String[] attributes) {
        _attributes = attributes;
    }

    public void setUnits(String units) {
        _units = units;
    }

    public void setAreaUnits(String areaUnits) {
        _area_units = areaUnits;
    }

    public String getUnits() {
        return _units;
    }

    public String getAreaUnits() {
        return _area_units;
    }

    public String[] getAttributes() {
        return _attributes;
    }
}
