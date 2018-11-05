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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.util.FormatUtility;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@ApiModel(value = "JSONIndividualRouteResponse", description = "An individual JSON based route created by the service")
public class JSONIndividualMatrixResponse extends JSONBasedIndividualMatrixResponse {
    private final int DURATIONS_DECIMAL_PLACES = 2;
    @ApiModelProperty(value = "The durations of the matrix calculations.")
    @JsonProperty("durations")
    private Double[] durations;

    @ApiModelProperty(value = "The individual destinations of the matrix calculations.")
    @JsonProperty("destinations")
    private List<JSON2DDestinations> destinations;

    @ApiModelProperty(value = "The individual sources of the matrix calculations.")
    @JsonProperty("sources")
    private List<JSON2DSources> sources;

    JSONIndividualMatrixResponse(MatrixResult matrixResult, MatrixRequest request) {
        super(request);
        durations = constructDurations(matrixResult, request);
        destinations = constructDestinations(matrixResult);
        sources = constructSources(matrixResult);

    }

    private Double[] constructDurations(MatrixResult matrixResult, MatrixRequest request) {
        float[] durations = matrixResult.getTable(request.getMetrics());
        Double[] constructedDurations = new Double[durations.length];
        for (int i = 0; i < durations.length; i++) {
            double duration = (double) durations[i];
            constructedDurations[i] = FormatUtility.roundToDecimals(duration, DURATIONS_DECIMAL_PLACES);
        }
        return constructedDurations;
    }


    public Double[] getDurations() {
        return durations;
    }

    public List<JSON2DDestinations> getDestinations() {
        return destinations;
    }

    public List<JSON2DSources> getSources() {
        return sources;
    }
}
