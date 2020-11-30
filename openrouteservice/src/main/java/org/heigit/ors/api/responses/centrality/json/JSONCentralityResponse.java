package org.heigit.ors.api.responses.centrality.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import io.swagger.annotations.ApiModel;
import org.heigit.ors.api.requests.centrality.CentralityRequest;
import org.heigit.ors.api.responses.centrality.CentralityResponse;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.util.FormatUtility;

import java.util.HashMap;

@ApiModel(description = "The Centrality Response contains one matrix for each specified `metrics` value.")
public class JSONCentralityResponse extends CentralityResponse {

    @JsonProperty("locations")
    public Double[][] locations;

    @JsonProperty("centralityScores")
    public Double[] scores;

    public JSONCentralityResponse(CentralityResult centralityResult, CentralityRequest request) throws StatusCodeException {
        super(centralityResult, request);
        HashMap<Coordinate, Double> centralityScores = centralityResult.getCentralityScores();
        int length = centralityScores.size();

        Double[][] locations = new Double[length][2];
        Double[] scores = new Double[length];
        int current = 0;

        for (HashMap.Entry<Coordinate, Double> centralityScore : centralityScores.entrySet()) {
            Coordinate location = centralityScore.getKey();
            Double score = centralityScore.getValue();

            locations[current][0] = FormatUtility.roundToDecimals(location.x, 6);  //COORDINATE_DECIMAL_PLACES in JSONLocation
            locations[current][1] = FormatUtility.roundToDecimals(location.y, 6);
            scores[current] = score;

            current += 1;
        }

        this.locations = locations;
        this.scores = scores;
    }
}
