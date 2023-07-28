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
package org.heigit.ors.routing;

import com.graphhopper.util.Helper;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.exceptions.UnknownParameterValueException;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import org.heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import org.heigit.ors.routing.parameters.ProfileParameters;
import org.heigit.ors.routing.parameters.VehicleParameters;
import org.heigit.ors.routing.parameters.WheelchairParameters;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.heigit.ors.util.GeomUtility;
import org.heigit.ors.util.StringUtility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * This class is used to store the search/calculation Parameters to calculate the desired Route/Isochrones etc…
 * It can be called from any class and the values be set according to the needs of the route calculation.
 */
public class RouteSearchParameters {

    public static final int DEFAULT_HGV_VEHICLE_TYPE = HeavyVehicleAttributes.HGV;
    private int profileType;
    private int weightingMethod = WeightingMethod.RECOMMENDED;
    private Boolean considerTurnRestrictions = false;
    private Polygon[] avoidAreas;
    private int avoidFeaturesTypes;
    private int vehicleType = HeavyVehicleAttributes.UNKNOWN;
    private ProfileParameters profileParams;
    private WayPointBearing[] bearings = null;
    private boolean continueStraight = false;
    private double[] maxRadiuses;
    private boolean flexibleMode = false;
    private boolean optimized = true;
    private int extraInfo;
    private boolean suppressWarnings = false;

    private int[] avoidCountries = null;
    private BordersExtractor.Avoid avoidBorders = BordersExtractor.Avoid.NONE;

    private int alternativeRoutesCount = -1;
    private double alternativeRoutesWeightFactor = 1.4;
    private double alternativeRoutesShareFactor = 0.6;

    private float roundTripLength = -1;
    private int roundTripPoints = 2;
    private long roundTripSeed = -1;

    private double maximumSpeed;
    private boolean hasMaximumSpeed = false;
    private LocalDateTime departure;
    private LocalDateTime arrival;
    private int scheduleRows;
    private Duration scheduleDuaration;
    private boolean ignoreTransfers = false;
    private Duration walkingTime;
    private boolean schedule;
    private boolean hasScheduleRows = false;
    private boolean hasWalkingTime = false;
    private boolean hasScheduleDuration = false;

    public int getProfileType() {
        return profileType;
    }

    public void setProfileType(int profileType) throws Exception {
        if (profileType == RoutingProfileType.UNKNOWN)
            throw new Exception("Routing profile is unknown.");

        this.profileType = profileType;

        if (RoutingProfileType.isHeavyVehicle(profileType))
            setVehicleType(DEFAULT_HGV_VEHICLE_TYPE);
    }

    public int getWeightingMethod() {
        return weightingMethod;
    }

    public void setWeightingMethod(int weightingMethod) {
        this.weightingMethod = weightingMethod;
    }

    public Polygon[] getAvoidAreas() {
        return avoidAreas;
    }

    public void setAvoidAreas(Polygon[] avoidAreas) {
        this.avoidAreas = avoidAreas;
    }

    public boolean hasAvoidAreas() {
        return avoidAreas != null && avoidAreas.length > 0;
    }

    public int getAvoidFeatureTypes() {
        return avoidFeaturesTypes;
    }

    public void setAvoidFeatureTypes(int avoidFeatures) {
        avoidFeaturesTypes = avoidFeatures;
    }

    public boolean hasAvoidFeatures() {
        return avoidFeaturesTypes > 0;
    }

    public int[] getAvoidCountries() {
        return avoidCountries;
    }

    public void setAvoidCountries(int[] avoidCountries) {
        this.avoidCountries = avoidCountries;
    }

    public boolean hasAvoidCountries() {
        return avoidCountries != null && avoidCountries.length > 0;
    }

    public boolean hasAvoidBorders() {
        return avoidBorders != BordersExtractor.Avoid.NONE;
    }

    public void setAvoidBorders(BordersExtractor.Avoid avoidBorders) {
        this.avoidBorders = avoidBorders;
    }

    public BordersExtractor.Avoid getAvoidBorders() {
        return avoidBorders;
    }

    public Boolean getConsiderTurnRestrictions() {
        return considerTurnRestrictions;
    }

    public void setConsiderTurnRestrictions(Boolean considerTurnRestrictions) {
        this.considerTurnRestrictions = considerTurnRestrictions;
    }

    public int getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(int vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getAlternativeRoutesCount() {
        return alternativeRoutesCount;
    }

    public void setAlternativeRoutesCount(int alternativeRoutesCount) {
        this.alternativeRoutesCount = alternativeRoutesCount;
    }

    public double getAlternativeRoutesWeightFactor() {
        return alternativeRoutesWeightFactor;
    }

    public void setAlternativeRoutesWeightFactor(double alternativeRoutesWeightFactor) {
        this.alternativeRoutesWeightFactor = alternativeRoutesWeightFactor;
    }

    public double getAlternativeRoutesShareFactor() {
        return alternativeRoutesShareFactor;
    }

    public void setAlternativeRoutesShareFactor(double alternativeRoutesShareFactor) {
        this.alternativeRoutesShareFactor = alternativeRoutesShareFactor;
    }

    public int getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(int extraInfo) {
        this.extraInfo = extraInfo;
    }

    public boolean getSuppressWarnings() {
        return suppressWarnings;
    }

    public void setSuppressWarnings(boolean suppressWarnings) {
        this.suppressWarnings = suppressWarnings;
    }

    public boolean hasParameters(Class<?> value) {
        if (profileParams == null)
            return false;

        return profileParams.getClass() == value;
    }

    public ProfileParameters getProfileParameters() {
        return profileParams;
    }

    public void setProfileParams(ProfileParameters profileParams) {
        this.profileParams = profileParams;
    }

    public boolean hasFlexibleMode() {
        return flexibleMode;
    }

    public void setFlexibleMode(boolean flexibleMode) {
        this.flexibleMode = flexibleMode;
    }

    public boolean getOptimized() {
        return optimized;
    }

    public void setOptimized(boolean optimized) {
        this.optimized = optimized;
    }

    public double[] getMaximumRadiuses() {
        return maxRadiuses;
    }

    public void setMaximumRadiuses(double[] maxRadiuses) {
        this.maxRadiuses = maxRadiuses;
    }

    public WayPointBearing[] getBearings() {
        return bearings;
    }

    public void setBearings(WayPointBearing[] bearings) {
        this.bearings = bearings;
    }

    public boolean hasBearings() {
        return bearings != null && bearings.length > 0;
    }

    public void setContinueStraight(boolean continueStraightAtWaypoints) {
        continueStraight = continueStraightAtWaypoints;
    }

    public boolean hasContinueStraight() {
        return continueStraight;
    }

    public void setRoundTripLength(float length) {
        roundTripLength = length;
    }

    public float getRoundTripLength() {
        return roundTripLength;
    }

    public void setRoundTripPoints(int points) {
        roundTripPoints = points;
    }

    public int getRoundTripPoints() {
        return roundTripPoints;
    }

    public void setRoundTripSeed(long seed) {
        roundTripSeed = seed;
    }

    public long getRoundTripSeed() {
        return roundTripSeed;
    }

    public double getMaximumSpeed() {
        return maximumSpeed;
    }

    public void setMaximumSpeed(double maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
        hasMaximumSpeed = true;
    }

    public boolean hasMaximumSpeed() {
        return hasMaximumSpeed;
    }

    public boolean isProfileTypeDriving() {
        return RoutingProfileType.isDriving(this.getProfileType());
    }

    public boolean isProfileTypeHeavyVehicle() {
        return RoutingProfileType.isHeavyVehicle(this.getProfileType());
    }

    public boolean hasNonDefaultVehicleType() {
        return isProfileTypeHeavyVehicle() && getVehicleType() != DEFAULT_HGV_VEHICLE_TYPE;
    }

    public boolean requiresDynamicPreprocessedWeights() {
        return hasAvoidAreas()
            || hasAvoidFeatures()
            || hasAvoidBorders()
            || hasAvoidCountries()
            || getConsiderTurnRestrictions()
            || hasNonDefaultVehicleType()
            || isProfileTypeDriving() && hasParameters(VehicleParameters.class)
            || hasMaximumSpeed()
            || hasFlexibleMode();
    }

    /**
     * Check if the request is compatible with preprocessed graphs
     */
    public boolean requiresFullyDynamicWeights() {
        return hasAvoidAreas()
            || hasBearings()
            || hasContinueStraight()
            || (getProfileParameters() != null && getProfileParameters().hasWeightings())
            || getAlternativeRoutesCount() > 0;
    }

    // time-dependent stuff
    public LocalDateTime getDeparture() {
        return departure;
    }

    public void setDeparture(LocalDateTime departure) {
        this.departure = departure;
    }

    public boolean hasDeparture() {
        return departure!=null;
    }

    public LocalDateTime getArrival() {
        return arrival;
    }

    public void setArrival(LocalDateTime arrival) {
        this.arrival = arrival;
    }

    public boolean hasArrival() { return arrival!=null; }

    public boolean isTimeDependent() {
        return (hasDeparture() || hasArrival());
    }

    public void setScheduleDuaration(Duration scheduleDuaration) {
        this.scheduleDuaration = scheduleDuaration;
        this.hasScheduleDuration = true;
    }

    public Duration getScheduleDuaration() {
        return scheduleDuaration;
    }

    public void setIgnoreTransfers(boolean ignoreTransfers) {
        this.ignoreTransfers = ignoreTransfers;
    }

    public boolean getIgnoreTransfers() {
        return this.ignoreTransfers;
    }

    public boolean hasScheduleRows() {
        return hasScheduleRows;
    }

    public void setScheduleRows(int scheduleRows) {
        this.scheduleRows = scheduleRows;
        this.hasScheduleRows = true;
    }

    public int getScheduleRows() {
        return scheduleRows;
    }

    public void setWalkingTime(Duration walkingTime) {
        this.walkingTime = walkingTime;
        this.hasWalkingTime = true;
    }

    public Duration getWalkingTime() {
        return walkingTime;
    }

    public boolean hasWalkingTime() {
        return this.hasWalkingTime;
    }

    public void setSchedule(boolean schedule) {
        this.schedule = schedule;
    }

    public boolean getSchedule() {
        return this.schedule;
    }

    public boolean hasSchedule() {
        return this.schedule;
    }

    public boolean hasScheduleDuration() {
        return this.hasScheduleDuration;
    }
}
