package org.heigit.ors.api.responses.centrality.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import io.swagger.annotations.ApiModel;
import org.heigit.ors.api.requests.centrality.CentralityRequest;
import org.heigit.ors.api.responses.centrality.CentralityResponse;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.util.FormatUtility;

import java.util.Map;

@ApiModel(description = "The Centrality Response contains one matrix for each specified `metrics` value.")
public class JSONCentralityResponse extends CentralityResponse {

    @JsonProperty("locations")
    public Double[][] locations;

    @JsonProperty("centralityScores")
    public Double[] scores;

    @JsonProperty("nodeIds")
    public Integer[] nodeIds;

    @JsonProperty("aggregated") // test if everything is returned in correct order
    public Double[][] aggregated;

    public JSONCentralityResponse(CentralityResult centralityResult, CentralityRequest request) throws StatusCodeException {
        super(centralityResult);
        Map<Coordinate, Double> centralityScores = centralityResult.getNodeCentralityScores();
        Map<Coordinate, Integer> nodes = centralityResult.getNodes();
        int length = centralityScores.size();

        this.locations = new Double[length][2];
        this.scores = new Double[length];
        this.aggregated = new Double[length][4];
        this.nodeIds = new Integer[length];
        int current = 0;

        for (Map.Entry<Coordinate, Double> centralityScore : centralityScores.entrySet()) {
            Coordinate location = centralityScore.getKey();
            Double score = centralityScore.getValue();
            Integer node = nodes.get(location);

            this.locations[current][0] = FormatUtility.roundToDecimals(location.x, 6);  //COORDINATE_DECIMAL_PLACES in JSONLocation
            this.locations[current][1] = FormatUtility.roundToDecimals(location.y, 6);
            this.scores[current] = score;
            this.nodeIds[current] = node;

            this.aggregated[current][0] = FormatUtility.roundToDecimals(location.x, 6);
            this.aggregated[current][1] = FormatUtility.roundToDecimals(location.y, 6);
            this.aggregated[current][2] = score;
            this.aggregated[current][3] = (double) node;

            current += 1;
        }
    }
}
