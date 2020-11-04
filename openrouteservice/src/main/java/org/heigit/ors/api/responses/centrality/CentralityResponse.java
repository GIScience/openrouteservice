package org.heigit.ors.api.responses.centrality;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.heigit.ors.api.requests.centrality.CentralityRequest;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBox;
import org.heigit.ors.centrality.CentralityResult;

//TODO: maybe include some CentralityResponseInfo, as is done in RouteResponse??
public class CentralityResponse {
    @JsonIgnore
    protected BoundingBox bbox;

    @JsonIgnore
    protected CentralityResult centralityResults;

    public CentralityResponse() {};

    public CentralityResponse(CentralityResult result, CentralityRequest request) {  //used to get metadata from centrality Request, see todo above
        this.centralityResults = result;
    }
    public BoundingBox getBbox() {
        return bbox;
    }

    public CentralityResult getCentralityResults() {
        return centralityResults;
    }
}
