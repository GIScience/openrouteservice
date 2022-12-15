package org.heigit.ors.api.requests.centrality;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.Doubles;
import com.graphhopper.util.shapes.BBox;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.common.APIRequest;
import org.heigit.ors.centrality.CentralityErrorCodes;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.routing.RoutingProfileManager;

import java.util.List;

@ApiModel(value = "Centrality Service", description = "The JSON body request sent to the centrality service which defines options and parameters regarding the centrality measure to calculate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class CentralityRequest extends APIRequest {
    public static final String PARAM_BBOX = "bbox";
    public static final String PARAM_EXCLUDENODES = "excludeNodes";
    public static final String PARAM_MODE = "mode";
    public static final String PARAM_FORMAT = "format";

    @ApiModelProperty(name = PARAM_BBOX, value = "The bounding box to use for the request as an array of `longitude/latitude` pairs in WGS 84 (EPSG:4326)",
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

    public List<List<Double>> getBbox () {
        return bbox;
    }

    public void setBbox(List<List<Double>> bbox ) {
        this.bbox = bbox;
    }

    public List<Integer> getExcludeNodes() {return excludeNodes; }

    public void setExcludeNodes(List<Integer> excludeNodes ) {
        this.excludeNodes = excludeNodes;
        this.hasExcludeNodes = true;
    }

    public boolean hasExcludeNodes() {return hasExcludeNodes; }


    public void setResponseType(APIEnums.CentralityResponseType responseType) {
        this.responseType = responseType;
    }

    public CentralityRequestEnums.Mode getMode() { return mode; }

    public void setMode(CentralityRequestEnums.Mode mode) {
        this.mode = mode;
    }

    public CentralityResult generateCentralityFromRequest() throws StatusCodeException {
        org.heigit.ors.centrality.CentralityRequest centralityRequest = convertCentralityRequest();

        try {
            return RoutingProfileManager.getInstance().computeCentrality(centralityRequest);
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, CentralityErrorCodes.UNKNOWN);
        }
    }

    private org.heigit.ors.centrality.CentralityRequest convertCentralityRequest() throws StatusCodeException {
        org.heigit.ors.centrality.CentralityRequest centralityRequest = new org.heigit.ors.centrality.CentralityRequest();

        if  (this.hasId())
            centralityRequest.setId(this.getId());

        int profileType = -1;

        try {
            profileType = convertRouteProfileType(this.getProfile());
            centralityRequest.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(CentralityErrorCodes.INVALID_PARAMETER_VALUE, CentralityRequest.PARAM_PROFILE);
        }

        centralityRequest.setBoundingBox(convertBBox(this.getBbox()));

        centralityRequest.setMode(this.getMode().toString());

        if (this.hasExcludeNodes()) {
            centralityRequest.setExcludeNodes(this.getExcludeNodes());
        }

        return centralityRequest;
    }

    BBox convertBBox(List<List<Double>> coordinates) throws ParameterValueException {
        if (coordinates.size() != 2) {
            throw new ParameterValueException(CentralityErrorCodes.INVALID_PARAMETER_VALUE, CentralityRequest.PARAM_BBOX);
        }

        double[] coords = {};

        for (List<Double> coord : coordinates) {
            coords = Doubles.concat(coords, convertSingleCoordinate(coord));
        }

        return new BBox(coords);
    }

    private double[] convertSingleCoordinate(List<Double> coordinate) throws ParameterValueException {
        if (coordinate.size() != 2) {
            throw new ParameterValueException(CentralityErrorCodes.INVALID_PARAMETER_VALUE, CentralityRequest.PARAM_BBOX);
        }

        return new double[]{coordinate.get(0), coordinate.get(1)};
    }

}
