package org.heigit.ors.api.requests.snapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.requests.common.APIRequest;
import org.heigit.ors.api.APIEnums;

import java.util.List;

@Schema(name = "SnappingRequest", description = "Snapping service endpoint.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class SnappingApiRequest extends APIRequest {
    public static final String PARAM_PROFILE = "profile";
    public static final String PARAM_LOCATIONS = "locations";
    public static final String PARAM_MAXIMUM_SEARCH_RADIUS = "radius";
    public static final String PARAM_FORMAT = "format";

    @Schema(name = PARAM_PROFILE, hidden = true)
    private APIEnums.Profile profile;

    @Schema(name = PARAM_LOCATIONS, description = "The locations to be snapped as array of `longitude/latitude` pairs.",
            example = "[[8.669629,49.413025],[8.675841,49.418532],[8.665144,49.415594]]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(PARAM_LOCATIONS)
    private List<List<Double>> locations; //apparently, this has to be a non-primitive type…

    @Schema(name = PARAM_FORMAT, hidden = true)
    @JsonProperty(PARAM_FORMAT)
    private APIEnums.SnappingResponseType responseType = APIEnums.SnappingResponseType.JSON;

    @Schema(name = PARAM_MAXIMUM_SEARCH_RADIUS, description = "Maximum radius in meters around given coordinates to search for graph edges.",
        example ="350", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(PARAM_MAXIMUM_SEARCH_RADIUS)
    private double maximumSearchRadius = 350;//TODO: allow profile-specific value set in config file, see #1798

    @JsonCreator
    public SnappingApiRequest(@JsonProperty(value = PARAM_LOCATIONS, required = true) List<List<Double>> locations) {
        this.locations = locations;
    }

    public APIEnums.Profile getProfile() {
        return profile;
    }

    public void setProfile(APIEnums.Profile profile) {
        this.profile = profile;
    }

    public double getMaximumSearchRadius() {
        return maximumSearchRadius;
    }

    public List<List<Double>> getLocations() {
        return locations;
    }

    public void setLocations(List<List<Double>> locations) {
        this.locations = locations;
    }

    public void setResponseType(APIEnums.SnappingResponseType responseType) {
        this.responseType = responseType;
    }
}
