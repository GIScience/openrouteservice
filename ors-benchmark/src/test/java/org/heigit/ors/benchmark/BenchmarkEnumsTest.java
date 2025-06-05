package org.heigit.ors.benchmark;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class BenchmarkEnumsTest {
    @Test
    void testUnitFromString() {
        assertEquals(BenchmarkEnums.TestUnit.DISTANCE, BenchmarkEnums.TestUnit.fromString("distance"));
        assertEquals(BenchmarkEnums.TestUnit.TIME, BenchmarkEnums.TestUnit.fromString("time"));
        
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> BenchmarkEnums.TestUnit.fromString("invalid"));
        assertTrue(exception instanceof IllegalArgumentException);
    }

    @Test
    void testDirectionsModesFromString() {
        assertEquals(BenchmarkEnums.DirectionsModes.ALGO_CH, BenchmarkEnums.DirectionsModes.fromString("algoch"));
        assertEquals(BenchmarkEnums.DirectionsModes.ALGO_CORE, BenchmarkEnums.DirectionsModes.fromString("algocore"));
        assertEquals(BenchmarkEnums.DirectionsModes.ALGO_LM_ASTAR, BenchmarkEnums.DirectionsModes.fromString("algolmastar"));

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> BenchmarkEnums.DirectionsModes.fromString("invalid"));
        assertTrue(exception instanceof IllegalArgumentException);
    }

    @Test
    void testDirectionsModesGetDefaultProfiles() {
        List<String> basicProfiles = BenchmarkEnums.DirectionsModes.ALGO_CH.getProfiles();
        assertTrue(basicProfiles.contains("driving-car"));
        assertTrue(basicProfiles.contains("foot-walking"));
        assertEquals(4, basicProfiles.size());
    }

    @Test
    void testDirectionsModesGetRequestParams() {
        Map<String, Object> basicParams = BenchmarkEnums.DirectionsModes.ALGO_CH.getRequestParams();
        assertEquals("recommended", basicParams.get("preference"));
        assertEquals(1, basicParams.size());

        Map<String, Object> avoidParams = BenchmarkEnums.DirectionsModes.ALGO_LM_ASTAR.getRequestParams();
        assertEquals("recommended", avoidParams.get("preference"));
        assertTrue(avoidParams.get("options") instanceof Map);
    }

    @Test
    void testRangeTypeGetValue() {
        assertEquals("time", BenchmarkEnums.RangeType.TIME.getValue());
        assertEquals("distance", BenchmarkEnums.RangeType.DISTANCE.getValue());
    }

    @Test
    void testMatrixModesFromString() {
        assertEquals(BenchmarkEnums.MatrixModes.ALGO_DIJKSTRA_MATRIX, BenchmarkEnums.MatrixModes.fromString("algodijkstra"));
        assertEquals(BenchmarkEnums.MatrixModes.ALGO_CORE_MATRIX, BenchmarkEnums.MatrixModes.fromString("algocore"));
        assertEquals(BenchmarkEnums.MatrixModes.ALGO_RPHAST_MATRIX, BenchmarkEnums.MatrixModes.fromString("algorphast"));

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> BenchmarkEnums.MatrixModes.fromString("invalid"));
        assertTrue(exception instanceof IllegalArgumentException);
    }

    @Test
    void testMatrixModesGetDefaultProfiles() {
        List<String> dijkstraProfiles = BenchmarkEnums.MatrixModes.ALGO_DIJKSTRA_MATRIX.getProfiles();
        assertTrue(dijkstraProfiles.contains("driving-car"));
        assertTrue(dijkstraProfiles.contains("foot-walking"));
        assertEquals(4, dijkstraProfiles.size());

        List<String> coreProfiles = BenchmarkEnums.MatrixModes.ALGO_CORE_MATRIX.getProfiles();
        assertTrue(coreProfiles.contains("driving-car"));
        assertTrue(coreProfiles.contains("foot-walking"));
        assertEquals(4, coreProfiles.size());
    }

    @Test
    void testMatrixModesGetRequestParams() {
        Map<String, Object> rphastParams = BenchmarkEnums.MatrixModes.ALGO_RPHAST_MATRIX.getRequestParams();
        assertEquals("recommended", rphastParams.get("preference"));
        assertEquals(1, rphastParams.size());

        Map<String, Object> coreParams = BenchmarkEnums.MatrixModes.ALGO_CORE_MATRIX.getRequestParams();
        assertEquals("recommended", coreParams.get("preference"));
        assertTrue(coreParams.get("options") instanceof Map);

        Map<String, Object> dijkstraParams = BenchmarkEnums.MatrixModes.ALGO_DIJKSTRA_MATRIX.getRequestParams();
        assertEquals("recommended", dijkstraParams.get("preference"));
        assertTrue(dijkstraParams.get("options") instanceof List);
    }
}
