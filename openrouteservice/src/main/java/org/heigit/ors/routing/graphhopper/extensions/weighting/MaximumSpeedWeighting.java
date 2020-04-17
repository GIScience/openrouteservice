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
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Parameters.Routing;

/**
 * This class creates the weighting for the routing according to the maximum speed set by user.
 *
 * @author Athanasios Kogios
 */

public class MaximumSpeedWeighting implements Weighting {
    protected final static double SPEED_CONV = 3.6; //From km/h to m/s.
    private final double headingPenalty;
    private final  double userMaxSpeed;
    private final Weighting superWeighting;
    private final FlagEncoder flagEncoder;
    private HintsMap hintsMap;

    public MaximumSpeedWeighting(FlagEncoder flagEncoder, HintsMap hintsMap, Weighting weighting) {
       this.flagEncoder = flagEncoder;
       this.hintsMap = hintsMap;
       this.superWeighting = weighting;
       if(hintsMap.getDouble("user_speed",80) < 80  ){
           throw new RuntimeException("User speed should be <= 80.");
       }
       userMaxSpeed = hintsMap.getDouble("user_speed",80);
       headingPenalty = hintsMap.getDouble(Routing.HEADING_PENALTY, Routing.DEFAULT_HEADING_PENALTY);
    }

    private double speedToTime_km_h(double speed, EdgeIteratorState edge){
        //Conversion of the speeds (km/h) to times taken from the edges into time adding the penalties
        double time = edge.getDistance() / speed * SPEED_CONV;

        // add direction penalties at start/stop/via points
        boolean unfavoredEdge = edge.get(EdgeIteratorState.UNFAVORED_EDGE);
        if (unfavoredEdge)
            time += headingPenalty;

        return time;
    }

    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
        CHEdgeIteratorState tmp = (CHEdgeIteratorState) edge;

        if(hintsMap.getWeighting() == "fastest") {
            if (tmp.isShortcut()) {
                return tmp.getWeight(); // If the edge is a shortcut we have a different way to get the weight.
            } else {
                //If it is not a shortcut we need to test both directions
                double speed = reverse ? edge.get(flagEncoder.getAverageSpeedEnc()) : edge.getReverse(flagEncoder.getAverageSpeedEnc());
                if (speed > userMaxSpeed) {
                    speed = userMaxSpeed;// Change the speed to the maximum one defined by the user.
                    return speedToTime_km_h(speed, edge);
                } else {
                    return superWeighting.calcWeight(edge, reverse, prevOrNextEdgeId);// If the speed of the edge is zero or lower than the one defined by user we call the superWeighting calcWEight method.
                }
            }
        }

        return superWeighting.calcWeight(edge, reverse, prevOrNextEdgeId);

    }

    @Override
    public long calcMillis(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId){
        if (edge instanceof CHEdgeIteratorState && ((CHEdgeIteratorState) edge).isShortcut()) {
            throw new IllegalStateException("calcMillis should only be called on original edges");
        }

        if(hintsMap.getWeighting() == "fastest") {
            //If it is not a shortcut we need to test both directions
            double speed = reverse ? edge.get(flagEncoder.getAverageSpeedEnc()) : edge.getReverse(flagEncoder.getAverageSpeedEnc());
            if (speed > userMaxSpeed) {
                speed = userMaxSpeed;// Change the speed to the maximum one defined by the user.
                return (long) speedToTime_km_h(speed, edge);
            } else {
                return superWeighting.calcMillis(edge, reverse, prevOrNextEdgeId);// If the speed of the edge is zero or lower than the one defined by user we call the superWeighting calcWEight method.
            }
        }

        return superWeighting.calcMillis(edge, reverse, prevOrNextEdgeId);
    }


    @Override
    public double getMinWeight(double distance) {
        return superWeighting.getMinWeight(distance);
    }

    @Override
    public FlagEncoder getFlagEncoder() {
        return superWeighting.getFlagEncoder();
    }

    @Override
    public boolean matches(HintsMap weightingMap) {
        return superWeighting.matches(weightingMap);
    }

   @Override
    public String toString() {
        return "maximum_speed|" + superWeighting.toString();
    }

    @Override
    public String getName() {
        return "maximum_speed|" + superWeighting.getName();
    }

}

