package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class HazmatAccessParser implements TagParser {
    private final BooleanEncodedValue hazmatAccessEnc;
    private final Pattern patternHazmat;

    public HazmatAccessParser() {
        this(HazmatAccess.create());
    }

    public HazmatAccessParser(BooleanEncodedValue hazmatAccessEnc) {
        this.hazmatAccessEnc = hazmatAccessEnc;
        patternHazmat = Pattern.compile("^hazmat(:[B-E])?$");
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(hazmatAccessEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean b, IntsRef relationFlags) {
        boolean hasHazmatRestriction = way.getTags().entrySet().stream().anyMatch(this::isHazmatRestriction);

        hazmatAccessEnc.setBool(false, edgeFlags, !hasHazmatRestriction);

        return edgeFlags;
    }

    private boolean isHazmatRestriction(Map.Entry<String, Object> entry) {
        String key = entry.getKey();
        String value = entry.getValue().toString();

        return patternHazmat.matcher(key).matches() && "no".equals(value);
    }
}
