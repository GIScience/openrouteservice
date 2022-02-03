package org.heigit.ors.api.responses.centrality;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.heigit.ors.api.requests.centrality.CentralityRequest;

import java.util.List;

//TODO: should this include CentralityResponseInfo, as does RouteResponse?
public class CentralityResponse {
    @JsonIgnore
    protected List<List<Double>> bbox;

    // In RouteResponse, this method was used to get metadata from RouteRequest.
    public CentralityResponse(CentralityRequest request) {
        this.bbox = request.getBbox();
    }

    public List<List<Double>> getBbox() {
        return bbox;
    }
}
