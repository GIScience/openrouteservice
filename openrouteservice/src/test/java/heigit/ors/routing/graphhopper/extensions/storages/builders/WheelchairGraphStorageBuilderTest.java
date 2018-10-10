package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.VirtualEdgeIteratorState;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class WheelchairGraphStorageBuilderTest {
    private WheelchairGraphStorageBuilder builder;

    public WheelchairGraphStorageBuilderTest() {
        builder = new WheelchairGraphStorageBuilder();
    }

    @Before
    public void reset() {
        builder = new WheelchairGraphStorageBuilder();
    }

    @Test
    public void TestProcessSeparateWay() {
        ReaderWay way = new ReaderWay(1);
        way.setTag("width", "0.5");
        way.setTag("incline", "2");
        way.setTag("kerb:height", "0.03");
        way.setTag("smoothness", "good");
        way.setTag("surface", "asphalt");
        way.setTag("tracktype", "grade4");

        builder.processWay(way);

        WheelchairAttributes attrs = builder.getStoredAttributes(null);
        Assert.assertEquals(0.5f, attrs.getWidth());
        Assert.assertEquals(2f, attrs.getIncline());
        Assert.assertEquals(0.03f, attrs.getSlopedKerbHeight());
        Assert.assertEquals(2, attrs.getSmoothnessType());
        Assert.assertEquals(2, attrs.getSurfaceType());
        Assert.assertEquals(4, attrs.getTrackType());
    }

    @Test
    public void TestProcessWayWithLeftSidewalkAttached() {
        ReaderWay way = constructSidedWay("left");
        builder.processWay(way);
        WheelchairAttributes attrs = builder.getStoredAttributes(WheelchairGraphStorageBuilder.Side.LEFT);
        assertAttributeValues(attrs);
    }
    @Test
    public void TestProcessWayWithRightSidewalkAttached() {
        ReaderWay way = constructSidedWay("right");
        builder.processWay(way);
        WheelchairAttributes attrs = builder.getStoredAttributes(WheelchairGraphStorageBuilder.Side.RIGHT);
        assertAttributeValues(attrs);
    }

    @Test
    public void TestProcessWayWithMultipleValues() {
        ReaderWay way = new ReaderWay(1);

        way.setTag("sidewalk:left:width", "0.6");
        way.setTag("sidewalk:right:width", "0.5");
        way.setTag("sidewalk:left:kerb:height", "0.01");
        way.setTag("sidewalk:right:kerb:height", "0.03");
        way.setTag("sidewalk:left:incline", "5");
        way.setTag("sidewalk:right:incline", "2");
        way.setTag("sidewalk:left:tracktype", "grade");
        way.setTag("sidewalk:right:tracktype", "grade2");
        way.setTag("sidewalk:left:smoothness", "intermediate");
        way.setTag("sidewalk:right:smoothness", "excellent");
        way.setTag("sidewalk:left:surface", "asphalt");
        way.setTag("sidewalk:right:surface", "paving_stones");

        WheelchairAttributes correctWheelchairAttributes = new WheelchairAttributes();
        correctWheelchairAttributes.setAttribute(WheelchairAttributes.Attribute.KERB, "0.03");
        correctWheelchairAttributes.setAttribute(WheelchairAttributes.Attribute.WIDTH, "0.5");
        correctWheelchairAttributes.setAttribute(WheelchairAttributes.Attribute.INCLINE, "5");
        correctWheelchairAttributes.setAttribute(WheelchairAttributes.Attribute.TRACK, "2");
        correctWheelchairAttributes.setAttribute(WheelchairAttributes.Attribute.SMOOTHNESS, "3");
        correctWheelchairAttributes.setAttribute(WheelchairAttributes.Attribute.SURFACE, "4");
        builder.processWay(way);

        WheelchairAttributes attrs = builder.combineAttributesOfWayWhenBothSidesPresent(new WheelchairAttributes());

        Assert.assertEquals(wheelchairAttributesAsString(correctWheelchairAttributes), wheelchairAttributesAsString(attrs));
    }

    @Test
    public void TestKerbHeightFromNode() {
        ReaderWay way = new ReaderWay(1);

        way.setTag("highway", "crossing");

        HashMap<Integer, HashMap<String,String>> nodeTags = new HashMap<>();
        HashMap<String, String> tags = new HashMap<>();
        tags.put("kerb:height", "0.03");
        nodeTags.put(1, tags);

        builder.processWay(way, new Coordinate[0], nodeTags);

        Assert.assertEquals(0.03f, builder.getKerbHeightForWayFromNodeTags(1, 2));
    }

    @Test
    public void TestAttachKerbHeightToCrossing() {
        builder = new WheelchairGraphStorageBuilder(true);

        ReaderWay way = new ReaderWay(1);

        way.setTag("footway", "crossing");

        HashMap<Integer, HashMap<String,String>> nodeTags = new HashMap<>();
        HashMap<String, String> tags = new HashMap<>();
        tags.put("kerb:height", "0.03");
        nodeTags.put(1, tags);

        builder.processWay(way, new Coordinate[0], nodeTags);
        EdgeIteratorState edge = new VirtualEdgeIteratorState(1,1,1,1,2,1,1,"",null);

        Assert.assertEquals(0.03f, builder.getKerbHeightForWay(way, edge));

        way = new ReaderWay(2);

        way.setTag("highway", "footway");

        builder.processWay(way, new Coordinate[0], nodeTags);

        Assert.assertEquals(-1f, builder.getKerbHeightForWay(way, edge));
    }

    private ReaderWay constructSidedWay(String side) {
        ReaderWay way = new ReaderWay(1);
        way.setTag("sidewalk", side);
        way.setTag("sidewalk:" + side + ":width", "0.5");
        way.setTag("sidewalk:" + side + ":incline", "2");
        way.setTag("sidewalk:" + side + ":kerb:height", "0.03");
        way.setTag("sidewalk:" + side + ":smoothness", "good");
        way.setTag("sidewalk:" + side + ":surface", "asphalt");
        way.setTag("sidewalk:" + side + ":tracktype", "grade4");

        return way;
    }

    private void assertAttributeValues(WheelchairAttributes attrs) {
        Assert.assertEquals(0.5f, attrs.getWidth());
        Assert.assertEquals(2f, attrs.getIncline());
        Assert.assertEquals(0.03f, attrs.getSlopedKerbHeight());
        Assert.assertEquals(2, attrs.getSmoothnessType());
        Assert.assertEquals(2, attrs.getSurfaceType());
        Assert.assertEquals(4, attrs.getTrackType());
    }

    private String wheelchairAttributesAsString(WheelchairAttributes attrs) {
        return attrs.getIncline() + ","
                + attrs.getSlopedKerbHeight() + ","
                + attrs.getSmoothnessType() + ","
                + attrs.getSurfaceType() + ","
                + attrs.getTrackType() + ","
                + attrs.getWidth();
    }
}
