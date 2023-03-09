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

package org.heigit.ors.api.requests.isochrones;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Coordinate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.common.APIRequest;
import org.heigit.ors.api.requests.routing.RouteRequestOptions;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.common.TravellerInfo;
import org.heigit.ors.config.IsochronesServiceSettings;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.exceptions.ParameterOutOfRangeException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.isochrones.*;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.util.DistanceUnitUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.heigit.ors.api.requests.isochrones.IsochronesRequestEnums.CalculationMethod.CONCAVE_BALLS;
import static org.heigit.ors.api.requests.isochrones.IsochronesRequestEnums.CalculationMethod.FASTISOCHRONE;


@ApiModel(value = "IsochronesRequest", description = "The JSON body request sent to the isochrones service which defines options and parameters regarding the isochrones to generate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class IsochronesRequest extends APIRequest {
    public static final String PARAM_LOCATIONS = "locations";
    public static final String PARAM_LOCATION_TYPE = "location_type";
    public static final String PARAM_OPTIONS = "options";
    public static final String PARAM_RANGE = "range";
    public static final String PARAM_RANGE_TYPE = "range_type";
    public static final String PARAM_RANGE_UNITS = "units";
    public static final String PARAM_AREA_UNITS = "area_units";
    public static final String PARAM_INTERSECTIONS = "intersections";
    public static final String PARAM_ATTRIBUTES = "attributes";
    public static final String PARAM_INTERVAL = "interval";
    public static final String PARAM_SMOOTHING = "smoothing";
    public static final String PARAM_TIME = "time";


    @ApiModelProperty(name = PARAM_LOCATIONS, value = "The locations to use for the route as an array of `longitude/latitude` pairs in WGS 84 (EPSG:4326)",
            example = "[[8.681495,49.41461],[8.686507,49.41943]]",
            required = true)
    @JsonProperty(PARAM_LOCATIONS)
    private Double[][] locations = new Double[][]{};
    @JsonIgnore
    private boolean hasLocations = false;

    @ApiModelProperty(name = PARAM_LOCATION_TYPE, value = "`start` treats the location(s) as starting point, `destination` as goal. CUSTOM_KEYS:{'apiDefault':'start'}",
            example = "start")
    @JsonProperty(value = PARAM_LOCATION_TYPE)
    private IsochronesRequestEnums.LocationType locationType;
    @JsonIgnore
    private boolean hasLocationType = false;

    @ApiModelProperty(name = PARAM_RANGE, value = "Maximum range value of the analysis in **seconds** for time and **metres** for distance." +
            "Alternatively a comma separated list of specific range values. Ranges will be the same for all locations.",
            example = "[ 300, 200 ]",
            required = true)
    @JsonProperty(PARAM_RANGE)
    private List<Double> range;
    @JsonIgnore
    private boolean hasRange = false;

    @ApiModelProperty(name = PARAM_RANGE_TYPE,
            value = "Specifies the isochrones reachability type. CUSTOM_KEYS:{'apiDefault':'time'}", example = "time")
    @JsonProperty(value = PARAM_RANGE_TYPE, defaultValue = "time")
    private IsochronesRequestEnums.RangeType rangeType;
    @JsonIgnore
    private boolean hasRangeType = false;

    // unit only valid for range_type distance, will be ignored for range_time time
    @ApiModelProperty(name = PARAM_RANGE_UNITS,
            value = "Specifies the distance units only if `range_type` is set to distance.\n" +
                    "Default: m. " +
                    "CUSTOM_KEYS:{'apiDefault':'m','validWhen':{'ref':'range_type','value':'distance'}}",
            example = "m")
    @JsonProperty(value = PARAM_RANGE_UNITS)
    private APIEnums.Units rangeUnit;
    @JsonIgnore
    private boolean hasRangeUnits = false;

    @ApiModelProperty(name = PARAM_OPTIONS,
            value = "Additional options for the isochrones request",
            example = "{\"avoid_borders\":\"all\"}")
    @JsonProperty(PARAM_OPTIONS)
    private RouteRequestOptions isochronesOptions;
    @JsonIgnore
    private boolean hasOptions = false;

    @ApiModelProperty(hidden = true)
    private APIEnums.RouteResponseType responseType = APIEnums.RouteResponseType.GEOJSON;

    @ApiModelProperty(name = PARAM_AREA_UNITS,
            value = "Specifies the area unit.\n" +
                    "Default: m. " +
                    "CUSTOM_KEYS:{'apiDefault':'m','validWhen':{'ref':'attributes','value':'area'}}")
    @JsonProperty(value = PARAM_AREA_UNITS)
    private APIEnums.Units areaUnit;
    @JsonIgnore
    private boolean hasAreaUnits = false;

    @ApiModelProperty(name = PARAM_INTERSECTIONS,
            value = "Specifies whether to return intersecting polygons. " +
                    "CUSTOM_KEYS:{'apiDefault':false}")
    @JsonProperty(value = PARAM_INTERSECTIONS)
    private boolean intersections;
    @JsonIgnore
    private boolean hasIntersections = false;

    @ApiModelProperty(name = PARAM_ATTRIBUTES, value = "List of isochrones attributes",
            example = "[\"area\"]")
    @JsonProperty(PARAM_ATTRIBUTES)
    private IsochronesRequestEnums.Attributes[] attributes;
    @JsonIgnore
    private boolean hasAttributes = false;

    @ApiModelProperty(name = PARAM_INTERVAL, value = "Interval of isochrones or equidistants. This is only used if a single range value is given. " +
            "Value in **seconds** for time and **meters** for distance.",
            example = "30"
    )
    @JsonProperty(PARAM_INTERVAL)
    private Double interval;
    @JsonIgnore
    private boolean hasInterval = false;

    @ApiModelProperty(name = PARAM_SMOOTHING,
            value = "Applies a level of generalisation to the isochrone polygons generated as a `smoothing_factor` between `0` and `100.0`.\n" +
                    "Generalisation is produced by determining a maximum length of a connecting line between two points found on the outside of a containing polygon.\n" +
                    "If the distance is larger than a threshold value, the line between the two points is removed and a smaller connecting line between other points is used.\n" +
                    "Note that the minimum length of this connecting line is ~1333m, and so when the `smoothing_factor` results in a distance smaller than this, the minimum value is used.\n" +
                    "The threshold value is determined as `(maximum_radius_of_isochrone / 100) * smoothing_factor`.\n" +
                    "Therefore, a value closer to 100 will result in a more generalised shape.\n" +
                    "The polygon generation algorithm is based on Duckham and al. (2008) `\"Efficient generation of simple polygons for characterizing the shape of a set of points in the plane.\"`",
            example = "25")
    @JsonProperty(value = PARAM_SMOOTHING)
    private Double smoothing;
    @JsonIgnore
    private boolean hasSmoothing = false;

    @ApiModelProperty(name = PARAM_TIME, value = "Departure date and time provided in local time zone",
            example = "2020-01-31T12:45:00", hidden = true)
    @JsonProperty(PARAM_TIME)
    private LocalDateTime time;
    @JsonIgnore
    private boolean hasTime = false;

    @JsonIgnore
    private IsochroneMapCollection isoMaps;
    @JsonIgnore
    private IsochroneRequest isochroneRequest;

    @JsonCreator
    public IsochronesRequest() {
    }

    static String[] convertAttributes(IsochronesRequestEnums.Attributes[] attributes) {
        return convertAPIEnumListToStrings(attributes);
    }

    protected static int convertToIsochronesProfileType(APIEnums.Profile profile) throws ParameterValueException {
        try {
            int profileFromString = RoutingProfileType.getFromString(profile.toString());
            if (profileFromString == 0) {
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "profile");
            }
            return profileFromString;
        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "profile");
        }
    }

    public APIEnums.Units getAreaUnit() {
        return areaUnit;
    }

    public void setAreaUnit(APIEnums.Units areaUnit) {
        this.areaUnit = areaUnit;
        hasAreaUnits = true;
    }

    public boolean hasAreaUnits() {
        return hasAreaUnits;
    }

    public Double getSmoothing() {
        return smoothing;
    }

    public void setSmoothing(Double smoothing) {
        this.smoothing = smoothing;
        this.hasSmoothing = true;
    }

    public boolean hasSmoothing() {
        return hasSmoothing;
    }

    public APIEnums.RouteResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(APIEnums.RouteResponseType responseType) {
        this.responseType = responseType;
    }

    public boolean getIntersections() {
        return intersections;
    }

    public void setIntersections(Boolean intersections) {
        this.intersections = intersections;
        hasIntersections = true;
    }

    public boolean hasIntersections() {
        return hasIntersections;
    }

    public APIEnums.Units getRangeUnit() {
        return rangeUnit;
    }

    public void setRangeUnit(APIEnums.Units rangeUnit) {
        this.rangeUnit = rangeUnit;
        hasRangeUnits = true;
    }

    public boolean hasRangeUnits() {
        return hasRangeUnits;
    }

    public IsochronesRequestEnums.Attributes[] getAttributes() {
        return attributes;
    }

    public void setAttributes(IsochronesRequestEnums.Attributes[] attributes) {
        this.attributes = attributes;
        this.hasAttributes = true;
    }

    public boolean hasAttributes() {
        return hasAttributes;
    }

    public Double[][] getLocations() {
        return locations;
    }

    public void setLocations(Double[][] locations) {
        this.locations = locations;
        hasLocations = true;
    }

    public boolean hasLocations() {
        return hasLocations;
    }

    public IsochronesRequestEnums.LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(IsochronesRequestEnums.LocationType locationType) {
        this.locationType = locationType;
        hasLocationType = true;
    }

    public boolean hasLocationType() {
        return hasLocationType;
    }

    public RouteRequestOptions getIsochronesOptions() {
        return isochronesOptions;
    }

    public void setIsochronesOptions(RouteRequestOptions isochronesOptions) {
        this.isochronesOptions = isochronesOptions;
        this.hasOptions = true;
    }

    public boolean hasOptions() {
        return this.hasOptions;
    }

    public List<Double> getRange() {
        return range;
    }

    public void setRange(List<Double> range) {
        this.range = range;
        hasRange = true;
    }

    public boolean hasRange() {
        return hasRange;
    }

    public IsochronesRequestEnums.RangeType getRangeType() {
        return rangeType;
    }

    public void setRangeType(IsochronesRequestEnums.RangeType rangeType) {
        this.rangeType = rangeType;
        hasRangeType = true;
    }

    public boolean hasRangeType() {
        return hasRangeType;
    }

    public Double getInterval() {
        return interval;
    }

    public void setInterval(Double interval) {
        this.interval = interval;
        hasInterval = true;
    }

    public boolean hasInterval() {
        return hasInterval;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
        hasTime = true;
    }

    public boolean hasTime() {
        return hasTime;
    }

    public void generateIsochronesFromRequest() throws Exception {
        this.isochroneRequest = this.convertIsochroneRequest();
        // request object is built, now check if ors config allows all settings
        List<TravellerInfo> travellers = this.isochroneRequest.getTravellers();

        // TODO REFACTORING where should we put the validation code?
        validateAgainstConfig(this.isochroneRequest, travellers);

        if (!travellers.isEmpty()) {
            isoMaps = new IsochroneMapCollection();

            for (int i = 0; i < travellers.size(); ++i) {
                IsochroneSearchParameters searchParams = this.isochroneRequest.getSearchParameters(i);
                IsochroneMap isochroneMap = RoutingProfileManager.getInstance().buildIsochrone(searchParams);
                isoMaps.add(isochroneMap);
            }

        }
    }

    Float convertSmoothing(Double smoothingValue) throws ParameterValueException {
        float f = (float) smoothingValue.doubleValue();

        if (smoothingValue < 0 || smoothingValue > 100)
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_SMOOTHING, smoothingValue.toString());

        return f;
    }

    String convertLocationType(IsochronesRequestEnums.LocationType locationType) throws ParameterValueException {
        IsochronesRequestEnums.LocationType value;

        switch (locationType) {
            case DESTINATION:
                value = IsochronesRequestEnums.LocationType.DESTINATION;
                break;
            case START:
                value = IsochronesRequestEnums.LocationType.START;
                break;
            default:
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_LOCATION_TYPE, locationType.toString());
        }

        return value.toString();
    }

    TravelRangeType convertRangeType(IsochronesRequestEnums.RangeType rangeType) throws ParameterValueException {
        TravelRangeType travelRangeType;

        switch (rangeType) {
            case DISTANCE:
                travelRangeType = TravelRangeType.DISTANCE;
                break;
            case TIME:
                travelRangeType = TravelRangeType.TIME;
                break;
            default:
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_RANGE_TYPE, rangeType.toString());
        }

        return travelRangeType;

    }

    String convertAreaUnit(APIEnums.Units unitsIn) throws ParameterValueException {

        DistanceUnit convertedAreaUnit;
        try {
            convertedAreaUnit = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.UNKNOWN);
            if (convertedAreaUnit == DistanceUnit.UNKNOWN)
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_AREA_UNITS, unitsIn.toString());

            return DistanceUnitUtil.toString(convertedAreaUnit);

        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_AREA_UNITS, unitsIn.toString());
        }
    }

    String convertRangeUnit(APIEnums.Units unitsIn) throws ParameterValueException {

        DistanceUnit units;
        try {
            units = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.UNKNOWN);
            if (units == DistanceUnit.UNKNOWN)
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_RANGE_UNITS, unitsIn.toString());
        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_RANGE_UNITS, unitsIn.toString());
        }
        return DistanceUnitUtil.toString(units);

    }

    Coordinate convertSingleCoordinate(Double[] coordinate) throws ParameterValueException {
        Coordinate realCoordinate;
        if (coordinate.length != 2) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_LOCATIONS);
        }
        try {
            realCoordinate = new Coordinate(coordinate[0], coordinate[1]);
        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_LOCATIONS);
        }
        return realCoordinate;
    }

    IsochroneRequest convertIsochroneRequest() throws Exception {
        IsochroneRequest convertedIsochroneRequest = new IsochroneRequest();


        for (int i = 0; i < locations.length; i++) {
            Double[] location = locations[i];
            TravellerInfo travellerInfo = this.constructTravellerInfo(location);
            travellerInfo.setId(Integer.toString(i));
            try {
                convertedIsochroneRequest.addTraveller(travellerInfo);
            } catch (Exception ex) {
                throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, IsochronesRequest.PARAM_INTERVAL);
            }
        }
        if (this.hasId())
            convertedIsochroneRequest.setId(this.getId());
        if (this.hasRangeUnits())
            convertedIsochroneRequest.setUnits(convertRangeUnit(rangeUnit));
        if (this.hasAreaUnits())
            convertedIsochroneRequest.setAreaUnits(convertAreaUnit(areaUnit));
        if (this.hasAttributes())
            convertedIsochroneRequest.setAttributes(convertAttributes(attributes));
        if (this.hasSmoothing())
            convertedIsochroneRequest.setSmoothingFactor(convertSmoothing(smoothing));
        if (this.hasIntersections())
            convertedIsochroneRequest.setIncludeIntersections(intersections);
        if (this.hasOptions())
            convertedIsochroneRequest.setCalcMethod(convertCalcMethod(CONCAVE_BALLS));
        else
            convertedIsochroneRequest.setCalcMethod(convertCalcMethod(FASTISOCHRONE));
        return convertedIsochroneRequest;

    }

    TravellerInfo constructTravellerInfo(Double[] coordinate) throws Exception {
        TravellerInfo travellerInfo = new TravellerInfo();

        RouteSearchParameters routeSearchParameters = this.constructRouteSearchParameters();
        travellerInfo.setRouteSearchParameters(routeSearchParameters);
        if (this.hasRangeType())
            travellerInfo.setRangeType(convertRangeType(rangeType));
        if (this.hasLocationType())
            travellerInfo.setLocationType(convertLocationType(locationType));
        travellerInfo.setLocation(convertSingleCoordinate(coordinate));
        travellerInfo.getRanges();
        //range + interval
        if (range == null) {
            throw new ParameterValueException(IsochronesErrorCodes.MISSING_PARAMETER, IsochronesRequest.PARAM_RANGE);
        }
        List<Double> rangeValues = range;
        Double intervalValue = interval;
        setRangeAndIntervals(travellerInfo, rangeValues, intervalValue);
        return travellerInfo;
    }

    RouteSearchParameters constructRouteSearchParameters() throws Exception {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        int profileType;
        try {
            profileType = convertToIsochronesProfileType(this.getProfile());
        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_PROFILE);
        }

        if (profileType == RoutingProfileType.UNKNOWN)
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_PROFILE);
        routeSearchParameters.setProfileType(profileType);

        if (this.hasOptions()) {
            routeSearchParameters = this.processIsochronesRequestOptions(routeSearchParameters);
        }
        if (this.hasTime()) {
            routeSearchParameters.setDeparture(this.getTime());
            routeSearchParameters.setArrival(this.getTime());
        }
        routeSearchParameters.setConsiderTurnRestrictions(false);
        return routeSearchParameters;
    }

    RouteSearchParameters processIsochronesRequestOptions(RouteSearchParameters parameters) throws StatusCodeException {
        RouteRequestOptions options = isochronesOptions;
        parameters = this.processRequestOptions(options, parameters);
        if (options.hasProfileParams())
            parameters.setProfileParams(convertParameters(options, parameters.getProfileType()));
        return parameters;
    }

    void validateAgainstConfig(IsochroneRequest isochroneRequest, List<TravellerInfo> travellers) throws StatusCodeException {

        if (!IsochronesServiceSettings.getAllowComputeArea() && isochroneRequest.hasAttribute("area"))
            throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.FEATURE_NOT_SUPPORTED, "Area computation is not enabled.");

        if (travellers.size() > IsochronesServiceSettings.getMaximumLocations())
            throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, IsochronesRequest.PARAM_LOCATIONS, Integer.toString(travellers.size()), Integer.toString(IsochronesServiceSettings.getMaximumLocations()));

        for (TravellerInfo traveller : travellers) {
            int maxAllowedRange = IsochronesServiceSettings.getMaximumRange(traveller.getRouteSearchParameters().getProfileType(), isochroneRequest.getCalcMethod(), traveller.getRangeType());
            double maxRange = traveller.getMaximumRange();
            if (maxRange > maxAllowedRange)
                throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, IsochronesRequest.PARAM_RANGE, Double.toString(maxRange), Integer.toString(maxAllowedRange));

            int maxIntervals = IsochronesServiceSettings.getMaximumIntervals();
            if (maxIntervals > 0 && maxIntervals < traveller.getRanges().length) {
                throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MINIMUM, IsochronesRequest.PARAM_INTERVAL, "Resulting number of " + traveller.getRanges().length + " isochrones exceeds maximum value of " + maxIntervals + ".");
            }
        }

    }

    void setRangeAndIntervals(TravellerInfo travellerInfo, List<Double> rangeValues, Double intervalValue) throws ParameterValueException, ParameterOutOfRangeException {
        double rangeValue = -1;
        if (rangeValues.size() == 1) {
            try {
                rangeValue = rangeValues.get(0);
                travellerInfo.setRanges(new double[]{rangeValue});
            } catch (NumberFormatException ex) {
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "range");
            }
        } else {
            double[] ranges = new double[rangeValues.size()];
            double maxRange = Double.MIN_VALUE;
            for (int i = 0; i < ranges.length; i++) {
                double dv = rangeValues.get(i);
                if (dv > maxRange)
                    maxRange = dv;
                ranges[i] = dv;
            }
            Arrays.sort(ranges);
            travellerInfo.setRanges(ranges);
        }
        // interval, only use if one range is defined

        if (rangeValues.size() == 1 && rangeValue != -1 && intervalValue != null) {
            if (intervalValue > rangeValue) {
                throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, IsochronesRequest.PARAM_INTERVAL, Double.toString(intervalValue), Double.toString(rangeValue));
            }
            travellerInfo.setRanges(rangeValue, intervalValue);
        }
    }

    String convertCalcMethod(IsochronesRequestEnums.CalculationMethod bareCalcMethod) throws ParameterValueException {
        try {
            switch (bareCalcMethod) {
                case CONCAVE_BALLS:
                    return "concaveballs";
                case GRID:
                    return "grid";
                case FASTISOCHRONE:
                    return "fastisochrone";
                default:
                    return "none";
            }
        } catch (Exception ex) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "calc_method");
        }
    }

    public IsochroneMapCollection getIsoMaps() {
        return isoMaps;
    }

    public IsochroneRequest getIsochroneRequest() {
        return isochroneRequest;
    }
}
