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

import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
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
    private final DecimalEncodedValue avSpeedEnc;
    private boolean calculateWeight;

    public MaximumSpeedWeighting(FlagEncoder flagEncoder, HintsMap hintsMap, Weighting weighting) {
        this.avSpeedEnc = flagEncoder.getAverageSpeedEnc();
        this.superWeighting = weighting;
        if(hintsMap.getDouble("user_speed",80) < 80  ){
            throw new RuntimeException("User speed should be <= 80.");
        }
        this.headingPenalty = hintsMap.getDouble(Routing.HEADING_PENALTY, Routing.DEFAULT_HEADING_PENALTY);
        this.userMaxSpeed = hintsMap.getDouble("user_speed",80);
        this.calculateWeight = (hintsMap.getWeighting() == "fastest");
    }

    /** This function computes the weight when the speed of the edge is greater than the user speed */
    private double calcMaximumSpeedWeight(double speed, EdgeIteratorState edge){
        //Conversion of the speeds to times including the factor for changing from km/h -> m/s.
        double time = edge.getDistance() / speed * SPEED_CONV;

        // add direction penalties at start/stop/via points
        boolean unfavoredEdge = edge.get(EdgeIteratorState.UNFAVORED_EDGE);
        if (unfavoredEdge)
            time += headingPenalty;

        return time;
    }

    /** This function is going to add the difference of: (edge speed - maximum_speed) in the calcMillis. */
    private double calcMaximumSpeedMillis(double userMaxSpeed, double speed, EdgeIteratorState edge){
        //Conversion of the speeds to times including the factor for changing from km/h -> m/ms.
        double time = Math.abs((edge.getDistance() * ( 1 / speed - 1 / userMaxSpeed ))) * SPEED_CONV * 1000;

        return time;
    }

    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
        if (calculateWeight) {
            double speed = reverse ? edge.get(avSpeedEnc) : edge.getReverse(avSpeedEnc);
            if (speed > userMaxSpeed)
                return calcMaximumSpeedWeight(userMaxSpeed, edge);// Use the maximum speed defined }
        }

        return superWeighting.calcWeight(edge, reverse, prevOrNextEdgeId);
    }

    @Override
    public long calcMillis(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId){
        double speed = reverse ? edge.get(avSpeedEnc) : edge.getReverse(avSpeedEnc);
        if (speed > userMaxSpeed) {
            return superWeighting.calcMillis(edge, reverse, prevOrNextEdgeId) + (long) calcMaximumSpeedMillis(userMaxSpeed, speed, edge);
        } else {
            return superWeighting.calcMillis(edge, reverse, prevOrNextEdgeId);// If the speed of the edge is zero or lower than the one defined by user we call the superWeighting calcWEight method.
        }
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
