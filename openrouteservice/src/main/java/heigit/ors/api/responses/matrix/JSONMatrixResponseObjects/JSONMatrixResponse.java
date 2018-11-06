/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.api.responses.matrix.JSONMatrixResponseObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.responses.matrix.MatrixResponse;
import heigit.ors.api.responses.matrix.MatrixResponseInfo;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "JSONMatrixResponse")
public class JSONMatrixResponse extends MatrixResponse {
    public JSONMatrixResponse(List<MatrixResult> matrixResults, List<MatrixRequest> matrixRequests, heigit.ors.api.requests.matrix.MatrixRequest originalRequest) {
        super(originalRequest);
        this.matrixResults = new ArrayList<JSONLocation>();
        this.matrixResults.add(constructCombindedMatrixResponse(matrixResults, matrixRequests));
    }

    private JSONIndividualMatrixResponse constructCombindedMatrixResponse(List<MatrixResult> matrixResults, List<MatrixRequest> matrixRequests) {
        JSONIndividualMatrixResponse combinedJSONIndividualMatrixResponse = null;
        for (int i = 0; i < matrixResults.size(); i++) {
            JSONIndividualMatrixResponse jsonIndividualMatrixResponse = new JSONIndividualMatrixResponse(matrixResults.get(i), matrixRequests.get(i));
            Double[] durations = jsonIndividualMatrixResponse.getDurations();
            Double[] distances = jsonIndividualMatrixResponse.getDistances();
            Double[] weights = jsonIndividualMatrixResponse.getWeights();
            if (combinedJSONIndividualMatrixResponse == null) {
                combinedJSONIndividualMatrixResponse = new JSONIndividualMatrixResponse(matrixRequests.get(i));
                combinedJSONIndividualMatrixResponse.setDestinations(jsonIndividualMatrixResponse.getDestinations());
                combinedJSONIndividualMatrixResponse.setSources(jsonIndividualMatrixResponse.getSources());
            }
            if (durations != null && durations.length > 0) {
                combinedJSONIndividualMatrixResponse.setDurations(durations);
            }
            if (distances != null && distances.length > 0) {
                combinedJSONIndividualMatrixResponse.setDistances(distances);
            }
            if (weights != null && weights.length > 0) {
                combinedJSONIndividualMatrixResponse.setWeights(weights);
            }
        }
        return combinedJSONIndividualMatrixResponse;
    }

    @JsonProperty("matrix")
    @ApiModelProperty(value = "A list of matrix calculations returned from the request")
    public JSONIndividualMatrixResponse[] getRoutes() {
        return (JSONIndividualMatrixResponse[]) matrixResults.toArray(new JSONIndividualMatrixResponse[0]);
    }

    @JsonProperty("info")
    @ApiModelProperty("Information about the service and request")
    public MatrixResponseInfo getInfo() {
        return responseInformation;
    }
}
