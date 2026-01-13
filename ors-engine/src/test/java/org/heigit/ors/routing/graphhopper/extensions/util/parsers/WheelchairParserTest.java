package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import org.heigit.ors.routing.graphhopper.extensions.util.WheelchairAttributesEncodedValues;
import org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WheelchairParserTest {
    private EncodingManager em;
    private IntsRef intsRef, relFlags;

    private WheelchairSurfaceParser parserSurface;
    private WheelchairSmoothnessParser parserSmoothness;
    private WheelchairTrackTypeParser parserTrackType;
    private WheelchairInclineParser parserIncline;
    private WheelchairKerbHeightParser parserKerbHeight;
    private WheelchairWidthParser parserWidth;
    private WheelchairSuitableParser parserSuitable;
    private WheelchairSurfaceQualityKnownParser parserSurfaceQualityKnown;
    private WheelchairSideParser parserSide;

    public WheelchairParserTest() {
        setUp();
    }

    @BeforeEach
    void setUp() {
        parserSurface = new WheelchairSurfaceParser();
        parserSmoothness = new WheelchairSmoothnessParser();
        parserTrackType = new WheelchairTrackTypeParser();
        parserIncline = new WheelchairInclineParser();
        parserKerbHeight = new WheelchairKerbHeightParser();
        parserWidth = new WheelchairWidthParser();
        parserSuitable = new WheelchairSuitableParser();
        parserSurfaceQualityKnown = new WheelchairSurfaceQualityKnownParser();
        parserSide = new WheelchairSideParser();

        em = new EncodingManager.Builder()
                .add(parserSurface)
                .add(parserSmoothness)
                .add(parserTrackType)
                .add(parserIncline)
                .add(parserKerbHeight)
                .add(parserWidth)
                .add(parserSuitable)
                .add(parserSurfaceQualityKnown)
                .add(parserSide).build();

        relFlags = em.createRelationFlags();
        intsRef = em.createEdgeFlags();
    }

    void executeParsers(ReaderWay way) {
        parserSurface.handleWayTags(intsRef, way, false, relFlags);
        parserSmoothness.handleWayTags(intsRef, way, false, relFlags);
        parserTrackType.handleWayTags(intsRef, way, false, relFlags);
        parserIncline.handleWayTags(intsRef, way, false, relFlags);
        parserKerbHeight.handleWayTags(intsRef, way, false, relFlags);
        parserWidth.handleWayTags(intsRef, way, false, relFlags);
        parserSuitable.handleWayTags(intsRef, way, false, relFlags);
        parserSurfaceQualityKnown.handleWayTags(intsRef, way, false, relFlags);
        parserSide.handleWayTags(intsRef, way, false, relFlags);
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

        executeParsers(way);

        WheelchairAttributesEncodedValues encValues = new WheelchairAttributesEncodedValues(em);
        WheelchairAttributes attrs = encValues.getAttributes(intsRef);

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

        executeParsers(way);

        WheelchairAttributesEncodedValues encValues = new WheelchairAttributesEncodedValues(em);
        WheelchairAttributes attrs = encValues.getAttributes(intsRef);

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
        executeParsers(way);
        WheelchairAttributesEncodedValues encValues = new WheelchairAttributesEncodedValues(em);
        WheelchairAttributes attrs = encValues.getAttributes(intsRef);
        assertAttributeValues(attrs);
    }

    @Test
    void TestProcessWayWithRightSidewalkAttached() {
        ReaderWay way = constructSidedWay("right");
        executeParsers(way);
        WheelchairAttributesEncodedValues encValues = new WheelchairAttributesEncodedValues(em);
        WheelchairAttributes attrs = encValues.getAttributes(intsRef);
        assertAttributeValues(attrs);
    }

    @Test
    void TestProcessWayWithBothSidewalksAttached() {
        ReaderWay way = constructSidedWay("both");
        WheelchairAttributes attrs = parseForSide(way, "right");
        assertAttributeValues(attrs);

        attrs = parseForSide(way, "left");
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

        WheelchairAttributes left_attrs = parseForSide(way, "left");
        assertTrue(left_attrs.isSurfaceQualityKnown());
        assertTrue(left_attrs.isSuitable());
        assertEquals(60, left_attrs.getWidth());
        assertEquals(5, left_attrs.getIncline());
        assertEquals(1, left_attrs.getSlopedKerbHeight());

        WheelchairAttributes right_attrs = parseForSide(way, "right");
        assertTrue(right_attrs.isSurfaceQualityKnown());
        assertTrue(right_attrs.isSuitable());
        assertEquals(50, right_attrs.getWidth());
        assertEquals(2, right_attrs.getIncline());
        assertEquals(3, right_attrs.getSlopedKerbHeight());

        WheelchairAttributes attrs = parseForSide(way, "both");
        assertEquals(wheelchairAttributesAsString(correctWheelchairAttributes), wheelchairAttributesAsString(attrs));
    }

    private WheelchairAttributes parseForSide(ReaderWay way, String side){
        assert(side.equals("left") || side.equals("right") || side.equals("both"));
        if(side.equals("both")) {
            way.removeTag("ors-sidewalk-side");
        } else {
            way.setTag("ors-sidewalk-side", side);
        }

        executeParsers(way);
        WheelchairAttributesEncodedValues encValues = new WheelchairAttributesEncodedValues(em);
        return encValues.getAttributes(intsRef);
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
        way.setTag("ors-sidewalk-side", side);

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
