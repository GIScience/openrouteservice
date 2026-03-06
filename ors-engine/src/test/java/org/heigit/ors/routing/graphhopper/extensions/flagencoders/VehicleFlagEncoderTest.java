package org.heigit.ors.routing.graphhopper.extensions.flagencoders;

import org.junit.jupiter.api.Test;

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
        int result = service.getTrackGradeLevel("grade1; grade3; grade2");
        assertEquals(3, result);
    }

    @Test
    void shouldReturnMaxGradeWhenMultipleGradesPresentDash() {
        int result = service.getTrackGradeLevel("grade1-grade3-grade2");
        assertEquals(3, result);
    }

    @Test
    void shouldReturn0WhenMultipleGradesAndParsingFails() {
        int result = service.getTrackGradeLevel("grade1-grade3-gradeA");
        assertEquals(0, result);
    }

    @Test
    void shouldReturn5WhenMultipleGradesAndValues678() {
        int result = service.getTrackGradeLevel("grade1-grade8-grade7");
        assertEquals(5, result);
    }

    @Test
    void shouldReturn5WhenMultipleGradesAndValuesHigherThan8() {
        int result = service.getTrackGradeLevel("grade1-grade8-grade77");
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

    @Test
    void shouldReturn1WhenGrade() {
        int result = service.getTrackGradeLevel("grade");
        assertEquals(1, result);
    }

    @Test
    void shouldReturn1WhenGrade1() {
        int result = service.getTrackGradeLevel("grade1");
        assertEquals(1, result);
    }

    @Test
    void shouldReturn2WhenGrade2() {
        int result = service.getTrackGradeLevel("grade2");
        assertEquals(2, result);
    }
    @Test
    void shouldReturn3WhenGrade3() {
        int result = service.getTrackGradeLevel("grade3");
        assertEquals(3, result);
    }
    @Test
    void shouldReturn4WhenGrade4() {
        int result = service.getTrackGradeLevel("grade4");
        assertEquals(4, result);
    }
    @Test
    void shouldReturn5WhenGrade5() {
        int result = service.getTrackGradeLevel("grade5");
        assertEquals(5, result);
    }

    @Test
    void shouldReturn5WhenGrade6() {
        int result = service.getTrackGradeLevel("grade6");
        assertEquals(5, result);
    }

    @Test
    void shouldReturn5WhenGrade7() {
        int result = service.getTrackGradeLevel("grade7");
        assertEquals(5, result);
    }

    @Test
    void shouldReturn5WhenGrade8() {
        int result = service.getTrackGradeLevel("grade8");
        assertEquals(5, result);
    }


}

