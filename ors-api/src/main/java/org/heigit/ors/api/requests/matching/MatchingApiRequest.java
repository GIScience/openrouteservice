package org.heigit.ors.api.requests.matching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.api.requests.common.APIRequest;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.simpleframework.xml.core.Validate;
import org.springframework.validation.annotation.Validated;

@Schema(name = "MatchingRequest", description = "Matching service endpoint.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class MatchingApiRequest extends APIRequest {
    public static final String PARAM_PROFILE = "profile";
    public static final String PARAM_KEY = "key";
    public static final String PARAM_FEATURES = "features";

    @Schema(name = PARAM_KEY)
    @Getter
    @Setter
    private String key;

    @Schema(name = PARAM_FEATURES)
    @Getter
    @Setter
    private JSONObject features = new JSONObject();

    @JsonCreator
    public MatchingApiRequest() {
    }


}
