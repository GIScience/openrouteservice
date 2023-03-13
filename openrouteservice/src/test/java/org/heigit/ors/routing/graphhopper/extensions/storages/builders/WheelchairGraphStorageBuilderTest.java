package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.reader.ReaderWay;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WheelchairGraphStorageBuilderTest {
    private WheelchairGraphStorageBuilder builder;

    public WheelchairGraphStorageBuilderTest() {
        builder = new WheelchairGraphStorageBuilder();
    }

    @BeforeEach
    void reset() {
        builder = new WheelchairGraphStorageBuilder();
    }

    @Test
    void TestProcessSeparateWay() {
        ReaderWay way = new ReaderWay(1);
        way.setTag("highway", "residential");
        way.setTag("width", "0.5");
        way.setTag("incline", "2");
        way.setTag("kerb:height", "0.03");
        way.setTag("smoothness", "good");
        way.setTag("surface", "asphalt");


        builder.processWay(way);

        WheelchairAttributes attrs = builder.getStoredAttributes(WheelchairGraphStorageBuilder.Side.NONE);
        assertEquals(50, attrs.getWidth());
        assertEquals(2, attrs.getIncline());
        assertEquals(3, attrs.getSlopedKerbHeight());
        assertEquals(2, attrs.getSmoothnessType());
        assertEquals(2, attrs.getSurfaceType());
        assertFalse(attrs.isSurfaceQualityKnown());
        assertFalse(attrs.isSuitable());
    }


    @Test
    void TestPedestrianisedWay() {
        ReaderWay way = new ReaderWay(1);
        way.setTag("highway", "track");
        way.setTag("width", "0.5");
        way.setTag("incline", "2");
        way.setTag("tracktype", "grade1");
        way.setTag("surface", "asphalt");


        builder.processWay(way);

        WheelchairAttributes attrs = builder.getStoredAttributes(WheelchairGraphStorageBuilder.Side.NONE);
        assertEquals(50, attrs.getWidth());
        assertEquals(2, attrs.getIncline());
        assertEquals(2, attrs.getSurfaceType());
        assertEquals(1, attrs.getTrackType());
        assertTrue(attrs.isSurfaceQualityKnown());
        assertTrue(attrs.isSuitable());
    }

    @Test
    void TestProcessWayWithLeftSidewalkAttached() {
        ReaderWay way = constructSidedWay("left");
        builder.processWay(way);
        WheelchairAttributes attrs = builder.getStoredAttributes(WheelchairGraphStorageBuilder.Side.LEFT);
        assertAttributeValues(attrs);
    }

    @Test
    void TestProcessWayWithRightSidewalkAttached() {
        ReaderWay way = constructSidedWay("right");
        builder.processWay(way);
        WheelchairAttributes attrs = builder.getStoredAttributes(WheelchairGraphStorageBuilder.Side.RIGHT);
        assertAttributeValues(attrs);
    }

    @Test
    void TestProcessWayWithBothSidewalksAttached() {
        ReaderWay way = constructSidedWay("both");
        builder.processWay(way);
        WheelchairAttributes attrs = builder.getStoredAttributes(WheelchairGraphStorageBuilder.Side.RIGHT);
        assertAttributeValues(attrs);
        attrs = builder.getStoredAttributes(WheelchairGraphStorageBuilder.Side.LEFT);
        assertAttributeValues(attrs);
    }

    @Test
    void TestProcessWayWithMultipleValues() {
        ReaderWay way = new ReaderWay(1);

        way.setTag("sidewalk:left:width", "0.6");
        way.setTag("sidewalk:right:width", "0.5");
        way.setTag("sidewalk:left:kerb:height:start", "0.01");
        way.setTag("sidewalk:right:kerb:height:start", "0.03");
        way.setTag("sidewalk:left:incline", "5");
        way.setTag("sidewalk:right:incline", "2");
        way.setTag("sidewalk:left:tracktype", "grade");
        way.setTag("sidewalk:right:tracktype", "grade2");
        way.setTag("sidewalk:left:smoothness", "intermediate");
        way.setTag("sidewalk:right:smoothness", "excellent");
        way.setTag("sidewalk:left:surface", "asphalt");
        way.setTag("sidewalk:right:surface", "paving_stones");

        WheelchairAttributes correctWheelchairAttributes = new WheelchairAttributes();
        correctWheelchairAttributes.setAttribute(WheelchairAttributes.Attribute.KERB, 3, false);
        correctWheelchairAttributes.setAttribute(WheelchairAttributes.Attribute.WIDTH, 50, false);
        correctWheelchairAttributes.setAttribute(WheelchairAttributes.Attribute.INCLINE, 5, false);
        correctWheelchairAttributes.setAttribute(WheelchairAttributes.Attribute.TRACK, 2, false);
        correctWheelchairAttributes.setAttribute(WheelchairAttributes.Attribute.SMOOTHNESS, 3, false);
        correctWheelchairAttributes.setAttribute(WheelchairAttributes.Attribute.SURFACE, 4, false);
        assertFalse(correctWheelchairAttributes.isSurfaceQualityKnown());
        assertFalse(correctWheelchairAttributes.isSuitable());

        builder.processWay(way);
        WheelchairAttributes left_attrs = builder.getStoredAttributes(WheelchairGraphStorageBuilder.Side.LEFT);
        assertTrue(left_attrs.isSurfaceQualityKnown());
        assertTrue(left_attrs.isSuitable());

        WheelchairAttributes right_attrs = builder.getStoredAttributes(WheelchairGraphStorageBuilder.Side.RIGHT);
        assertTrue(right_attrs.isSurfaceQualityKnown());
        assertTrue(right_attrs.isSuitable());

        WheelchairAttributes attrs = builder.combineAttributesOfWayWhenBothSidesPresent(new WheelchairAttributes());
        assertFalse(attrs.isSurfaceQualityKnown());
        assertFalse(attrs.isSuitable());

        assertEquals(wheelchairAttributesAsString(correctWheelchairAttributes), wheelchairAttributesAsString(attrs));
    }

    @Test
    void TestKerbHeightFromNode() {
        ReaderWay way = new ReaderWay(1);

        way.setTag("highway", "crossing");

        Map<Integer, Map<String,String>> nodeTags = new HashMap<>();
        Map<String, String> tags = new HashMap<>();
        tags.put("kerb:height", "0.03");
        nodeTags.put(1, tags);

        builder.processWay(way, new Coordinate[0], nodeTags);

        assertEquals(3, builder.getKerbHeightFromNodeTags());
    }

    @Test
    void TestAttachKerbHeightToCrossing() {
        builder = new WheelchairGraphStorageBuilder(true);

        ReaderWay way = new ReaderWay(1);

        way.setTag("footway", "crossing");

        Map<Integer, Map<String,String>> nodeTags = new HashMap<>();
        Map<String, String> tags = new HashMap<>();
        tags.put("kerb:height", "0.03");
        nodeTags.put(1, tags);

        builder.processWay(way, new Coordinate[0], nodeTags);

        assertEquals(3, builder.getKerbHeightForEdge(way));

        way = new ReaderWay(2);

        way.setTag("highway", "footway");

        builder.processWay(way, new Coordinate[0], nodeTags);

        assertEquals(-1, builder.getKerbHeightForEdge(way));
    }

    private ReaderWay constructSidedWay(String side) {
        ReaderWay way = new ReaderWay(1);
        way.setTag("sidewalk", side);
        way.setTag("sidewalk:" + side + ":width", "0.5");
        way.setTag("sidewalk:" + side + ":incline", "2");
        way.setTag("sidewalk:" + side + ":kerb:height:start", "0.03");
        way.setTag("sidewalk:" + side + ":kerb:height:end", "0.01");
        way.setTag("sidewalk:" + side + ":smoothness", "good");
        way.setTag("sidewalk:" + side + ":surface", "asphalt");
        way.setTag("sidewalk:" + side + ":tracktype", "grade4");

        return way;
    }

    private void assertAttributeValues(WheelchairAttributes attrs) {
        assertEquals(50, attrs.getWidth());
        assertEquals(2, attrs.getIncline());
        assertEquals(3, attrs.getSlopedKerbHeight());
        assertEquals(2, attrs.getSmoothnessType());
        assertEquals(2, attrs.getSurfaceType());
        assertEquals(4, attrs.getTrackType());
        assertTrue(attrs.isSuitable());
        assertTrue(attrs.isSurfaceQualityKnown());
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
