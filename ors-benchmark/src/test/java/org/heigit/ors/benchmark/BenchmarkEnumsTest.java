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
        assertEquals(8, basicProfiles.size());
    }

    @Test
    void testDirectionsModesGetRequestParams() {
        Map<String, Object> basicParams = BenchmarkEnums.DirectionsModes.ALGO_CH.getRequestParams();
        assertEquals("fastest", basicParams.get("preference"));
        assertEquals(1, basicParams.size());

        Map<String, Object> avoidParams = BenchmarkEnums.DirectionsModes.ALGO_LM_ASTAR.getRequestParams();
        assertEquals("fastest", avoidParams.get("preference"));
        assertTrue(avoidParams.get("options") instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> options = (Map<String, Object>) avoidParams.get("options");
        assertTrue(options.get("avoid_features") instanceof List);
    }

    @Test
    void testRangeTypeGetValue() {
        assertEquals("time", BenchmarkEnums.RangeType.TIME.getValue());
        assertEquals("distance", BenchmarkEnums.RangeType.DISTANCE.getValue());
    }
}
