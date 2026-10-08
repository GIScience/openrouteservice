package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.ev.WheelchairIncline;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.util.UnitsConverter;

public class WheelchairInclineParser extends WheelchairBaseParser<IntEncodedValue> {
    public static final String TAG_NAME = "incline";
    public static final int INCLINE_MAX_VALUE = 15;

    public WheelchairInclineParser() {
        this.encoder = WheelchairIncline.create();
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        int value = defaultHandleWayTags(way, TAG_NAME, this::getInclineFromTagValue, false);
        encoder.setInt(false, edgeFlags, Math.max(0, value));
        return edgeFlags;
    }

    private int getInclineFromTagValue(String inclineValue) {
        int decimalIncline = UnitsConverter.convertOSMInclineValueToPercentage(inclineValue, true);
        return Math.min(decimalIncline, INCLINE_MAX_VALUE);
    }
}
