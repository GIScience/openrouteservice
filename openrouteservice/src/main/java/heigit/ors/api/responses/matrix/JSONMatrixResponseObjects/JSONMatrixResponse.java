package heigit.ors.api.responses.matrix.JSONMatrixResponseObjects;

import heigit.ors.api.requests.matrix.SpringMatrixRequest;
import heigit.ors.api.responses.matrix.MatrixResponse;
import heigit.ors.matrix.MatrixResult;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "JSONMatrixResponse")
public class JSONMatrixResponse extends MatrixResponse {
    public JSONMatrixResponse(MatrixResult[] matrixResults, SpringMatrixRequest request) {
        //TODO

    }
}
