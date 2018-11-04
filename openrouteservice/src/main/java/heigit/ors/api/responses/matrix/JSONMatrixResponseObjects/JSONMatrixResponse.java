package heigit.ors.api.responses.matrix.JSONMatrixResponseObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.matrix.MatrixRequest;
import heigit.ors.api.responses.matrix.IndividualMatrixResponse;
import heigit.ors.api.responses.matrix.MatrixResponse;
import heigit.ors.api.responses.matrix.MatrixResponseInfo;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.matrix.MatrixResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;

@ApiModel(value = "JSONMatrixResponse")
public class JSONMatrixResponse extends MatrixResponse {
    public JSONMatrixResponse(MatrixResult[] matrixResults, MatrixRequest request) throws StatusCodeException {
        super(request);
        //TODO
        this.matrixResults = new ArrayList<IndividualMatrixResponse>();
        for(MatrixResult result: matrixResults){
            this.matrixResults.add(new JSONIndividualMatrixResponse(result, request));
        }
    }
    @JsonProperty("matrix")
    @ApiModelProperty(value = "A list of matrix calculatinos returned from the request")
    public JSONIndividualMatrixResponse[] getRoutes() {
        return (JSONIndividualMatrixResponse[]) matrixResults.toArray(new JSONIndividualMatrixResponse[matrixResults.size()]);
    }
    @JsonProperty("info")
    @ApiModelProperty("Information about the service and request")
    public MatrixResponseInfo getInfo() {
        return responseInformation;
    }
}
