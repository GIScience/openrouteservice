package org.heigit.ors.api.responses.centrality;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.heigit.ors.api.requests.centrality.CentralityRequest;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBox;

import java.util.List;

//TODO: maybe include some CentralityResponseInfo, as is done in RouteResponse??
public class CentralityResponse {
    @JsonIgnore
    protected BoundingBox bbox;

    @JsonIgnore
    protected List centralityResults;

    public CentralityResponse() {};

    public CentralityResponse(CentralityRequest request) { //used to get metadata from centrality Request, see todo above
    }
    public BoundingBox getBbox() {
        return bbox;
    }

    public List getCentralityResults() {
        return centralityResults;
    }
}
