package org.heigit.ors.api.requests.centrality;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.requests.common.APIEnums;

import java.util.List;

@ApiModel(value = "Centrality Service", description = "The JSON body request sent to the centrality service which defines options and parameters regarding the centrality measure to calculate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class CentralityRequest {
    public static final String PARAM_ID = "id";
    public static final String PARAM_BBOX = "bbox";
    public static final String PARAM_EXCLUDENODES = "excludeNodes";
    public static final String PARAM_MODE = "mode";
    public static final String PARAM_PROFILE = "profile";
    public static final String PARAM_FORMAT = "format";

    @ApiModelProperty(name = PARAM_ID, value = "Arbitrary identification string of the request reflected in the meta information.",
            example = "routing_request")
    @JsonProperty(PARAM_ID)
    private String id;
    @JsonIgnore
    private boolean hasId = false;

    @ApiModelProperty(name = PARAM_PROFILE, hidden = true)
    private APIEnums.Profile profile;

    @ApiModelProperty(name = PARAM_BBOX, value = "The bounding box to use for the request as an array of `longitude/latitude` pairs",
            example = "[8.681495,49.41461,8.686507,49.41943]",
            required = true)
    @JsonProperty(PARAM_BBOX)
    private List<List<Double>> bbox; //apparently, this has to be a non-primitive type…

    @ApiModelProperty(name = PARAM_EXCLUDENODES, value = "List of node Ids to exclude when calculating centrality",
            example = "[1661, 1662, 1663]")
    @JsonProperty(PARAM_EXCLUDENODES)
    private List<Integer> excludeNodes;
    private boolean hasExcludeNodes = false;

    @ApiModelProperty(name = PARAM_FORMAT, hidden = true)
    @JsonProperty(PARAM_FORMAT)
    private APIEnums.CentralityResponseType responseType = APIEnums.CentralityResponseType.JSON;

     @ApiModelProperty(name = PARAM_MODE, value = "Specifies the centrality calculation mode. Currently, node-based and edge-based centrality calculation is supported.", example = "nodes")
    @JsonProperty(PARAM_MODE)
    private CentralityRequestEnums.Mode mode = CentralityRequestEnums.Mode.NODES;

    @JsonCreator
    public CentralityRequest(@JsonProperty(value = PARAM_BBOX, required = true) List<List<Double>> bbox) {
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

    public List<List<Double>> getBoundingBox() {
        return bbox;
    }

    public void setBoundingBox(List<List<Double>> bbox ) {
        this.bbox = bbox;
    }

    public List<Integer> getExcludeNodes() {return excludeNodes; }

    public void setExcludeNodes(List<Integer> excludeNodes ) {
        this.excludeNodes = excludeNodes;
        this.hasExcludeNodes = true;
    }

    public boolean hasExcludeNodes() {return hasExcludeNodes; }

    public APIEnums.Profile getProfile() {
        return profile;
    }

    public void setProfile(APIEnums.Profile profile) {
        this.profile = profile;
    }

    public void setResponseType(APIEnums.CentralityResponseType responseType) {
        this.responseType = responseType;
    }

    public CentralityRequestEnums.Mode getMode() { return mode; }

    public void setMode(CentralityRequestEnums.Mode mode) {
        this.mode = mode;
    }


}
