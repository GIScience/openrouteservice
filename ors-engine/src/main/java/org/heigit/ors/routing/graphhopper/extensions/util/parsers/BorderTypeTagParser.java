package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.graphhopper.extensions.ev.BorderType;

import java.util.List;

public class BorderTypeTagParser implements TagParser {

    private final EnumEncodedValue<BorderType> borderTypeEnc;

    public BorderTypeTagParser() {
        this(new EnumEncodedValue<>(BorderType.KEY, BorderType.class));
    }

    public BorderTypeTagParser(EnumEncodedValue borderTypeEnc) {
        this.borderTypeEnc = borderTypeEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup lookup, List<EncodedValue> registerNewEncodedValue) {
        registerNewEncodedValue.add(borderTypeEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        return null;
    }
}
