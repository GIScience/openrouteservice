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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
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

    @ApiModelProperty(hidden = true)
    private APIEnums.RouteResponseType responseType = APIEnums.RouteResponseType.GEOJSON;

    @ApiModelProperty(value = "List with the traveller objects", name = "travellers")
    private List<IsochronesRequestTraveller> travellers = new ArrayList<>();

    // unit only valid for range_type distance, will be ignored for range_time time
    @ApiModelProperty(name = "range_unit",
            value = "Specifies the distance unit if range_type is set to distance.\n" +
                    "Default: m.")
    @JsonProperty(value = "range_unit", defaultValue = "m")
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

    @JsonIgnore
    private boolean hasAttributes = false;


    @ApiModelProperty(name = "smoothing",
            value = "Applies a level of generalisation to the isochrone polygons generated as a smoothing_factor between 0 and 1.0.\n" +
                    "Generalisation is produced by determining a maximum length of a connecting line between two points found on the outside of a containing polygon.\n" +
                    "If the distance is larger than a threshold value, the line between the two points is removed and a smaller connecting line between other points is used.\n" +
                    "The threshold value is determined as (smoothing_factor * maximum_radius_of_isochrone) / 10.\n" +
                    "Therefore, a value closer to 1 will result in a more generalised shape.\n" +
                    "The polygon generation algorithm is based on Duckham and al. (2008) \"Efficient generation of simple polygons for characterizing the shape of a set of points in the plane.\"")
    @JsonProperty(value = "smoothing", defaultValue = "false")
    private Double smoothing;
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

    public List<IsochronesRequestTraveller> getTravellers() {
        return travellers;
    }

    public void setTravellers(List<IsochronesRequestTraveller> travellers) {
        this.travellers = travellers;
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
}



