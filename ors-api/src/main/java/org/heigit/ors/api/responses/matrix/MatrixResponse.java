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

package org.heigit.ors.api.responses.matrix;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.heigit.ors.api.config.EndpointsProperties;
import org.heigit.ors.api.config.SystemMessageProperties;
import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.matrix.MatrixResult;

public class MatrixResponse {
    @JsonIgnore
    protected MatrixResponseInfo responseInformation;
    @JsonIgnore
    protected MatrixResult matrixResult;
    @JsonIgnore
    protected MatrixRequest matrixRequest;

    public MatrixResponse(MatrixResult result, MatrixRequest request, SystemMessageProperties systemMessageProperties, EndpointsProperties endpointsProperties) {
        this.matrixResult = result;
        this.matrixRequest = request;
        responseInformation = new MatrixResponseInfo(request, systemMessageProperties, endpointsProperties);
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
