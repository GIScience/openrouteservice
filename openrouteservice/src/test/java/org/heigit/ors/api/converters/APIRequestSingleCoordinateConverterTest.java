package org.heigit.ors.api.converters;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static java.lang.Double.NaN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class APIRequestSingleCoordinateConverterTest {
    APIRequestSingleCoordinateConverter apiRequestSingleCoordinateConverter;


    @BeforeEach
    void setUp() {
        apiRequestSingleCoordinateConverter = new APIRequestSingleCoordinateConverter();
    }

    @ParameterizedTest
    @CsvSource({
            "8.5555, 48.80987",
            "48.80987, 8.5555"
    })
    void convert(String x, String y) {
        String coordinateString = x + "," + y;
        assertEquals(new Coordinate(Double.parseDouble(x), Double.parseDouble(y), NaN), apiRequestSingleCoordinateConverter.convert(coordinateString));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "8.5555",
            "48.80987,",
            "48.80987,8.5555,300",
            "foo"
    })
    void convertMustFail(String coordinateString) {
        assertThrows(RuntimeException.class, () -> apiRequestSingleCoordinateConverter.convert(coordinateString));
    }
}