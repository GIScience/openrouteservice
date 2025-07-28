package org.heigit.ors.api.requests.matching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.api.requests.common.APIRequest;

@Schema(name = "MatchingRequest", description = "Matching service endpoint.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class MatchingApiRequest extends APIRequest {
    public static final String PARAM_PROFILE = "profile";
    public static final String PARAM_FOO = "foo";

    @Schema(name = PARAM_PROFILE, hidden = true)

    @Getter
    @Setter
    private APIEnums.Profile profile;

    @Schema(name = PARAM_FOO)

    @Getter
    @Setter
    private String foo;

    @JsonCreator
    public MatchingApiRequest() { }


}
