package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

import java.util.List;

public class LogieBridgesParser implements TagParser {
    private final EnumEncodedValue<LogieBridges> logieBridgesEnc;

    public LogieBridgesParser() {
        this(new EnumEncodedValue<>(LogieBridges.KEY, LogieBridges.class));
    }

    public LogieBridgesParser(EnumEncodedValue<LogieBridges> logieBridgesEnc) {
        this.logieBridgesEnc = logieBridgesEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(logieBridgesEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeRef, ReaderWay readerWay, boolean b, IntsRef relationRef) {
        // do nothing
        return edgeRef;
    }
}
