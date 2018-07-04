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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.pathprocessors.BordersExtractor;
import org.apache.log4j.Logger;

public class AvoidBordersEdgeFilter implements EdgeFilter {
    private BordersExtractor.Avoid _avoidBorders = BordersExtractor.Avoid.NONE;
    private boolean _avoidCountries = false;

    private BordersExtractor _bordersExtractor;

    public AvoidBordersEdgeFilter(RouteSearchParameters searchParams, BordersGraphStorage extBorders) {
        init(searchParams, extBorders);
    }

    public AvoidBordersEdgeFilter(RouteSearchParameters searchParams, GraphStorage graphStorage) {
        BordersGraphStorage extBorders = GraphStorageUtils.getGraphExtension(graphStorage, BordersGraphStorage.class);
        init(searchParams, extBorders);
    }

    /**
     * Initialise the edge filter object based on the type of borders to filter
     *
     * @param searchParams      The search parameters passed into the request
     * @param extBorders        The extended borders graph storage to use
     */
    private void init(RouteSearchParameters searchParams, BordersGraphStorage extBorders) {
        // Init the graph storage
        if(extBorders != null) {
            int[] avoidCountries;
            if(searchParams.hasAvoidCountries())
                avoidCountries = searchParams.getAvoidCountries();
            else
                avoidCountries = new int[0];

            _avoidCountries = avoidCountries.length > 0;

            if(searchParams.hasAvoidBorders()) {
                _avoidBorders = searchParams.getAvoidBorders();
            }

            _bordersExtractor = new BordersExtractor(extBorders, searchParams.getProfileParameters(), avoidCountries);
        }
    }

    /**
     * Determine whether the edge should be accepted for processing or reject. Depending on whether the request was to
     * not cross any border or not cross controlled borders determines the type of border to reject.
     *
     * @param iter      An iterator to the edges that need to be filtered
     * @return
     */
    @Override
    public final boolean accept(EdgeIteratorState iter) {

        if (_avoidBorders != BordersExtractor.Avoid.NONE) {
            // We have been told to avoid some form of border
            switch(_avoidBorders) {
                case ALL:
                    if(_bordersExtractor.isBorder(iter.getEdge())) {
                        // It is a border, and we want to avoid all borders
                        return false;
                    }
                case CONTROLLED:
                    if(_bordersExtractor.isControlledBorder(iter.getEdge())) {
                        // We want to only avoid controlled borders
                        return false;
                    }
                    break;
            }
        }

        if(_avoidCountries) {
            if(_bordersExtractor.restrictedCountry(iter.getEdge())) {
                return false;
            }
        }

        return true;

    }

}
