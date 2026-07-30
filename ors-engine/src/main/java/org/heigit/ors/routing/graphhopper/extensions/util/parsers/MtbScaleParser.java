package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.Helper;

import java.util.List;

public class MtbScaleParser implements TagParser {
    private IntEncodedValue mtbScaleEnc;
    private boolean isUphill;

    public MtbScaleParser(IntEncodedValue mtbScaleEnc, boolean isUphill) {
        this.mtbScaleEnc = mtbScaleEnc;
        this.isUphill = isUphill;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(mtbScaleEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean b, IntsRef relationFlags) {
        int mtbScale = 0;

        if (isUphill) {
            mtbScale = getMtbScale(way.getTag("mtb:scale:uphill"));
        }
        else {
            mtbScale = getMtbScale(way.getTag("mtb:scale"));
            if (mtbScale == 0)
                mtbScale = getMtbScale(way.getTag("mtb:scale:imba"));
        }

        if (mtbScale > 0 && mtbScale < 8)
            mtbScaleEnc.setInt(false, edgeFlags, mtbScale);

        return edgeFlags;
    }

    private int getMtbScale(String value) {
        if (!Helper.isEmpty(value)) {
            try {
                return Integer.parseInt(value) + 1;
            } catch (Exception ex) {
                // do nothing
            }
        }
        return 0;
    }
}
