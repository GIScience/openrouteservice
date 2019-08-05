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
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.util.FormatUtility;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@ApiModel(value = "JSONIndividualMatrixResponse", description = "An individual JSON based route created by the service")
public class JSONIndividualMatrixResponse extends JSONBasedIndividualMatrixResponse {
    @ApiModelProperty(value = "The durations of the matrix calculations.", example = "[[0,25],[25,0]]")
    @JsonProperty("durations")
    private Double[][] durations;

    @ApiModelProperty(value = "The distances of the matrix calculations.", example = "[[0,0.25],[0.25,0]]")
    @JsonProperty("distances")
    private Double[][] distances;

    @ApiModelProperty(value = "The individual destinations of the matrix calculations.")
    @JsonProperty("destinations")
    private List<JSON2DDestinations> destinations;

    @ApiModelProperty(value = "The individual sources of the matrix calculations.")
    @JsonProperty("sources")
    private List<JSON2DSources> sources;

    JSONIndividualMatrixResponse(MatrixResult result, MatrixRequest request) {
        super(request);

        destinations = constructDestinations(result);
        sources = constructSources(result);

        for (int i=0; i<result.getTables().length; i++) {
            if (result.getTable(i) != null) {
                switch (i) {
                    case MatrixMetricsType.Duration:
                        durations = constructMetric(result.getTable(i), result);
                        break;
                    case MatrixMetricsType.Distance:
                        distances = constructMetric(result.getTable(i), result);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private Double[][] constructMetric(float[] table, MatrixResult result) {
        int sourceCount = result.getSources().length;
        int destinationCount = result.getDestinations().length;

        Double[][] constructedTable = new Double[sourceCount][destinationCount];

        for (int i=0; i<sourceCount; i++) {
            for (int j=0; j<destinationCount; j++) {
                double value = (double) table[(i*destinationCount) + j];
                if (value == -1)
                    constructedTable[i][j] = null;
                else
                    constructedTable[i][j] = FormatUtility.roundToDecimals(value, 2);
            }
        }

        return constructedTable;
    }

    public Double[][] getDurations() {
        return durations;
    }

    public List<JSON2DDestinations> getDestinations() {
        return destinations;
    }

    public List<JSON2DSources> getSources() {
        return sources;
    }

    public Double[][] getDistances() {
        return distances;
    }

    public void setDistances(Double[][] distances) {
        this.distances = distances;
    }

    public void setDurations(Double[][] durations) {
        this.durations = durations;
    }

    public void setDestinations(List<JSON2DDestinations> destinations) {
        this.destinations = destinations;
    }

    public void setSources(List<JSON2DSources> sources) {
        this.sources = sources;
    }
}
