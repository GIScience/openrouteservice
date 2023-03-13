package org.heigit.ors.api.converters;

import org.heigit.ors.api.requests.common.APIEnums;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class APIMatrixRequestProfileConverterTest {
    private APIEnums.Profile cyclingElectric;
    private APIEnums.Profile cyclingMountain;
    private APIEnums.Profile cyclingRegular;
    private APIEnums.Profile cyclingRoad;
    private APIEnums.Profile drivingCar;
    private APIEnums.Profile drivingHgv;
    private APIEnums.Profile footHiking;
    private APIEnums.Profile footWalking;
    private APIEnums.Profile wheelchair;
    private APIRequestProfileConverter apiRequestProfileConverter;


    @BeforeEach
    void setUp() {
        cyclingElectric = APIEnums.Profile.CYCLING_ELECTRIC;
        cyclingMountain = APIEnums.Profile.CYCLING_MOUNTAIN;
        cyclingRegular = APIEnums.Profile.CYCLING_REGULAR;
        cyclingRoad = APIEnums.Profile.CYCLING_ROAD;
        drivingCar = APIEnums.Profile.DRIVING_CAR;
        drivingHgv = APIEnums.Profile.DRIVING_HGV;
        footHiking = APIEnums.Profile.FOOT_HIKING;
        footWalking = APIEnums.Profile.FOOT_WALKING;
        wheelchair = APIEnums.Profile.WHEELCHAIR;
        apiRequestProfileConverter = new APIRequestProfileConverter();
    }

    @Test
    void convert() {
        assertEquals(drivingCar, apiRequestProfileConverter.convert("driving-car"));
        assertEquals(drivingHgv, apiRequestProfileConverter.convert("driving-hgv"));
        assertEquals(cyclingRegular, apiRequestProfileConverter.convert("cycling-regular"));
        assertEquals(cyclingRoad, apiRequestProfileConverter.convert("cycling-road"));
        assertEquals(cyclingMountain, apiRequestProfileConverter.convert("cycling-mountain"));
        assertEquals(cyclingElectric, apiRequestProfileConverter.convert("cycling-electric"));
        assertEquals(footWalking, apiRequestProfileConverter.convert("foot-walking"));
        assertEquals(footHiking, apiRequestProfileConverter.convert("foot-hiking"));
        assertEquals(wheelchair, apiRequestProfileConverter.convert("wheelchair"));
    }

    @Test
    void convertProfileFail() {
        assertThrows(IllegalArgumentException.class, () -> apiRequestProfileConverter.convert("flying-foo"));
    }
}