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
        ALGO_CH,
        ALGO_CORE,
        ALGO_LM_ASTAR;

        public static DirectionsModes fromString(String value) {
            return switch (value.toLowerCase()) {
                case "algoch" -> ALGO_CH;
                case "algocore" -> ALGO_CORE;
                case "algolmastar" -> ALGO_LM_ASTAR;
                default -> throw new IllegalArgumentException("Invalid directions mode: " + value);
            };
        }

        public List<String> getProfiles() {
            return switch (this) {
                case ALGO_CH, ALGO_CORE, ALGO_LM_ASTAR -> List.of("driving-car", "driving-hgv", "cycling-regular", "cycling-road", "cycling-mountain", "cycling-electric", "foot-walking", "foot-hiking");
            };
        }

        public Map<String, Object> getRequestParams() {
            return switch (this) {
                case ALGO_CH -> Map.of("preference", "recommended");
                case ALGO_CORE -> Map.of("preference", "recommended", "options", Map.of("avoid_features", List.of("ferries")));
                case ALGO_LM_ASTAR -> Map.of("preference", "recommended", "options", Map.of("avoid_polygons", Map.of("type", "Polygon", "coordinates", List.of(List.of(List.of(100.0, 100.0), List.of(100.001, 100.0), List.of(100.001, 100.001), List.of(100.0, 100.001), List.of(100.0, 100.0))))));
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
