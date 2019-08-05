package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OSMAttachedSidewalkProcessorTest {
    OSMAttachedSidewalkProcessor processor;

    public OSMAttachedSidewalkProcessorTest() {
        processor = new OSMAttachedSidewalkProcessor();
    }

    @Test
    public void TestDetectSidewalkInfoFromTags() {
        ReaderWay way = new ReaderWay(1);
        way.setTag("sidewalk:left:surface", "asphalt");

        assertTrue(processor.hasSidewalkInfo(way));

        way = new ReaderWay(1);
        way.setTag("footway:right:width", "0.5");
        assertTrue(processor.hasSidewalkInfo(way));

        way = new ReaderWay(1);
        assertFalse(processor.hasSidewalkInfo(way));
    }

    @Test
    public void TestIdentificationOfSidesWithSidewalkInfo() {
        ReaderWay way = new ReaderWay(1);
        way.setTag("sidewalk:left:surface", "asphalt");
        assertEquals(OSMAttachedSidewalkProcessor.Side.LEFT, processor.identifySidesWhereSidewalkIsPresent(way));

        way = new ReaderWay(1);
        way.setTag("sidewalk", "both");
        assertEquals(OSMAttachedSidewalkProcessor.Side.BOTH, processor.identifySidesWhereSidewalkIsPresent(way));

        way = new ReaderWay(1);
        way.setTag("sidewalk", "none");
        assertEquals(OSMAttachedSidewalkProcessor.Side.NONE, processor.identifySidesWhereSidewalkIsPresent(way));

        way = new ReaderWay(1);
        way.setTag("footway:right:width", "0.5");
        assertEquals(OSMAttachedSidewalkProcessor.Side.RIGHT, processor.identifySidesWhereSidewalkIsPresent(way));
    }

    @Test
    public void TestAttachingORSSidewalkSideTagForWayWithSingleSide() {
        ReaderWay way = new ReaderWay(1);

        way = processor.attachSidewalkTag(way, OSMAttachedSidewalkProcessor.Side.LEFT);
        assertTrue(way.hasTag("ors-sidewalk-side"));

        String side = way.getTag("ors-sidewalk-side");
        assertEquals("left", side);

        way = new ReaderWay(1);

        way = processor.attachSidewalkTag(way, OSMAttachedSidewalkProcessor.Side.RIGHT);
        assertTrue(way.hasTag("ors-sidewalk-side"));

        side = way.getTag("ors-sidewalk-side");
        assertEquals("right", side);

    }

    @Test
    public void TestAttchingNoSidewalkRemovesAnyAlreadyAttachedORSSidewalkTags() {
        ReaderWay way = new ReaderWay(1);

        way = processor.attachSidewalkTag(way, OSMAttachedSidewalkProcessor.Side.NONE);
        assertFalse(way.hasTag("ors-sidewalk-side"));

        way = new ReaderWay(1);
        way = processor.attachSidewalkTag(way, OSMAttachedSidewalkProcessor.Side.LEFT);
        way = processor.attachSidewalkTag(way, OSMAttachedSidewalkProcessor.Side.NONE);
        assertFalse(way.hasTag("ors-sidewalk-side"));
    }

    @Test
    public void TestAttachingORSSidealkTagsWhenBothSidesHaveValues() {
        ReaderWay way = new ReaderWay(1);

        way = processor.attachSidewalkTag(way, OSMAttachedSidewalkProcessor.Side.BOTH);
        assertTrue(way.hasTag("ors-sidewalk-side"));

        String side = way.getTag("ors-sidewalk-side");
        assertEquals("left", side);

        way = processor.attachSidewalkTag(way, OSMAttachedSidewalkProcessor.Side.BOTH);
        assertTrue(way.hasTag("ors-sidewalk-side"));

        side = way.getTag("ors-sidewalk-side");
        assertEquals("right", side);
    }
}
