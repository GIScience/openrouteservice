package org.heigit.ors.routing.graphhopper.extensions.flagencoders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager.Access;
import com.graphhopper.routing.util.TransportationMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VehicleFlagEncoderTest {

    static class TestVehicleFlagEncoder extends VehicleFlagEncoder {
                
        TestVehicleFlagEncoder(int testSpeedBits,
                                    double testSpeedFactor,
                                    int testMaxTurnCost
                                ) {
                                    super(testSpeedBits, testSpeedFactor, testMaxTurnCost);
                                }
                
        @Override
        public String toString() {
            return "test";
        }

        @Override
        public TransportationMode getTransportationMode() {
            return TransportationMode.CAR;
        }

        @Override
        public double getMeanSpeed() {
            return 50;
        }

        @Override
        public Access getAccess(ReaderWay way) {
            return Access.WAY;        
        }
    }

    private final VehicleFlagEncoder service = new TestVehicleFlagEncoder(0, 0, 0);

    @Test
    void shouldReturnZeroWhenGradeIsNull() {
        int result = service.getTrackGradeLevel(null);
        assertEquals(0, result);
    }

    @Test
    void shouldReturnZeroWhenGradeIsUnknown() {
        int result = service.getTrackGradeLevel("unknown");
        assertEquals(0, result);
    }

    @Test
    void shouldReturnMaxGradeWhenMultipleGradesPresentSemicolon() {
        int result = service.getTrackGradeLevel("grade3;grade2");
        assertEquals(3, result);
    }

    @Test
    void shouldReturnMaxGradeWhenMultipleGradesPresentDash() {
        int result = service.getTrackGradeLevel("grade1-3");
        assertEquals(3, result);
    }

    @Test
    void shouldReturn0WhenMultipleGradesAndParsingFails() {
        int result = service.getTrackGradeLevel("grade1-A");
        assertEquals(0, result);
    }

    @Test
    void shouldReturn5WhenMultipleGradesAndValues678() {
        int result = service.getTrackGradeLevel("grade1-7");
        assertEquals(5, result);
    }

    @Test
    void shouldReturn5WhenMultipleGradesAndValuesHigherThan8() {
        int result = service.getTrackGradeLevel("grade1-77");
        assertEquals(5, result);
    }

    @Test
    void shouldHandleWhitespaceCorrectly() {
        int result = service.getTrackGradeLevel(" grade1 ;  grade5 ; grade2 ");
        assertEquals(5, result);
    }

    @Test
    void shouldReturn0IfParsingFails() {
        int result = service.getTrackGradeLevel("gradeA; gradeB");
        assertEquals(0, result);
    }

    @Test
    void shouldReturn0WhenGradeAllCaps() {
        int result = service.getTrackGradeLevel("GRADE");
        assertEquals(0, result);
    }

    @ParameterizedTest(name = "should return {1} for {0}")
    @CsvSource({
        "grade, 0",
        "grade1, 1",
        "grade2, 2",
        "grade3, 3",
        "grade4, 4"
    })
    void shouldReturnExpectedGradeLevel(String grade, int expected) {
        int result = service.getTrackGradeLevel(grade);
        assertEquals(expected, result);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"grade5", "grade6", "grade7", "grade8"})
    void shouldReturn5WhenGradeHigherThan5(String grade) {
        int result = service.getTrackGradeLevel(grade);
        assertEquals(5, result);
    }

}

