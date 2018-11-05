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
import heigit.ors.api.requests.matrix.MatrixRequest;
import heigit.ors.api.responses.matrix.MatrixResponse;
import heigit.ors.api.responses.matrix.MatrixResponseInfo;
import heigit.ors.matrix.MatrixResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;

@ApiModel(value = "JSONMatrixResponse")
public class JSONMatrixResponse extends MatrixResponse {
    public JSONMatrixResponse(MatrixResult[] matrixResults, MatrixRequest request) {
        super(request);
        this.matrixResults = new ArrayList<JSONLocation>();
        for (MatrixResult result : matrixResults) {
            this.matrixResults.add(new JSONIndividualMatrixResponse(result, request));
        }
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
