package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

import java.util.List;

public class HillIndexParser implements TagParser {
    private IntEncodedValue hillIndexEnc;

    public HillIndexParser() {
        this(HillIndex.create());
    }

    public HillIndexParser(IntEncodedValue hillIndexEnc) {
        this.hillIndexEnc = hillIndexEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(hillIndexEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean b, IntsRef relationFlags) {
        byte hillIndexFwd = way.getTag("ors:hill_index_fwd", (byte) 0);
        byte hillIndexBwd = way.getTag("ors:hill_index_bwd", (byte) 0);
        hillIndexEnc.setInt(false, edgeFlags, hillIndexFwd);
        hillIndexEnc.setInt(true, edgeFlags, hillIndexBwd);
        return edgeFlags;
    }
}
