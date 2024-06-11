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
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.requests.common.APIRequest;
import org.heigit.ors.api.requests.routing.RouteRequestOptions;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.isochrones.IsochroneMapCollection;
import org.heigit.ors.isochrones.IsochroneRequest;
import org.heigit.ors.isochrones.IsochronesErrorCodes;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.routing.RoutingProfileType;

import java.time.LocalDateTime;
import java.util.List;

import static org.heigit.ors.api.services.ApiService.convertAPIEnumListToStrings;


@Schema(name = "IsochronesRequest", description = "The JSON body request sent to the isochrones service which defines options and parameters regarding the isochrones to generate.")
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

    @Schema(name = PARAM_LOCATIONS, description = "The locations to use for the route as an array of `longitude/latitude` pairs in WGS 84 (EPSG:4326)",
            example = "[[8.681495,49.41461],[8.686507,49.41943]]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(PARAM_LOCATIONS)
    private Double[][] locations = new Double[][]{};
    @JsonIgnore
    private boolean hasLocations = false;

    @Schema(name = PARAM_LOCATION_TYPE, description = "`start` treats the location(s) as starting point, `destination` as goal.",
            defaultValue = "start")
    @JsonProperty(value = PARAM_LOCATION_TYPE)
    private IsochronesRequestEnums.LocationType locationType;
    @JsonIgnore
    private boolean hasLocationType = false;

    @Schema(name = PARAM_RANGE, description = """
            Maximum range value of the analysis in **seconds** for time and **metres** for distance.\
            Alternatively a comma separated list of specific range values. Ranges will be the same for all locations.\
            """,
            example = "[ 300, 200 ]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(PARAM_RANGE)
    private List<Double> range;
    @JsonIgnore
    private boolean hasRange = false;

    @Schema(name = PARAM_RANGE_TYPE,
            description = "Specifies the isochrones reachability type.", defaultValue = "time")
    @JsonProperty(value = PARAM_RANGE_TYPE, defaultValue = "time")
    private IsochronesRequestEnums.RangeType rangeType;
    @JsonIgnore
    private boolean hasRangeType = false;

    // unit only valid for range_type distance, will be ignored for range_time time
    @Schema(name = PARAM_RANGE_UNITS,
            description = """
                    Specifies the distance units only if `range_type` is set to distance.
                    Default: m. \
                    """,
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "range_type"),
                    @ExtensionProperty(name = "value", value = "distance")}
            )},
            defaultValue = "m")
    @JsonProperty(value = PARAM_RANGE_UNITS)
    private APIEnums.Units rangeUnit;
    @JsonIgnore
    private boolean hasRangeUnits = false;

    @Schema(name = PARAM_OPTIONS,
            description = "Additional options for the isochrones request",
            example = "{\"avoid_borders\":\"all\"}")
    @JsonProperty(PARAM_OPTIONS)
    private RouteRequestOptions isochronesOptions;
    @JsonIgnore
    private boolean hasOptions = false;

    @Schema(hidden = true)
    private APIEnums.RouteResponseType responseType = APIEnums.RouteResponseType.GEOJSON;

    @Schema(name = PARAM_AREA_UNITS,
            description = """
                    Specifies the area unit.
                    Default: m. \
                    """,
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "attributes"),
                    @ExtensionProperty(name = "value", value = "area")}
            )},
            defaultValue = "m")
    @JsonProperty(value = PARAM_AREA_UNITS)
    private APIEnums.Units areaUnit;
    @JsonIgnore
    private boolean hasAreaUnits = false;

    @Schema(name = PARAM_INTERSECTIONS,
            description = "Specifies whether to return intersecting polygons. ", defaultValue = "false")
    @JsonProperty(value = PARAM_INTERSECTIONS)
    private boolean intersections;
    @JsonIgnore
    private boolean hasIntersections = false;

    @Schema(name = PARAM_ATTRIBUTES, description = "List of isochrones attributes",
            example = "[\"area\"]")
    @JsonProperty(PARAM_ATTRIBUTES)
    private IsochronesRequestEnums.Attributes[] attributes;
    @JsonIgnore
    private boolean hasAttributes = false;

    @Schema(name = PARAM_INTERVAL, description = """
            Interval of isochrones or equidistants. This is only used if a single range value is given. \
            Value in **seconds** for time and **meters** for distance.\
            """,
            example = "30"
    )
    @JsonProperty(PARAM_INTERVAL)
    private Double interval;
    @JsonIgnore
    private boolean hasInterval = false;

    @Schema(name = PARAM_SMOOTHING,
            description = """
                    Applies a level of generalisation to the isochrone polygons generated as a `smoothing_factor` between `0` and `100.0`.
                    Generalisation is produced by determining a maximum length of a connecting line between two points found on the outside of a containing polygon.
                    If the distance is larger than a threshold value, the line between the two points is removed and a smaller connecting line between other points is used.
                    Note that the minimum length of this connecting line is ~1333m, and so when the `smoothing_factor` results in a distance smaller than this, the minimum value is used.
                    The threshold value is determined as `(maximum_radius_of_isochrone / 100) * smoothing_factor`.
                    Therefore, a value closer to 100 will result in a more generalised shape.
                    The polygon generation algorithm is based on Duckham and al. (2008) `"Efficient generation of simple polygons for characterizing the shape of a set of points in the plane."`""",
            example = "25")
    @JsonProperty(value = PARAM_SMOOTHING)
    private Double smoothing;
    @JsonIgnore
    private boolean hasSmoothing = false;

    @Schema(name = PARAM_TIME, description = "Departure date and time provided in local time zone",
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

    public static String[] convertAttributes(IsochronesRequestEnums.Attributes[] attributes) {
        return convertAPIEnumListToStrings(attributes);
    }

    public static int convertToIsochronesProfileType(APIEnums.Profile profile) throws ParameterValueException {
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

    public IsochroneMapCollection getIsoMaps() {
        return isoMaps;
    }

    public void setIsoMaps(IsochroneMapCollection isoMaps) {
        this.isoMaps = isoMaps;
    }

    public IsochroneRequest getIsochroneRequest() {
        return isochroneRequest;
    }

    public void setIsochroneRequest(IsochroneRequest isochroneRequest) {
        this.isochroneRequest = isochroneRequest;
    }
}
