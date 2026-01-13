package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.WheelchairWidth;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.util.UnitsConverter;

public class WheelchairWidthParser extends WheelchairBaseParser {
    public static final String TAG_NAME = "width";

    public WheelchairWidthParser(){
        this.encoder = WheelchairWidth.create();
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        return defaultHandleWayTags(
                edgeFlags,
                way,
                TAG_NAME,
                this::widthFromTag,
                false,
                true // a wider way is better for wheelchair users, therefore the worst value is the smallest one
        );
    }

    private int widthFromTag(String value) {
        return (int) (UnitsConverter.convertOSMDistanceTagToMeters(value.toLowerCase()) * 100);
    }
}
