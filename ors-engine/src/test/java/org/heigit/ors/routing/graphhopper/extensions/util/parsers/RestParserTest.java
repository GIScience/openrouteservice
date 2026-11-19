package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.Rest;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestParserTest {

    @Test
    void registersRestEncodedValue() {
        EncodingManager em = new EncodingManager.Builder().add(new RestParser()).build();

        assertTrue(em.hasEncodedValue(Rest.KEY));
    }

    @Test
    void wayTagsDoNotSetRestPoints() {
        RestParser parser = new RestParser();
        EncodingManager em = new EncodingManager.Builder().add(parser).build();
        DecimalEncodedValue restEnc = em.getDecimalEncodedValue(Rest.KEY);
        IntsRef edgeFlags = em.createEdgeFlags();
        ReaderWay way = new ReaderWay(1);
        way.setTag("highway", "footway");
        way.setTag("bench", "yes");

        IntsRef result = parser.handleWayTags(edgeFlags, way, false, em.createRelationFlags());

        assertSame(edgeFlags, result);
        assertEquals(-1, restEnc.getDecimal(false, result));
    }
}
