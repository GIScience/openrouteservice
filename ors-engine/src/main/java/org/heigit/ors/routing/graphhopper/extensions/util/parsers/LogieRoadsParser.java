package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.LogieRoads;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

import java.util.List;

public class LogieRoadsParser implements TagParser {
    private final EnumEncodedValue<LogieRoads> logieRoadsEnc;

    public LogieRoadsParser() {
        this(new EnumEncodedValue<>(LogieRoads.KEY, LogieRoads.class));
    }

    public LogieRoadsParser(EnumEncodedValue<LogieRoads> logieRoadsEnc) {
        this.logieRoadsEnc = logieRoadsEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(logieRoadsEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeRef, ReaderWay readerWay, boolean b, IntsRef relationRef) {
        // do nothing
        return edgeRef;
    }
}
