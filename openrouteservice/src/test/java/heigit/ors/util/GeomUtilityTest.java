package heigit.ors.util;

import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeomUtilityTest {
    private static PointList pointList3D = new PointList(14, true);
    private static PointList pointList2D = new PointList(14, false);
    private static PointList emptyPointList = new PointList(14, false);
    @BeforeClass
    public static void setUp() {
        pointList3D.add(41.310824, -3.164063, 113.0);
        pointList3D.add(45.089036, 2.900391, 250.0);
        pointList3D.add(45.336702, -7.119141,244.55);
        pointList3D.add(38.959409, 7.294922, 409.0);
        pointList3D.add(50.958427, 12.304688, 122.0);
        pointList3D.add(18.729502, 38.408203, 400.443);
        pointList3D.add(-12.897489, -35.507813, 400.3333);

        pointList2D.add(41.310824, -3.164063);
        pointList2D.add(45.089036, 2.900391);
        pointList2D.add(45.336702, -7.11914);
        pointList2D.add(38.959409, 7.294922);
        pointList2D.add(50.958427, 12.304688);
        pointList2D.add(18.729502, 38.408203);
        pointList2D.add(-12.897489, -35.507813);

    }

    @Test
    public void calculateBoundingBox() {
        BBox _fallback = new BBox(0, 0, 0, 0);
        BBox bbox3D = GeomUtility.CalculateBoundingBox(pointList3D, _fallback);
        BBox bbox2D = GeomUtility.CalculateBoundingBox(pointList2D, _fallback);
        BBox bbox_fallback = GeomUtility.CalculateBoundingBox(emptyPointList,_fallback);

        Assert.assertEquals(-35.507813,bbox3D.minLon, 0.000009);
        Assert.assertEquals(38.408203,bbox3D.maxLon, 0.0000009);
        Assert.assertEquals(-12.897489,bbox3D.minLat, 0.000009);
        Assert.assertEquals(50.958427,bbox3D.maxLat, 0.0000009);
        Assert.assertEquals(113.0,bbox3D.minEle, 0.09);
        Assert.assertEquals(409.0,bbox3D.maxEle, 0.09);

        Assert.assertEquals(-35.507813,bbox2D.minLon, 0.000009);
        Assert.assertEquals(38.408203,bbox2D.maxLon, 0.000009);
        Assert.assertEquals(-12.897489,bbox2D.minLat, 0.000009);
        Assert.assertEquals(50.958427,bbox2D.maxLat, 0.000009);

        Assert.assertEquals(0f,bbox_fallback.minLon, 0.000009);
        Assert.assertEquals(0f,bbox_fallback.maxLon, 0.000009);
        Assert.assertEquals(0f,bbox_fallback.minLat, 0.000009);
        Assert.assertEquals(0f,bbox_fallback.maxLat, 0.000009);
    }
}
