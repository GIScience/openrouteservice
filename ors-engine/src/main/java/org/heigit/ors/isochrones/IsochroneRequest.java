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

import org.heigit.ors.common.ServiceRequest;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.common.TravellerInfo;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.locationtech.jts.geom.Coordinate;

import java.util.*;

public class IsochroneRequest extends ServiceRequest {
    private final List<TravellerInfo> travellers;
    private String calcMethod;
    private String units = null;
    private String areaUnits = null;
    private boolean includeIntersections = false;
    private String[] attributes;
    private float smoothingFactor = -1.0f;
    private int maximumLocations;
    private boolean allowComputeArea;
    private int maximumIntervals;
    // TODO instead of copying the structure maybe keep a reference to EndpointProperties?
    private int maximumRangeDistanceDefault;
    private Map<Integer, Integer> profileMaxRangeDistances;
    private int maximumRangeDistanceDefaultFastisochrones;
    private Map<Integer, Integer> profileMaxRangeDistancesFastisochrones;
    private int maximumRangeTimeDefault;
    private Map<Integer, Integer> profileMaxRangeTimes;
    private int maximumRangeTimeDefaultFastisochrones;
    private Map<Integer, Integer> profileMaxRangeTimesFastisochrones;

    public IsochroneRequest() {
        travellers = new ArrayList<>();
    }

    public String getCalcMethod() {
        return calcMethod;
    }

    public void setCalcMethod(String calcMethod) {
        this.calcMethod = calcMethod;
    }

    public String getUnits() {
        return units;
    }

    public String getAreaUnits() {
        return areaUnits;
    }

    public void setUnits(String units) {
        this.units = units.toLowerCase();
    }

    public void setAreaUnits(String areaUnits) {
        this.areaUnits = areaUnits.toLowerCase();
    }

    public boolean isValid() {
        return !travellers.isEmpty();
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    public boolean hasAttribute(String attr) {
        if (attributes == null || attr == null)
            return false;

        for (String attribute : attributes)
            if (attr.equalsIgnoreCase(attribute))
                return true;

        return false;
    }

    public boolean getIncludeIntersections() {
        return includeIntersections;
    }

    public void setIncludeIntersections(boolean value) {
        includeIntersections = value;
    }

    public Coordinate[] getLocations() {
        return travellers.stream().map(TravellerInfo::getLocation).toArray(Coordinate[]::new);
    }

    public void setSmoothingFactor(float smoothingFactor) {
        this.smoothingFactor = smoothingFactor;
    }

    public IsochroneSearchParameters getSearchParameters(int travellerIndex) {
        TravellerInfo traveller = travellers.get(travellerIndex);
        double[] ranges = traveller.getRanges();

        // convert ranges in units to meters or seconds
        if (!(units == null || "m".equalsIgnoreCase(units))) {
            double scale = 1.0;
            if (traveller.getRangeType() == TravelRangeType.DISTANCE) {
                switch (units) {
                    default:
                    case "m":
                        break;
                    case "km":
                        scale = 1000;
                        break;
                    case "mi":
                        scale = 1609.34;
                        break;
                }
            }

            if (scale != 1.0) {
                for (int i = 0; i < ranges.length; i++)
                    ranges[i] = ranges[i] * scale;
            }
        }

        IsochroneSearchParameters parameters = new IsochroneSearchParameters(travellerIndex, traveller.getLocation(), ranges);
        parameters.setLocation(traveller.getLocation());
        parameters.setRangeType(traveller.getRangeType());
        parameters.setCalcMethod(calcMethod);
        parameters.setAttributes(attributes);
        parameters.setUnits(units);
        parameters.setAreaUnits(areaUnits);
        parameters.setRouteParameters(traveller.getRouteSearchParameters());
        if ("destination".equalsIgnoreCase(traveller.getLocationType()))
            parameters.setReverseDirection(true);
        parameters.setSmoothingFactor(smoothingFactor);
        return parameters;
    }

    public List<TravellerInfo> getTravellers() {
        return travellers;
    }

    public void addTraveller(TravellerInfo traveller) throws Exception {
        if (traveller == null)
            throw new Exception("'traveller' argument is null.");

        travellers.add(traveller);
    }

    public Set<String> getProfilesForAllTravellers() {
        Set<String> ret = new HashSet<>();
        for (TravellerInfo traveller : travellers)
            ret.add(RoutingProfileType.getName(traveller.getRouteSearchParameters().getProfileType()));
        return ret;
    }

    public Set<String> getWeightingsForAllTravellers() {
        Set<String> ret = new HashSet<>();
        for (TravellerInfo traveller : travellers)
            ret.add(WeightingMethod.getName(traveller.getRouteSearchParameters().getWeightingMethod()));
        return ret;
    }

    public void setMaximumLocations(int maximumLocations) {
        this.maximumLocations = maximumLocations;
    }

    public int getMaximumLocations() {
        return maximumLocations;
    }

    public boolean isAllowComputeArea() {
        return allowComputeArea;
    }

    public void setAllowComputeArea(boolean allowComputeArea) {
        this.allowComputeArea = allowComputeArea;
    }

    public int getMaximumIntervals() {
        return maximumIntervals;
    }

    public void setMaximumIntervals(int maximumIntervals) {
        this.maximumIntervals = maximumIntervals;
    }

    public int getMaximumRangeDistanceDefault() {
        return maximumRangeDistanceDefault;
    }

    public void setMaximumRangeDistanceDefault(int maximumRangeDistanceDefault) {
        this.maximumRangeDistanceDefault = maximumRangeDistanceDefault;
    }

    public Map<Integer, Integer> getProfileMaxRangeDistances() {
        return profileMaxRangeDistances;
    }

    public void setProfileMaxRangeDistances(Map<Integer, Integer> profileMaxRangeDistances) {
        this.profileMaxRangeDistances = profileMaxRangeDistances;
    }

    public int getMaximumRangeDistanceDefaultFastisochrones() {
        return maximumRangeDistanceDefaultFastisochrones;
    }

    public void setMaximumRangeDistanceDefaultFastisochrones(int maximumRangeDistanceDefaultFastisochrones) {
        this.maximumRangeDistanceDefaultFastisochrones = maximumRangeDistanceDefaultFastisochrones;
    }

    public Map<Integer, Integer> getProfileMaxRangeDistancesFastisochrones() {
        return profileMaxRangeDistancesFastisochrones;
    }

    public void setProfileMaxRangeDistancesFastisochrones(Map<Integer, Integer> profileMaxRangeDistancesFastisochrones) {
        this.profileMaxRangeDistancesFastisochrones = profileMaxRangeDistancesFastisochrones;
    }

    public int getMaximumRangeTimeDefault() {
        return maximumRangeTimeDefault;
    }

    public void setMaximumRangeTimeDefault(int maximumRangeTimeDefault) {
        this.maximumRangeTimeDefault = maximumRangeTimeDefault;
    }

    public Map<Integer, Integer> getProfileMaxRangeTimes() {
        return profileMaxRangeTimes;
    }

    public void setProfileMaxRangeTimes(Map<Integer, Integer> profileMaxRangeTimes) {
        this.profileMaxRangeTimes = profileMaxRangeTimes;
    }

    public int getMaximumRangeTimeDefaultFastisochrones() {
        return maximumRangeTimeDefaultFastisochrones;
    }

    public void setMaximumRangeTimeDefaultFastisochrones(int maximumRangeTimeDefaultFastisochrones) {
        this.maximumRangeTimeDefaultFastisochrones = maximumRangeTimeDefaultFastisochrones;
    }

    public Map<Integer, Integer> getProfileMaxRangeTimesFastisochrones() {
        return profileMaxRangeTimesFastisochrones;
    }

    public void setProfileMaxRangeTimesFastisochrones(Map<Integer, Integer> profileMaxRangeTimesFastisochrones) {
        this.profileMaxRangeTimesFastisochrones = profileMaxRangeTimesFastisochrones;
    }
}
