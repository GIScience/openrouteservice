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

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.util.shapes.GHPoint3D;

public class AccessibilityMap {
    private final IntObjectMap<SPTEntry> map;
    private final SPTEntry edgeEntry;
    private final GHPoint3D snappedPosition;

    public AccessibilityMap(IntObjectMap<SPTEntry> map, SPTEntry edgeEntry) {
        this(map, edgeEntry, null);
    }

    public AccessibilityMap(IntObjectMap<SPTEntry> map, SPTEntry edgeEntry, GHPoint3D snappedPosition) {
        this.map = map;
        this.edgeEntry = edgeEntry;
        this.snappedPosition = snappedPosition;
    }

    public AccessibilityMap(IntObjectMap<SPTEntry> map, GHPoint3D snappedPosition) {
        this(map, null, snappedPosition);
    }

    public boolean isEmpty() {
        return map.size() == 0;
    }

    public IntObjectMap<SPTEntry> getMap() {
        return map;
    }

    public SPTEntry getEdgeEntry() {
        return edgeEntry;
    }

    public GHPoint3D getSnappedPosition() {
        return snappedPosition;
    }
}
