package org.heigit.ors.routing.graphhopper.extensions.reader.heretraffic;

public class HereTrafficPattern {
    private int patternId;
    private HereTrafficEnums.PatternResolution resolution;
    private short[] values;

    public HereTrafficPattern(int patternId, HereTrafficEnums.PatternResolution resolution, short[] values) {
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



