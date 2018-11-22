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

package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;
import heigit.ors.routing.graphhopper.extensions.storages.RoadAccessRestrictionsGraphStorage;

import java.util.*;

public class RoadAccessRestrictionsGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private RoadAccessRestrictionsGraphStorage storage;
    private boolean hasRestrictions = false;
    private int restrictions;
    private List<String> accessRestrictedTags = new ArrayList<String>(5);
    private List<String> motorCarTags = new ArrayList<String>(5);
    private List<String> motorCycleTags = new ArrayList<String>(5);
    private Set<String> restrictedValues = new HashSet<String>(5);
    private Set<String> permissiveValues = new HashSet<String>(5);

    private int profileType;

    public RoadAccessRestrictionsGraphStorageBuilder() {
        accessRestrictedTags.addAll(Arrays.asList("motorcar", "motor_vehicle", "vehicle", "access", "bicycle", "foot"));
        motorCarTags.addAll(Arrays.asList("motorcar", "motor_vehicle"));
        motorCycleTags.addAll(Arrays.asList("motorcycle", "motor_vehicle"));

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

    public GraphExtension init(GraphHopper graphhopper, int profileType) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        this.profileType = profileType;

        storage = new RoadAccessRestrictionsGraphStorage();

        if (_parameters.containsKey("use_for_warnings"))
            storage.setIsUsedForWarning(Boolean.parseBoolean(_parameters.get("use_for_warnings")));

        return storage;
    }

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

        if (_parameters.containsKey("use_for_warnings"))
            storage.setIsUsedForWarning(Boolean.parseBoolean(_parameters.get("use_for_warnings")));

        return storage;
    }

    public void processWay(ReaderWay way) {
        this.processWay(way, null, null);
    }

    public void processWay(ReaderWay way, Coordinate[] coords, HashMap<Integer, HashMap<String,String>> nodeTags) {
        if (hasRestrictions) {
            hasRestrictions = false;
            restrictions = 0;
        }

        if(nodeTags != null) {
            for (HashMap<String, String> tagPairs : nodeTags.values()) {
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
                restrictions = isAccessAllowed(way, "bicycle") ? 0 : getRestrictionType(way, "bicycle");
            if (RoutingProfileType.isPedestrian(profileType))
                restrictions = isAccessAllowed(way, "foot") ? 0 : getRestrictionType(way, "foot");
        }
    }

    private int getRestrictionType(ReaderWay way, List<String> tags) {
        int res = 0;

        String tagValue = way.getTag("access");
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

    private int getRestrictionType(ReaderWay way, String tag) {
        int res = 0;

        String tagValue = way.getTag("access");
        if (tagValue != null)
            res = updateRestriction(res, tagValue);

        tagValue = way.getTag(tag);
        res = updateRestriction(res, tagValue);

        return res;
    }

    private int updateRestriction(int encodedRestrictions, String restrictionValue) {
        int res = encodedRestrictions;
        if (restrictionValue != null && !restrictionValue.isEmpty()) {
            switch (restrictionValue) {
                case "no":
                    res |= AccessRestrictionType.No;
                    break;
                case "destination":
                    res |= AccessRestrictionType.Destination;
                    break;
                case "private":
                    res |= AccessRestrictionType.Private;
                    break;
                case "permissive":
                    res |= AccessRestrictionType.Permissive;
                    break;
                case "delivery":
                    res |= AccessRestrictionType.Delivery;
                    break;
                case "customers":
                    res |= AccessRestrictionType.Customers;
                    break;

            }
        }

        return res;
    }

    private boolean isAccessAllowed(ReaderWay way, List<String> tagNames) {
        return way.hasTag(tagNames, permissiveValues);
    }

    private boolean isAccessAllowed(ReaderWay way, String tagName) {
        return way.hasTag(tagName, permissiveValues);
    }

    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        if (hasRestrictions)
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
