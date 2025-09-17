package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.ev.OsmWayId;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

import java.util.List;

public class OsmWayIdParser implements TagParser {
    private IntEncodedValue osmWayIdEnc;

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
// TODO: this code does not work for some reason (getMaxInt returns 0)
        if (way.getId() > osmWayIdEnc.getMaxInt())
            throw new IllegalArgumentException("OSM way-ID is too large: "
                    + way.getId() + " > " + osmWayIdEnc.getMaxInt() + ".");
        int wayId = Math.toIntExact(way.getId());
        osmWayIdEnc.setInt(false, edgeFlags, wayId);
return null;
    }
}
