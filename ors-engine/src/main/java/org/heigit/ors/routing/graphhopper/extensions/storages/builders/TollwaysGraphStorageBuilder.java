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
import org.heigit.ors.routing.graphhopper.extensions.TollwayType;
import org.heigit.ors.routing.graphhopper.extensions.storages.TollwaysGraphStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TollwaysGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private TollwaysGraphStorage storage;
    private int tollways;
    private final List<String> tollTags = new ArrayList<>(6);

    public TollwaysGraphStorageBuilder() {
        // Currently consider only toll tags relevant to cars or hgvs:
        tollTags.addAll(Arrays.asList("toll", "toll:hgv", "toll:N1", "toll:N2", "toll:N3", "toll:motorcar"));
    }

    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        storage = new TollwaysGraphStorage();

        return storage;
    }

    public void processWay(ReaderWay way) {
        tollways = TollwayType.NONE;

        for (String key : tollTags) {
            if (way.hasTag(key)) {
                String value = way.getTag(key);

                if (value != null) {
                    switch (key) {
                        case "toll" -> setFlag(TollwayType.GENERAL, value);
                        case "toll:hgv" -> setFlag(TollwayType.HGV, value);
                        case "toll:N1" -> //currently not used in OSM
                                setFlag(TollwayType.N1, value);
                        case "toll:N2" -> setFlag(TollwayType.N2, value);
                        case "toll:N3" -> setFlag(TollwayType.N3, value);
                        case "toll:motorcar" -> setFlag(TollwayType.MOTORCAR, value);
                        default -> {
                        }
                    }
                }
            }
        }

    }

    private void setFlag(int flag, String value) {
        switch (value) {
            case "yes" -> tollways |= flag;
            case "no" -> tollways &= ~flag;
            default -> {
            }
        }
    }

    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        storage.setEdgeValue(edge.getEdge(), tollways);
    }

    @Override
    public String getName() {
        return "Tollways";
    }
}
