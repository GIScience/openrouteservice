package org.heigit.ors.api.responses.centrality.geojson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.Doubles;
import com.vividsolutions.jts.geom.Coordinate;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.requests.centrality.CentralityRequest;
import org.heigit.ors.api.responses.centrality.CentralityResponse;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.common.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeoJsonCentralityResponse extends CentralityResponse {
    @JsonProperty("type")
    public final String type = "FeatureCollection";

    @JsonProperty("bbox")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @ApiModelProperty(value = "Bounding box that covers all returned routes", example = "[49.414057, 8.680894, 49.420514, 8.690123]")
    public double[] getBBoxAsArray() {
        double[] retval = {};

        for (List<Double> coord : bbox) {
            retval = Doubles.concat(retval, new double[]{coord.get(0), coord.get(1)});
        }

        return retval;
    }

    public GeoJsonCentralityResponse(CentralityRequest request, CentralityResult result) {
        super(request);

        this.featureList = new ArrayList<>();

        if (result.hasNodeCentralityScores()) {
            for (Map.Entry<Integer, Coordinate> location : result.getLocations().entrySet()) {
                Integer id = location.getKey();
                Coordinate coord = location.getValue();
                Double score = result.getNodeCentralityScores().get(id);

                this.featureList.add(new GeoJsonCentralityNode(coord, score));
            }
        }

        if (result.hasEdgeCentralityScores()) {
            Map<Integer, Coordinate> locations = result.getLocations();
            for (Map.Entry<Pair<Integer, Integer>, Double> edgeScore : result.getEdgeCentralityScores().entrySet()) {
                Coordinate from = locations.get(edgeScore.getKey().first);
                Coordinate to = locations.get(edgeScore.getKey().second);
                Double score = edgeScore.getValue();

                this.featureList.add(new GeoJsonCentralityEdge(from, to, score));
            }
        }

    }

    @JsonProperty("features")
    public List featureList;

}
