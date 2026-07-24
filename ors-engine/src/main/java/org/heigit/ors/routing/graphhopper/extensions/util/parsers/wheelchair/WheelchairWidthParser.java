package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.UnsignedDecimalEncodedValue;
import com.graphhopper.routing.ev.WheelchairWidth;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.util.UnitsConverter;

public class WheelchairWidthParser extends WheelchairBaseParser<UnsignedDecimalEncodedValue> {
    public static final String TAG_NAME = "width";

    public WheelchairWidthParser(){
        this.encoder = WheelchairWidth.create();
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        int value = defaultHandleWayTags(
                way, TAG_NAME, this::widthFromTag, true // a wider way is better for wheelchair users, therefore the worst value is the smallest one
        );
        encoder.setDecimal(false, edgeFlags, Math.max(0, value));
        return edgeFlags;
    }

    private int widthFromTag(String value) {
        return (int) (UnitsConverter.convertOSMDistanceTagToMeters(value.toLowerCase()) * 100);
    }
}
