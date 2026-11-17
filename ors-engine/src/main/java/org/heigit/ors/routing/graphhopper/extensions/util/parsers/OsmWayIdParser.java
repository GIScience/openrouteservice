package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.ev.OsmWayId;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;
import org.apache.log4j.Logger;

import java.util.List;

public class OsmWayIdParser implements TagParser {
    private static final Logger LOGGER = Logger.getLogger(OsmWayIdParser.class);
    private IntEncodedValue osmWayIdEnc;
    private boolean negativeWayIdWarned = false;

    public OsmWayIdParser() {
        this(OsmWayId.create());
    }

    public OsmWayIdParser(IntEncodedValue osmWayIdEnc) {
        this.osmWayIdEnc = osmWayIdEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(osmWayIdEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean b, IntsRef relationFlags) {
        if (way.getId() > osmWayIdEnc.getMaxInt())
            throw new IllegalArgumentException("OSM way-ID is too large: "
                    + way.getId() + " > " + osmWayIdEnc.getMaxInt() + ".");
        int wayId;
        if (way.getId() < 0) {
            if (!negativeWayIdWarned) {
                LOGGER.warn("Negative OSM way-ID " + way.getId() + " cannot be stored and is replaced by 0. "
                        + "Further negative way-IDs are also replaced by 0 without additional warnings.");
                negativeWayIdWarned = true;
            }
            wayId = 0;
        } else {
            wayId = Math.toIntExact(way.getId());
        }
        osmWayIdEnc.setInt(false, edgeFlags, wayId);
        return edgeFlags;
    }
}
