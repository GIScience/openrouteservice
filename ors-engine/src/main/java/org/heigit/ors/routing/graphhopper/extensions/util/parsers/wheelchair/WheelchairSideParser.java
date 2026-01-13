package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.WheelchairSide;
import com.graphhopper.storage.IntsRef;

import static org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes.*;
import static org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMAttachedSidewalkProcessor.KEY_ORS_SIDEWALK_SIDE;

public class WheelchairSideParser extends WheelchairBaseParser {
    public WheelchairSideParser(){
        this.encoder = WheelchairSide.create();
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        Side sideProp = Side.UNKNOWN;

        if (way.hasTag(KEY_ORS_SIDEWALK_SIDE)) {
            String side = way.getTag(KEY_ORS_SIDEWALK_SIDE);
            if (side.equals(SW_VAL_LEFT)) {
                sideProp = Side.LEFT;
            } else if (side.equals(SW_VAL_RIGHT)) {
                sideProp = Side.RIGHT;
            }
        }

        ((EnumEncodedValue<Side>) encoder).setEnum(false, edgeFlags, sideProp);
        return edgeFlags;
    }
}
