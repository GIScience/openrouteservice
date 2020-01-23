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

package org.heigit.ors.api.responses.matrix.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.api.responses.matrix.MatrixResponse;
import org.heigit.ors.api.responses.matrix.MatrixResponseInfo;
import org.heigit.ors.matrix.MatrixResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The Matrix Response contains one matrix for each specified `metrics` value.")
public class JSONMatrixResponse extends MatrixResponse {
    public JSONMatrixResponse(MatrixResult result, MatrixRequest request) {
        super(result, request);
        responseInformation.setGraphDate(result.getGraphDate());
    }

    @JsonProperty("matrix")
    @JsonUnwrapped
    public JSONIndividualMatrixResponse getMatrix() {
        return new JSONIndividualMatrixResponse(matrixResult, matrixRequest);
    }

    @JsonProperty("metadata")
    @ApiModelProperty("Information about the service and request")
    public MatrixResponseInfo getInfo() {
        return responseInformation;
    }
}
