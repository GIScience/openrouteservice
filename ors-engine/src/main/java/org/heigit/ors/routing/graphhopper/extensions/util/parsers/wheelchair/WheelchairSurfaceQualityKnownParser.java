package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.ev.WheelchairSurfaceQualityKnown;
import com.graphhopper.storage.IntsRef;

import java.util.Arrays;
import java.util.List;

import static org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMAttachedSidewalkProcessor.KEY_ORS_SIDEWALK_SIDE;

public class WheelchairSurfaceQualityKnownParser extends WheelchairBaseParser {
    public WheelchairSurfaceQualityKnownParser(){
        this.encoder = WheelchairSurfaceQualityKnown.create();
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        beforeHandleWayTags(way);

        boolean separateFootway = isSeparateFootway(way);
        if(!separateFootway) {
            ((BooleanEncodedValue) encoder).setBool(false, edgeFlags, false);
            return edgeFlags;
        }

        boolean center = false;

        final List<String> keys = Arrays.asList("surface", "smoothness", "tracktype");

        boolean left = false;
        boolean right = false;

        for(String key : keys) {
            // Read center
            if (cleanedTags.containsKey(key)) {
                center = true;
            }

            // Read sides
            String[] tagValues = getSidedTagValue(key);
            if (tagValues[0] != null && !tagValues[0].isEmpty())
                left = true;
            if (tagValues[1] != null && !tagValues[1].isEmpty())
                right = true;
        }

        if (way.hasTag(KEY_ORS_SIDEWALK_SIDE)) {
            String side = way.getTag(KEY_ORS_SIDEWALK_SIDE);
            if (side.equals(SW_VAL_LEFT)) {
                center = left;
            } else if (side.equals(SW_VAL_RIGHT)) {
                center = right;
            }
        }
        ((SimpleBooleanEncodedValue) encoder).setBool(false, edgeFlags, center);
        return edgeFlags;
    }
}
