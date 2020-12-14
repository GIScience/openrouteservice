package org.heigit.ors.routing.graphhopper.extensions.reader.traffic;

public class TrafficPattern {
    private int patternId;
    private TrafficEnums.PatternResolution resolution;
    private short[] values;

    public TrafficPattern(int patternId, TrafficEnums.PatternResolution resolution, short[] values) {
        this.patternId = patternId;
        this.resolution = resolution;

        this.values = values;
    }

    public int getPatternId() {
        return patternId;
    }

    public short[] getValues() {
        return values;
    }
}



