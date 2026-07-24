package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.ev.WheelchairSurface;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;

public class WheelchairSurfaceParser extends WheelchairBaseParser<IntEncodedValue> {
    public static final String TAG_NAME = "surface";

    public WheelchairSurfaceParser() {
        this.encoder = WheelchairSurface.create();
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        int value = defaultHandleWayTags(way, TAG_NAME, WheelchairTypesEncoder::getSurfaceType);
        encoder.setInt(false, edgeFlags, Math.max(0, value));
        return edgeFlags;
    }
}
