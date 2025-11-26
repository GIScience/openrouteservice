package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;

import java.util.*;


public class VehicleAccessParser implements TagParser {
    private final BooleanEncodedValue vehicleAccessEnc;
    private final int targetVehicleType;
    private int blockedVehicleTypes = 0;
    private static final List<String> motorVehicleRestrictions = List.of("motorcar", "motor_vehicle", "vehicle", "access");
    private static final Set<String> motorVehicleRestrictedValues = Set.of("private", "no", "restricted", "military");
    private static final Set<String> motorVehicleHgvValues = Set.of("hgv", "goods", "bus", "agricultural", "forestry", "delivery");
    private static final Set<String> noValues = Set.of("no", "private");
    private static final Set<String> yesValues = Set.of("yes", "designated");

    public VehicleAccessParser(BooleanEncodedValue vehicleAccessEnc, int vehicleType) {
        this.vehicleAccessEnc = vehicleAccessEnc;
        this.targetVehicleType = vehicleType;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(vehicleAccessEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay readerWay, boolean b, IntsRef relationFlags) {
        processWay(readerWay);
        boolean blocked = (blockedVehicleTypes & targetVehicleType) == targetVehicleType;
        vehicleAccessEnc.setBool(false, edgeFlags, !blocked);
        return edgeFlags;
    }

    public void processWay(ReaderWay way) {
        if (!way.hasTag("highway"))
            return;

        blockedVehicleTypes = 0;// reset values

        // process motor vehicle restrictions before any more specific vehicle type tags which override the former
        processMotorVehicleRestrictions(way);

        processSpecificVehicleTypes(way);
    }

    private void processMotorVehicleRestrictions(ReaderWay way) {
        // if there are any generic motor vehicle restrictions restrict all types ...
        if (way.hasTag(motorVehicleRestrictions, motorVehicleRestrictedValues)) {
            blockedVehicleTypes = HeavyVehicleAttributes.ANY;
        }
        //... except ones explicitly listed
        if (way.hasTag(motorVehicleRestrictions, motorVehicleHgvValues)) {
            int allowedTypes = getAllowedVehicleTypes(way);
            blockedVehicleTypes = HeavyVehicleAttributes.ANY & ~allowedTypes;
        }
    }

    private int getAllowedVehicleTypes(ReaderWay way) {
        int flag = 0;
        for (String key : motorVehicleRestrictions) {
            for (String val : way.getTagValues(key)) {
                if (motorVehicleHgvValues.contains(val)) {
                    flag |= HeavyVehicleAttributes.getFromString(val);
                }
            }
        }
        return flag;
    }

    private void processSpecificVehicleTypes(ReaderWay way) {
        Iterator<Map.Entry<String, Object>> it = way.getProperties();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            if (!motorVehicleHgvValues.contains(key)) {
                continue;
            }

            String value = entry.getValue().toString();
            //TODO: account for <vehicle_type>:[forward/backward] keys
            //TODO: allow access:<vehicle_type> as described in #703. Might be necessary to adjust the upstream PBF parsing part as well.
            String vehicleType = getVehicleType(key, value);
            String accessValue = getVehicleAccess(vehicleType, value);

            setAccessFlags(vehicleType, accessValue);
            if (vehicleType.equals(value)) {// e.g. hgv=delivery implies that hgv other than delivery vehicles are blocked
                setAccessFlags(key, "no");
            }
        }
    }

    private String getVehicleType(String key, String value) {
        return motorVehicleHgvValues.contains(value) ? value : key;// hgv=[delivery/agricultural/forestry]
    }

    private String getVehicleAccess(String vehicleType, String value) {
        if (vehicleType.equals(value) || yesValues.contains(value))
            return "yes";
        else if (noValues.contains(value))
            return "no";

        return null;
    }

    /**
     * Toggle on/off the bit corresponding to a given hgv type defined by {@code flag} inside binary restriction masks
     * based on the value of {@code tag}. "no" sets the bit in {@code _hgvType}, while "yes" unsets it.
     *
     * @param vehicle a String describing one of the vehicle types defined in {@code HeavyVehicleAttributes}
     * @param access  a String describing the access restriction
     */
    private void setAccessFlags(String vehicle, String access) {
        int flag = HeavyVehicleAttributes.getFromString(vehicle);
        if (access != null) {
            if ("no".equals(access))
                blockedVehicleTypes |= flag;
            else if ("yes".equals(access))
                blockedVehicleTypes &= ~flag;
        }
    }

}
