package org.heigit.ors.api.requests.export;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.Doubles;
import com.graphhopper.util.shapes.BBox;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.common.APIRequest;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.export.ExportErrorCodes;
import org.heigit.ors.export.ExportResult;
import org.heigit.ors.routing.RoutingProfileManager;

import java.util.List;

@ApiModel(value = "Centrality Service", description = "The JSON body request sent to the centrality service which defines options and parameters regarding the centrality measure to calculate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ExportRequest extends APIRequest {
    public static final String PARAM_ID = "id";
    public static final String PARAM_BBOX = "bbox";
    public static final String PARAM_PROFILE = "profile";
    public static final String PARAM_FORMAT = "format";

    public static final String PARAM_DEBUG = "debug";

    @ApiModelProperty(name = PARAM_ID, value = "Arbitrary identification string of the request reflected in the meta information.",
            example = "centrality_request")
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

    @ApiModelProperty(name = PARAM_FORMAT, hidden = true)
    @JsonProperty(PARAM_FORMAT)
    private APIEnums.CentralityResponseType responseType = APIEnums.CentralityResponseType.JSON;

    @ApiModelProperty(name = PARAM_DEBUG, hidden = true)
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

    public List<List<Double>> getBbox () {
        return bbox;
    }

    public void setBbox(List<List<Double>> bbox ) {
        this.bbox = bbox;
    }

    public APIEnums.Profile getProfile() {
        return profile;
    }

    public void setProfile(APIEnums.Profile profile) {
        this.profile = profile;
    }

    public void setResponseType(APIEnums.CentralityResponseType responseType) {
        this.responseType = responseType;
    }

    public ExportResult generateExportFromRequest() throws StatusCodeException {
        org.heigit.ors.export.ExportRequest exportRequest = this.convertExportRequest();

        try {
            return RoutingProfileManager.getInstance().computeExport(exportRequest);
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, ExportErrorCodes.UNKNOWN);
        }
    }

    private org.heigit.ors.export.ExportRequest convertExportRequest() throws StatusCodeException {
        org.heigit.ors.export.ExportRequest exportRequest = new org.heigit.ors.export.ExportRequest();

        if  (this.hasId())
            exportRequest.setId(this.getId());

        int profileType = -1;

        try {
            profileType = convertRouteProfileType(this.getProfile());
            exportRequest.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(ExportErrorCodes.INVALID_PARAMETER_VALUE, ExportRequest.PARAM_PROFILE);
        }

        exportRequest.setBoundingBox(convertBBox(this.getBbox()));
        exportRequest.setDebug(debug);

        return exportRequest;
    }

    BBox convertBBox(List<List<Double>> coordinates) throws ParameterValueException {
        if (coordinates.size() != 2) {
            throw new ParameterValueException(ExportErrorCodes.INVALID_PARAMETER_VALUE, ExportRequest.PARAM_BBOX);
        }

        double[] coords = {};

        for (List<Double> coord : coordinates) {
            coords = Doubles.concat(coords, convertSingleCoordinate(coord));
        }

        return new BBox(coords);
    }

    private double[] convertSingleCoordinate(List<Double> coordinate) throws ParameterValueException {
        if (coordinate.size() != 2) {
            throw new ParameterValueException(ExportErrorCodes.INVALID_PARAMETER_VALUE, ExportRequest.PARAM_BBOX);
        }

        return new double[]{coordinate.get(0), coordinate.get(1)};
    }

}
