package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

import java.util.List;


public class LogieBordersParser implements TagParser {
    private final EnumEncodedValue<LogieBorders> logieBordersEnc;

    public LogieBordersParser() {
        this(new EnumEncodedValue<>(LogieBorders.KEY, LogieBorders.class));
    }

    public LogieBordersParser(EnumEncodedValue<LogieBorders> logieBordersEnc) {
        this.logieBordersEnc = logieBordersEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(logieBordersEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay readerWay, boolean b, IntsRef relationFlags) {
        // do nothing
        return edgeFlags;
    }
}
