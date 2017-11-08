/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by adam on 26/10/2017.
 * Modified from the green index routing
 */
public class BordersWeighting extends FastestWeighting {
    private BordersGraphStorage _gsBorders;
    private byte[] _buffer = new byte[1];
    private int level = 0;

    private ArrayList<Integer> avoidCountries = new ArrayList<>();

    public BordersWeighting(FlagEncoder encoder, PMap map, GraphStorage graphStorage) {
        super(encoder, map);

        _gsBorders = GraphStorageUtils.getGraphExtension(graphStorage, BordersGraphStorage.class);
        level = map.getInt("level", 0);
        int ct = map.getInt("country", 0);
        if(ct != 0)
            avoidCountries.add(ct);
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        if (_gsBorders != null) {
            // Check if we are looking at restricting by country or by border type
            if(level > 0) {
                // Looking at the type of border
                int borderLevel = _gsBorders.getEdgeValue(edgeState.getOriginalEdge(), _buffer, BordersGraphStorage.Property.TYPE);
                if(borderLevel > 0) {
                    // Now check for the corresponding level
                    // 0 = cross any border
                    // 1 = cross no borders
                    // 2 = only cross "soft" borders (i.e. Shengen borders)
                    double weighting = 1.0;
                    switch(level) {
                        case 0:
                            break;
                        case 1: // No border crossings
                            weighting = Double.MAX_VALUE;
                            break;
                        case 2: // Only exclude non-soft borders
                            if(borderLevel == 1)
                                weighting = Double.MAX_VALUE;
                            break;
                    }

                    return weighting;
                }
            } else if(avoidCountries.size() > 0) {
                // Looking at avoiding specific countires, so we need to work with the start and end points of the ways
                // if either are the avoid country then we do not use them
                int start = _gsBorders.getEdgeValue(edgeState.getOriginalEdge(), _buffer, BordersGraphStorage.Property.START);
                int end = _gsBorders.getEdgeValue(edgeState.getOriginalEdge(), _buffer, BordersGraphStorage.Property.END);

                double weighting = 1.0;

                for(Integer i : avoidCountries) {
                    if(i == start || i == end) {
                        weighting = Double.MAX_VALUE;
                        break;
                    }
                }

                return weighting;
            }
        }

        return 1.0;
    }
}
