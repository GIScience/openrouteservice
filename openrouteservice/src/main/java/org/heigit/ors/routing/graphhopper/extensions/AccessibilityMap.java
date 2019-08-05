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
package heigit.ors.routing.graphhopper.extensions;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.shapes.GHPoint3D;

public class AccessibilityMap {
    private IntObjectMap<SPTEntry> map;
    private SPTEntry edgeEntry;
    private GHPoint3D snappedPosition;

    public AccessibilityMap(IntObjectMap<SPTEntry> map, SPTEntry edgeEntry) {

        this(map, edgeEntry, null);
    }

    public AccessibilityMap(IntObjectMap<SPTEntry> map, SPTEntry edgeEntry, GHPoint3D snappedPosition) {

        this.map = map;
        this.edgeEntry = edgeEntry;
        this.snappedPosition = snappedPosition;

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
