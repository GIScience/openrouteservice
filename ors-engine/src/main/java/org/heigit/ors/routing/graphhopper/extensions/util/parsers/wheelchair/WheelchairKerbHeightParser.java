package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.google.common.collect.Maps;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.ev.WheelchairKerb;
import com.graphhopper.storage.IntsRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WheelchairKerbHeightParser extends WheelchairBaseParser {
    public static final String TAG_NAME = "kerb_height";
    public static boolean kerbHeightOnlyOnCrossing = true; // TODO: expose this as a configuration option

    private final Map<String, String> nodeTagsOnWay = Maps.newHashMap();

    public WheelchairKerbHeightParser(){
        this.encoder = WheelchairKerb.create();
    }

     @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        beforeHandleWayTags(way);

        for (var entry : way.getTags().entrySet()) {
            if(!entry.getKey().startsWith("ors_node:")) continue;

            final String key = entry.getKey().replace("ors_node:", "");
            nodeTagsOnWay.put(key, entry.getValue().toString());
        }


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

         int nodeBasedKerbHeight = getKerbHeightForEdge(way);
         if (nodeBasedKerbHeight > -1) {
             height = nodeBasedKerbHeight;
         }

         height = selectIntValueForSidewalkSide(way, height, left, right);

         if(height > -1)
            ((IntEncodedValue) encoder).setInt(false, edgeFlags, height);

         return edgeFlags;
     }

    int getKerbHeightForEdge(ReaderWay way) {
        int kerbHeight = -1;

        if (!kerbHeightOnlyOnCrossing || (way.hasTag("footway") && way.getTag("footway").equals("crossing"))) {
            // Look for kerb information
            kerbHeight = getKerbHeightFromNodeTags();
        }

        return kerbHeight;
    }

    int getKerbHeightFromNodeTags() {
        // Assumed kerb heights are those obtained from a tag without the explicit :height attribute
        List<Integer> assumedKerbHeights = new ArrayList<>();
        // Explicit heights are those provided by the :height tag - these should take precidence
        List<Integer> explicitKerbHeights = new ArrayList<>();


        for (Map.Entry<String, String> tag : nodeTagsOnWay.entrySet()) {
            switch (tag.getKey()) {
                case "sloped_curb", "curb", "kerb", "sloped_kerb" ->
                        assumedKerbHeights.add(convertKerbTagValueToCentimetres(tag.getValue()));
                case "kerb:height", "curb:height" -> explicitKerbHeights.add(convertKerbTagValueToCentimetres(tag.getValue()));
                default -> {}
            }
        }

        if (!explicitKerbHeights.isEmpty()) {
            return Collections.max(explicitKerbHeights);
        } else if (!assumedKerbHeights.isEmpty()) {
            // If we have multiple kerb heights, we need to apply the largest to the edge as this is the worst
            return Collections.max(assumedKerbHeights);
        } else {
            return -1;
        }
    }
}
