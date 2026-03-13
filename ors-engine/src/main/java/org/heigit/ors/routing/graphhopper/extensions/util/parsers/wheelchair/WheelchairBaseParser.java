package org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.util.UnitsConverter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntFunction;

import static org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMAttachedSidewalkProcessor.KEY_ORS_SIDEWALK_SIDE;

public abstract class WheelchairBaseParser implements TagParser {
    public static final String SW_VAL_RIGHT = "right";
    public static final String SW_VAL_LEFT = "left";
    public static final String KEY_BOTH = "both";
    public static final String KEY_SIDEWALK = "sidewalk";
    public static final String KEY_SIDEWALK_BOTH = "sidewalk:both:";
    public static final String KEY_FOOTWAY_BOTH = "footway:both:";

    protected EncodedValue encoder;

    protected boolean hasLeftSidewalk = false;
    protected boolean hasRightSidewalk = false;

    protected Map<String, Object> cleanedTags;

    protected static String[] assumedKerbTags = new String[]{
            "curb",
            "kerb",
            "sloped_curb",
            "sloped_kerb"
    };

    protected static String[] explicitKerbTags = new String[]{
            "kerb:height",
            "curb:height"
    };

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(encoder);
    }

    protected void beforeHandleWayTags(ReaderWay way) {
        cleanedTags = cleanTags(way.getTags());
        detectAndRecordSidewalkSide(way);
        detectAndRecordSidewalkSideWithKerb();
    }

    /**
     * Detect if there are sidewalks stored on the way and if so, mark that these are present
     *
     * @param way The way to look for sidewalks on
     */
    private void detectAndRecordSidewalkSide(ReaderWay way) {
        if (way.hasTag(KEY_SIDEWALK)) {
            String sw = way.getTag(KEY_SIDEWALK);
            switch (sw) {
                case SW_VAL_LEFT -> hasLeftSidewalk = true;
                case SW_VAL_RIGHT -> hasRightSidewalk = true;
                case KEY_BOTH -> {
                    hasLeftSidewalk = true;
                    hasRightSidewalk = true;
                }
                default -> {
                    // leave as is
                }
            }
        }
    }

    private void detectAndRecordSidewalkSideWithKerb() {
        // Now for if the values are attached to sides of the way
        int[] heights = calcSingleKerbHeightFromSidedTagList(assumedKerbTags, new int[]{-1, -1});
        heights = calcSingleKerbHeightFromSidedTagList(explicitKerbTags, heights);

        if (heights[0] > -1) {
            hasLeftSidewalk = true;
        }

        if (heights[1] > -1) {
            hasRightSidewalk = true;
        }
    }

    /**
     * Calculate the kerb height from the way that should be stored on the graph bsaed on the tag keys specified
     *
     * @param kerbTags     The tag keys that should be evaluated
     * @param initialValue The initial value for the return. If no kerb height info is found, this value is returned
     * @return The value to use as the kerb height derived from the specified tag keys.
     */
    protected int calcSingleKerbHeightFromTagList(String[] kerbTags, int initialValue) {
        int height = initialValue;
        for (String kerbTag : kerbTags) {
            int kerbHeightValue = convertKerbTagValueToCentimetres((String) cleanedTags.get(kerbTag));
            if (kerbHeightValue != -1) {
                height = kerbHeightValue;
            }
        }
        return height;
    }

    /**
     * Calculate the kerb heights from the way that should be stored on the graph bsaed on the tag keys specified.
     * This method looks at the tags which specify a side to the road)
     *
     * @param kerbTags      The tag keys that should be evaluated
     * @param initialValues The initial value for the return. If no kerb height info is found, this value is returned
     * @return The values to use as the kerb height derived from the specified tag keys. The first item
     * in the array is for the left side, and the second is the right side.
     */
    protected int[] calcSingleKerbHeightFromSidedTagList(String[] kerbTags, int[] initialValues) {
        int[] heights = initialValues;
        int height = -1;
        for (String kerbTag : kerbTags) {
            String[] tagValues = getSidedKerbTagValuesToApply(kerbTag);
            if (tagValues[0] != null && !tagValues[0].isEmpty()) {
                height = convertKerbTagValueToCentimetres(tagValues[0].toLowerCase());
                if (height > -1) {
                    heights[0] = height;
                }
            }
            if (tagValues[1] != null && !tagValues[1].isEmpty()) {
                height = convertKerbTagValueToCentimetres(tagValues[1].toLowerCase());
                if (height > -1) {
                    heights[1] = height;
                }
            }
        }

        return heights;
    }

    /**
     * Go through tags and attempt to remove any invalid keys (i.e. when compound keys have been entered using a '.' rather than ':'
     *
     * @param dirtyTags The OSM tag collection that needs to be cleaned
     * @return A cleaned version of the tags on the way (. replaced with : in tag names)
     */
    protected HashMap<String, Object> cleanTags(Map<String, Object> dirtyTags) {
        HashMap<String, Object> cleanedTagsMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : dirtyTags.entrySet()) {
            String cleanKey = entry.getKey().replace(".", ":");
            cleanedTagsMap.put(cleanKey, entry.getValue());
        }
        return cleanedTagsMap;
    }

    protected String[] getSidedTagValue(String property) {
        String[] values = new String[2];
        // Left side
        if (cleanedTags.containsKey("sidewalk:left:" + property))
            values[0] = (String) cleanedTags.get("sidewalk:left:" + property);
        else if (cleanedTags.containsKey("footway:left:" + property))
            values[0] = (String) cleanedTags.get("footway:left:" + property);
        // Right side
        if (cleanedTags.containsKey("sidewalk:right:" + property))
            values[1] = (String) cleanedTags.get("sidewalk:right:" + property);
        else if (cleanedTags.containsKey("footway:right:" + property))
            values[1] = (String) cleanedTags.get("footway:right:" + property);

        // Both
        if (cleanedTags.containsKey(KEY_SIDEWALK_BOTH + property)) {
            values[0] = (String) cleanedTags.get(KEY_SIDEWALK_BOTH + property);
            values[1] = (String) cleanedTags.get(KEY_SIDEWALK_BOTH + property);
        } else if (cleanedTags.containsKey(KEY_FOOTWAY_BOTH + property)) {
            values[0] = (String) cleanedTags.get(KEY_FOOTWAY_BOTH + property);
            values[1] = (String) cleanedTags.get(KEY_FOOTWAY_BOTH + property);
        }
        return values;
    }

    /** Select the worst value (maximum) for the sidewalk side based on the presence of the ors-sidewalk-side tag and the presence of sidewalk tags on the sides of the way */
    protected int selectIntValueForSidewalkSide(ReaderWay way, int center, int left, int right) {
        return selectIntValueForSidewalkSide(way, center, left, right, false);
    }

    /** Select the worst value (maximum or minimum depending on the value of moreIsBetter) for the sidewalk side based on the presence of the ors-sidewalk-side tag and the presence of sidewalk tags on the sides of the way */
    protected int selectIntValueForSidewalkSide(ReaderWay way, int center, int left, int right, boolean moreIsBetter) {
        IntBinaryOperator op = moreIsBetter ? Math::min : Math::max;

        IntBinaryOperator combine = (a, b) -> {
            if (a == -1) return b;
            if (b == -1) return a;
            return op.applyAsInt(a, b);
        };

        if (way.hasTag(KEY_ORS_SIDEWALK_SIDE)) {
            String side = way.getTag(KEY_ORS_SIDEWALK_SIDE);
            if (side.equals(SW_VAL_LEFT)) {
                return combine.applyAsInt(center, left);
            } else if (side.equals(SW_VAL_RIGHT)) {
                return combine.applyAsInt(center, right);
            }
        } else if (hasLeftSidewalk || hasRightSidewalk) {
            return combine.applyAsInt(center, combine.applyAsInt(left, right));
        }
        return center;
    }

    /**
     * Converts a kerb height value to a numerical height (in centimetres). A kerb could be stored as an explicit height or
     * as an indicator as to whether the kerb is lowered or not.
     *
     * @param value The value of the tag
     * @return The presumed height of the kerb in metres
     */
    protected int convertKerbTagValueToCentimetres(String value) {
        int centimetreHeight = -1;

        if (value == null) {
            return -1;
        }
        switch (value) {
            case "yes", KEY_BOTH, "low", "lowered", "dropped", "sloped" -> centimetreHeight = 3;
            case "no", "none", "one", "rolled", "regular" -> centimetreHeight = 15;
            case "at_grade", "flush" -> centimetreHeight = 0;
            default -> {
                double metresHeight = UnitsConverter.convertOSMDistanceTagToMeters(value);
                // If no unit was given in the tag, the value might be in meters or centimeters; we can only guess
                // depending on the value
                if (metresHeight < 0.15) {
                    centimetreHeight = (int) (metresHeight * 100);
                } else {
                    centimetreHeight = (int) metresHeight;
                }
            }
        }

        return centimetreHeight;
    }

    /**
     * Look at way and try to find the correct kerb heights for it. In some cases when the kerbs are attached directly to a way they are
     * marked as start and end and so we need to look through the various tags to try and find these.
     *
     * @param key The base key that we are investigating (e.g. "kerb", "sloped_kerb" etc.)
     * @return The textual tag that should be used as the kerb height
     */
    protected String[] getSidedKerbTagValuesToApply(String key) {
        // If we are looking at the kerbs, sometimes the start and end of a way is marked as having different kerb
        // heights using the ...:start and ...:end tags. For now, we just want to get the worse of these values (the
        // highest)
        double leftStart = -1;
        double leftEnd = -1;
        double rightStart = -1;
        double rightEnd = -1;

        String[] endValues = getSidedTagValue(key + ":end");
        // Convert
        if (endValues[0] != null && !endValues[0].isEmpty()) {
            leftEnd = convertKerbTagValueToCentimetres(endValues[0]);
        }
        if (endValues[1] != null && !endValues[1].isEmpty()) {
            rightEnd = convertKerbTagValueToCentimetres(endValues[1]);
        }
        String[] startValues = getSidedTagValue(key + ":start");
        // Convert
        if (startValues[0] != null && !startValues[0].isEmpty()) {
            leftStart = convertKerbTagValueToCentimetres(startValues[0]);
        }
        if (startValues[1] != null && !startValues[1].isEmpty()) {
            rightStart = convertKerbTagValueToCentimetres(startValues[1]);
        }

        // Now compare to find the worst
        String[] values = new String[2];
        if (leftEnd > leftStart)
            values[0] = endValues[0];
        else if (leftStart > leftEnd)
            values[0] = startValues[0];

        if (rightEnd > rightStart)
            values[1] = endValues[1];
        else if (rightStart > rightEnd)
            values[1] = startValues[1];

        return values;
    }

    protected boolean selectBooleanValueForSidewalkSide(ReaderWay way, boolean center, boolean left, boolean right) {
        if (way.hasTag(KEY_ORS_SIDEWALK_SIDE)) {
            String side = way.getTag(KEY_ORS_SIDEWALK_SIDE);
            if (side.equals(SW_VAL_LEFT)) {
                return center && left;
            } else if (side.equals(SW_VAL_RIGHT)) {
                return center && right;
            }
        } else if (hasLeftSidewalk || hasRightSidewalk) {
            return center && left && right;
        }
        return center;
    }


    protected IntsRef defaultHandleWayTags(IntsRef edgeFlags, ReaderWay way, String tagName, ToIntFunction<String> tagValueToEncodedValueFunction){
        return defaultHandleWayTags(edgeFlags, way, tagName, tagValueToEncodedValueFunction, false, false);
    }

    protected IntsRef defaultHandleWayTags(IntsRef edgeFlags, ReaderWay way, String tagName, ToIntFunction<String> tagValueToEncodedValueFunction, boolean addOne, boolean moreIsBetter) {
        beforeHandleWayTags(way);

        int center = -1;
        int left = -1;
        int right = -1;

        // Read center
        if (cleanedTags.containsKey(tagName)) {
            center = tagValueToEncodedValueFunction.applyAsInt(((String) cleanedTags.get(tagName)).toLowerCase());
        }

        // Read sides
        String[] tagValues = getSidedTagValue(tagName);
        if (tagValues[0] != null && !tagValues[0].isEmpty())
            left = tagValueToEncodedValueFunction.applyAsInt(tagValues[0].toLowerCase());
        if (tagValues[1] != null && !tagValues[1].isEmpty())
            right = tagValueToEncodedValueFunction.applyAsInt(tagValues[1].toLowerCase());

        // Select based on artificial ors-sidewalk-side tag
        center = selectIntValueForSidewalkSide(way, center, left, right, moreIsBetter);

        if(addOne)
            center += 1; // Add 1 to the value to ensure the number can be stored as unsigned integer (as -1 is used to indicate unknown value)

        if(center > -1)
            ((IntEncodedValue) encoder).setInt(false, edgeFlags, center);

        return edgeFlags;
    }

    /**
     * Determine if the way is a separate footway object or a road feature.
     *
     * @param way The OSM way object to be assessed
     * @return Whether the way is seen as a separately drawn footway (true) or a road (false)
     */
    protected boolean isSeparateFootway(ReaderWay way) {
        String type = way.getTag("highway", "");

        String[] pedestrianWayTypes = {
                "living_street",
                "pedestrian",
                "footway",
                "path",
                "crossing",
                "track"
        };

        // Check if it is a footpath or pedestrian
        if (!type.isEmpty()) {
            // We are looking at a separate footpath
            // we are looking at a road feature so any footway would be attached to it as a tag
            return Arrays.asList(pedestrianWayTypes).contains(type);
        }

        return true;
    }
}
