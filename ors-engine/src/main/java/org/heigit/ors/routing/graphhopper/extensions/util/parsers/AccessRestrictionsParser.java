package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.AccessRestriction;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;

import java.util.*;


public class AccessRestrictionsParser implements TagParser {
    private static final String VAL_ACCESS = "access";
    private static final String VAL_MOTOR_VEHICLE = "motor_vehicle";
    private static final String VAL_MOTORCAR = "motorcar";
    private static final String VAL_VEHICLE = "vehicle";
    private static final String VAL_MOTORCYCLE = "motorcycle";
    private static final String VAL_HGV = "hgv";
    private static final String VAL_BICYCLE = "bicycle";
    private static final String VAL_FOOT = "foot";
    private static final List<String> motorcarTags = Arrays.asList(VAL_MOTORCAR, VAL_MOTOR_VEHICLE, VAL_VEHICLE, VAL_ACCESS);
    private static final List<String> motorcycleTags = Arrays.asList(VAL_MOTORCYCLE, VAL_MOTOR_VEHICLE, VAL_VEHICLE, VAL_ACCESS);
    private static final List<String> hgvTags = Arrays.asList(VAL_HGV, VAL_MOTOR_VEHICLE, VAL_VEHICLE, VAL_ACCESS);
    private static final List<String> bicycleTags = Arrays.asList(VAL_BICYCLE, VAL_VEHICLE, VAL_ACCESS);
    private static final List<String> footTags = Arrays.asList(VAL_FOOT, VAL_ACCESS);
    private final IntEncodedValue accessRestrictionEnc;
    private final int profileType;

    public AccessRestrictionsParser(int profileType) {
        this(AccessRestriction.create(), profileType);
    }

    public AccessRestrictionsParser(IntEncodedValue accessRestrictionEnc, int profileType) {
        this.accessRestrictionEnc = accessRestrictionEnc;
        this.profileType = profileType;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(accessRestrictionEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay readerWay, boolean b, IntsRef relationFlags) {
        int accessRestrictionValue = processWay(readerWay);
        accessRestrictionEnc.setInt(false, edgeFlags, accessRestrictionValue);
        return edgeFlags;
    }

    public int processWay(ReaderWay way) {
        if (profileType == RoutingProfileType.DRIVING_CAR) {
            return getRestrictionType(way, motorcarTags);
        }
        if (profileType == RoutingProfileType.DRIVING_MOTORCYCLE) {
            return getRestrictionType(way, motorcycleTags);
        }
        if (RoutingProfileType.isHeavyVehicle(profileType)) {
            return getRestrictionType(way, hgvTags);
        }
        if (RoutingProfileType.isCycling(profileType)) {
            return getRestrictionType(way, bicycleTags);
        }
        if (RoutingProfileType.isPedestrian(profileType)) {
            return getRestrictionType(way, footTags);
        }
        if (profileType == RoutingProfileType.UNKNOWN) {
            return getRestrictionType(way, Collections.singletonList(VAL_ACCESS));
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

        for (String value : way.getFirstPriorityTagValues(tags)) {
            res = updateRestriction(res, value);
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
        return encodedRestrictions | AccessRestrictionType.getFromString(restrictionValue);
    }
}
