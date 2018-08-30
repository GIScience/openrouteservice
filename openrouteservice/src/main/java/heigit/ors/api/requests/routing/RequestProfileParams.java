package heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "Profile Parameters", parent = RouteRequestOptions.class, description = "Specifies additional routing parameters.")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RequestProfileParams {
    @JsonProperty("weightings")
    private RequestProfileParamsWeightings weightings;
    @JsonIgnore
    private boolean hasWeightings = false;

    @JsonProperty("restrictions")
    private RequestProfileParamsRestrictions restrictions;
    @JsonIgnore
    private boolean hasRestrictions = false;

    public RequestProfileParamsWeightings getWeightings() {
        return weightings;
    }

    public void setWeightings(RequestProfileParamsWeightings weightings) {
        this.weightings = weightings;
        hasWeightings = true;
    }

    public RequestProfileParamsRestrictions getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(RequestProfileParamsRestrictions restrictions) {
        this.restrictions = restrictions;
        hasRestrictions = true;
    }

    public boolean hasWeightings() {
        return hasWeightings;
    }

    public boolean hasRestrictions() {
        return hasRestrictions;
    }
}
