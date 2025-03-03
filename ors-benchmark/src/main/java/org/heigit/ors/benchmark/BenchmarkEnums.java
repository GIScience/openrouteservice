package org.heigit.ors.benchmark;

import java.util.List;
import java.util.Map;

public class BenchmarkEnums {
    public enum TestUnit {
        DISTANCE,
        TIME;

        public static TestUnit fromString(String value) {
            return switch (value.toLowerCase()) {
                case "distance" -> DISTANCE;
                case "time" -> TIME;
                default -> throw new IllegalArgumentException("Invalid test unit: " + value);
            };
        }
    }

    public enum DirectionsModes {
        BASIC_FASTEST,
        AVOID_HIGHWAY,
        AVOID_AREA;

        public static DirectionsModes fromString(String value) {
            return switch (value.toLowerCase()) {
                case "basicfastest" -> BASIC_FASTEST;
                case "avoidhighway" -> AVOID_HIGHWAY;
                case "avoidarea" -> AVOID_AREA;
                default -> throw new IllegalArgumentException("Invalid directions mode: " + value);
            };
        }

        public List<String> getProfiles() {
            return switch (this) {
                case BASIC_FASTEST -> List.of("driving-car", "foot-walking");
                case AVOID_HIGHWAY -> List.of("driving-car", "driving-hgv");
                case AVOID_AREA -> List.of("driving-car", "driving-hgv");
            };
        }

        public Map<String, Object> getRequestParams() {
            return switch (this) {
                case BASIC_FASTEST -> Map.of("preference", "fastest");
                case AVOID_HIGHWAY -> Map.of("preference", "fastest", 
                    "options", Map.of("avoid_features", List.of("highways")));
                case AVOID_AREA -> Map.of("preference", "fastest",
                        "options", Map.of("avoid_polygon", List.of("highways")));
            };
        }
    }
    
    public enum RangeType {
        TIME("time"),
        DISTANCE("distance");

        private final String value;

        RangeType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
