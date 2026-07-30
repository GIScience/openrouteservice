package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.Helper;

import java.util.List;

public class SacScaleParser implements TagParser {
    private IntEncodedValue sacScaleEnc;

    public SacScaleParser() {
        this(SacScale.create());
    }

    public SacScaleParser(IntEncodedValue sacScaleEnc) {
        this.sacScaleEnc = sacScaleEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(sacScaleEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean b, IntsRef relationFlags) {
        String tagValue = way.getTag("sac_scale");

        if (Helper.isEmpty(tagValue)) {
            return edgeFlags;
        }

        int value = switch (tagValue) {
            case "hiking" -> 1;
            case "mountain_hiking" -> 2;
            case "demanding_mountain_hiking" -> 3;
            case "alpine_hiking" -> 4;
            case "demanding_alpine_hiking" -> 5;
            case "difficult_alpine_hiking" -> 6;
            default -> 0;
        };

        sacScaleEnc.setInt(false, edgeFlags, value);

        return edgeFlags;
    }
}
