package org.heigit.ors.api.responses.centrality;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBox;
import org.heigit.ors.centrality.CentralityResult;

//TODO Refactoring: should this include CentralityResponseInfo, as does RouteResponse?
public class CentralityResponse {
    @JsonIgnore
    protected BoundingBox bbox;

    @JsonIgnore
    protected CentralityResult centralityResults;

    // In RouteResponse, this method was used to get metadata from RouteRequest.
    public CentralityResponse(CentralityResult result) {
        this.centralityResults = result;
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public CentralityResult getCentralityResults() {
        return centralityResults;
    }
}
