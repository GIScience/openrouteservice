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
    private EnumEncodedValue<AccessRestriction> accessRestrictionEnc;

    private static final String VAL_BICYCLE = "bicycle";
    private static final String VAL_ACCESS = "access";
    private static final String VAL_MOTOR_VEHICLE = "motor_vehicle";
    private final List<String> accessRestrictedTags = new ArrayList<>(5);
    private final List<String> motorCarTags = new ArrayList<>(5);
    private final List<String> motorCycleTags = new ArrayList<>(5);
    private final Set<String> restrictedValues = new HashSet<>(5);
    private final Set<String> permissiveValues = new HashSet<>(5);

    private int profileType;

    public void initTags() {
        accessRestrictedTags.addAll(Arrays.asList("motorcar", VAL_MOTOR_VEHICLE, "vehicle", VAL_ACCESS, VAL_BICYCLE, "foot"));
        motorCarTags.addAll(Arrays.asList("motorcar", VAL_MOTOR_VEHICLE));
        motorCycleTags.addAll(Arrays.asList("motorcycle", VAL_MOTOR_VEHICLE));

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
        if (way.hasTag(accessRestrictedTags, restrictedValues)) {
            if (RoutingProfileType.isDriving(profileType))
                return isAccessAllowed(way, motorCarTags) ? 0 : getRestrictionType(way, motorCarTags);
            if (profileType == RoutingProfileType.DRIVING_MOTORCYCLE)
                return isAccessAllowed(way, motorCycleTags) ? 0 : getRestrictionType(way, motorCycleTags);
            if (RoutingProfileType.isCycling(profileType))
                return isAccessAllowed(way, VAL_BICYCLE) ? 0 : getRestrictionType(way, VAL_BICYCLE);
            if (RoutingProfileType.isPedestrian(profileType))
                return isAccessAllowed(way, "foot") ? 0 : getRestrictionType(way, "foot");
            if (profileType == RoutingProfileType.UNKNOWN)
                return getRestrictionType(way, VAL_ACCESS);
        }

        return 0;
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
     * Get the type of restrictions that have been set on the way.
     *
     * @param way The way to be checked
     * @param tag The tag(key) that should be accessed for the access restrictions
     * @return 0 if no restriction, else the integer encoded restriction value for the way
     */
    private int getRestrictionType(ReaderWay way, String tag) {
        int res = 0;

        String tagValue = way.getTag(VAL_ACCESS);
        if (tagValue != null)
            res = updateRestriction(res, tagValue);

        tagValue = way.getTag(tag);
        res = updateRestriction(res, tagValue);

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
            switch (restrictionValue) {
                case "no" -> res = AccessRestrictionType.NO;
                case "destination" -> res = AccessRestrictionType.DESTINATION;
                case "private" -> res = AccessRestrictionType.PRIVATE;
                case "permissive" -> res = AccessRestrictionType.PERMISSIVE;
                case "delivery" -> res = AccessRestrictionType.DELIVERY;
                case "customers" -> res = AccessRestrictionType.CUSTOMERS;
                case "permit" -> res = AccessRestrictionType.PERMIT;
                default -> {
                }
            }
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

    /**
     * Check if access is allowed on the way. e.g. it would check if motor_car=yes/permissive/destination etc. is set
     *
     * @param way     The OSM way to be checked
     * @param tagName The single tag (key) to be checked
     * @return Whether access is allowed on the way
     */
    private boolean isAccessAllowed(ReaderWay way, String tagName) {
        return way.hasTag(tagName, permissiveValues);
    }

}
