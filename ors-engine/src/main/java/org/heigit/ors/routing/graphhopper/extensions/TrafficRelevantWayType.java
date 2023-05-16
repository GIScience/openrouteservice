/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions;

import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;

public class TrafficRelevantWayType {
    public enum RelevantWayTypes {
        CLASS1(1), // motorway | motorroad. Definitely needed. Map matching works quite good.
        CLASS2(2), // motorway | trunk | primary. Definitely needed. Map matching works quite good.
        CLASS3(3), // primary | secondary. Definitely needed. Map matching works quite good.
        CLASS4(4), // secondary | tertiary. Definitely needed. Map matching works quite good.
        UNCLASSIFIED(5),
        CLASS5(6),  // residential | living_street | service... Rarely needed! Streets Here doesn't provide traffic data for anyways.
        CLASS1LINK(7),
        CLASS2LINK(8),
        CLASS3LINK(9),
        CLASS4LINK(10),
        UNWANTED(0); // Not needed! Streets Here doesn't provide traffic data for anyways.

        public final byte value;

        RelevantWayTypes(int value) {
            this.value = (byte) value;
        }
    }

    private TrafficRelevantWayType() {
    }

    public static byte getHereTrafficClassFromOSMRoadType(short roadType) {
        if (roadType == TrafficGraphStorage.RoadTypes.MOTORWAY.value || roadType == TrafficGraphStorage.RoadTypes.MOTORROAD.value) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS1.value;
        } else if (roadType == TrafficGraphStorage.RoadTypes.PRIMARY.value || roadType == TrafficGraphStorage.RoadTypes.TRUNK.value) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS2.value;
        } else if (roadType == TrafficGraphStorage.RoadTypes.SECONDARY.value) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS3.value;
        } else if (roadType == TrafficGraphStorage.RoadTypes.TERTIARY.value) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS4.value;
        } else if (roadType == TrafficGraphStorage.RoadTypes.MOTORWAY_LINK.value) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS1LINK.value;
        } else if (roadType == TrafficGraphStorage.RoadTypes.PRIMARY_LINK.value || roadType == TrafficGraphStorage.RoadTypes.TRUNK_LINK.value) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS2LINK.value;
        } else if (roadType == TrafficGraphStorage.RoadTypes.SECONDARY_LINK.value) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS3LINK.value;
        } else if (roadType == TrafficGraphStorage.RoadTypes.TERTIARY_LINK.value) {
            return TrafficRelevantWayType.RelevantWayTypes.CLASS4LINK.value;
        } else {
            return TrafficRelevantWayType.RelevantWayTypes.UNWANTED.value;
        }
    }
}
