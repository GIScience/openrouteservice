/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.routing.ch;

import com.graphhopper.routing.weighting.AbstractAdjustedWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Used in CH preparation and therefore assumed that all edges are of type CHEdgeIteratorState
 * <p>
 *
 * @author Peter Karich
 * @see PrepareContractionHierarchies
 */
// ORS-GH MOD START - this class has been heavily refactored an modified to accommodate for time-dependent routing
public class PreparationWeighting extends AbstractAdjustedWeighting {

    public PreparationWeighting(Weighting superWeighting) {
        super(superWeighting);
    }

    @Override
    public final double getMinWeight(double distance) {
        return superWeighting.getMinWeight(distance);
    }

    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
        if (isShortcut(edge))
            return ((CHEdgeIteratorState) edge).getWeight();

        return superWeighting.calcWeight(edge, reverse, prevOrNextEdgeId);
    }

    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId, long edgeEnterTime) {
        if (isShortcut(edge))
            return ((CHEdgeIteratorState) edge).getWeight();

        return superWeighting.calcWeight(edge, reverse, prevOrNextEdgeId, edgeEnterTime);
    }

    @Override
    public long calcMillis(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
        if (isShortcut(edge))
            return ((CHEdgeIteratorState) edge).getTime();

        return superWeighting.calcMillis(edge, reverse, prevOrNextEdgeId);
    }

    @Override
    public long calcMillis(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId, long edgeEnterTime) {
        if (isShortcut(edge))
            return ((CHEdgeIteratorState) edge).getTime();

        return super.calcMillis(edge, reverse, prevOrNextEdgeId, edgeEnterTime);
    }

    boolean isShortcut(EdgeIteratorState edge) {
        return (edge instanceof CHEdgeIteratorState && ((CHEdgeIteratorState) edge).isShortcut());
    }

    @Override
    public String getName() {
        return "prepare";
    }
}
// ORS-GH MOD END
