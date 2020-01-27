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
package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Parameters.Routing;

/**
 * This class creates the weighting for the routing according to the maximum speed set by user.
 *
 * @author Athanasios Kogios
 */

public class MaximumSpeedWeighting extends FastestWeighting {
    protected final static double SPEED_CONV = 3.6; //From km/h to m/s.
    private final double headingPenalty;
    private final  double userMaxSpeed;
    private final String weighting;

    public MaximumSpeedWeighting(FlagEncoder encoder, HintsMap map) {
        super(encoder, map);
        userMaxSpeed = map.getDouble("user_speed",80);
        headingPenalty = map.getDouble(Routing.HEADING_PENALTY, Routing.DEFAULT_HEADING_PENALTY);
        weighting = map.get("weighting","fastest");
    }

    //TODO Find correct way to parse data.

    private double speedToTime_km_h(double speed, EdgeIteratorState edge){
        //Conversion of the speeds (km/h) to times taken from the edges into time adding the penalties
        double time = edge.getDistance() / speed * SPEED_CONV;

        // add direction penalties at start/stop/via points
        boolean unfavoredEdge = edge.get(EdgeIteratorState.UNFAVORED_EDGE);
        if (unfavoredEdge)
            time += headingPenalty;

        return time;
    }

    private double speedToTime_m_s(double speed, EdgeIteratorState edge){
        //Conversion of the speeds (m/s) to times taken from the edges into time adding the penalties
        double time = edge.getDistance() / speed;

        // add direction penalties at start/stop/via points
        boolean unfavoredEdge = edge.get(EdgeIteratorState.UNFAVORED_EDGE);
        if (unfavoredEdge)
            time += headingPenalty;

        return time;
    }

    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
        CHEdgeIteratorState tmp = (CHEdgeIteratorState) edge;
        if (tmp.isShortcut()) {
            //If a shortcut is in both directions the weight is identical => no need for 'reverse'
            //tmp.getWeight() is in time (seconds) so we first change it to speed
            double time = tmp.getWeight();
            double distance = edge.getDistance();
            double speed = edge.getDistance()/ tmp.getWeight();
            //Check for zero to avoid infinities
            if (speed == 0) {
                return Double.POSITIVE_INFINITY;
            }
            //Find the minimum of the two values. if (speed > userMaxSpeed) -> userMaxSpeed, else -> speed
            speed =  java.lang.Math.min(userMaxSpeed, speed);
            //Convert speed to time
            return speedToTime_m_s(speed, edge);
        }
        else{
            //If it is not a shortcut we need to test both directions
            double speed = reverse ? edge.get(flagEncoder.getAverageSpeedEnc()) : edge.getReverse(flagEncoder.getAverageSpeedEnc());
            if (speed == 0) {
                return Double.POSITIVE_INFINITY;
            }
            if(speed > userMaxSpeed) {
                speed = userMaxSpeed;
                return speedToTime_km_h(speed, edge);
            }else{
                return super.calcWeight(edge, reverse, prevOrNextEdgeId);
            }
        }
    }

    @Override
    public String getName() {
        return "maximum_speed";
    }

}
