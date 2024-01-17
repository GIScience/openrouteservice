package org.heigit.ors.api.requests.export;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.requests.common.APIRequest;
import org.heigit.ors.api.APIEnums;

import java.util.List;

@Schema(title = "Graph export Service", name = "graphExportService", description = "Graph export service endpoint.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ExportRequest extends APIRequest {
    public static final String PARAM_ID = "id";
    public static final String PARAM_BBOX = "bbox";
    public static final String PARAM_PROFILE = "profile";
    public static final String PARAM_FORMAT = "format";

    public static final String PARAM_DEBUG = "debug";

    @Schema(name = PARAM_ID, description = "Arbitrary identification string of the request reflected in the meta information.",
            example = "export_request")
    @JsonProperty(PARAM_ID)
    private String id;
    @JsonIgnore
    private boolean hasId = false;

    @Schema(name = PARAM_PROFILE, hidden = true)
    private APIEnums.Profile profile;

    @Schema(name = PARAM_BBOX, description = "The bounding box to use for the request as an array of `longitude/latitude` pairs",
            example = "[[8.681495,49.41461],[8.686507,49.41943]]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(PARAM_BBOX)
    private List<List<Double>> bbox; //apparently, this has to be a non-primitive type…

    @Schema(name = PARAM_FORMAT, hidden = true)
    @JsonProperty(PARAM_FORMAT)
    private APIEnums.ExportResponseType responseType = APIEnums.ExportResponseType.JSON;

    @Schema(name = PARAM_DEBUG, hidden = true)
    @JsonProperty(PARAM_DEBUG)
    private boolean debug;

    @JsonCreator
    public ExportRequest(@JsonProperty(value = PARAM_BBOX, required = true) List<List<Double>> bbox) {
        this.bbox = bbox;
    }

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

    public boolean debug() {
        return debug;
    }

    public List<List<Double>> getBbox() {
        return bbox;
    }

    public void setBbox(List<List<Double>> bbox) {
        this.bbox = bbox;
    }

    public APIEnums.Profile getProfile() {
        return profile;
    }

    public void setProfile(APIEnums.Profile profile) {
        this.profile = profile;
    }

    public void setResponseType(APIEnums.ExportResponseType responseType) {
        this.responseType = responseType;
    }

}
