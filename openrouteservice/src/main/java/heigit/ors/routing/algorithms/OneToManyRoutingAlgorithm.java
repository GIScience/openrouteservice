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
package heigit.ors.routing.algorithms;

import com.graphhopper.storage.SPTEntry;

public interface OneToManyRoutingAlgorithm {
	
    void prepare(int[] from, int[] to);
    
    SPTEntry[] calcPaths(int from, int[] to);
    
    void reset();

    /**
     * Limit the search to numberOfNodes. See #681
     */
    void setMaxVisitedNodes(int numberOfNodes);

    /**
     * @return name of this algorithm
     */
    String getName();

    /**
     * Returns the visited nodes after searching. Useful for debugging.
     */
    int getVisitedNodes();
}
