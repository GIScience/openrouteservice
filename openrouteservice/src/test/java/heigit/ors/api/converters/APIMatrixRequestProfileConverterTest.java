package heigit.ors.api.converters;

import heigit.ors.api.requests.common.APIEnums;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class APIMatrixRequestProfileConverterTest {
    private APIEnums.Profile cyclingElectric;
    private APIEnums.Profile cyclingMountain;
    private APIEnums.Profile cyclingRegular;
    private APIEnums.Profile cyclingRoad;
    private APIEnums.Profile cyclingSafe;
    private APIEnums.Profile cyclingTour;
    private APIEnums.Profile drivingCar;
    private APIEnums.Profile drivingHgv;
    private APIEnums.Profile footHiking;
    private APIEnums.Profile footWalking;
    private APIEnums.Profile wheelchair;
    private APIMatrixRequestProfileConverter apiMatrixRequestProfileConverter;


    @Before
    public void setUp() {
        cyclingElectric = APIEnums.Profile.CYCLING_ELECTRIC;
        cyclingMountain = APIEnums.Profile.CYCLING_MOUNTAIN;
        cyclingRegular = APIEnums.Profile.CYCLING_REGULAR;
        cyclingRoad = APIEnums.Profile.CYCLING_ROAD;
        cyclingSafe = APIEnums.Profile.CYCLING_SAFE;
        cyclingTour = APIEnums.Profile.CYCLING_TOUR;
        drivingCar = APIEnums.Profile.DRIVING_CAR;
        drivingHgv = APIEnums.Profile.DRIVING_HGV;
        footHiking = APIEnums.Profile.FOOT_HIKING;
        footWalking = APIEnums.Profile.FOOT_WALKING;
        wheelchair = APIEnums.Profile.WHEELCHAIR;
        apiMatrixRequestProfileConverter = new APIMatrixRequestProfileConverter();
    }

    @Test()
    public void convert() {
        Assert.assertEquals(drivingCar, apiMatrixRequestProfileConverter.convert("driving-car"));
        Assert.assertEquals(drivingHgv, apiMatrixRequestProfileConverter.convert("driving-hgv"));
        Assert.assertEquals(cyclingRegular, apiMatrixRequestProfileConverter.convert("cycling-regular"));
        Assert.assertEquals(cyclingRoad, apiMatrixRequestProfileConverter.convert("cycling-road"));
        Assert.assertEquals(cyclingSafe, apiMatrixRequestProfileConverter.convert("cycling-safe"));
        Assert.assertEquals(cyclingMountain, apiMatrixRequestProfileConverter.convert("cycling-mountain"));
        Assert.assertEquals(cyclingTour, apiMatrixRequestProfileConverter.convert("cycling-tour"));
        Assert.assertEquals(cyclingElectric, apiMatrixRequestProfileConverter.convert("cycling-electric"));
        Assert.assertEquals(footWalking, apiMatrixRequestProfileConverter.convert("foot-walking"));
        Assert.assertEquals(footHiking, apiMatrixRequestProfileConverter.convert("foot-hiking"));
        Assert.assertEquals(wheelchair, apiMatrixRequestProfileConverter.convert("wheelchair"));
        Assert.assertNull(apiMatrixRequestProfileConverter.convert("flying-foo"));
    }
}