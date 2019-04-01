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

package heigit.ors.api.requests.isochrones;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.routing.RouteRequestOptions;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.isochrones.IsochronesErrorCodes;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;


@ApiModel(value = "IsochronesRequest", description = "The JSON body request sent to the isochrones service which defines options and parameters regarding the isochrones to generate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class IsochronesRequest {
    public static final String PARAM_ID = "id";
    public static final String PARAM_PROFILE = "profile";
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

    @ApiModelProperty(name = PARAM_ID,
            value = "Arbitrary identification string of the request reflected in the meta information.",
            example = "isochrones_request")
    @JsonProperty(PARAM_ID)
    private String id;
    @JsonIgnore
    private boolean hasId = false;

    @ApiModelProperty(name = PARAM_LOCATIONS, value = "The locations to use for the route as an array of `longitude/latitude` pairs",
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
            "Alternatively a comma separated list of specific single range values if more than one location is set.",
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

    @ApiModelProperty(name = PARAM_PROFILE, hidden = true, required = true)
    @JsonIgnore
    private APIEnums.Profile profile;

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
    private Boolean intersections;
    @JsonIgnore
    private boolean hasIntersections = false;

    @ApiModelProperty(name = PARAM_ATTRIBUTES, value = "List of isochrones attributes",
            example = "[\"area\"]")
    @JsonProperty(PARAM_ATTRIBUTES)
    private IsochronesRequestEnums.Attributes[] attributes;
    @JsonIgnore
    private boolean hasAttributes = false;

    @ApiModelProperty(name = PARAM_INTERVAL, value = "Interval of isochrones or equidistants for one range value. " +
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

    @JsonCreator
    public IsochronesRequest() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.hasId = true;
    }

    public boolean hasId() {
        return hasId;
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

    public Boolean getIntersections() {
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

    public APIEnums.Profile getProfile() {
        return profile;
    }

    public void setProfile(APIEnums.Profile profile) {
        this.profile = profile;
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
}
