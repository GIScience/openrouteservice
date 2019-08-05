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
    private APIEnums.Profile drivingCar;
    private APIEnums.Profile drivingHgv;
    private APIEnums.Profile footHiking;
    private APIEnums.Profile footWalking;
    private APIEnums.Profile wheelchair;
    private APIRequestProfileConverter apiRequestProfileConverter;


    @Before
    public void setUp() {
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

    @Test()
    public void convert() {
        Assert.assertEquals(drivingCar, apiRequestProfileConverter.convert("driving-car"));
        Assert.assertEquals(drivingHgv, apiRequestProfileConverter.convert("driving-hgv"));
        Assert.assertEquals(cyclingRegular, apiRequestProfileConverter.convert("cycling-regular"));
        Assert.assertEquals(cyclingRoad, apiRequestProfileConverter.convert("cycling-road"));
        Assert.assertEquals(cyclingMountain, apiRequestProfileConverter.convert("cycling-mountain"));
        Assert.assertEquals(cyclingElectric, apiRequestProfileConverter.convert("cycling-electric"));
        Assert.assertEquals(footWalking, apiRequestProfileConverter.convert("foot-walking"));
        Assert.assertEquals(footHiking, apiRequestProfileConverter.convert("foot-hiking"));
        Assert.assertEquals(wheelchair, apiRequestProfileConverter.convert("wheelchair"));
        Assert.assertNull(apiRequestProfileConverter.convert("flying-foo"));
    }
}