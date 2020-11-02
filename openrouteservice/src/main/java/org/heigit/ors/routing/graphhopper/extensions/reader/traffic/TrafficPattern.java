package org.heigit.ors.routing.graphhopper.extensions.reader.traffic;

public class TrafficPattern {
    private int patternId;
    private TrafficEnums.PatternResolution resolution;
    private short[] values;

    public enum SpeedType {MPH, KPH}

    public TrafficPattern(int patternId, TrafficEnums.PatternResolution resolution, short[] values) {
        this.patternId = patternId;
        this.resolution = resolution;

        this.values = values;
    }

    public int getPatternId() {
        return patternId;
    }

    public TrafficEnums.PatternResolution getResolution() {
        return resolution;
    }

    public short[] getValues() {
        return values;
    }
}



