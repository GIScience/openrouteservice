package org.heigit.ors.api.responses.centrality.json;

import org.heigit.ors.api.requests.centrality.CentralityRequest;
import org.heigit.ors.api.responses.centrality.CentralityResponse;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.exceptions.StatusCodeException;

public class JSONCentralityResponse extends CentralityResponse {
        public JSONCentralityResponse(CentralityResult centralityResult, CentralityRequest request) throws StatusCodeException {
        super(request);
    }
}
