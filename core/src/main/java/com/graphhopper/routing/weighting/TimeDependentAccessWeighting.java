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
package com.graphhopper.routing.weighting;

import com.graphhopper.routing.util.ConditionalAccessEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import us.dustinj.timezonemap.TimeZoneMap;

/**
 * Calculates the fastest route with the specified vehicle (VehicleEncoder). Calculates the time-dependent weight
 * in seconds.
 * <p>
 *
 * @author Andrzej Oles
 */
public class TimeDependentAccessWeighting extends AbstractAdjustedWeighting {
    private ConditionalAccessEdgeFilter edgeFilter;
    public TimeDependentAccessWeighting(Weighting weighting, GraphHopperStorage graph, FlagEncoder encoder, TimeZoneMap timeZoneMap) {
        super(weighting);
        this.edgeFilter = new ConditionalAccessEdgeFilter(graph, encoder, timeZoneMap);
    }

    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
        return superWeighting.calcWeight(edge, reverse, prevOrNextEdgeId);
    }

    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId, long linkEnterTime) {
        if (edgeFilter.accept(edge, linkEnterTime)) {
            return calcWeight(edge, reverse, prevOrNextEdgeId);
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    @Override
    public long calcMillis(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId, long linkEnterTime) {
        return calcMillis(edge, reverse, prevOrNextEdgeId);
    }

    @Override
    public double getMinWeight(double distance) {
        return superWeighting.getMinWeight(distance);
    }

    @Override
    public boolean matches(HintsMap map) {
        return superWeighting.matches(map);
    }

    @Override
    public String getName() {
        return "td_access";
    }

    @Override
    public boolean isTimeDependent() {
        return true;
    }
}
