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
package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import org.heigit.ors.routing.graphhopper.extensions.storages.TrailDifficultyScaleGraphStorage;

public class TrailDifficultyScaleGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private TrailDifficultyScaleGraphStorage storage;
    private int hikingScale;
    private int mtbScale;
    private int mtbUphillScale;

    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");
        storage = new TrailDifficultyScaleGraphStorage();
        return storage;
    }

    public void processWay(ReaderWay way) {
        hikingScale = getSacScale(way.getTag("sac_scale"));
        mtbScale = getMtbScale(way.getTag("mtb:scale"));
        if (mtbScale == 0)
            mtbScale = getMtbScale(way.getTag("mtb:scale:imba"));
        mtbUphillScale = getMtbScale(way.getTag("mtb:scale:uphill"));
        if (mtbUphillScale == 0)
            mtbUphillScale = mtbScale;
    }

    private int getSacScale(String value) {

        //Keep in sync with documentation: trail-difficulty.md

        if (!Helper.isEmpty(value)) {
            switch (value) {
                case "hiking":
                    return 1;
                case "mountain_hiking":
                    return 2;
                case "demanding_mountain_hiking":
                    return 3;
                case "alpine_hiking":
                    return 4;
                case "demanding_alpine_hiking":
                    return 5;
                case "difficult_alpine_hiking":
                    return 6;
                default:
            }
        }
        return 0;
    }

    private int getMtbScale(String value) {
        if (!Helper.isEmpty(value)) {
            try {
                return Integer.parseInt(value) + 1;
            } catch (Exception ex) {
                // do nothing
            }
        }
        return 0;
    }

    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        storage.setEdgeValue(edge.getEdge(), hikingScale, mtbScale, mtbUphillScale);
    }

    @Override
    public String getName() {
        return "TrailDifficulty";
    }
}
