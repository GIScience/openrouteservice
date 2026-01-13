package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.WheelchairSuitable;
import com.graphhopper.storage.IntsRef;

public class WheelchairSuitableParser extends WheelchairBaseParser {
    public WheelchairSuitableParser() {
        this.encoder = WheelchairSuitable.create();
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        boolean suited = isSeparateFootway(way) || way.hasTag("wheelchair_accessible", true);

        // the sidewalks always imply known suitability
        boolean suitedLeft = true;
        boolean suitedRight = true;

        suited = selectBooleanValueForSidewalkSide(way, suited, suitedLeft, suitedRight);

        ((BooleanEncodedValue) encoder).setBool(false, edgeFlags, suited);
        return edgeFlags;
    }
}
