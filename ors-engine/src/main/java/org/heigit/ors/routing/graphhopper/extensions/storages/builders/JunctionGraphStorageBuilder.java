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
import org.heigit.ors.routing.graphhopper.extensions.JunctionType;
import org.heigit.ors.routing.graphhopper.extensions.storages.JunctionGraphStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JunctionGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private JunctionGraphStorage storage;
    private int junction;
    private final List<String> junctionTags = new ArrayList<>(6);

    public JunctionGraphStorageBuilder() {
        // Eventually add more junction tags, e.g. "junction=roundabout"
        junctionTags.addAll(Arrays.asList("junction"));
    }

    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        storage = new JunctionGraphStorage();

        return storage;
    }

    public void processWay(ReaderWay way) {
        junction = JunctionType.NONE;

        for (String key : junctionTags) {
            if (way.hasTag(key)) {
                String value = way.getTag(key);

                if (value != null) {
                    switch (key) {
                        case "junction" -> setFlag(JunctionType.CYCLING_CARGO, value);
                        // case "toll:hgv" -> setFlag(JunctionType.HGV, value);
                        // case "toll:N1" -> //currently not used in OSM
                        // setFlag(JunctionType.N1, value);
                        // case "toll:N2" -> setFlag(JunctionType.N2, value);
                        // case "toll:N3" -> setFlag(JunctionType.N3, value);
                        // case "toll:motorcar" -> setFlag(JunctionType.MOTORCAR, value);
                        default -> {
                        }
                    }
                }
            }
        }

    }

    private void setFlag(int flag, String value) {
        switch (value) {
            case "yes" -> junction |= flag;
            case "no" -> junction &= ~flag;
            default -> {
            }
        }
    }

    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        storage.setJunctionEdgeValue(edge.getEdge(), junction);
    }

    @Override
    public String getName() {
        return "junction";
    }

    // Added getter method to access the junction attribute
    public int getCurrentJunctionValue() {
        return this.junction;
    }
}
