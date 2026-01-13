package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.WheelchairIncline;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.util.UnitsConverter;

public class WheelchairInclineParser extends WheelchairBaseParser {
    public static final String TAG_NAME = "incline";

    public WheelchairInclineParser(){
        this.encoder = WheelchairIncline.create();
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        return defaultHandleWayTags(edgeFlags, way, TAG_NAME, this::getInclineFromTagValue, true, false);
    }

    private int getInclineFromTagValue(String inclineValue) {
        double decimalIncline = UnitsConverter.convertOSMInclineValueToPercentage(inclineValue, true);
        decimalIncline = Math.min(decimalIncline, 15.0);
        return (int) Math.round(decimalIncline);
    }
}
