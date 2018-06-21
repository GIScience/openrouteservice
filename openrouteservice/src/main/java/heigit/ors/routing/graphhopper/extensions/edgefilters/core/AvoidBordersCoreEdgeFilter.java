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
package heigit.ors.routing.graphhopper.extensions.edgefilters.core;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;

public class AvoidBordersCoreEdgeFilter extends EdgeFilterSeq {
    private BordersGraphStorage _extBorders;

    public AvoidBordersCoreEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage, EdgeFilter prevFilter) {
        super(encoder, true, true, prevFilter);
        this._extBorders = GraphStorageUtils.getGraphExtension(graphStorage, BordersGraphStorage.class);
    }

    @Override
    protected final boolean check(EdgeIteratorState iter) {

        if (_out && iter.isForward(_encoder) || _in && iter.isBackward(_encoder)) {
            // accept if an edge does not cross a border
            return _extBorders.getEdgeValue(iter.getEdge(), BordersGraphStorage.Property.TYPE) == BordersGraphStorage.NO_BORDER;
        }

        return false;
    }
}
