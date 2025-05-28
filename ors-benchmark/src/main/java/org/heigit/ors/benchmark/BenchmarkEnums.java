package org.heigit.ors.benchmark;

import java.util.List;
import java.util.Map;

public class BenchmarkEnums {

    // Constant for preference
    public static final String PREFERENCE = "preference";
    // Constant for recommended
    public static final String RECOMMENDED = "recommended";

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
                case ALGO_CH, ALGO_CORE, ALGO_LM_ASTAR -> List.of("driving-car", "driving-hgv", "cycling-regular", "foot-walking");
            };
        }

        public Map<String, Object> getRequestParams() {
            return switch (this) {
                case ALGO_CH -> Map.of(PREFERENCE, RECOMMENDED);
                case ALGO_CORE -> Map.of(PREFERENCE,
                        RECOMMENDED, "options", Map.of("avoid_features", List.of("ferries")));
                case ALGO_LM_ASTAR -> Map.of(PREFERENCE, RECOMMENDED, "options",
                        Map.of("avoid_polygons",
                                Map.of("type", "Polygon", "coordinates",
                                        List.of(List.of(List.of(100.0, 100.0), List.of(100.001, 100.0),
                                                List.of(100.001, 100.001), List.of(100.0, 100.001),
                                                List.of(100.0, 100.0))))));
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

    /**
     * Enum representing different matrix modes for benchmarking.
     * getRequestParams() provides the parameters to trigger each matrix algorithm.
     */
    public enum MatrixModes {
        ALGO_DIJKSTRA_MATRIX,
        ALGO_CORE_MATRIX,
        ALGO_RPHAST_MATRIX;

        public static MatrixModes fromString(String value) {
            return switch (value.toLowerCase()) {
                case "algodijkstra" -> ALGO_DIJKSTRA_MATRIX;
                case "algocore" -> ALGO_CORE_MATRIX;
                case "algorphast" -> ALGO_RPHAST_MATRIX;
                default -> throw new IllegalArgumentException("Invalid directions mode: " + value);
            };
        }

        public List<String> getProfiles() {
            return switch (this) {
                case ALGO_DIJKSTRA_MATRIX, ALGO_CORE_MATRIX, ALGO_RPHAST_MATRIX -> List.of("driving-car", "driving-hgv", "cycling-regular", "foot-walking");
            };
        }
        /**
         * Returns the request parameters for the matrix algorithm.
         * These parameters are used to trigger the specific matrix algorithm.
         * This is not great as we have to maintain this in multiple places,
         * at the moment this is the only way to trigger the algorithms.
         * What would be better is to have a common interface for the algorithms,
         * but that would require a larger refactor of the codebase.
         * @return a map of request parameters
         */
        public Map<String, Object> getRequestParams() {
            return switch (this) {
                case ALGO_RPHAST_MATRIX -> Map.of(PREFERENCE, RECOMMENDED);
                case ALGO_CORE_MATRIX -> Map.of(PREFERENCE,
                        RECOMMENDED, "options", Map.of("dynamic_speeds", "true"));
                case ALGO_DIJKSTRA_MATRIX -> Map.of(PREFERENCE,
                        RECOMMENDED, "options", List.of(Map.of("dynamic_speeds", "false"), Map.of("avoid_features", List.of("ferries"))));

            };
        }
    }

}
