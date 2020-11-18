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
    public static final byte CLASS1 = 1; // motorway | motorroad. Definitely needed. Map matching works quite good.
    public static final byte CLASS2 = 2; // motorway | trunk | primary. Definitely needed. Map matching works quite good.
    public static final byte CLASS3 = 3; // primary | secondary. Definitely needed. Map matching works quite good.
    public static final byte CLASS4 = 4; // secondary | tertiary. Definitely needed. Map matching works quite good.
    public static final byte UNCLASSIFIED = 5;
    public static final byte CLASS5 = 6; // residential | living_street | service... Rarely needed! Streets Here doesn't provide traffic data for anyways.
    public static final byte CLASS1LINK = 7;
    public static final byte CLASS2LINK = 8;
    public static final byte CLASS3LINK = 9;
    public static final byte CLASS4LINK = 10;
    public static final byte UNWANTED = 0; // Not needed! Streets Here doesn't provide traffic data for anyways.
    // Class 1 = motorway | motorroad
    // Class 2 = motorway | trunk | primary
    // Class 3 = primary | secondary
    // Class 4 = secondary | tertiary
    // Class 5 = residential | living_street | service | ...

    private TrafficRelevantWayType() {
    }

    public static byte getHereTrafficClassFromOSMRoadType(short roadType) {
        if (roadType == TrafficGraphStorage.MOTORWAY || roadType == TrafficGraphStorage.MOTORROAD) {
            return TrafficRelevantWayType.CLASS1;
        } else if (roadType == TrafficGraphStorage.PRIMARY || roadType == TrafficGraphStorage.TRUNK) {
            return TrafficRelevantWayType.CLASS2;
        } else if (roadType == TrafficGraphStorage.SECONDARY) {
            return TrafficRelevantWayType.CLASS3;
        } else if (roadType == TrafficGraphStorage.TERTIARY) {
            return TrafficRelevantWayType.CLASS4;
//        } else if (roadType == TrafficGraphStorage.UNCLASSIFIED) {
//            return TrafficRelevantWayType.UNCLASSIFIED;
//        } else if (roadType == TrafficGraphStorage.RESIDENTIAL) {
//            return TrafficRelevantWayType.CLASS5;
        } else if (roadType == TrafficGraphStorage.MOTORWAY_LINK) {
            return TrafficRelevantWayType.CLASS1LINK;
        } else if (roadType == TrafficGraphStorage.PRIMARY_LINK || roadType == TrafficGraphStorage.TRUNK_LINK) {
            return TrafficRelevantWayType.CLASS2LINK;
        } else if (roadType == TrafficGraphStorage.SECONDARY_LINK) {
            return TrafficRelevantWayType.CLASS3LINK;
        } else if (roadType == TrafficGraphStorage.TERTIARY_LINK) {
            return TrafficRelevantWayType.CLASS4LINK;
        } else {
            return TrafficRelevantWayType.UNWANTED;
        }
    }
}
