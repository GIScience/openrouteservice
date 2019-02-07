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

package heigit.ors.api.responses.matrix;

import com.fasterxml.jackson.annotation.JsonIgnore;
import heigit.ors.api.requests.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;

import java.util.List;

public class MatrixResponse {
    @JsonIgnore
    protected MatrixResponseInfo responseInformation;
    @JsonIgnore
    protected MatrixResult matrixResult;
    @JsonIgnore
    protected MatrixRequest matrixRequest;

    public MatrixResponse(MatrixResult result, MatrixRequest request) {
        this.matrixResult = result;
        this.matrixRequest = request;
        responseInformation = new MatrixResponseInfo(request);
    }

    public MatrixResponseInfo getResponseInformation() {
        return responseInformation;
    }

    public MatrixResult getMatrixResult() {
        return matrixResult;
    }

    public MatrixRequest getMatrixRequest() {
        return matrixRequest;
    }
}
