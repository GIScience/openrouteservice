package heigit.ors.services.isochrones.requestprocessors.json;

import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;


public class JsonIsochronesRequestProcessorTest {

    private Envelope negativeEnv;
    private Envelope positiveEnv;
    private Envelope mixedEnv;

    @Before
    public void setUp() {
        ArrayList<Double[]> bareNegativeCoordinateList = new ArrayList<>(12);
        Coordinate[] negativeCoordinates = new Coordinate[12];
        bareNegativeCoordinateList.add(new Double[]{-77.033874, -12.122793});
        bareNegativeCoordinateList.add(new Double[]{-77.032343, -12.125334});
        bareNegativeCoordinateList.add(new Double[]{-77.031219, -12.127203});
        bareNegativeCoordinateList.add(new Double[]{-77.030908, -12.127332});
        bareNegativeCoordinateList.add(new Double[]{-77.026632, -12.126794});
        bareNegativeCoordinateList.add(new Double[]{-77.025097, -12.12488});
        bareNegativeCoordinateList.add(new Double[]{-77.025082, -12.124412});
        bareNegativeCoordinateList.add(new Double[]{-77.026141, -12.123228});
        bareNegativeCoordinateList.add(new Double[]{-77.030908, -12.120505});
        bareNegativeCoordinateList.add(new Double[]{-77.031669, -12.120756});
        bareNegativeCoordinateList.add(new Double[]{-77.033806, -12.121682});
        bareNegativeCoordinateList.add(new Double[]{-77.033874, -12.122793});

        ArrayList<Double[]> barePositiveCoordinateList = new ArrayList<>(12);
        Coordinate[] positiveCoordinates = new Coordinate[14];
        barePositiveCoordinateList.add(new Double[]{2.288033, 48.856386});
        barePositiveCoordinateList.add(new Double[]{2.291753, 48.854886});
        barePositiveCoordinateList.add(new Double[]{2.300739, 48.85848});
        barePositiveCoordinateList.add(new Double[]{2.302588, 48.859432});
        barePositiveCoordinateList.add(new Double[]{2.304801, 48.860647});
        barePositiveCoordinateList.add(new Double[]{2.304745, 48.864247});
        barePositiveCoordinateList.add(new Double[]{2.301436, 48.864227});
        barePositiveCoordinateList.add(new Double[]{2.300037, 48.864114});
        barePositiveCoordinateList.add(new Double[]{2.299522, 48.864051});
        barePositiveCoordinateList.add(new Double[]{2.291279, 48.862698});
        barePositiveCoordinateList.add(new Double[]{2.289955, 48.862126});
        barePositiveCoordinateList.add(new Double[]{2.289711, 48.861983});
        barePositiveCoordinateList.add(new Double[]{2.289098, 48.860238});
        barePositiveCoordinateList.add(new Double[]{2.288033, 48.856386});

        ArrayList<Double[]> bareMixedCoordinateList = new ArrayList<>(12);
        Coordinate[] mixedCoordinates = new Coordinate[13];
        bareMixedCoordinateList.add(new Double[]{18.395489, -33.907743});
        bareMixedCoordinateList.add(new Double[]{18.395657, -33.908133});
        bareMixedCoordinateList.add(new Double[]{18.39697, -33.90904});
        bareMixedCoordinateList.add(new Double[]{18.401868, -33.908735});
        bareMixedCoordinateList.add(new Double[]{18.403667, -33.907228});
        bareMixedCoordinateList.add(new Double[]{18.409442, -33.90136});
        bareMixedCoordinateList.add(new Double[]{18.40994, -33.899745});
        bareMixedCoordinateList.add(new Double[]{18.409858, -33.89932});
        bareMixedCoordinateList.add(new Double[]{18.409166, -33.897771});
        bareMixedCoordinateList.add(new Double[]{18.407953, -33.897864});
        bareMixedCoordinateList.add(new Double[]{18.40041, -33.901431});
        bareMixedCoordinateList.add(new Double[]{18.395636, -33.905697});
        bareMixedCoordinateList.add(new Double[]{18.395489, -33.907743});


        for (int i = 0; i < bareNegativeCoordinateList.size(); i++) {
            Coordinate coordinate = new Coordinate();
            Double[] bareCoordinate = bareNegativeCoordinateList.get(i);
            coordinate.x = bareCoordinate[0];
            coordinate.y = bareCoordinate[1];
            negativeCoordinates[i] = coordinate;
        }

        for (int i = 0; i < barePositiveCoordinateList.size(); i++) {
            Coordinate coordinate = new Coordinate();
            Double[] bareCoordinate = barePositiveCoordinateList.get(i);
            coordinate.x = bareCoordinate[0];
            coordinate.y = bareCoordinate[1];
            positiveCoordinates[i] = coordinate;
        }

        for (int i = 0; i < bareMixedCoordinateList.size(); i++) {
            Coordinate coordinate = new Coordinate();
            Double[] bareCoordinate = bareMixedCoordinateList.get(i);
            coordinate.x = bareCoordinate[0];
            coordinate.y = bareCoordinate[1];
            mixedCoordinates[i] = coordinate;
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        Polygon polygonFactory = geometryFactory.createPolygon(negativeCoordinates);
        LineString negativeShell = polygonFactory.getExteriorRing();

        geometryFactory = new GeometryFactory();
        polygonFactory = geometryFactory.createPolygon(positiveCoordinates);
        LineString positiveShell = polygonFactory.getExteriorRing();

        geometryFactory = new GeometryFactory();
        polygonFactory = geometryFactory.createPolygon(mixedCoordinates);
        LineString mixedShell = polygonFactory.getExteriorRing();

        negativeEnv = negativeShell.getEnvelopeInternal();
        positiveEnv = positiveShell.getEnvelopeInternal();
        mixedEnv = mixedShell.getEnvelopeInternal();
    }

    @Test
    public void constructNegativeIsochroneBBoxTest() {
        BBox bbox = JsonIsochronesRequestProcessor.constructIsochroneBBox(negativeEnv);
        BBox expectedBBox = new BBox(-77.033874, -77.025082, -12.127332, -12.120505);
        Assert.assertTrue(bbox.isValid());
        Assert.assertEquals(expectedBBox.maxLat, bbox.maxLat, 0.0);
        Assert.assertEquals(expectedBBox.maxLon, bbox.maxLon, 0.0);
        Assert.assertEquals(expectedBBox.minLat, bbox.minLat, 0.0);
        Assert.assertEquals(expectedBBox.minLon, bbox.minLon, 0.0);
        Assert.assertEquals(Double.NaN, bbox.minEle, 0.0);
        Assert.assertEquals(Double.NaN, bbox.maxEle, 0.0);
    }

    @Test
    public void constructPositiveIsochroneBBoxTest() {
        BBox bbox = JsonIsochronesRequestProcessor.constructIsochroneBBox(positiveEnv);
        BBox expectedBBox = new BBox(2.288033, 2.304801, 48.854886, 48.864247);
        Assert.assertTrue(bbox.isValid());
        Assert.assertEquals(expectedBBox.maxLat, bbox.maxLat, 0.0);
        Assert.assertEquals(expectedBBox.maxLon, bbox.maxLon, 0.0);
        Assert.assertEquals(expectedBBox.minLat, bbox.minLat, 0.0);
        Assert.assertEquals(expectedBBox.minLon, bbox.minLon, 0.0);
        Assert.assertEquals(Double.NaN, bbox.minEle, 0.0);
        Assert.assertEquals(Double.NaN, bbox.maxEle, 0.0);
    }

    @Test
    public void constructMixedIsochroneBBoxTest() {
        BBox bbox = JsonIsochronesRequestProcessor.constructIsochroneBBox(mixedEnv);
        BBox expectedBBox = new BBox(18.395489, 18.409940, -33.909040, -33.897771);
        Assert.assertTrue(bbox.isValid());
        Assert.assertEquals(expectedBBox.maxLat, bbox.maxLat, 0.0);
        Assert.assertEquals(expectedBBox.maxLon, bbox.maxLon, 0.0);
        Assert.assertEquals(expectedBBox.minLat, bbox.minLat, 0.0);
        Assert.assertEquals(expectedBBox.minLon, bbox.minLon, 0.0);
        Assert.assertEquals(Double.NaN, bbox.minEle, 0.0);
        Assert.assertEquals(Double.NaN, bbox.maxEle, 0.0);
    }
}