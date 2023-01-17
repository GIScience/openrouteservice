package org.heigit.ors.exceptions;

public class MaxVisitedNodesExceededException extends Exception {

    public MaxVisitedNodesExceededException() {
        super("Search exceeds the limit of visited nodes.");
    }

}
