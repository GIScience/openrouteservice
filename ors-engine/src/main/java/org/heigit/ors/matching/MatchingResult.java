package org.heigit.ors.matching;


import lombok.Getter;

import java.util.List;

@Getter
public class MatchingResult {
    private String graphDate = "empty";

    private final List<Integer> matched;

    public MatchingResult(String graphDate, List<Integer> matched) {
        this.graphDate = graphDate;
        this.matched = matched;
    }
}
