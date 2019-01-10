package heigit.ors.api.requests.isochrones;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.common.DistanceUnit;
import heigit.ors.common.TravelRangeType;
import heigit.ors.common.TravellerInfo;
import heigit.ors.exceptions.ParameterValueException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class IsochronesRequestHandlerTest {
    IsochronesRequestHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = new IsochronesRequestHandler();
    }

    @Test
    public void convertSmoothing() throws ParameterValueException {
        Float smoothing = handler.convertSmoothing(10.234);
        Assert.assertEquals(10.234, smoothing, 0.01);
    }

    @Test(expected = ParameterValueException.class)
    public void convertSmoothingFailWhenTooHigh() throws ParameterValueException {
        handler.convertSmoothing(105.0);
    }

    @Test(expected = ParameterValueException.class)
    public void convertSmoothingFailWhenTooLow() throws ParameterValueException {
        handler.convertSmoothing(-5.0);
    }

    @Test
    public void convertLocationType() throws ParameterValueException {
        String locationType = handler.convertLocationType(IsochronesRequestEnums.LocationType.DESTINATION);
        Assert.assertEquals("destination", locationType);
        locationType = handler.convertLocationType(IsochronesRequestEnums.LocationType.START);
        Assert.assertEquals("start", locationType);
    }

    @Test
    public void convertRangeType() throws ParameterValueException {
        TravelRangeType rangeType = handler.convertRangeType(IsochronesRequestEnums.RangeType.DISTANCE);
        Assert.assertEquals(TravelRangeType.Distance, rangeType);
        rangeType = handler.convertRangeType(IsochronesRequestEnums.RangeType.TIME);
        Assert.assertEquals(TravelRangeType.Time, rangeType);
    }

    @Test
    public void convertAreaUnit() throws ParameterValueException {
        DistanceUnit unit = handler.convertAreaUnit(APIEnums.Units.KILOMETRES);
        Assert.assertEquals(DistanceUnit.Kilometers, unit);
        unit = handler.convertAreaUnit(APIEnums.Units.METRES);
        Assert.assertEquals(DistanceUnit.Meters, unit);
        unit = handler.convertAreaUnit(APIEnums.Units.MILES);
        Assert.assertEquals(DistanceUnit.Miles, unit);
    }

    @Test
    public void convertRangeUnit() throws ParameterValueException {
        DistanceUnit unit = handler.convertRangeUnit(APIEnums.Units.KILOMETRES);
        Assert.assertEquals(DistanceUnit.Kilometers, unit);
        unit = handler.convertRangeUnit(APIEnums.Units.METRES);
        Assert.assertEquals(DistanceUnit.Meters, unit);
        unit = handler.convertRangeUnit(APIEnums.Units.MILES);
        Assert.assertEquals(DistanceUnit.Miles, unit);
    }

    @Test
    public void convertSingleCoordinate() throws ParameterValueException {
        Coordinate coord = handler.convertSingleCoordinate(new Double[]{123.4, 321.0});
        Assert.assertEquals(123.4, coord.x, 0.0001);
        Assert.assertEquals(321.0, coord.y, 0.0001);
    }

    @Test(expected = ParameterValueException.class)
    public void convertSingleCoordinateInvalidLengthShort() throws ParameterValueException {
        handler.convertSingleCoordinate(new Double[]{123.4});
    }

    @Test(expected = ParameterValueException.class)
    public void convertSingleCoordinateInvalidLengthLong() throws ParameterValueException {
        handler.convertSingleCoordinate(new Double[]{123.4, 123.4, 123.4});
    }

    @Test
    public void setRangeAndIntervals() throws ParameterValueException {
        TravellerInfo info = new TravellerInfo();
        List<Double> rangeValues = new ArrayList<>();
        rangeValues.add(20.0);
        double intervalValue = 10;

        handler.setRangeAndIntervals(info, rangeValues, intervalValue);

        Assert.assertEquals(10.0, info.getRanges()[0], 0.0f);
        Assert.assertEquals(20.0, info.getRanges()[1], 0.0f);

        info = new TravellerInfo();
        rangeValues = new ArrayList<>();
        rangeValues.add(15.0);
        rangeValues.add(30.0);
        handler.setRangeAndIntervals(info, rangeValues, intervalValue);
        Assert.assertEquals(15.0, info.getRanges()[0], 0.0f);
        Assert.assertEquals(30.0, info.getRanges()[1], 0.0f);

    }

    @Test
    public void convertAttributes() {
        IsochronesRequestEnums.Attributes[] atts = new IsochronesRequestEnums.Attributes[]{IsochronesRequestEnums.Attributes.AREA, IsochronesRequestEnums.Attributes.REACH_FACTOR, IsochronesRequestEnums.Attributes.TOTAL_POPULATION};
        String[] attStr = handler.convertAttributes(atts);
        Assert.assertEquals("area", attStr[0]);
        Assert.assertEquals("reachfactor", attStr[1]);
        Assert.assertEquals("total_pop", attStr[2]);
    }

    @Test
    public void convertCalcMethod() throws ParameterValueException {
        String calcMethod = handler.convertCalcMethod(IsochronesRequestEnums.CalculationMethod.CONCAVE_BALLS);
        Assert.assertEquals("concaveballs", calcMethod);
        calcMethod = handler.convertCalcMethod(IsochronesRequestEnums.CalculationMethod.GRID);
        Assert.assertEquals("grid", calcMethod);
    }
}