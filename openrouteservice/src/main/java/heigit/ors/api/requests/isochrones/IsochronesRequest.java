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


/*
  The general idea is that a request can consist of multiple travellers whom can have their own options.
  Every traveller will only accept one coordinate pair in contrary to the solution before.
  To calculate isochrones for more than one coordinate users can easily add a new traveller with the desired coordinates.
  That way users have more flexibility in defining their isochrones even more individually.
  **/
@ApiModel(value = "IsochronesRequest", description = "The JSON body request sent to the isochrones service which defines options and parameters regarding the isochrones to generate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class IsochronesRequest {

    @ApiModelProperty(value = "Arbitrary identification string of the request reflected in the meta information.")
    private String id;
    private boolean hasId = false;

    @ApiModelProperty(name = "location", value = "The location to use for the route as an array of longitude/latitude pairs", example = "[[8.681495,49.41461],[8.686507,49.41943],[8.687872,49.420318]]")
    @JsonProperty("location")
    private Double[][] location;

    @JsonProperty(value = "location_type", defaultValue = "start")
    private IsochronesRequestEnums.LocationType locationType = IsochronesRequestEnums.LocationType.START;

    @ApiModelProperty(hidden = true, required = true)
    private APIEnums.Profile profile;

    @ApiModelProperty(name = "options",
            value = "Additional options for the isochrones request")
    @JsonProperty("options")
    private RouteRequestOptions isochronesOptions;


    @ApiModelProperty(hidden = true)
    private APIEnums.RouteResponseType responseType = APIEnums.RouteResponseType.GEOJSON;

    @ApiModelProperty(name = "range", value = "Maximum range value of the analysis in seconds for time and meters for distance." +
            "Alternatively a comma separated list of specific single range values if more than one location is set.",
            example = "[ 300, 200 ]"
    )
    @JsonProperty("range")
    private List<Double> range;

    @ApiModelProperty(name = "range_type",
            value = "Specifies the isochrones reachability type")
    @JsonProperty(value = "range_type", defaultValue = "time")
    private IsochronesRequestEnums.RangeType rangeType = IsochronesRequestEnums.RangeType.TIME;

    // unit only valid for range_type distance, will be ignored for range_time time
    @ApiModelProperty(name = "units",
            value = "Specifies the distance units only if range_type is set to distance.\n" +
                    "Default: m.")
    @JsonProperty(value = "units", defaultValue = "m")
    private APIEnums.Units rangeUnit = APIEnums.Units.METRES;

    @ApiModelProperty(name = "area_unit",
            value = "Specifies the area unit.\n" +
                    "Default: m.")
    @JsonProperty(value = "area_unit", defaultValue = "m")
    private APIEnums.Units areaUnit = APIEnums.Units.METRES;

    @ApiModelProperty(name = "calc_method",
            value = "Specifies the calculation method. ConcaveBalls or Grid")
    @JsonProperty(value = "calc_method", defaultValue = "ConcaveBalls")
    private IsochronesRequestEnums.CalculationMethod calcMethod = IsochronesRequestEnums.CalculationMethod.CONCAVE_BALLS;

    @ApiModelProperty(name = "intersections",
            value = "Specifies whether to return intersecting polygons")
    @JsonProperty(value = "intersections", defaultValue = "false")
    private Boolean intersections = false;

    @ApiModelProperty(name = "attributes", value = "List of isochrones attributes")
    @JsonProperty("attributes")
    private IsochronesRequestEnums.Attributes[] attributes;

    @ApiModelProperty(name = "interval", value = "Interval of isochrones or equidistants for one range value. " +
            "value in seconds for time and meters for distance.",
            example = "30"
    )
    @JsonProperty("interval")
    private Double interval;


    @JsonIgnore
    private boolean hasAttributes = false;


    @JsonIgnore
    private boolean hasIsochronesOptions = false;

    @ApiModelProperty(name = "smoothing",
            value = "Applies a level of generalisation to the isochrone polygons generated as a smoothing_factor between 0 and 1.0.\n" +
                    "Generalisation is produced by determining a maximum length of a connecting line between two points found on the outside of a containing polygon.\n" +
                    "If the distance is larger than a threshold value, the line between the two points is removed and a smaller connecting line between other points is used.\n" +
                    "The threshold value is determined as (smoothing_factor * maximum_radius_of_isochrone) / 10.\n" +
                    "Therefore, a value closer to 1 will result in a more generalised shape.\n" +
                    "The polygon generation algorithm is based on Duckham and al. (2008) \"Efficient generation of simple polygons for characterizing the shape of a set of points in the plane.\"")
    @JsonProperty(value = "smoothing", defaultValue = "None")
    private Double smoothing;

    @JsonIgnore
    private boolean hasSmoothing = false;

    @JsonCreator
    public IsochronesRequest() {
    }

    @JsonCreator
    public IsochronesRequest(@JsonProperty(value = "location", required = true) Double[][] locations) throws ParameterValueException {
        int maximumLocations = IsochronesServiceSettings.getMaximumLocations();
        if (locations.length > maximumLocations)
            throw new ParameterValueException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "location");
        if (locations.length < 1)
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "location");
        for (Double[] location : locations) {
            if (location.length != 2)
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "location");
        }
        this.location = locations;
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

    public void setIntersection(Boolean intersections) {
        this.intersections = intersections;
    }

    public APIEnums.Units getRangeUnits() {
        return rangeUnit;
    }

    public void setRangeUnits(APIEnums.Units rangeUnit) {
        this.rangeUnit = rangeUnit;
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

    public IsochronesRequestEnums.CalculationMethod getCalcMethod() {
        return calcMethod;
    }

    public void setCalcMethod(IsochronesRequestEnums.CalculationMethod calcMethod) {
        this.calcMethod = calcMethod;
    }

    public Double[][] getLocation() {
        return location;
    }

    public void setLocation(Double[][] location) {
        this.location = location;
    }

    public IsochronesRequestEnums.LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(IsochronesRequestEnums.LocationType locationType) {
        this.locationType = locationType;
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
        this.hasIsochronesOptions = true;
    }

    public boolean hasIsochronesOptions() {
        return this.hasIsochronesOptions;
    }

    public List<Double> getRange() {
        return range;
    }

    public void setRange(List<Double> range) {
        this.range = range;
    }

    public IsochronesRequestEnums.RangeType getRangeType() {
        return rangeType;
    }

    public void setRangeType(IsochronesRequestEnums.RangeType rangeType) {
        this.rangeType = rangeType;
    }

    public Double getInterval() {
        return interval;
    }

    public void setInterval(Double interval) {
        this.interval = interval;
    }


}
