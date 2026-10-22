package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.ev.OsmWayId;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OsmWayIdParserTest {
    private IntEncodedValue osmWayIdEnc;
    private OsmWayIdParser parser;
    private IntsRef intsRef;
    private IntsRef relFlags;

    @BeforeEach
    void setUp() {
        parser = new OsmWayIdParser();
        EncodingManager em = new EncodingManager.Builder().add(parser).build();
        osmWayIdEnc = em.getIntEncodedValue(OsmWayId.KEY);
        relFlags = em.createRelationFlags();
        intsRef = em.createEdgeFlags();
    }

    @Test
    void testPositiveWayId() {
        parser.handleWayTags(intsRef, new ReaderWay(123456789L), false, relFlags);
        assertEquals(123456789, osmWayIdEnc.getInt(false, intsRef));
    }

    @Test
    void testNegativeWayIdIsStoredAsZero() {
        parser.handleWayTags(intsRef, new ReaderWay(-42L), false, relFlags);
        assertEquals(0, osmWayIdEnc.getInt(false, intsRef));
    }

    @Test
    void testTooLargeWayIdThrows() {
        ReaderWay way = new ReaderWay(1L + osmWayIdEnc.getMaxInt());
        assertThrows(IllegalArgumentException.class, () -> parser.handleWayTags(intsRef, way, false, relFlags));
    }
}
