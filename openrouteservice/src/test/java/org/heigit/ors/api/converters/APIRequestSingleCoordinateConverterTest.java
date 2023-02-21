package org.heigit.ors.api.converters;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.lang.Double.NaN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class APIRequestSingleCoordinateConverterTest {
    APIRequestSingleCoordinateConverter apiRequestSingleCoordinateConverter;


    @BeforeEach
    void setUp() {
        apiRequestSingleCoordinateConverter = new APIRequestSingleCoordinateConverter();
    }

    @Test
    void convert() {
        assertEquals(new Coordinate(8.5555, 48.80987, NaN), apiRequestSingleCoordinateConverter.convert("8.5555,48.80987"));
        assertEquals(new Coordinate(48.80987, 8.5555, NaN), apiRequestSingleCoordinateConverter.convert("48.80987,8.5555"));
        assertThrows(RuntimeException.class, () -> {
            apiRequestSingleCoordinateConverter.convert("8.5555");
            apiRequestSingleCoordinateConverter.convert("8.5555,");
            apiRequestSingleCoordinateConverter.convert(",48.5555");
            apiRequestSingleCoordinateConverter.convert("48.80987,8.5555,300");
            apiRequestSingleCoordinateConverter.convert("foo");
        });
    }
}