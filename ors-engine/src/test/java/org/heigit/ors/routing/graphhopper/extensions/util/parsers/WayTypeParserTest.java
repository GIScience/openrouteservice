package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.WayType;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMAttachedSidewalkProcessor.KEY_ORS_SIDEWALK_SIDE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WayTypeParserTest {
    private EncodingManager em;
    private EnumEncodedValue<WayType> wayTypeEnc;
    private WayTypeParser parser;
    private IntsRef intsRef, relFlags;

    @BeforeEach
    void setUp() {
        parser = new WayTypeParser();
        em = new EncodingManager.Builder().add(parser).build();
        wayTypeEnc = em.getEnumEncodedValue(WayType.KEY, WayType.class);
        relFlags = em.createRelationFlags();
        intsRef = em.createEdgeFlags();
    }

    @Test
    void testHighwayMissing() {
        ReaderWay readerWay = new ReaderWay(1);
        parser.handleWayTags(intsRef, readerWay, false, relFlags);
        assertEquals(WayType.UNKNOWN, wayTypeEnc.getEnum(false, intsRef));
    }

    @Test
    void testHighwayTrack() {
        ReaderWay readerWay = new ReaderWay(1);
        readerWay.setTag("highway", "track");
        parser.handleWayTags(intsRef, readerWay, false, relFlags);
        assertEquals(WayType.TRACK, wayTypeEnc.getEnum(false, intsRef));
    }

    @Test
    void testHighwayResidential() {
        ReaderWay readerWay = new ReaderWay(1);
        readerWay.setTag("highway", "residential");
        parser.handleWayTags(intsRef, readerWay, false, relFlags);
        assertEquals(WayType.STREET, wayTypeEnc.getEnum(false, intsRef));
    }

    @Test
    void testFerryRoute() {
        ReaderWay readerWay = new ReaderWay(1);
        readerWay.setTag("route", "ferry");
        parser.handleWayTags(intsRef, readerWay, false, relFlags);
        assertEquals(WayType.FERRY, wayTypeEnc.getEnum(false, intsRef));
    }

    @Test
    void testSidewalkTag() {
        ReaderWay readerWay = new ReaderWay(1);
        readerWay.setTag("highway", "residential");
        readerWay.setTag(KEY_ORS_SIDEWALK_SIDE, "both");
        parser.handleWayTags(intsRef, readerWay, false, relFlags);
        assertEquals(WayType.FOOTWAY, wayTypeEnc.getEnum(false, intsRef));
    }
}