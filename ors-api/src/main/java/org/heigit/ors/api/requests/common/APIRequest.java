package org.heigit.ors.api.requests.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.APIEnums;

public class APIRequest {
    public static final String PARAM_ID = "id";
    public static final String PARAM_PROFILE = "profile";

    @Schema(name = PARAM_ID, description = "Arbitrary identification string of the request reflected in the meta information.",
            example = "my_request")
    @JsonProperty(PARAM_ID)
    protected String id;
    @JsonIgnore
    private boolean hasId = false;

    @Schema(name = PARAM_PROFILE, hidden = true)
    protected APIEnums.Profile profile;

    public boolean hasId() {
        return hasId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.hasId = true;
    }

    public APIEnums.Profile getProfile() {
        return profile;
    }

    public void setProfile(APIEnums.Profile profile) {
        this.profile = profile;
    }

}
