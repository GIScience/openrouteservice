package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.ev.WheelchairSurfaceQualityKnown;
import com.graphhopper.storage.IntsRef;

import java.util.Arrays;
import java.util.List;

public class WheelchairSurfaceQualityKnownParser extends WheelchairBaseParser {
    public WheelchairSurfaceQualityKnownParser(){
        this.encoder = WheelchairSurfaceQualityKnown.create();
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        beforeHandleWayTags(way);

        boolean markSurfaceQualityKnown = isSeparateFootway(way);
        final List<String> keys = Arrays.asList("surface", "smoothness", "tracktype");

        boolean center = false;
        boolean left = false;
        boolean right = false;

        for(String key : keys) {
            // Read center
            if (cleanedTags.containsKey(key)) {
                center = markSurfaceQualityKnown;
            }

            // Read sides
            String[] tagValues = getSidedTagValue(key);
            if (tagValues[0] != null && !tagValues[0].isEmpty())
                left = true;
            if (tagValues[1] != null && !tagValues[1].isEmpty())
                right = true;
        }

        center = selectBooleanValueForSidewalkSide(way, center, left, right);

        ((SimpleBooleanEncodedValue) encoder).setBool(false, edgeFlags, center);
        return edgeFlags;
    }
}
