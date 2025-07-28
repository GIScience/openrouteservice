package org.heigit.ors.api.responses.matching;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.heigit.ors.matching.MatchingResult;

public class MatchingResponse {
    @JsonIgnore
    private final MatchingResult result;

    public MatchingResponse(MatchingResult result) {
        this.result = result;
    }
}
