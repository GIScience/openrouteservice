package heigit.ors.api.requests.isochrones;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.routing.RouteRequestOptions;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.isochrones.IsochronesErrorCodes;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IsochronesRequestTraveller {
    @ApiModelProperty(name = "location", value = "The location to use for the route as an array of longitude/latitude pairs", example = "[[8.681495,49.41461],[8.686507,49.41943],[8.687872,49.420318]]")
    @JsonProperty("location")
    private Double[] location;

    @ApiModelProperty(hidden = true, required = true)
    private APIEnums.Profile profile;


    @ApiModelProperty(name = "id", value = "Set an id for the individual traveller")
    @JsonProperty("id")
    private String id;
    private boolean hasId = false;

    @ApiModelProperty(name = "options",
            value = "Additional options for the isochrones request")
    @JsonProperty("options")
    private RouteRequestOptions isochronesOptions = new RouteRequestOptions();

    @JsonIgnore
    private boolean hasIsochronesOptions = false;

    @ApiModelProperty(name = "range_type",
            value = "Specifies the isochrones reachability type")
    @JsonProperty(value = "range_type", defaultValue = "time")
    private IsochronesRequestEnums.RangeType rangeType = IsochronesRequestEnums.RangeType.TIME;

    @ApiModelProperty(name = "location_type",
            value = "Start treats the location(s) as starting point, destination as goal. " +
                    "Has an influence in mountainous areas"
    )
    @JsonProperty(value = "location_type", defaultValue = "start")
    private IsochronesRequestEnums.LocationType locationType = IsochronesRequestEnums.LocationType.START;

    @ApiModelProperty(name = "range", value = "Maximum range value of the analysis in seconds for time and meters for distance." +
            "Alternatively a comma separated list of specific single range values if more than one location is set.",
            example = "[ 300, 200 ]"
    )
    @JsonProperty("range")
    private List<Double> range;

    @ApiModelProperty(name = "interval", value = "Interval of isochrones or equidistants for one range value. " +
            "value in seconds for time and meters for distance.",
            example = "30"
    )
    @JsonProperty("interval")
    private Double interval;


    public IsochronesRequestTraveller(@JsonProperty(value = "location", required = true) Double[] location) throws ParameterValueException {
        if (location.length != 2)
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "location");
        this.location = location;
    }

    public Double[] getLocation() {
        return location;
    }

    public void setLocation(Double[] location) {
        this.location = location;
    }

    public APIEnums.Profile getProfile() {
        return profile;
    }

    public void setProfile(APIEnums.Profile profile) {
        this.profile = profile;
    }

    public IsochronesRequestEnums.RangeType getRangeType() {
        return rangeType;
    }

    public void setRangeType(IsochronesRequestEnums.RangeType rangeType) {
        this.rangeType = rangeType;
    }

    public IsochronesRequestEnums.LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(IsochronesRequestEnums.LocationType locationType) {
        this.locationType = locationType;
    }

    public List<Double> getRange() {
        return range;
    }

    public void setRange(List<Double> range) {
        this.range = range;
    }

    public Double getInterval() {
        return interval;
    }

    public void setInterval(Double interval) {
        this.interval = interval;
    }

    public RouteRequestOptions getIsochronesOptions() {
        return isochronesOptions;
    }

    public void setIsochronesOptions(RouteRequestOptions isochronesOptions) {
        this.isochronesOptions = isochronesOptions;
        this.hasIsochronesOptions = true;
    }


    public boolean hasIsochronesOptions() {
        return hasIsochronesOptions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.hasId = true;
        this.id = id;
    }

    public boolean hasId() {
        return hasId;
    }

}
