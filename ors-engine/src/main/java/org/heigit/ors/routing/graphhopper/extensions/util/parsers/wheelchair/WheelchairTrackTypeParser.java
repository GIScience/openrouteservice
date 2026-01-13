package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.WheelchairTrackType;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;

public class WheelchairTrackTypeParser extends WheelchairBaseParser {
    public static final String TAG_NAME = "tracktype";

    public WheelchairTrackTypeParser(){
        this.encoder = WheelchairTrackType.create();
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        return defaultHandleWayTags(edgeFlags, way, TAG_NAME, WheelchairTypesEncoder::getTrackType);
    }
}
