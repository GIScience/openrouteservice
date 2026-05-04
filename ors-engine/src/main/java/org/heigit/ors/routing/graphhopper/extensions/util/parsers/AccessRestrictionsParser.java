package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.AccessRestriction;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;

import java.util.*;


public class AccessRestrictionsParser implements TagParser {
    private final EnumEncodedValue<AccessRestriction> accessRestrictionEnc;

    private static final String VAL_ACCESS = "access";
    private static final String VAL_MOTOR_VEHICLE = "motor_vehicle";
    private static final String VAL_MOTORCAR = "motorcar";
    private static final String VAL_VEHICLE = "vehicle";
    private static final String VAL_MOTORCYCLE = "motorcycle";
    private static final String VAL_BICYCLE = "bicycle";
    private static final String VAL_FOOT = "foot";
    private final List<String> accessRestrictedTags = new ArrayList<>(5);
    private final List<String> motorCarTags = new ArrayList<>(5);
    private final List<String> motorCycleTags = new ArrayList<>(5);
    private final Set<String> restrictedValues = new HashSet<>(5);
    private final Set<String> permissiveValues = new HashSet<>(5);

    private final int profileType;

    public void initTags() {
        accessRestrictedTags.addAll(Arrays.asList(VAL_MOTORCAR, VAL_MOTOR_VEHICLE, VAL_VEHICLE, VAL_ACCESS, VAL_BICYCLE, VAL_FOOT));
        motorCarTags.addAll(Arrays.asList(VAL_MOTORCAR, VAL_MOTOR_VEHICLE));
        motorCycleTags.addAll(Arrays.asList(VAL_MOTORCYCLE, VAL_MOTOR_VEHICLE));

        restrictedValues.add("private");
        restrictedValues.add("no");
        restrictedValues.add("restricted");
        restrictedValues.add("military");
        restrictedValues.add("destination");
        restrictedValues.add("customers");
        restrictedValues.add("emergency");
        restrictedValues.add("permissive");
        restrictedValues.add("delivery");
        restrictedValues.add("permit");

        permissiveValues.add("yes");
        permissiveValues.add("designated");
        permissiveValues.add("official");
    }


    public AccessRestrictionsParser(int profileType) {
        this(new EnumEncodedValue<>(AccessRestriction.KEY, AccessRestriction.class), profileType);
    }

    public AccessRestrictionsParser(EnumEncodedValue<AccessRestriction> accessRestrictionEnc, int profileType) {
        this.accessRestrictionEnc = accessRestrictionEnc;
        this.profileType = profileType;
        initTags();
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(accessRestrictionEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay readerWay, boolean b, IntsRef relationFlags) {
        int accessRestrictionValue = processWay(readerWay);
        accessRestrictionEnc.setEnum(false, edgeFlags, AccessRestriction.fromValue(accessRestrictionValue));
        return edgeFlags;
    }

    public int processWay(ReaderWay way) {
        //TODO: modify the following logic to process access restriction tags from the most specific to the least
        //      specific, e.g. motorcar > motor_vehicle > vehicle > access via a call to way.getFirstPriorityTag

        if (!way.hasTag(accessRestrictedTags, restrictedValues)) {
            return 0;
        }

        if (RoutingProfileType.isDriving(profileType)) {
            return processAccess(way, motorCarTags);
        }
        if (profileType == RoutingProfileType.DRIVING_MOTORCYCLE) {
            return processAccess(way, motorCycleTags);
        }
        if (RoutingProfileType.isCycling(profileType)) {
            return processAccess(way, VAL_BICYCLE);
        }
        if (RoutingProfileType.isPedestrian(profileType)) {
            return processAccess(way, VAL_FOOT);
        }
        if (profileType == RoutingProfileType.UNKNOWN) {
            return processAccess(way, VAL_ACCESS);
        }

        return 0;
    }

    private int processAccess(ReaderWay way, String tag) {
        return processAccess(way, Collections.singletonList(tag));
    }

    private int processAccess(ReaderWay way, List<String> tags) {
        return isAccessAllowed(way, tags) ? 0 : getRestrictionType(way, tags);
    }

    /**
     * Get the type of restrictions that have been set on the way.
     *
     * @param way  The way to be checked
     * @param tags The tags(keys) that should be accessed for the access restrictions
     * @return 0 if no restriction, else the integer encoded restriction value for the way
     */
    private int getRestrictionType(ReaderWay way, List<String> tags) {
        int res = 0;

        String tagValue = way.getTag(VAL_ACCESS);
        if (tagValue != null)
            res = updateRestriction(res, tagValue);

        if (tags != null) {
            for (String key : tags) {
                tagValue = way.getTag(key);
                res = updateRestriction(res, tagValue);
            }
        }

        return res;
    }

    /**
     * Take the encoded restriction value and update it with the passed restriction value
     *
     * @param encodedRestrictions Integer representation of the current restrictions
     * @param restrictionValue    The new restriction to be applied
     * @return An integer encoded representation of all restrictions that have been set
     */
    private int updateRestriction(int encodedRestrictions, String restrictionValue) {
        int res = encodedRestrictions;
        if (restrictionValue != null && !restrictionValue.isEmpty()) {
            res = AccessRestrictionType.getFromString(restrictionValue);
        }

        return res;
    }

    /**
     * Check if access is allowed on the way. e.g. it would check if motor_car=yes/permissive/destination etc. is set
     *
     * @param way      The OSM way to be checked
     * @param tagNames The tags (keys) to be checked
     * @return Whether access is allowed on the way
     */
    private boolean isAccessAllowed(ReaderWay way, List<String> tagNames) {
        return way.hasTag(tagNames, permissiveValues);
    }

}
