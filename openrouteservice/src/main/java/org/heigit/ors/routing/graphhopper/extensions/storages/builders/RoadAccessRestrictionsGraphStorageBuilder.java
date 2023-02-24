/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;
import org.heigit.ors.routing.graphhopper.extensions.storages.RoadAccessRestrictionsGraphStorage;

import java.util.*;

/**
 * Builder for road access restrictions information. The purpose is to record for edges any restrictions that are in
 * place for the particular vehicle type that is being processed for the profile.
 */
public class RoadAccessRestrictionsGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private static final String KEY_USE_FOR_WARNINGS = "use_for_warnings";
    private static final String VAL_BICYCLE = "bicycle";
    private static final String VAL_ACCESS = "access";
    private static final String VAL_MOTOR_VEHICLE = "motor_vehicle";
    private RoadAccessRestrictionsGraphStorage storage;
    private boolean hasRestrictions = false;
    private int restrictions;
    private final List<String> accessRestrictedTags = new ArrayList<>(5);
    private final List<String> motorCarTags = new ArrayList<>(5);
    private final List<String> motorCycleTags = new ArrayList<>(5);
    private final Set<String> restrictedValues = new HashSet<>(5);
    private final Set<String> permissiveValues = new HashSet<>(5);

    private int profileType;

    public RoadAccessRestrictionsGraphStorageBuilder() {
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

        permissiveValues.add("yes");
        permissiveValues.add("designated");
        permissiveValues.add("official");
    }

    /**
     * Initialise the road access restrictions graph storage builder for a profile and set the profile type to be that
     * specified.
     * @param graphhopper   The graphhopper instance being used (not used)
     * @param profileType   The id of the profile type that the RoadAccessRestrictions are for
     * @return              The RoadAccessRestrictionStorage object created as part of the initialisation
     * @throws Exception
     */
    public GraphExtension init(GraphHopper graphhopper, int profileType) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        this.profileType = profileType;

        storage = new RoadAccessRestrictionsGraphStorage();

        if (parameters.containsKey(KEY_USE_FOR_WARNINGS))
            storage.setIsUsedForWarning(Boolean.parseBoolean(parameters.get(KEY_USE_FOR_WARNINGS)));

        return storage;
    }

    /**
     * Initialise the road access restrictions graph storage builder for a profile
     * @param graphhopper   The graphhopper instance being used
     * @return              The RoadAccessRestrictionStorage object created as part of the initialisation
     * @throws Exception
     */
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        // extract profiles from GraphHopper instance
        EncodingManager encMgr = graphhopper.getEncodingManager();
        List<FlagEncoder> encoders = encMgr.fetchEdgeEncoders();
        int[] profileTypes = new int[encoders.size()];
        int i = 0;
        for (FlagEncoder enc : encoders) {
            profileTypes[i] = RoutingProfileType.getFromEncoderName(enc.toString());
            i++;
        }

        profileType = profileTypes[0];

        storage = new RoadAccessRestrictionsGraphStorage();

        if (parameters.containsKey(KEY_USE_FOR_WARNINGS))
            storage.setIsUsedForWarning(Boolean.parseBoolean(parameters.get(KEY_USE_FOR_WARNINGS)));

        return storage;
    }

    public void processWay(ReaderWay way) {
        this.processWay(way, null, null);
    }

    /**
     * Process the road access restrictions of a  way feature ready for processing into edges. It checks to see if there
     * are restrictions present in the form of tags (e.g. access=private) and then stores the information accordingly.
     * It first checks if there have already been restrictions recorded and if so clears them.
     * @param way       The way to be processed
     * @param coords    List of coordinates for the way (not used)
     * @param nodeTags  List of node ids and the key value pairs for the tags of that node. These values can be used to
     *                  apply restrictions on a way introduced by items like lift gates that are nodes on the way
     */
    @Override
    public void processWay(ReaderWay way, Coordinate[] coords, Map<Integer, Map<String,String>> nodeTags) {
        if (hasRestrictions) {
            hasRestrictions = false;
            restrictions = 0;
        }

        if(nodeTags != null) {
            for (Map<String, String> tagPairs : nodeTags.values()) {
                for (Map.Entry<String, String> pair : tagPairs.entrySet()) {
                    way.setTag(pair.getKey(), pair.getValue());
                }
            }
        }

        if (way.hasTag(accessRestrictedTags, restrictedValues)) {
            hasRestrictions = true;
            if (RoutingProfileType.isDriving(profileType))
                restrictions = isAccessAllowed(way, motorCarTags) ? 0 : getRestrictionType(way, motorCarTags);
            if (profileType == RoutingProfileType.DRIVING_MOTORCYCLE)
                restrictions = isAccessAllowed(way, motorCycleTags) ? 0 : getRestrictionType(way, motorCycleTags);
            if (RoutingProfileType.isCycling(profileType))
                restrictions = isAccessAllowed(way, VAL_BICYCLE) ? 0 : getRestrictionType(way, VAL_BICYCLE);
            if (RoutingProfileType.isPedestrian(profileType))
                restrictions = isAccessAllowed(way, "foot") ? 0 : getRestrictionType(way, "foot");
        }
    }

    /**
     * Get the type of restrictions that have been set on the way.
     * @param way   The way to be checked
     * @param tags  The tags(keys) that should be accessed for the access restrictions
     * @return      0 if no restriction, else the integer encoded restriction value for the way
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
     * @param way   The way to be checked
     * @param tag   The tag(key) that should be accessed for the access restrictions
     * @return      0 if no restriction, else the integer encoded restriction value for the way
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
     * @param encodedRestrictions   Integer representation of the current restrictions
     * @param restrictionValue      The new restriction to be applied
     * @return                      An integer encoded representation of all restrictions that have been set
     */
    private int updateRestriction(int encodedRestrictions, String restrictionValue) {
        int res = encodedRestrictions;
        if (restrictionValue != null && !restrictionValue.isEmpty()) {
            switch (restrictionValue) {
                case "no":
                    res |= AccessRestrictionType.NO;
                    break;
                case "destination":
                    res |= AccessRestrictionType.DESTINATION;
                    break;
                case "private":
                    res |= AccessRestrictionType.PRIVATE;
                    break;
                case "permissive":
                    res |= AccessRestrictionType.PERMISSIVE;
                    break;
                case "delivery":
                    res |= AccessRestrictionType.DELIVERY;
                    break;
                case "customers":
                    res |= AccessRestrictionType.CUSTOMERS;
                    break;
                default:
            }
        }

        return res;
    }

    /**
     * Check if access is allowed on the way. e.g. it would check if motor_car=yes/permissive/destination etc. is set
     * @param way       The OSM way to be checked
     * @param tagNames  The tags (keys) to be checked
     * @return          Whether access is allowed on the way
     */
    private boolean isAccessAllowed(ReaderWay way, List<String> tagNames) {
        return way.hasTag(tagNames, permissiveValues);
    }

    /**
     * Check if access is allowed on the way. e.g. it would check if motor_car=yes/permissive/destination etc. is set
     * @param way       The OSM way to be checked
     * @param tagName   The single tag (key) to be checked
     * @return          Whether access is allowed on the way
     */
    private boolean isAccessAllowed(ReaderWay way, String tagName) {
        return way.hasTag(tagName, permissiveValues);
    }

    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        storage.setEdgeValue(edge.getEdge(), restrictions);
    }

    public final int getRestrictions() {
        return restrictions;
    }

    @Override
    public String getName() {
        return "roadaccessrestrictions";
    }
}
