package org.heigit.ors.routing.graphhopper.extensions.reader.traffic;

public class TrafficPattern {
    private final int patternId;
    private final short[] values;

    public TrafficPattern(int patternId, short[] values) {
        this.patternId = patternId;
        this.values = values;
    }

    public int getPatternId() {
        return patternId;
    }

    public short[] getValues() {
        return values;
    }
}



