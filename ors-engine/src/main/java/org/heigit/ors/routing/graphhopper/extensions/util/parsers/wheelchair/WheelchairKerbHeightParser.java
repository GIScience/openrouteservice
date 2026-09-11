package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.coll.GHLongObjectHashMap;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.ev.WheelchairKerb;
import com.graphhopper.storage.IntsRef;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WheelchairKerbHeightParser extends WheelchairBaseParser<IntEncodedValue> {
    public static final String TAG_NAME = "kerb_height";
    public static final int KERB_MAX_VALUE = 15;

    @Getter
    @Setter
    private static boolean kerbHeightOnlyOnCrossing = true;

    public WheelchairKerbHeightParser() {
        this.encoder = WheelchairKerb.create();
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        beforeHandleWayTags(way);

        GHLongObjectHashMap<Map<String, String>> nodeTags = way.getTag("ors:node_tags", new GHLongObjectHashMap<>());

        int heightC = -1;
        int heightL = -1;
        int heightR = -1;

        int height = calcSingleKerbHeightFromTagList(assumedKerbTags, -1);
        height = calcSingleKerbHeightFromTagList(explicitKerbTags, height);

        if (height > -1) {
            heightC = height;
        }

        // Now for if the values are attached to sides of the way
        int[] heights = calcSingleKerbHeightFromSidedTagList(assumedKerbTags, new int[]{-1, -1});
        heights = calcSingleKerbHeightFromSidedTagList(explicitKerbTags, heights);

        if (heights[0] > -1) {
            heightL = heights[0];
        }

        if (heights[1] > -1) {
            heightR = heights[1];
        }


        int kerbHeight = getKerbHeightForEdge(way, nodeTags);
        if (kerbHeight > -1) {
            heightC = kerbHeight;
        }

        int finalHeight = selectIntValueForSidewalkSide(way, heightC, heightL, heightR);
        finalHeight = Math.min(finalHeight, KERB_MAX_VALUE);

        encoder.setInt(false, edgeFlags, Math.max(0, finalHeight));

        return edgeFlags;
    }


    /**
     * Get an overriding kerb height if needed from the nodes that are on the way rather than the data stored on the way itself.
     * This should be the case if we are specifying to only store kerb heights on crossings as these features do not normally
     * have kerb heights attached to them
     *
     * @param way The way that is being investigated
     * @return A kerb height from the tags of the nodes on the way, or -1 if no kerb heights are found/required
     */
    int getKerbHeightForEdge(ReaderWay way, GHLongObjectHashMap<Map<String, String>> nodeTags) {
        int kerbHeight = -1;

        if (!kerbHeightOnlyOnCrossing || (way.hasTag("footway") && way.getTag("footway").equals("crossing"))) {
            // Look for kerb information
            kerbHeight = getKerbHeightFromNodeTags(nodeTags);
        }

        return kerbHeight;
    }

    /**
     * Look at the information stored against the nodes of the way and extract the kerb height to use for the whole way
     * from those data.
     *
     * @return The derived kerb height in centimetres from teh nodes that are on the way
     */
    int getKerbHeightFromNodeTags(GHLongObjectHashMap<Map<String, String>> nodeTags) {
        // Assumed kerb heights are those obtained from a tag without the explicit :height attribute
        List<Integer> assumedKerbHeights = new ArrayList<>();
        // Explicit heights are those provided by the :height tag - these should take precidence
        List<Integer> explicitKerbHeights = new ArrayList<>();

        for (var entry : nodeTags.values()) {
            Map<String, String> tags = entry.value;
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                switch (tag.getKey()) {
                    case "sloped_curb", "curb", "kerb", "sloped_kerb" ->
                            assumedKerbHeights.add(convertKerbTagValueToCentimetres(tag.getValue()));
                    case "kerb:height" -> explicitKerbHeights.add(convertKerbTagValueToCentimetres(tag.getValue()));
                    default -> {
                        // tag irrelevant for kerb height
                    }
                }
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


