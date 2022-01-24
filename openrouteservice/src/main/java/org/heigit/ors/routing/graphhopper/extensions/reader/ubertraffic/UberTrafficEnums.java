package org.heigit.ors.routing.graphhopper.extensions.reader.ubertraffic;

public class UberTrafficEnums {
    public enum PatternResolution {
        MINUTES_15(15),
        MINUTES_60(60);

        private final int value;

        PatternResolution(int resolution) {
            this.value = resolution;
        }

        public int getValue() {
            return value;
        }
    }
}
