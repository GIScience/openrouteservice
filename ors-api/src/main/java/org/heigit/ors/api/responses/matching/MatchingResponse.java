package org.heigit.ors.api.responses.matching;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.heigit.ors.matching.MatchingRequest;

public class MatchingResponse {
    @JsonIgnore
    private final MatchingRequest.MatchingResult result;

    public MatchingResponse(MatchingRequest.MatchingResult result) {
        this.result = result;
    }
}
