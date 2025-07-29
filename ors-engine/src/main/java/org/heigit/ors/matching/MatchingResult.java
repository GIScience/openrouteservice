package org.heigit.ors.matching;


import lombok.Getter;

public class MatchingResult {
    @Getter
    private String graphDate = "empty";

    @Getter
    private int matched = 0;

    public MatchingResult(String graphDate, int matched) {
        this.graphDate = graphDate;
        this.matched = matched;
    }
}
