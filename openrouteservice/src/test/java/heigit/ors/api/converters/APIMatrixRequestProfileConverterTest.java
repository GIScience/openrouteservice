package heigit.ors.api.converters;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static heigit.ors.api.requests.common.APIEnums.MatrixProfile;

public class APIMatrixRequestProfileConverterTest {
    private MatrixProfile cyclingElectric;
    private MatrixProfile cyclingMountain;
    private MatrixProfile cyclingRegular;
    private MatrixProfile cyclingRoad;
    private MatrixProfile cyclingSafe;
    private MatrixProfile cyclingTour;
    private MatrixProfile drivingCar;
    private MatrixProfile drivingHgv;
    private MatrixProfile footHiking;
    private MatrixProfile footWalking;
    private MatrixProfile wheelchair;
    private APIMatrixRequestProfileConverter apiMatrixRequestProfileConverter;


    @Before
    public void setUp() {
        cyclingElectric = MatrixProfile.CYCLING_ELECTRIC;
        cyclingMountain = MatrixProfile.CYCLING_MOUNTAIN;
        cyclingRegular = MatrixProfile.CYCLING_REGULAR;
        cyclingRoad = MatrixProfile.CYCLING_ROAD;
        cyclingSafe = MatrixProfile.CYCLING_SAFE;
        cyclingTour = MatrixProfile.CYCLING_TOUR;
        drivingCar = MatrixProfile.DRIVING_CAR;
        drivingHgv = MatrixProfile.DRIVING_HGV;
        footHiking = MatrixProfile.FOOT_HIKING;
        footWalking = MatrixProfile.FOOT_WALKING;
        wheelchair = MatrixProfile.WHEELCHAIR;
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