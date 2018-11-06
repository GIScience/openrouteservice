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
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.util.FormatUtility;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@ApiModel(value = "JSONIndividualRouteResponse", description = "An individual JSON based route created by the service")
public class JSONIndividualMatrixResponse extends JSONBasedIndividualMatrixResponse {
    private final int DURATIONS_DECIMAL_PLACES = 2;
    private final int DISTANCES_DECIMAL_PLACES = 2;
    private final int WEIGHT_DECIMAL_PLACES = 2;
    @ApiModelProperty(value = "The durations of the matrix calculations.")
    @JsonProperty("durations")
    private Double[] durations;

    @ApiModelProperty(value = "The distances of the matrix calculations.")
    @JsonProperty("distances")
    private Double[] distances;

    @ApiModelProperty(value = "The weights of the matrix calculations.")
    @JsonProperty("weights")
    private Double[] weights;

    @ApiModelProperty(value = "The individual destinations of the matrix calculations.")
    @JsonProperty("destinations")
    private List<JSON2DDestinations> destinations;

    @ApiModelProperty(value = "The individual sources of the matrix calculations.")
    @JsonProperty("sources")
    private List<JSON2DSources> sources;

    JSONIndividualMatrixResponse(MatrixResult matrixResult, MatrixRequest request) {
        super(request);
        int metric = request.getMetrics();
        switch (metric) {
            case MatrixMetricsType.Duration:
                durations = constructDurations(matrixResult, request);
                break;
            case MatrixMetricsType.Distance:
                distances = constructDistances(matrixResult, request);
                break;
            case MatrixMetricsType.Weight:
                weights = constructWeights(matrixResult, request);
                break;
            default:
                break;
        }
        destinations = constructDestinations(matrixResult);
        sources = constructSources(matrixResult);

    }

    public JSONIndividualMatrixResponse(MatrixRequest request) {
        super(request);
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

    private Double[] constructDistances(MatrixResult matrixResult, MatrixRequest request) {
        float[] distances = matrixResult.getTable(request.getMetrics());
        Double[] constructedDistances = new Double[distances.length];
        for (int i = 0; i < distances.length; i++) {
            double distance = (double) distances[i];
            constructedDistances[i] = FormatUtility.roundToDecimals(distance, DISTANCES_DECIMAL_PLACES);
        }
        return constructedDistances;
    }

    private Double[] constructWeights(MatrixResult matrixResult, MatrixRequest request) {
        float[] weights = matrixResult.getTable(request.getMetrics());
        Double[] constructedWeights = new Double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            double weight = (double) weights[i];
            constructedWeights[i] = FormatUtility.roundToDecimals(weight, WEIGHT_DECIMAL_PLACES);
        }
        return constructedWeights;
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

    public Double[] getDistances() {
        return distances;
    }

    public void setDistances(Double[] distances) {
        this.distances = distances;
    }

    public Double[] getWeights() {
        return weights;
    }

    public void setWeights(Double[] weights) {
        this.weights = weights;
    }

    public void setDurations(Double[] durations) {
        this.durations = durations;
    }

    public void setDestinations(List<JSON2DDestinations> destinations) {
        this.destinations = destinations;
    }

    public void setSources(List<JSON2DSources> sources) {
        this.sources = sources;
    }

}
