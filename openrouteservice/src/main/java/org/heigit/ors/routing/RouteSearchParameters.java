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

import com.graphhopper.gtfs.Request;
import com.graphhopper.util.Helper;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.requests.routing.RouteRequestOptions;
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

    public static final String KEY_AVOID_COUNTRIES = "avoid_countries";
    public static final String KEY_AVOID_BORDERS = "avoid_borders";
    public static final String KEY_PROFILE_PARAMS = "profile_params";
    public static final String KEY_AVOID_FEATURES = "avoid_features";
    public static final String KEY_AVOID_POLYGONS = "avoid_polygons";
    public static final String KEY_ALTERNATIVE_ROUTES_WEIGHT_FACTOR = "alternative_routes_weight_factor";
    public static final String KEY_ALTERNATIVE_ROUTES_SHARE_FACTOR = "alternative_routes_share_factor";
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

    private String options;

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

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) throws Exception {
        if (options == null)
            return;

        this.options = StringUtility.trim(options, '\"');
        JSONObject json;
        try {
            json = new JSONObject(this.options);
        } catch (Exception ex) {
            throw new ParseException(ex.getMessage(), 0);
        }

        if (json.has(KEY_AVOID_FEATURES)) {
            String keyValue = json.getString(KEY_AVOID_FEATURES);
            if (!Helper.isEmpty(keyValue)) {
                String[] avoidFeatures = keyValue.split("\\|");
                if (avoidFeatures.length > 0) {
                    int flags = 0;
                    for (String featName : avoidFeatures) {
                        if (featName != null) {
                            int flag = AvoidFeatureFlags.getFromString(featName);
                            if (flag == 0)
                                throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, KEY_AVOID_FEATURES, featName);

                            if (!AvoidFeatureFlags.isValid(profileType, flag))
                                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, KEY_AVOID_FEATURES, featName);

                            flags |= flag;
                        }
                    }
                    if (flags != 0)
                        avoidFeaturesTypes = flags;
                }
            }
        }

        if (json.has(KEY_AVOID_COUNTRIES)) {
            String keyValue = json.getString(KEY_AVOID_COUNTRIES);
            if (!Helper.isEmpty(keyValue)) {
                String[] countries = keyValue.split("\\|");
                if (countries.length > 0) {
                    this.avoidCountries = new int[countries.length];
                    for (int i = 0; i < countries.length; i++) {
                        try {
                            this.avoidCountries[i] = Integer.parseInt(countries[i]);
                        } catch (NumberFormatException nfe) {
                            // Check if ISO-3166-1 Alpha-2 / Alpha-3 code
                            int countryId = CountryBordersReader.getCountryIdByISOCode(countries[i]);
                            if (countryId > 0) {
                                this.avoidCountries[i] = countryId;
                            } else {
                                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, KEY_AVOID_COUNTRIES, countries[i]);
                            }
                        }
                    }
                }
            }
        }

        if (json.has(KEY_AVOID_BORDERS)) {
            String keyValue = json.getString(KEY_AVOID_BORDERS);
            if (!Helper.isEmpty(keyValue)) {
                if (keyValue.equals("controlled")) {
                    avoidBorders = BordersExtractor.Avoid.CONTROLLED;
                } else if (keyValue.equals("all")) {
                    avoidBorders = BordersExtractor.Avoid.ALL;
                } else {
                    throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, KEY_AVOID_BORDERS, keyValue);
                }
            }
        }

        if (json.has(KEY_PROFILE_PARAMS) && profileType == RoutingProfileType.DRIVING_CAR) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, KEY_PROFILE_PARAMS);
        } else if (json.has(KEY_PROFILE_PARAMS)) {
            JSONObject jProfileParams = json.getJSONObject(KEY_PROFILE_PARAMS);
            JSONObject jRestrictions = null;

            if (jProfileParams.has("restrictions"))
                jRestrictions = jProfileParams.getJSONObject("restrictions");

            if (RoutingProfileType.isHeavyVehicle(profileType)) {
                VehicleParameters vehicleParams = new VehicleParameters();

                if (json.has("vehicle_type")) {
                    String type = json.getString("vehicle_type");
                    setVehicleType(HeavyVehicleAttributes.getFromString(type));
                }

                // Since 4.2, all restrictions are packed in its own element
                if (jRestrictions == null)
                    jRestrictions = jProfileParams;

                if (jRestrictions.has("length"))
                    vehicleParams.setLength(jRestrictions.getDouble("length"));

                if (jRestrictions.has("width"))
                    vehicleParams.setWidth(jRestrictions.getDouble("width"));

                if (jRestrictions.has("height"))
                    vehicleParams.setHeight(jRestrictions.getDouble("height"));

                if (jRestrictions.has("weight"))
                    vehicleParams.setWeight(jRestrictions.getDouble("weight"));

                if (jRestrictions.has("axleload"))
                    vehicleParams.setAxleload(jRestrictions.getDouble("axleload"));

                int loadCharacteristics = 0;
                if (jRestrictions.has("hazmat") && jRestrictions.getBoolean("hazmat"))
                    loadCharacteristics |= VehicleLoadCharacteristicsFlags.HAZMAT;

                if (loadCharacteristics != 0)
                    vehicleParams.setLoadCharacteristics(loadCharacteristics);

                profileParams = vehicleParams;
            } else if (profileType == RoutingProfileType.WHEELCHAIR) {
                WheelchairParameters wheelchairParams = new WheelchairParameters();

                // Since 4.2, all restrictions are packed in its own element
                if (jRestrictions == null)
                    jRestrictions = jProfileParams;

                if (jRestrictions.has("surface_type"))
                    wheelchairParams.setSurfaceType(WheelchairTypesEncoder.getSurfaceType(jRestrictions.getString("surface_type")));

                if (jRestrictions.has("track_type"))
                    wheelchairParams.setTrackType(WheelchairTypesEncoder.getTrackType(jRestrictions.getString("track_type")));

                if (jRestrictions.has("smoothness_type"))
                    wheelchairParams.setSmoothnessType(WheelchairTypesEncoder.getSmoothnessType(APIEnums.SmoothnessTypes.forValue(jRestrictions.getString("smoothness_type"))));

                if (jRestrictions.has("maximum_sloped_kerb"))
                    wheelchairParams.setMaximumSlopedKerb((float) jRestrictions.getDouble("maximum_sloped_kerb"));

                if (jRestrictions.has("maximum_incline"))
                    wheelchairParams.setMaximumIncline((float) jRestrictions.getDouble("maximum_incline"));

                if (jRestrictions.has("minimum_width")) {
                    wheelchairParams.setMinimumWidth((float) jRestrictions.getDouble("minimum_width"));
                }

                if (jRestrictions.has("surface_quality_known")) {
                    wheelchairParams.setSurfaceQualityKnown(jRestrictions.getBoolean("surface_quality_known"));
                }

                profileParams = wheelchairParams;
            } else
                profileParams = new ProfileParameters();

            processWeightings(jProfileParams, profileParams);
        }

        if (json.has(KEY_AVOID_POLYGONS)) {
            JSONObject jFeature = (JSONObject) json.get(KEY_AVOID_POLYGONS);

            Geometry geom = null;
            try {
                geom = GeometryJSON.parse(jFeature);
            } catch (Exception ex) {
                throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, KEY_AVOID_POLYGONS);
            }

            if (geom instanceof Polygon) {
                avoidAreas = new Polygon[]{(Polygon) geom};
            } else if (geom instanceof MultiPolygon) {
                MultiPolygon multiPoly = (MultiPolygon) geom;
                avoidAreas = new Polygon[multiPoly.getNumGeometries()];
                for (int i = 0; i < multiPoly.getNumGeometries(); i++)
                    avoidAreas[i] = (Polygon) multiPoly.getGeometryN(i);
            } else {
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, KEY_AVOID_POLYGONS);
            }

            String paramMaxAvoidPolygonArea = AppConfig.getGlobal().getRoutingProfileParameter(RoutingProfileType.getName(profileType), "maximum_avoid_polygon_area");
            String paramMaxAvoidPolygonExtent = AppConfig.getGlobal().getRoutingProfileParameter(RoutingProfileType.getName(profileType), "maximum_avoid_polygon_extent");
            double areaLimit = StringUtility.isNullOrEmpty(paramMaxAvoidPolygonArea) ? 0 : Double.parseDouble(paramMaxAvoidPolygonArea);
            double extentLimit = StringUtility.isNullOrEmpty(paramMaxAvoidPolygonExtent) ? 0 : Double.parseDouble(paramMaxAvoidPolygonExtent);
            for (Polygon avoidArea : avoidAreas) {
                try {
                    if (areaLimit > 0) {
                        long area = Math.round(GeomUtility.getArea(avoidArea, true));
                        if (area > areaLimit) {
                            throw new StatusCodeException(StatusCode.BAD_REQUEST, RoutingErrorCodes.INVALID_PARAMETER_VALUE, String.format("The area of a polygon to avoid must not exceed %s square meters.", areaLimit));
                        }
                    }
                    if (extentLimit > 0) {
                        long extent = Math.round(GeomUtility.calculateMaxExtent(avoidArea));
                        if (extent > extentLimit) {
                            throw new StatusCodeException(StatusCode.BAD_REQUEST, RoutingErrorCodes.INVALID_PARAMETER_VALUE, String.format("The extent of a polygon to avoid must not exceed %s meters.", extentLimit));
                        }
                    }
                } catch (InternalServerException e) {
                    throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequestOptions.PARAM_AVOID_POLYGONS);
                }
            }
        }

        if (json.has("alternative_routes_count")) {
            try {
                alternativeRoutesCount = json.getInt("alternative_routes_count");
            } catch (Exception ex) {
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "alternative_routes", json.getString("alternative_routes"));
            }
            String paramMaxAlternativeRoutesCount = AppConfig.getGlobal().getRoutingProfileParameter(RoutingProfileType.getName(profileType), "maximum_alternative_routes");
            int countLimit = StringUtility.isNullOrEmpty(paramMaxAlternativeRoutesCount) ? 0 : Integer.parseInt(paramMaxAlternativeRoutesCount);
            if (countLimit > 0 && alternativeRoutesCount > countLimit) {
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_ALTERNATIVE_ROUTES, Integer.toString(alternativeRoutesCount), "The target alternative routes count has to be equal to or less than " + paramMaxAlternativeRoutesCount);
            }
            if (json.has(KEY_ALTERNATIVE_ROUTES_WEIGHT_FACTOR)) {
                try {
                    alternativeRoutesWeightFactor = json.getDouble(KEY_ALTERNATIVE_ROUTES_WEIGHT_FACTOR);
                } catch (Exception ex) {
                    throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, KEY_ALTERNATIVE_ROUTES_WEIGHT_FACTOR, json.getString(KEY_ALTERNATIVE_ROUTES_WEIGHT_FACTOR));
                }
            }
            if (json.has(KEY_ALTERNATIVE_ROUTES_SHARE_FACTOR)) {
                try {
                    alternativeRoutesShareFactor = json.getDouble(KEY_ALTERNATIVE_ROUTES_SHARE_FACTOR);
                } catch (Exception ex) {
                    throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, KEY_ALTERNATIVE_ROUTES_SHARE_FACTOR, json.getString(KEY_ALTERNATIVE_ROUTES_SHARE_FACTOR));
                }
            }
        }
    }

    private void processWeightings(JSONObject json, ProfileParameters profileParams) throws Exception {
        if (json != null && json.has("weightings")) {
            JSONObject jWeightings = json.getJSONObject("weightings");
            JSONArray jNames = jWeightings.names();

            if (jNames == null)
                return;

            for (int i = 0; i < jNames.length(); i++) {
                String name = jNames.getString(i);
                ProfileWeighting pw = new ProfileWeighting(name);

                JSONObject jw = jWeightings.getJSONObject(name);
                Iterator<String> keys = jw.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    pw.addParameter(key, jw.optString(key));
                }

                profileParams.add(pw);
            }
        }
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
