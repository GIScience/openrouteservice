package org.heigit.ors.matching;


import lombok.Getter;

public class MatchingResult {
    @Getter
    private String graphDate = "empty";

    public MatchingResult(String graphDate) {
        this.graphDate = graphDate;
    }
}
