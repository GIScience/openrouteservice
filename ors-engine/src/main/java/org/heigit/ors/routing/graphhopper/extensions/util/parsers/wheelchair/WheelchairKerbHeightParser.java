package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.ev.WheelchairKerb;
import com.graphhopper.storage.IntsRef;

public class WheelchairKerbHeightParser extends WheelchairBaseParser {
    public static final String TAG_NAME = "kerb_height";

    public WheelchairKerbHeightParser(){
        this.encoder = WheelchairKerb.create();
    }

     @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        beforeHandleWayTags(way);

         int height = calcSingleKerbHeightFromTagList(assumedKerbTags, -1);
         height = calcSingleKerbHeightFromTagList(explicitKerbTags, height);

         int left =  -1;
         int right = -1;

         // Now for if the values are attached to sides of the way
         int[] heights = calcSingleKerbHeightFromSidedTagList(assumedKerbTags, new int[]{-1, -1});
         heights = calcSingleKerbHeightFromSidedTagList(explicitKerbTags, heights);

         if (heights[0] > -1) {
             left = heights[0];
         }

         if (heights[1] > -1) {
             right = heights[1];
         }

         height = selectIntValueForSidewalkSide(way, height, left, right);

         if(height > -1)
            ((IntEncodedValue) encoder).setInt(false, edgeFlags, height);

         return edgeFlags;
     }
}
